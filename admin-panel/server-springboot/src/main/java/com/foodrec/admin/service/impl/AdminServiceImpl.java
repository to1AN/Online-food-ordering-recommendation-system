package com.foodrec.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodrec.admin.entity.*;
import com.foodrec.admin.mapper.*;
import com.foodrec.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private UserMapper userMapper;
    @Autowired private MerchantMapper merchantMapper;
    @Autowired private StallMapper stallMapper;
    @Autowired private DishMapper dishMapper;
    @Autowired private SelectionHistoryMapper historyMapper;
    @Autowired private FavoriteMapper favoriteMapper;

    @Value("${backup.dir:./backup}")
    private String backupDir;

    @Value("${backup.mysqldump-path:mysqldump}")
    private String mysqldumpPath;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private String getDbName() {
        String[] parts = dbUrl.split("\\?")[0].split("/");
        return parts[parts.length - 1];
    }

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
            qw.like(User::getUsername, keyword);
        }
        qw.orderByDesc(User::getRegisterTime);
        Page<User> p = new Page<>(page, pageSize);
        return userMapper.selectPage(p, qw).getRecords();
    }

    @Override
    public long getUserCount(String keyword) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(User::getUsername, keyword);
        }
        return userMapper.selectCount(qw);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        // 1. 删除该用户的收藏记录
        LambdaQueryWrapper<Favorite> favQw = new LambdaQueryWrapper<>();
        favQw.eq(Favorite::getUserId, id);
        favoriteMapper.delete(favQw);
        // 2. 删除该用户的选餐历史
        LambdaQueryWrapper<SelectionHistory> histQw = new LambdaQueryWrapper<>();
        histQw.eq(SelectionHistory::getUserId, id);
        historyMapper.delete(histQw);
        // 3. 删除用户
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
    @Transactional
    public boolean deleteMerchant(Long id) {
        // 1. 查询该商户下的所有档口
        LambdaQueryWrapper<Stall> stallQw = new LambdaQueryWrapper<>();
        stallQw.eq(Stall::getMerchantId, id);
        List<Stall> stalls = stallMapper.selectList(stallQw);
        // 2. 删除每个档口下的菜品，再删除档口
        for (Stall stall : stalls) {
            LambdaQueryWrapper<Dish> dishQw = new LambdaQueryWrapper<>();
            dishQw.eq(Dish::getStallId, stall.getStallId());
            dishMapper.delete(dishQw);
            stallMapper.deleteById(stall.getStallId());
        }
        // 3. 删除商户
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
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(Favorite::getDishId, keyword);
        }
        return favoriteMapper.selectCount(qw);
    }

    @Override
    public List<Map<String, Object>> getHistoryList(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return historyMapper.selectHistoryWithNames(keyword, offset, pageSize);
    }

    @Override
    public long getHistoryCount(String keyword) {
        LambdaQueryWrapper<SelectionHistory> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(SelectionHistory::getDishId, keyword);
        }
        return historyMapper.selectCount(qw);
    }

    // ==================== 数据库备份 ====================
    @Override
    public List<Map<String, Object>> getBackupList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Path dir = Paths.get(backupDir);
        if (!Files.exists(dir)) return list;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.sql")) {
            for (Path entry : stream) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", entry.getFileName().toString());
                b.put("filename", entry.getFileName().toString());
                try {
                    b.put("size", String.format("%.1f MB", Files.size(entry) / 1048576.0));
                } catch (IOException e) {
                    b.put("size", "未知");
                }
                b.put("time", Files.getLastModifiedTime(entry).toString());
                b.put("type", entry.getFileName().toString().contains("auto") ? "自动备份" : "手动备份");
                list.add(b);
            }
        } catch (IOException e) { /* ignore */ }
        list.sort((a, b) -> ((String) b.get("time")).compareTo((String) a.get("time")));
        return list;
    }

    @Override
    public String createBackup() {
        try {
            Files.createDirectories(Paths.get(backupDir));
            String filename = "backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
            Path filepath = Paths.get(backupDir, filename);
            ProcessBuilder pb = new ProcessBuilder(
                mysqldumpPath,
                "-u" + dbUsername,
                "-p" + dbPassword,
                "--databases", getDbName(),
                "--result-file=" + filepath.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "备份失败，mysqldump 退出码: " + exitCode;
            }
            return "备份成功！文件: " + filename;
        } catch (Exception e) {
            return "备份失败: " + e.getMessage();
        }
    }

    @Override
    public String restoreBackup(Long id) {
        String filename = String.valueOf(id);
        Path filepath = Paths.get(backupDir, filename);
        if (!Files.exists(filepath)) {
            return "备份文件不存在: " + filename;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-u" + dbUsername,
                "-p" + dbPassword,
                getDbName()
            );
            pb.redirectInput(filepath.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "恢复失败，mysql 退出码: " + exitCode;
            }
            return "从备份 " + filename + " 恢复数据成功！";
        } catch (Exception e) {
            return "恢复失败: " + e.getMessage();
        }
    }

    @Override
    public String deleteBackup(Long id) {
        String filename = String.valueOf(id);
        Path filepath = Paths.get(backupDir, filename);
        try {
            Files.deleteIfExists(filepath);
            return "备份文件已删除: " + filename;
        } catch (IOException e) {
            return "删除失败: " + e.getMessage();
        }
    }
}
