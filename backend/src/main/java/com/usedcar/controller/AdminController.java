package com.usedcar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.AdminPO;
import com.usedcar.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminPO> login(@RequestBody AdminPO admin) {
        AdminPO loginAdmin = adminService.login(admin.getAdminName(), admin.getPassword());
        if (loginAdmin != null) {
            loginAdmin.setPassword(null);
            return Result.success(loginAdmin);
        }
        return Result.error("管理员账号或密码错误");
    }

    /**
     * 新增管理员（注册）
     */
    @PostMapping("/register") // 或者直接用原来的 @PostMapping
    public Result<Boolean> register(@RequestBody AdminPO admin) {
        // 检查重名
        AdminPO existing = adminService.getOne(new LambdaQueryWrapper<AdminPO>()
                .eq(AdminPO::getAdminName, admin.getAdminName()), false);
        if (existing != null) {
            return Result.error("该管理员账号已存在");
        }

        return Result.success(adminService.register(admin));
    }

    @GetMapping("/{adminId}")
    public Result<AdminPO> getById(@PathVariable Long adminId) {
        return Result.success(adminService.getById(adminId));
    }

    @GetMapping("/page")
    public Result<IPage<AdminPO>> page(
            AdminPO admin,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return Result.success(adminService.paginQuery(admin, pageNum, pageSize));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody AdminPO admin) {
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        if (admin.getState() == null) admin.setState(1); // 默认在职
        return Result.success(adminService.save(admin));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody AdminPO admin) {
        admin.setUpdateTime(LocalDateTime.now());
        return Result.success(adminService.updateById(admin));
    }

    @DeleteMapping("/{adminId}")
    public Result<Boolean> remove(@PathVariable Long adminId) {
        return Result.success(adminService.removeById(adminId));
    }
}