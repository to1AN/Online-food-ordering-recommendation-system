package com.foodrec.admin.controller;

import com.foodrec.admin.common.Result;
import com.foodrec.admin.entity.Dish;
import com.foodrec.admin.service.miniapp.MiniAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 微信小程序接口控制器
 * 对应需求：
 *   成员A - 登录注册、收藏管理、评分点赞、选餐历史
 *   成员B - 随机选餐、猜你喜欢、今日好评榜、菜品详情
 *
 * 路径 /api/miniapp/*  区分于管理后台 /api/admin/*
 */
@RestController
@RequestMapping("/api/miniapp")
@CrossOrigin(origins = "*")
public class MiniAppController {

    @Autowired
    private MiniAppService miniAppService;

    // ==================== 登录 ====================

    /**
     * 微信登录
     * POST /api/miniapp/login
     * Body: { "code": "wx.login() 返回的 code" }
     * 返回: { token, userInfo: { userId, username, avatar } }
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return Result.fail("缺少 code 参数");
        }
        try {
            Map<String, Object> data = miniAppService.login(code);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    // ==================== 推荐 ====================

    /**
     * 随机推荐一道菜
     * GET /api/miniapp/recommend/random
     * 返回: { dish: {...} }
     */
    @GetMapping("/recommend/random")
    public Result<Map<String, Object>> randomDish() {
        Dish dish = miniAppService.getRandomDish();
        if (dish == null) {
            return Result.fail("暂无菜品数据");
        }
        return Result.ok(Map.of("dish", dish));
    }

    /**
     * 今日好评榜 TOP 10
     * GET /api/miniapp/recommend/today-praise
     */
    @GetMapping("/recommend/today-praise")
    public Result<Map<String, Object>> todayPraise() {
        List<Map<String, Object>> list = miniAppService.getTodayPraise();
        return Result.ok(Map.of("list", list));
    }

    /**
     * 猜你喜欢
     * GET /api/miniapp/recommend/guess-like?userId=
     */
    @GetMapping("/recommend/guess-like")
    public Result<Map<String, Object>> guessLike(@RequestParam Long userId) {
        List<Map<String, Object>> list = miniAppService.getGuessLike(userId);
        return Result.ok(Map.of("list", list));
    }

    // ==================== 收藏 ====================

    /**
     * 添加收藏
     * POST /api/miniapp/favorite
     * Body: { userId, dishId }
     */
    @PostMapping("/favorite")
    public Result<String> addFavorite(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long dishId = body.get("dishId");
        if (userId == null || dishId == null) {
            return Result.fail("userId 和 dishId 不能为空");
        }
        return miniAppService.addFavorite(userId, dishId)
                ? Result.ok("收藏成功")
                : Result.fail("收藏失败");
    }

    /**
     * 取消收藏
     * DELETE /api/miniapp/favorite?userId=&dishId=
     */
    @DeleteMapping("/favorite")
    public Result<String> removeFavorite(@RequestParam Long userId,
                                          @RequestParam Long dishId) {
        return miniAppService.removeFavorite(userId, dishId)
                ? Result.ok("已取消收藏")
                : Result.fail("取消失败");
    }

    /**
     * 我的收藏列表
     * GET /api/miniapp/favorites?userId=&page=&pageSize=
     */
    @GetMapping("/favorites")
    public Result<Map<String, Object>> getFavorites(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<Map<String, Object>> list = miniAppService.getFavorites(userId, page, pageSize);
        return Result.ok(Map.of("list", list));
    }

    // ==================== 互动 ====================

    /**
     * 评分
     * POST /api/miniapp/score
     * Body: { userId, dishId, score (1~5) }
     */
    @PostMapping("/score")
    public Result<String> scoreDish(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        Long dishId = toLong(body.get("dishId"));
        int score = (int) body.getOrDefault("score", 0);
        if (userId == null || dishId == null || score < 1 || score > 5) {
            return Result.fail("参数错误：需要 userId, dishId, score(1~5)");
        }
        return miniAppService.scoreDish(userId, dishId, score)
                ? Result.ok("评分成功")
                : Result.fail("评分失败");
    }

    /**
     * 点赞 / 取消点赞
     * POST /api/miniapp/like
     * Body: { userId, dishId, likeStatus: true/false }
     */
    @PostMapping("/like")
    public Result<String> likeDish(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        Long dishId = toLong(body.get("dishId"));
        Boolean likeStatus = (Boolean) body.get("likeStatus");
        if (userId == null || dishId == null || likeStatus == null) {
            return Result.fail("参数错误：需要 userId, dishId, likeStatus");
        }
        return miniAppService.likeDish(userId, dishId, likeStatus)
                ? Result.ok(likeStatus ? "已点赞" : "已取消点赞")
                : Result.fail("操作失败");
    }

    // ==================== 历史 ====================

    /**
     * 选餐历史
     * GET /api/miniapp/history?userId=&page=&pageSize=
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<Map<String, Object>> list = miniAppService.getHistory(userId, page, pageSize);
        return Result.ok(Map.of("list", list));
    }

    // ==================== 工具方法 ====================

    /** 安全转换 Number → Long（JSON 反序列化时 number 可能为 Integer） */
    private Long toLong(Object val) {
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) return Long.parseLong((String) val);
        return null;
    }
}
