package com.usedcar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.CarInfoPO;
import com.usedcar.service.CarInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/carInfo")
public class CarInfoController {
    @Autowired
    private CarInfoService carInfoService;

    @GetMapping("/{carInfoId}")
    public Result<CarInfoPO> getById(@PathVariable Long carInfoId) {
        return Result.success(carInfoService.getById(carInfoId));
    }

    @GetMapping("/page")
    public Result<IPage<CarInfoPO>> page(
            CarInfoPO carInfo,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return Result.success(carInfoService.paginQuery(carInfo, pageNum, pageSize));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody CarInfoPO carInfo) {
        if (carInfo.getCreateTime() == null) {
            carInfo.setCreateTime(LocalDateTime.now());
        }
        if (carInfo.getUpdateTime() == null) {
            carInfo.setUpdateTime(LocalDateTime.now());
        }
        return Result.success(carInfoService.save(carInfo));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody CarInfoPO carInfo) {
        return Result.success(carInfoService.updateById(carInfo));
    }

    @DeleteMapping("/{carInfoId}")
    public Result<Boolean> remove(@PathVariable Long carInfoId) {
        return Result.success(carInfoService.removeById(carInfoId));
    }

    // 第一步：添加车辆基本信息
    @PostMapping("/add")
    public Result<CarInfoPO> addCar(@RequestBody CarInfoPO car) {
        car.setCreateTime(LocalDateTime.now());
        car.setUpdateTime(LocalDateTime.now());
        carInfoService.save(car);
        return Result.success(car);
    }
}