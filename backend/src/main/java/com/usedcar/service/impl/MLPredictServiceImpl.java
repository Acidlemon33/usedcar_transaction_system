package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.CarInfoPO;
import com.usedcar.po.MLPredictPO;
import com.usedcar.repository.MLPredictRepository;
import com.usedcar.service.MLPredictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MLPredictServiceImpl extends ServiceImpl<MLPredictRepository, MLPredictPO> implements MLPredictService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public IPage<MLPredictPO> paginQuery(MLPredictPO mlPredict, Long pageNum, Long pageSize) {
        Page<MLPredictPO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }

    // --- 新增：直接对接 Python Flask 并返回价格 ---
    @Override
    public Double predictFromPython(CarInfoPO car, Long userId) {
        // 1. 按照开发规范，组织发送给 Python 的数据 (使用下划线命名)
        Map<String, Object> params = new HashMap<>();
        params.put("brand", car.getBrand());
        params.put("model", car.getModel());
        params.put("model_year", car.getCarYear() != null ? car.getCarYear().getYear() : 2023);
        params.put("milage", car.getMileage());
        params.put("fuel_type", car.getFuelType());
        params.put("accident", car.getAccident());
        params.put("clean_title", car.getCleanTitle());
        params.put("engine_hp", car.getEngineHp());
        params.put("engine_liters", car.getEngineLiters());

        // 2. 调用 Python 接口 (假设地址为 localhost:8000/predict)
        String url = "http://localhost:8000/predict";
        try {
            // 发送 POST 请求
            Map<String, Object> response = restTemplate.postForObject(url, params, Map.class);

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
        } catch (Exception e) {
            throw new RuntimeException("Python预测服务调用失败: " + e.getMessage());
        }
        return null;
    }
}