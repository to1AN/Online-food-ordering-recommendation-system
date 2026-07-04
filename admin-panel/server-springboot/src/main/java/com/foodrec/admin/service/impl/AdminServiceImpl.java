package com.foodrec.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodrec.admin.entity.*;
import com.foodrec.admin.mapper.*;
import com.foodrec.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private UserMapper userMapper;
    @Autowired private MerchantMapper merchantMapper;
    @Autowired private StallMapper stallMapper;
    @Autowired private DishMapper dishMapper;
    @Autowired private SelectionHistoryMapper historyMapper;
    @Autowired private FavoriteMapper favoriteMapper;

    // 内存模拟备份存储
    private final List<Map<String, Object>> backupStore = new CopyOnWriteArrayList<>();
    private final AtomicLong backupIdGen = new AtomicLong(1);

    // ==================== 仪表盘 ====================
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalMerchants", merchantMapper.selectCount(null));
        stats.put("totalStalls", stallMapper.selectCount(null));
        stats.put("totalDishes", dishMapper.selectCount(null));
        stats.put("totalFavorites", favoriteMapper.selectCount(null));
        stats.put("totalHistories", historyMapper.selectCount(null));

        // 平均评分
        QueryWrapper<SelectionHistory> scoreQw = new QueryWrapper<>();
        scoreQw.select("COALESCE(AVG(score), 0) as avgScore");
        Map<String, Object> scoreResult = historyMapper.selectMaps(scoreQw).get(0);
        Object avg = scoreResult.get("avgScore");
        double avgScore = avg != null ? ((Number) avg).doubleValue() : 0;
        stats.put("avgScore", Math.round(avgScore * 10.0) / 10.0);

        // 今日选餐次数
        LambdaQueryWrapper<SelectionHistory> todayQw = new LambdaQueryWrapper<>();
        todayQw.ge(SelectionHistory::getSelectTime, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        stats.put("todaySelects", historyMapper.selectCount(todayQw));

        // 菜品分类分布
        QueryWrapper<Dish> categoryQw = new QueryWrapper<>();
        categoryQw.select("category, COUNT(*) as value")
                  .groupBy("category")
                  .orderByDesc("value");
        List<Map<String, Object>> categoryDistribution = dishMapper.selectMaps(categoryQw);
        stats.put("categoryDistribution", categoryDistribution);

        // 各档口菜品数量
        List<Map<String, Object>> stallDistribution = stallMapper.selectStallDishCount();
        stats.put("stallDistribution", stallDistribution);

        return stats;
    }

    @Override
    public Map<String, Object> getDashboardTrends() {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = today.minusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            LambdaQueryWrapper<SelectionHistory> qw = new LambdaQueryWrapper<>();
            qw.ge(SelectionHistory::getSelectTime, dayStart)
              .lt(SelectionHistory::getSelectTime, dayEnd);
            long count = historyMapper.selectCount(qw);

            dates.add(dayStart.format(DateTimeFormatter.ofPattern("M/d")));
            values.add((int) count);
        }
        result.put("dates", dates);
        result.put("values", values);
        return result;
    }

    @Override
    public List<Integer> getScoreDistribution() {
        List<Integer> dist = new ArrayList<>();
        for (int score = 1; score <= 5; score++) {
            LambdaQueryWrapper<SelectionHistory> qw = new LambdaQueryWrapper<>();
            qw.eq(SelectionHistory::getScore, score);
            dist.add(historyMapper.selectCount(qw).intValue());
        }
        return dist;
    }

    // ==================== 用户管理 ====================
    @Override
    public List<User> getUserList(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(User::getUsername, keyword)
                          .or().like(User::getUserId, keyword));
        }
        qw.orderByDesc(User::getRegisterTime);
        Page<User> p = new Page<>(page, pageSize);
        return userMapper.selectPage(p, qw).getRecords();
    }

    @Override
    public long getUserCount(String keyword) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(User::getUsername, keyword)
                          .or().like(User::getUserId, keyword));
        }
        return userMapper.selectCount(qw);
    }

    @Override
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    // ==================== 商户管理 ====================
    @Override
    public List<Merchant> getMerchantList(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<Merchant> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(Merchant::getMerchantName, keyword)
                          .or().like(Merchant::getMerchantId, keyword));
        }
        qw.orderByDesc(Merchant::getCreateTime);
        Page<Merchant> p = new Page<>(page, pageSize);
        return merchantMapper.selectPage(p, qw).getRecords();
    }

    @Override
    public long getMerchantCount(String keyword) {
        LambdaQueryWrapper<Merchant> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(Merchant::getMerchantName, keyword)
                          .or().like(Merchant::getMerchantId, keyword));
        }
        return merchantMapper.selectCount(qw);
    }

    @Override
    public boolean addMerchant(Merchant merchant) {
        if (merchant.getCreateTime() == null) {
            merchant.setCreateTime(java.time.LocalDate.now());
        }
        return merchantMapper.insert(merchant) > 0;
    }

    @Override
    public boolean updateMerchant(Merchant merchant) {
        return merchantMapper.updateById(merchant) > 0;
    }

    @Override
    public boolean deleteMerchant(Long id) {
        return merchantMapper.deleteById(id) > 0;
    }

    // ==================== 档口总览 ====================
    @Override
    public List<Map<String, Object>> getStallList(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return stallMapper.selectStallWithMerchant(keyword, offset, pageSize);
    }

    @Override
    public long getStallCount(String keyword) {
        return stallMapper.countStallWithMerchant(keyword);
    }

    // ==================== 菜品总览 ====================
    @Override
    public List<Map<String, Object>> getDishList(String keyword, String category, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return dishMapper.selectDishWithStall(keyword, category, offset, pageSize);
    }

    @Override
    public long getDishCount(String keyword, String category) {
        return dishMapper.countDishWithStall(keyword, category);
    }

    @Override
    public Dish getDishById(Long id) {
        return dishMapper.selectById(id);
    }

    @Override
    public boolean addDish(Dish dish) {
        return dishMapper.insert(dish) > 0;
    }

    @Override
    public boolean updateDish(Dish dish) {
        return dishMapper.updateById(dish) > 0;
    }

    @Override
    public boolean deleteDish(Long id) {
        return dishMapper.deleteById(id) > 0;
    }

    // ==================== 档口 CRUD ====================
    @Override
    public boolean addStall(Stall stall) {
        return stallMapper.insert(stall) > 0;
    }

    @Override
    public boolean updateStall(Stall stall) {
        return stallMapper.updateById(stall) > 0;
    }

    @Override
    public boolean deleteStall(Long id) {
        // 级联删除该档口下的所有菜品
        LambdaQueryWrapper<Dish> dishQw = new LambdaQueryWrapper<>();
        dishQw.eq(Dish::getStallId, id);
        dishMapper.delete(dishQw);
        return stallMapper.deleteById(id) > 0;
    }

    @Override
    public List<Map<String, Object>> getStallDishes(Long stallId, int page, int pageSize) {
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.eq(Dish::getStallId, stallId);
        Page<Dish> p = new Page<>(page, pageSize);
        return dishMapper.selectPage(p, qw).getRecords().stream().map(dish -> {
            Map<String, Object> map = new HashMap<>();
            map.put("dishId", dish.getDishId());
            map.put("dishName", dish.getDishName());
            map.put("price", dish.getPrice());
            map.put("category", dish.getCategory());
            map.put("description", dish.getDescription());
            map.put("imageUrl", dish.getImageUrl());
            map.put("stallId", dish.getStallId());
            return map;
        }).toList();
    }

    // ==================== 全局数据查询 ====================
    @Override
    public List<Map<String, Object>> getFavoriteList(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return favoriteMapper.selectFavoriteWithNames(keyword, offset, pageSize);
    }

    @Override
    public long getFavoriteCount(String keyword) {
        if (keyword == null || keyword.isEmpty()) return favoriteMapper.selectCount(null);
        return favoriteMapper.selectCount(null);
    }

    @Override
    public List<Map<String, Object>> getHistoryList(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return historyMapper.selectHistoryWithNames(keyword, offset, pageSize);
    }

    @Override
    public long getHistoryCount(String keyword) {
        if (keyword == null || keyword.isEmpty()) return historyMapper.selectCount(null);
        return historyMapper.selectCount(null);
    }

    // ==================== 数据库备份 ====================
    @Override
    public List<Map<String, Object>> getBackupList() {
        if (backupStore.isEmpty()) {
            // 初始化模拟数据
            for (int i = 1; i <= 4; i++) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", backupIdGen.getAndIncrement());
                b.put("filename", "backup_2025060" + i + "_030000.sql");
                b.put("size", String.format("%.1f MB", 2.2 + i * 0.1));
                b.put("time", "2025-06-0" + i + " 03:00:00");
                b.put("type", "自动备份");
                backupStore.add(b);
            }
        }
        return new ArrayList<>(backupStore);
    }

    @Override
    public String createBackup() {
        String filename = "backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
        Map<String, Object> b = new HashMap<>();
        b.put("id", backupIdGen.getAndIncrement());
        b.put("filename", filename);
        b.put("size", "2.6 MB");
        b.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        b.put("type", "手动备份");
        backupStore.add(0, b);
        return "备份成功！文件: " + filename;
    }

    @Override
    public String restoreBackup(Long id) {
        // 模拟恢复操作
        return "从备份 ID=" + id + " 恢复数据成功！";
    }

    @Override
    public String deleteBackup(Long id) {
        backupStore.removeIf(b -> id.equals(b.get("id")));
        return "备份文件已删除";
    }
}
