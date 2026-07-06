package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.UserPO;

public interface UserService extends IService<UserPO> {
    IPage<UserPO> paginQuery(UserPO user, Long pageNum, Long pageSize);
    UserPO login(String username, String password); // 添加登录
    boolean register(UserPO user);
}