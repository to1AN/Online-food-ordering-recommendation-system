package com.foodrec.admin.service.admin;

import com.foodrec.admin.entity.*;
import com.foodrec.admin.mapper.*;
import com.foodrec.admin.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminServiceImplTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private StallMapper stallMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private SelectionHistoryMapper historyMapper;

    // ==================== 仪表盘测试 ====================

    @Test
    void getDashboardStatsShouldReturnStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalUsers"));
        assertTrue(stats.containsKey("totalMerchants"));
        assertTrue(stats.containsKey("totalStalls"));
        assertTrue(stats.containsKey("totalDishes"));
        assertTrue(stats.containsKey("totalFavorites"));
        assertTrue(stats.containsKey("totalHistories"));
        assertTrue(stats.containsKey("avgScore"));
        assertTrue(stats.containsKey("todaySelects"));
    }

    @Test
    void getDashboardTrendsShouldReturnArrays() {
        Map<String, Object> trends = adminService.getDashboardTrends();
        assertNotNull(trends);
        assertTrue(trends.containsKey("dates"));
        assertTrue(trends.containsKey("values"));
        @SuppressWarnings("unchecked")
        List<String> dates = (List<String>) trends.get("dates");
        @SuppressWarnings("unchecked")
        List<Integer> values = (List<Integer>) trends.get("values");
        assertEquals(7, dates.size());
        assertEquals(7, values.size());
    }

    @Test
    void getScoreDistributionShouldReturnFiveElements() {
        List<Integer> dist = adminService.getScoreDistribution();
        assertNotNull(dist);
        assertEquals(5, dist.size());
        // All values should be zero initially (no scored records)
        for (Integer count : dist) {
            assertTrue(count >= 0);
        }
    }

    // ==================== 用户管理测试 ====================

    @Test
    void getUserCountShouldFilterByKeyword() {
        // Create users with specific names
        User user1 = new User();
        user1.setUsername("张三同学");
        user1.setPassword("");
        user1.setOpenid("openid_zhangsan");
        user1.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user1);

        User user2 = new User();
        user2.setUsername("李四老师");
        user2.setPassword("");
        user2.setOpenid("openid_lisi");
        user2.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user2);

        // Filter by keyword
        long countZhang = adminService.getUserCount("张三");
        assertTrue(countZhang > 0);

        long countLi = adminService.getUserCount("李四");
        assertTrue(countLi > 0);

        long countNonexistent = adminService.getUserCount("不存在的人XYZ");
        assertEquals(0, countNonexistent);
    }

    @Test
    void getUserListShouldPaginate() {
        // Create 5 users
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUsername("批量用户" + i);
            user.setPassword("");
            user.setOpenid("openid_batch_" + i);
            user.setRegisterTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        List<User> page1 = adminService.getUserList(null, 1, 2);
        assertEquals(2, page1.size());

        List<User> page2 = adminService.getUserList(null, 2, 2);
        assertEquals(2, page2.size());

        List<User> page3 = adminService.getUserList(null, 3, 2);
        assertTrue(page3.size() > 0);
    }

    @Test
    @Transactional
    void deleteUserShouldCascadeCleanup() {
        // Create user
        User user = new User();
        user.setUsername("待删除用户");
        user.setPassword("");
        user.setOpenid("openid_delete_test");
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);

        // Create a dish for the favorite
        Dish dish = new Dish();
        dish.setDishName("级联删除测试菜");
        dish.setPrice(new BigDecimal("12.00"));
        dish.setCategory("湘菜");
        dish.setStallId(1L);
        dish.setStatus("approved");
        dishMapper.insert(dish);

        // Add a favorite
        Favorite fav = new Favorite();
        fav.setUserId(user.getUserId());
        fav.setDishId(dish.getDishId());
        fav.setFavoriteTime(LocalDateTime.now());
        favoriteMapper.insert(fav);

        // Add a history record
        SelectionHistory history = new SelectionHistory();
        history.setUserId(user.getUserId());
        history.setDishId(dish.getDishId());
        history.setScore(5);
        history.setSelectTime(LocalDateTime.now());
        historyMapper.insert(history);

        // Verify records exist before deletion
        assertNotNull(userMapper.selectById(user.getUserId()));

        // Delete user
        boolean deleted = adminService.deleteUser(user.getUserId());
        assertTrue(deleted);

        // User should be gone
        assertNull(userMapper.selectById(user.getUserId()));

        // Favorites and histories should be cleaned up
        long favCount = favoriteMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, user.getUserId()));
        assertEquals(0, favCount);

        long histCount = historyMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SelectionHistory>()
                        .eq(SelectionHistory::getUserId, user.getUserId()));
        assertEquals(0, histCount);
    }

    // ==================== 商户管理测试 ====================

    @Test
    void addAndUpdateMerchantShouldWork() {
        Merchant merchant = new Merchant();
        merchant.setMerchantName("测试商户");
        merchant.setContactInfo("13800138000");
        merchant.setCreateTime(LocalDate.now());

        boolean added = adminService.addMerchant(merchant);
        assertTrue(added);
        assertNotNull(merchant.getMerchantId());

        // Get the list and verify
        List<Merchant> list = adminService.getMerchantList("测试商户", 1, 10);
        assertFalse(list.isEmpty());
        assertEquals("测试商户", list.get(0).getMerchantName());

        // Update
        merchant.setMerchantName("更新后的商户名");
        boolean updated = adminService.updateMerchant(merchant);
        assertTrue(updated);
    }

    @Test
    @Transactional
    void deleteMerchantShouldCascadeCleanup() {
        // Create merchant
        Merchant merchant = new Merchant();
        merchant.setMerchantName("待删除商户");
        merchant.setContactInfo("13800138001");
        merchant.setCreateTime(LocalDate.now());
        merchantMapper.insert(merchant);

        // Create stall under this merchant
        Stall stall = new Stall();
        stall.setStallName("待删除档口");
        stall.setLocation("一楼A区");
        stall.setMerchantId(merchant.getMerchantId());
        stallMapper.insert(stall);

        // Create dish under this stall
        Dish dish = new Dish();
        dish.setDishName("待删除菜品");
        dish.setPrice(new BigDecimal("25.00"));
        dish.setCategory("川菜");
        dish.setStallId(stall.getStallId());
        dish.setStatus("approved");
        dishMapper.insert(dish);

        // Delete merchant - should cascade
        boolean deleted = adminService.deleteMerchant(merchant.getMerchantId());
        assertTrue(deleted);

        // Merchant should be gone
        assertNull(merchantMapper.selectById(merchant.getMerchantId()));

        // Stall should be gone
        assertNull(stallMapper.selectById(stall.getStallId()));

        // Dish should be gone
        assertNull(dishMapper.selectById(dish.getDishId()));
    }

    @Test
    void getMerchantCountShouldFilterByKeyword() {
        Merchant m1 = new Merchant();
        m1.setMerchantName("张三餐饮");
        m1.setContactInfo("11111111111");
        m1.setCreateTime(LocalDate.now());
        merchantMapper.insert(m1);

        Merchant m2 = new Merchant();
        m2.setMerchantName("李四美食");
        m2.setContactInfo("22222222222");
        m2.setCreateTime(LocalDate.now());
        merchantMapper.insert(m2);

        long countZhang = adminService.getMerchantCount("张三");
        assertTrue(countZhang > 0);

        long countNonexistent = adminService.getMerchantCount("不存在XYZ");
        assertEquals(0, countNonexistent);
    }

    // ==================== 菜品管理测试 ====================

    @Test
    void addAndDeleteDishShouldWork() {
        Dish dish = new Dish();
        dish.setDishName("新增测试菜品");
        dish.setPrice(new BigDecimal("18.80"));
        dish.setCategory("苏菜");
        dish.setDescription("这是一道测试菜品");
        dish.setStallId(1L);
        dish.setStatus("approved");

        boolean added = adminService.addDish(dish);
        assertTrue(added);
        assertNotNull(dish.getDishId());

        Dish fetched = adminService.getDishById(dish.getDishId());
        assertNotNull(fetched);
        assertEquals("新增测试菜品", fetched.getDishName());
        assertEquals(0, new BigDecimal("18.80").compareTo(fetched.getPrice()));

        boolean deleted = adminService.deleteDish(dish.getDishId());
        assertTrue(deleted);
        assertNull(adminService.getDishById(dish.getDishId()));
    }

    @Test
    void updateDishShouldWork() {
        Dish dish = new Dish();
        dish.setDishName("原始菜名");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCategory("鲁菜");
        dish.setStallId(1L);
        dish.setStatus("approved");
        adminService.addDish(dish);

        dish.setDishName("修改后菜名");
        dish.setPrice(new BigDecimal("12.50"));
        boolean updated = adminService.updateDish(dish);
        assertTrue(updated);

        Dish fetched = adminService.getDishById(dish.getDishId());
        assertEquals("修改后菜名", fetched.getDishName());
        assertEquals(0, new BigDecimal("12.50").compareTo(fetched.getPrice()));
    }

    @Test
    void getDishListShouldFilterByCategory() {
        Dish dish1 = new Dish();
        dish1.setDishName("川菜测试1");
        dish1.setPrice(new BigDecimal("10.00"));
        dish1.setCategory("川菜");
        dish1.setStallId(1L);
        dish1.setStatus("approved");
        dishMapper.insert(dish1);

        Dish dish2 = new Dish();
        dish2.setDishName("粤菜测试1");
        dish2.setPrice(new BigDecimal("20.00"));
        dish2.setCategory("粤菜");
        dish2.setStallId(1L);
        dish2.setStatus("approved");
        dishMapper.insert(dish2);

        long chuanCount = adminService.getDishCount(null, "川菜");
        assertTrue(chuanCount >= 1);

        long yueCount = adminService.getDishCount(null, "粤菜");
        assertTrue(yueCount >= 1);
    }

    @Test
    void getDishStatsShouldReturnStats() {
        Dish dish = new Dish();
        dish.setDishName("统计测试菜");
        dish.setPrice(new BigDecimal("15.00"));
        dish.setCategory("浙菜");
        dish.setStallId(1L);
        dish.setStatus("approved");
        dishMapper.insert(dish);

        Map<String, Object> stats = adminService.getDishStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("total"));
        assertTrue(stats.containsKey("categoryCount"));
        assertTrue(stats.containsKey("avgPrice"));
        assertTrue(((Number) stats.get("total")).longValue() > 0);
    }

    // ==================== 数据查询测试 ====================

    @Test
    void getFavoriteListShouldReturnResults() {
        // Setup: user + dish + favorite
        User user = new User();
        user.setUsername("收藏查询用户");
        user.setPassword("");
        user.setOpenid("openid_fav_query");
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);

        Dish dish = new Dish();
        dish.setDishName("收藏查询菜品");
        dish.setPrice(new BigDecimal("30.00"));
        dish.setCategory("闽菜");
        dish.setStallId(1L);
        dish.setStatus("approved");
        dishMapper.insert(dish);

        Favorite fav = new Favorite();
        fav.setUserId(user.getUserId());
        fav.setDishId(dish.getDishId());
        fav.setFavoriteTime(LocalDateTime.now());
        favoriteMapper.insert(fav);

        List<Map<String, Object>> favs = adminService.getFavoriteList(null, 1, 10);
        assertNotNull(favs);
        assertFalse(favs.isEmpty());
    }

    @Test
    void getFavoriteCountShouldWork() {
        long count = adminService.getFavoriteCount(null);
        assertTrue(count >= 0);
    }

    @Test
    void getHistoryListShouldReturnResults() {
        // Setup: user + dish + history
        User user = new User();
        user.setUsername("历史查询用户");
        user.setPassword("");
        user.setOpenid("openid_hist_query");
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);

        Dish dish = new Dish();
        dish.setDishName("历史查询菜品");
        dish.setPrice(new BigDecimal("22.00"));
        dish.setCategory("徽菜");
        dish.setStallId(1L);
        dish.setStatus("approved");
        dishMapper.insert(dish);

        SelectionHistory history = new SelectionHistory();
        history.setUserId(user.getUserId());
        history.setDishId(dish.getDishId());
        history.setScore(4);
        history.setSelectTime(LocalDateTime.now());
        historyMapper.insert(history);

        List<Map<String, Object>> histories = adminService.getHistoryList(null, 1, 10);
        assertNotNull(histories);
        assertFalse(histories.isEmpty());
    }

    // ==================== 菜品状态测试 ====================

    @Test
    void updateDishStatusShouldWork() {
        Dish dish = new Dish();
        dish.setDishName("状态测试菜品");
        dish.setPrice(new BigDecimal("8.00"));
        dish.setCategory("小吃");
        dish.setStallId(1L);
        dish.setStatus("pending");
        dishMapper.insert(dish);

        boolean updated = adminService.updateDishStatus(dish.getDishId(), "approved");
        assertTrue(updated);

        Dish fetched = adminService.getDishById(dish.getDishId());
        assertEquals("approved", fetched.getStatus());

        // Update non-existent dish should return false
        boolean notFound = adminService.updateDishStatus(99999L, "approved");
        assertFalse(notFound);
    }

    // ==================== 通知测试 ====================

    @Test
    void getNotificationCountShouldReturnCounts() {
        Map<String, Object> counts = adminService.getNotificationCount();
        assertNotNull(counts);
        assertTrue(counts.containsKey("pendingDishes"));
        assertTrue(counts.containsKey("totalBackups"));
    }
}
