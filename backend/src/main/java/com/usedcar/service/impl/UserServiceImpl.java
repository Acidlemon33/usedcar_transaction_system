package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.UserPO;
import com.usedcar.repository.UserRepository;
import com.usedcar.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserRepository, UserPO> implements UserService {

    // “盐”
    private static final String SALT = "USED_CAR_#@!_2026_PROJECT";

    @Override
    public IPage<UserPO> paginQuery(UserPO user, Long pageNum, Long pageSize) {
        Page<UserPO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }

    /**
     * 登录逻辑：将输入的明文密码加密后，去数据库比对
     */
    @Override
    public UserPO login(String username, String password) {
        String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        return this.getOne(new LambdaQueryWrapper<UserPO>()
                .eq(UserPO::getUsername, username)
                .eq(UserPO::getPassword, md5Password));
    }

    /**
     * 注册逻辑：将输入的明文密码加密后存入数据库
     */
    @Override
    public boolean register(UserPO user) {
        // 1. 获取明文，拼接盐，计算MD5
        String md5Password = DigestUtils.md5DigestAsHex((SALT + user.getPassword()).getBytes());
        // 2. 覆盖原密码
        user.setPassword(md5Password);

        // 3. 补全其他信息
        user.setCreateTime(LocalDateTime.now());
        if (user.getCreditScore() == null) user.setCreditScore(100);
        if (user.getState() == null) user.setState(1);

        return this.save(user);
    }
}