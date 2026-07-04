package com.foodrec.admin.service.miniapp;

import com.foodrec.admin.entity.Dish;

import java.util.List;
import java.util.Map;

/**
 * 微信小程序接口服务
 * 对应需求：成员A(用户互动) + 成员B(选餐推荐)
 */
public interface MiniAppService {

    // ==================== 登录 ====================
    /** 微信 code 换取 token + userInfo */
    Map<String, Object> login(String code);

    /** 退出登录（JWT 无状态，无需服务端操作） */
    boolean logout(String token);

    /** 根据 token 获取 userId */
    Long getUserIdByToken(String token);

    // ==================== 推荐 ====================
    /** 随机推荐一道菜 */
    Dish getRandomDish();

    /** 今日好评榜（今日评分 ≥ 4 的菜品，按均分排序） */
    List<Map<String, Object>> getTodayPraise();

    /** 猜你喜欢（根据用户历史偏好推荐） */
    List<Map<String, Object>> getGuessLike(Long userId);

    // ==================== 收藏 ====================
    boolean addFavorite(Long userId, Long dishId);
    boolean removeFavorite(Long userId, Long dishId);
    List<Map<String, Object>> getFavorites(Long userId, int page, int pageSize);

    // ==================== 互动 ====================
    /** 评分（1~5），自动插入或更新 selection_history */
    boolean scoreDish(Long userId, Long dishId, int score);

    /** 点赞/取消点赞 */
    boolean likeDish(Long userId, Long dishId, boolean likeStatus);

    // ==================== 历史 ====================
    List<Map<String, Object>> getHistory(Long userId, int page, int pageSize);
}
