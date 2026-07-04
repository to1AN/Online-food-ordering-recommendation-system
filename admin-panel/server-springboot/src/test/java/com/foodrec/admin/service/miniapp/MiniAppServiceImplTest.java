package com.foodrec.admin.service.miniapp;

import com.foodrec.admin.entity.Dish;
import com.foodrec.admin.entity.User;
import com.foodrec.admin.mapper.DishMapper;
import com.foodrec.admin.mapper.FavoriteMapper;
import com.foodrec.admin.mapper.SelectionHistoryMapper;
import com.foodrec.admin.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MiniAppServiceImplTest {

    @Autowired
    private MiniAppService miniAppService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private SelectionHistoryMapper historyMapper;

    private User testUser;
    private Dish testDish;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("测试用户");
        testUser.setPassword("");
        testUser.setOpenid("test_openid_unit");
        testUser.setRegisterTime(LocalDateTime.now());
        userMapper.insert(testUser);

        // Create test dish
        testDish = new Dish();
        testDish.setDishName("测试菜品");
        testDish.setPrice(new BigDecimal("15.50"));
        testDish.setCategory("川菜");
        testDish.setDescription("测试描述");
        testDish.setStallId(1L);
        testDish.setStatus("approved");
        dishMapper.insert(testDish);
    }

    // ==================== 登录测试 ====================

    @Test
    void loginShouldReturnTokenAndUserInfo() {
        Map<String, Object> result = miniAppService.login("test_code_abc");
        assertNotNull(result);
        assertNotNull(result.get("token"));
        assertNotNull(result.get("userInfo"));
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) result.get("userInfo");
        assertNotNull(userInfo.get("userId"));
        assertNotNull(userInfo.get("username"));
    }

    @Test
    void logoutShouldReturnTrue() {
        assertTrue(miniAppService.logout("any_token"));
    }

    // ==================== 推荐测试 ====================

    @Test
    void getRandomDishShouldReturnDish() {
        Dish dish = miniAppService.getRandomDish();
        assertNotNull(dish);
        assertNotNull(dish.getDishName());
    }

    @Test
    void getGuessLikeShouldReturnRecommendations() {
        // First create a history record with a good score so the user has a preference
        com.foodrec.admin.entity.SelectionHistory history =
                new com.foodrec.admin.entity.SelectionHistory();
        history.setUserId(testUser.getUserId());
        history.setDishId(testDish.getDishId());
        history.setScore(5);
        history.setSelectTime(LocalDateTime.now());
        historyMapper.insert(history);

        List<Map<String, Object>> results = miniAppService.getGuessLike(testUser.getUserId());
        // May be empty if no other dishes in the same category, but should not throw
        assertNotNull(results);
    }

    // ==================== 收藏测试 ====================

    @Test
    void addAndRemoveFavoriteShouldWork() {
        // Add favorite
        boolean added = miniAppService.addFavorite(testUser.getUserId(), testDish.getDishId());
        assertTrue(added);

        // Verify it exists in the list
        List<Map<String, Object>> favs = miniAppService.getFavorites(testUser.getUserId(), 1, 10);
        assertFalse(favs.isEmpty());
        boolean found = favs.stream()
                .anyMatch(f -> testDish.getDishId().equals(f.get("dish_id")));
        assertTrue(found, "Newly added favorite should appear in list");

        // Remove favorite
        boolean removed = miniAppService.removeFavorite(testUser.getUserId(), testDish.getDishId());
        assertTrue(removed);

        // Verify it's gone
        List<Map<String, Object>> favsAfter = miniAppService.getFavorites(testUser.getUserId(), 1, 10);
        boolean foundAfter = favsAfter.stream()
                .anyMatch(f -> testDish.getDishId().equals(f.get("dish_id")));
        assertFalse(foundAfter, "Removed favorite should not appear in list");
    }

    @Test
    void addFavoriteDuplicateShouldSucceed() {
        // First add
        miniAppService.addFavorite(testUser.getUserId(), testDish.getDishId());
        // Second add should return true (idempotent)
        boolean secondAdd = miniAppService.addFavorite(testUser.getUserId(), testDish.getDishId());
        assertTrue(secondAdd);
    }

    @Test
    void getFavoritesShouldPaginate() {
        // Add 2 favorites
        miniAppService.addFavorite(testUser.getUserId(), testDish.getDishId());

        // Create another dish and favorite it
        Dish dish2 = new Dish();
        dish2.setDishName("第二道菜");
        dish2.setPrice(new BigDecimal("20.00"));
        dish2.setCategory("粤菜");
        dish2.setStallId(1L);
        dish2.setStatus("approved");
        dishMapper.insert(dish2);
        miniAppService.addFavorite(testUser.getUserId(), dish2.getDishId());

        // Page 1, size 1
        List<Map<String, Object>> page1 = miniAppService.getFavorites(testUser.getUserId(), 1, 1);
        assertEquals(1, page1.size());

        // Page 2, size 1
        List<Map<String, Object>> page2 = miniAppService.getFavorites(testUser.getUserId(), 2, 1);
        assertEquals(1, page2.size());
    }

    // ==================== 互动测试 ====================

    @Test
    void scoreDishShouldCreateRecord() {
        boolean scored = miniAppService.scoreDish(testUser.getUserId(), testDish.getDishId(), 4);
        assertTrue(scored);

        List<Map<String, Object>> history = miniAppService.getHistory(testUser.getUserId(), 1, 10);
        assertFalse(history.isEmpty());
        assertEquals(4, history.get(0).get("score"));
    }

    @Test
    void scoreDishShouldUpdateExistingRecord() {
        // First score
        miniAppService.scoreDish(testUser.getUserId(), testDish.getDishId(), 3);

        // Update score for same user+dish
        boolean updated = miniAppService.scoreDish(testUser.getUserId(), testDish.getDishId(), 5);
        assertTrue(updated);

        List<Map<String, Object>> history = miniAppService.getHistory(testUser.getUserId(), 1, 10);
        assertEquals(1, history.size());
        assertEquals(5, history.get(0).get("score"));
    }

    @Test
    void likeDishShouldCreateRecord() {
        boolean liked = miniAppService.likeDish(testUser.getUserId(), testDish.getDishId(), true);
        assertTrue(liked);

        List<Map<String, Object>> history = miniAppService.getHistory(testUser.getUserId(), 1, 10);
        assertFalse(history.isEmpty());
        assertEquals(true, history.get(0).get("like_status"));
    }

    @Test
    void likeDishShouldToggleLikeStatus() {
        // First like
        miniAppService.likeDish(testUser.getUserId(), testDish.getDishId(), true);

        // Unlike
        boolean unliked = miniAppService.likeDish(testUser.getUserId(), testDish.getDishId(), false);
        assertTrue(unliked);

        List<Map<String, Object>> history = miniAppService.getHistory(testUser.getUserId(), 1, 10);
        assertEquals(false, history.get(0).get("like_status"));
    }

    // ==================== 历史测试 ====================

    @Test
    void getHistoryShouldReturnEmptyForNewUser() {
        List<Map<String, Object>> history = miniAppService.getHistory(testUser.getUserId(), 1, 10);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void getHistoryShouldPaginate() {
        // Create 3 history records
        for (int i = 0; i < 3; i++) {
            Dish d = new Dish();
            d.setDishName("分页测试菜品" + i);
            d.setPrice(new BigDecimal("10.00"));
            d.setCategory("面食");
            d.setStallId(1L);
            d.setStatus("approved");
            dishMapper.insert(d);
            miniAppService.scoreDish(testUser.getUserId(), d.getDishId(), 3 + i);
        }

        List<Map<String, Object>> page1 = miniAppService.getHistory(testUser.getUserId(), 1, 2);
        assertEquals(2, page1.size());

        List<Map<String, Object>> page2 = miniAppService.getHistory(testUser.getUserId(), 2, 2);
        assertEquals(1, page2.size());
    }
}
