package com.foodrec.admin.service;

import com.foodrec.admin.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.io.IOException;

public interface AdminService {

    // ==================== 仪表盘 ====================
    Map<String, Object> getDashboardStats();
    Map<String, Object> getDashboardTrends();
    List<Integer> getScoreDistribution();

    // ==================== 用户管理 ====================
    List<User> getUserList(String keyword, int page, int pageSize);
    long getUserCount(String keyword);
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean updateUserStatus(Long userId, Integer status);
    boolean deleteUser(Long id);
    boolean batchDeleteUsers(List<Long> ids);

    // ==================== 商户管理 ====================
    List<Merchant> getMerchantList(String keyword, int page, int pageSize);
    long getMerchantCount(String keyword);
    boolean addMerchant(Merchant merchant);
    boolean updateMerchant(Merchant merchant);
    boolean deleteMerchant(Long id);

    // ==================== 档口总览 ====================
    List<Map<String, Object>> getStallList(String keyword, int page, int pageSize);
    long getStallCount(String keyword);
    boolean addStall(Stall stall);
    boolean updateStall(Stall stall);
    boolean deleteStall(Long id);
    List<Map<String, Object>> getStallDishes(Long stallId, int page, int pageSize);

    // ==================== 菜品总览 ====================
    List<Map<String, Object>> getDishList(String keyword, String category, String status, int page, int pageSize);
    long getDishCount(String keyword, String category, String status);
    Dish getDishById(Long id);
    boolean addDish(Dish dish);
    boolean updateDish(Dish dish);
    boolean deleteDish(Long id);

    // ==================== 全局数据查询 ====================
    List<Map<String, Object>> getFavoriteList(String keyword, int page, int pageSize);
    long getFavoriteCount(String keyword);
    List<Map<String, Object>> getHistoryList(String keyword, int page, int pageSize);
    long getHistoryCount(String keyword);

    // ==================== 数据库备份 ====================
    List<Map<String, Object>> getBackupList();
    String createBackup();
    String restoreBackup(Long id);
    String deleteBackup(Long id);
    String cleanupBackups(int retention);

    // ==================== 系统日志 ====================
    List<Map<String, Object>> getSystemLogs(int limit);

    // ==================== 菜品统计与状态 ====================
    Map<String, Object> getDishStats();
    boolean updateDishStatus(Long dishId, String status);
    boolean auditDish(Long dishId, String status, String reason);

    // ==================== 导出、通知、备份下载 ====================
    byte[] exportCsv(String type) throws IOException;
    Map<String, Object> getNotificationCount();
    byte[] downloadBackup(String filename) throws IOException;
}
