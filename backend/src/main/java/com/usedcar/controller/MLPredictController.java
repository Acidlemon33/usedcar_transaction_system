package com.usedcar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.CarInfoPO;
import com.usedcar.po.MLPredictPO;
import com.usedcar.service.CarInfoService;
import com.usedcar.service.MLPredictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ml_predict")
public class MLPredictController {

    @Autowired
    private MLPredictService mlPredictService;

    @Autowired
    private CarInfoService carInfoService;

    /**
     * 核心功能：发起价格预测
     * 请求方式：POST
     * 参数：car_id, user_id
     */
    @PostMapping("/predict_price")
    public Result<Double> predictPrice(@RequestParam Long carId, @RequestParam Long userId) {
        // 1. 获取车辆基础数据
        CarInfoPO car = carInfoService.getById(carId);
        if (car == null) {
            return Result.error("未找到对应的车辆信息");
        }

        // 2. 调用 Service 层转发 Python 接口并保存记录
        try {
            Double price = mlPredictService.predictFromPython(car, userId);
            return Result.success(price);
        } catch (Exception e) {
            return Result.error("AI 预测失败: " + e.getMessage());
        }
    }

    /**
     * 查询单条预测记录
     */
    @GetMapping("/{predictId}")
    public Result<MLPredictPO> getById(@PathVariable Long predictId) {
        return Result.success(mlPredictService.getById(predictId));
    }

    /**
     * 分页查询预测历史
     */
    @GetMapping("/page")
    public Result<IPage<MLPredictPO>> page(
            MLPredictPO mlPredict,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return Result.success(mlPredictService.paginQuery(mlPredict, pageNum, pageSize));
    }

    /**
     * 更新预测记录
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody MLPredictPO mlPredict) {
        return Result.success(mlPredictService.updateById(mlPredict));
    }

    /**
     * 删除预测记录
     */
    @DeleteMapping("/{predictId}")
    public Result<Boolean> remove(@PathVariable Long predictId) {
        return Result.success(mlPredictService.removeById(predictId));
    }
}