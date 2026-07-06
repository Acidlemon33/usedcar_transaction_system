package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.CarInfoPO;
import com.usedcar.po.MLPredictPO;
import com.usedcar.repository.MLPredictRepository;
import com.usedcar.service.MLPredictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MLPredictServiceImpl extends ServiceImpl<MLPredictRepository, MLPredictPO> implements MLPredictService {

    private static final Logger log = LoggerFactory.getLogger(MLPredictServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public IPage<MLPredictPO> paginQuery(MLPredictPO mlPredict, Long pageNum, Long pageSize) {
        Page<MLPredictPO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }

    /**
     * 调用 Python FastAPI 进行价格预测
     * <p>
     * ⚠ 必须使用 JSON 请求体发送（不能是 form-urlencoded），
     *   因为 FastAPI 的 pydantic 模型 CarFeatures 接收 JSON。
     * </p>
     */
    @Override
    public Double predictFromPython(CarInfoPO car, Long userId) {
        // 1. 按照开发规范，组织发送给 Python 的数据 (使用下划线命名)
        Map<String, Object> params = new HashMap<>();
        params.put("brand", car.getBrand() != null ? car.getBrand() : "");
        params.put("model", car.getModel() != null ? car.getModel() : "");
        params.put("model_year", car.getCarYear() != null ? car.getCarYear().getYear() : 2023);
        params.put("milage", car.getMileage() != null ? car.getMileage() : 0);
        params.put("fuel_type", car.getFuelType() != null ? car.getFuelType() : "");
        params.put("accident", car.getAccident() != null ? car.getAccident() : 0);
        params.put("clean_title", car.getCleanTitle() != null ? car.getCleanTitle() : 1);
        params.put("engine_hp", car.getEngineHp() != null ? car.getEngineHp() : 0);
        params.put("engine_liters", car.getEngineLiters() != null ? car.getEngineLiters() : 0.0);
        // --- 补充字段（FastAPI 模型需要） ---
        params.put("engine_type", "Unknown");
        params.put("trans_type", "Unknown");
        params.put("ext_color_simple", car.getExtColor() != null ? car.getExtColor() : "Other");
        params.put("int_color_simple", car.getIntColor() != null ? car.getIntColor() : "Other");

        // 2. 调用 Python 接口 (地址：localhost:8000/predict)
        String url = "http://localhost:8000/predict";
        try {
            // ⭐ 必须将参数包装为 JSON 请求体（Content-Type: application/json）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);

            Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);

            if (response != null && response.get("price") != null) {
                Double price = Double.valueOf(response.get("price").toString());

                // 3. 将预测结果存入数据库 ml_predict 表，留作记录
                MLPredictPO record = new MLPredictPO();
                record.setCarId(car.getCarId());
                record.setUserId(userId);
                record.setPredictPrice(price);
                record.setAccuracy(0.98); // 默认置信度
                record.setModelVersion("V1.0");
                record.setCreateTime(LocalDateTime.now());
                this.save(record);

                return price;
            }
            log.warn("Python 预测服务返回异常 response: {}", response);
        } catch (Exception e) {
            // ❌ 不抛异常，降级返回 null，让调用方自行处理
            log.error("Python 预测服务调用失败: {}", e.getMessage(), e);
        }
        return null;
    }
}
