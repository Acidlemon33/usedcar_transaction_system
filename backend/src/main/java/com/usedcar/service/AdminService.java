package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.AdminPO;

public interface AdminService extends IService<AdminPO> {
    /**
     * 分页查询
     * @param admin 查询条件
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @return 分页数据
     */
    IPage<AdminPO> paginQuery(AdminPO admin, Long pageNum, Long pageSize);

    AdminPO login(String adminName, String password);

    boolean register(AdminPO admin);
}