package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.MLPredictPO;

public interface MLPredictService extends IService<MLPredictPO> {
    IPage<MLPredictPO> paginQuery(MLPredictPO mlPredict, Long pageNum, Long pageSize);

    Double predictFromPython(com.usedcar.po.CarInfoPO car, Long userId);

}