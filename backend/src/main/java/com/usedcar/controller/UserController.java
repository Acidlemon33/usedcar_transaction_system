package com.usedcar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.UserPO;
import com.usedcar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public Result<UserPO> getById(@PathVariable Long userId) {
        return Result.success(userService.getById(userId));
    }

    @GetMapping("/page")
    public Result<IPage<UserPO>> paginQuery(
            UserPO user,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return Result.success(userService.paginQuery(user, pageNum, pageSize));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody UserPO user) {
        return Result.success(userService.save(user));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody UserPO user) {
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{userId}")
    public Result<Boolean> remove(@PathVariable Long userId) {
        return Result.success(userService.removeById(userId));
    }

    // --- 1. 登录接口  ---
    @PostMapping("/login")
    public Result<?> login(@RequestBody UserPO user) {
        // 调用加密后的登录逻辑
        UserPO loginUser = userService.login(user.getUsername(), user.getPassword());

        if (loginUser != null) {
            loginUser.setPassword(null);
            return Result.success(loginUser);
        }
        return Result.error("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody UserPO user) {
        // 检查重复
        UserPO existingUser = userService.getOne(new LambdaQueryWrapper<UserPO>()
                .eq(UserPO::getUsername, user.getUsername()), false);
        if (existingUser != null) {
            return Result.error("用户名已被占用");
        }

        // 调用加密后的注册逻辑
        boolean saved = userService.register(user);
        return Result.success(saved);
    }

    // --- 3. 信用分展示接口 ---
    @GetMapping("/{userId}/credit")
    public Result<Integer> getCredit(@PathVariable Long userId) {
        UserPO user = userService.getById(userId);
        return Result.success(user != null ? user.getCreditScore() : 0);
    }
}