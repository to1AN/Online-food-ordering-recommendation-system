package com.foodrec.admin.controller.admin;

import com.foodrec.admin.common.Result;
import com.foodrec.admin.entity.Merchant;
import com.foodrec.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
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
    public Result<String> addMerchant(@Valid @RequestBody Merchant merchant) {
        return adminService.addMerchant(merchant)
            ? Result.ok("添加成功")
            : Result.fail("添加失败");
    }

    @PutMapping("/merchants/{id}")
    public Result<String> updateMerchant(@PathVariable Long id, @Valid @RequestBody Merchant merchant) {
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

    // ==================== 导出、通知、备份下载 ====================
    @GetMapping("/export/{type}")
    public ResponseEntity<byte[]> exportCsv(@PathVariable String type) throws IOException {
        byte[] data = adminService.exportCsv(type);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/csv; charset=UTF-8");
        headers.add("Content-Disposition", "attachment; filename=" + type + ".csv");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/notifications/count")
    public Result<Map<String, Object>> notificationCount() {
        return Result.ok(adminService.getNotificationCount());
    }

    @GetMapping("/backups/{filename}/download")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable String filename) throws IOException {
        byte[] data = adminService.downloadBackup(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
