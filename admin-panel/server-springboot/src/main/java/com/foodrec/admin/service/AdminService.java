package com.foodrec.admin.service;

import com.foodrec.admin.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {

    // ==================== 仪表盘 ====================
    Map<String, Object> getDashboardStats();
    Map<String, Object> getDashboardTrends();
    List<Integer> getScoreDistribution();

    // ==================== 用户管理 ====================
    List<User> getUserList(String keyword, int page, int pageSize);
    long getUserCount(String keyword);
    boolean deleteUser(Long id);

    // ==================== 商户管理 ====================
    List<Merchant> getMerchantList(String keyword, int page, int pageSize);
    long getMerchantCount(String keyword);
    boolean addMerchant(Merchant merchant);
    boolean updateMerchant(Merchant merchant);
    boolean deleteMerchant(Long id);

    // ==================== 档口总览 ====================
    List<Map<String, Object>> getStallList(String keyword, int page, int pageSize);
    long getStallCount(String keyword);

    // ==================== 菜品总览 ====================
    List<Map<String, Object>> getDishList(String keyword, String category, int page, int pageSize);
    long getDishCount(String keyword, String category);

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
}
