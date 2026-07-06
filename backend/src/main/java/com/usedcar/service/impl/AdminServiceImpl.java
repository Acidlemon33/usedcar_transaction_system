package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.AdminPO;
import com.usedcar.repository.AdminRepository;
import com.usedcar.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminRepository, AdminPO> implements AdminService {

    // 使用和用户模块一致的“盐”
    private static final String SALT = "USED_CAR_#@!_2026_PROJECT";

    @Override
    public IPage<AdminPO> paginQuery(AdminPO admin, Long pageNum, Long pageSize) {
        Page<AdminPO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }

    /**
     * 管理员登录：加密比对
     */
    @Override
    public AdminPO login(String adminName, String password) {
        // 1. 将输入密码加密
        String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        // 2. 去数据库查
        return this.getOne(new LambdaQueryWrapper<AdminPO>()
                .eq(AdminPO::getAdminName, adminName)
                .eq(AdminPO::getPassword, md5Password));
    }

    /**
     * 管理员注册/新增：加密存储
     */
    @Override
    public boolean register(AdminPO admin) {
        // 1. 密码加密
        String md5Password = DigestUtils.md5DigestAsHex((SALT + admin.getPassword()).getBytes());
        admin.setPassword(md5Password);

        // 2. 补全信息
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        if (admin.getState() == null) admin.setState(1); // 默认在职

        return this.save(admin);
    }
}