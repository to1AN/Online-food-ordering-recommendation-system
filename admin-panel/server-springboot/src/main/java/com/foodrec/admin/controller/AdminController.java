package com.foodrec.admin.controller;

import com.foodrec.admin.common.Result;
import com.foodrec.admin.entity.Merchant;
import com.foodrec.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")  // 允许前端跨域
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ==================== 仪表盘 ====================
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/trends")
    public Result<Map<String, Object>> dashboardTrends() {
        return Result.ok(adminService.getDashboardTrends());
    }

    @GetMapping("/dashboard/score-distribution")
    public Result<List<Integer>> scoreDistribution() {
        return Result.ok(adminService.getScoreDistribution());
    }

    // ==================== 用户管理 ====================
    @GetMapping("/users")
    public Result<Map<String, Object>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getUserList(keyword, page, pageSize),
            "total", adminService.getUserCount(keyword)
        ));
    }

    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        return adminService.deleteUser(id)
            ? Result.ok("删除成功")
            : Result.fail("删除失败");
    }

    // ==================== 商户管理 ====================
    @GetMapping("/merchants")
    public Result<Map<String, Object>> getMerchants(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getMerchantList(keyword, page, pageSize),
            "total", adminService.getMerchantCount(keyword)
        ));
    }

    @PostMapping("/merchants")
    public Result<String> addMerchant(@RequestBody Merchant merchant) {
        return adminService.addMerchant(merchant)
            ? Result.ok("添加成功")
            : Result.fail("添加失败");
    }

    @PutMapping("/merchants/{id}")
    public Result<String> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        merchant.setMerchantId(id);
        return adminService.updateMerchant(merchant)
            ? Result.ok("更新成功")
            : Result.fail("更新失败");
    }

    @DeleteMapping("/merchants/{id}")
    public Result<String> deleteMerchant(@PathVariable Long id) {
        return adminService.deleteMerchant(id)
            ? Result.ok("删除成功")
            : Result.fail("删除失败");
    }

    // ==================== 档口总览 ====================
    @GetMapping("/stalls")
    public Result<Map<String, Object>> getStalls(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getStallList(keyword, page, pageSize),
            "total", adminService.getStallCount(keyword)
        ));
    }

    // ==================== 菜品总览 ====================
    @GetMapping("/dishes")
    public Result<Map<String, Object>> getDishes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getDishList(keyword, category, page, pageSize),
            "total", adminService.getDishCount(keyword, category)
        ));
    }

    // ==================== 全局数据查询 ====================
    @GetMapping("/favorites")
    public Result<Map<String, Object>> getFavorites(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getFavoriteList(keyword, page, pageSize),
            "total", adminService.getFavoriteCount(keyword)
        ));
    }

    @GetMapping("/histories")
    public Result<Map<String, Object>> getHistories(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getHistoryList(keyword, page, pageSize),
            "total", adminService.getHistoryCount(keyword)
        ));
    }

    // ==================== 数据库备份与恢复 ====================
    @GetMapping("/backups")
    public Result<?> getBackups() {
        return Result.ok(adminService.getBackupList());
    }

    @PostMapping("/backups")
    public Result<String> createBackup() {
        return Result.ok(adminService.createBackup());
    }

    @PostMapping("/restore/{id}")
    public Result<String> restoreBackup(@PathVariable Long id) {
        return Result.ok(adminService.restoreBackup(id));
    }

    @DeleteMapping("/backups/{id}")
    public Result<String> deleteBackup(@PathVariable Long id) {
        return Result.ok(adminService.deleteBackup(id));
    }
}
