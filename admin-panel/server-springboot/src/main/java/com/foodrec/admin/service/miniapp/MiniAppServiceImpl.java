package com.foodrec.admin.service.miniapp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodrec.admin.entity.*;
import com.foodrec.admin.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import com.foodrec.admin.common.JwtUtils;
import java.util.stream.Collectors;

@Service
public class MiniAppServiceImpl implements MiniAppService {

    @Autowired private UserMapper userMapper;
    @Autowired private DishMapper dishMapper;
    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private SelectionHistoryMapper historyMapper;
    @Autowired private JwtUtils jwtUtils;

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    // ==================== 登录 ====================

    @Override
    public Map<String, Object> login(String code) {
        String openid;
        // 尝试调用微信接口获取 openid
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session"
                    + "?appid=" + appid
                    + "&secret=" + secret
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";
            Map<?, ?> wxResp = restTemplate.getForObject(url, Map.class);
            if (wxResp != null && wxResp.get("openid") != null) {
                openid = (String) wxResp.get("openid");
            } else {
                // 微信接口返回错误（如 AppID/Secret 未配置），使用开发模式
                openid = "dev_openid_" + code.hashCode();
            }
        } catch (Exception e) {
            // 网络异常或配置未就绪，降级为开发模式
            openid = "dev_openid_" + Math.abs(code.hashCode());
        }

        // 根据 openid 查找或创建用户
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(qw);

        if (user == null) {
            // 新用户注册
            user = new User();
            user.setUsername("微信用户" + (1000 + Math.abs(openid.hashCode()) % 9000));
            user.setPassword("");           // 微信登录无需密码
            user.setOpenid(openid);
            user.setRegisterTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        // 生成 JWT token
        String token = jwtUtils.generateToken(user.getUserId(), openid);

        // 构造返回
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("userInfo", userInfo);

        return result;
    }

    @Override
    public boolean logout(String token) {
        // JWT is stateless, no server-side invalidation needed
        return true;
    }

    // ==================== 推荐 ====================

    @Override
    public Dish getRandomDish() {
        return dishMapper.selectRandomDish();
    }

    @Override
    public List<Map<String, Object>> getTodayPraise() {
        return dishMapper.selectTodayPraise();
    }

    @Override
    public List<Map<String, Object>> getGuessLike(Long userId) {
        // 1. 找到用户历史中评分最高的菜品分类
        LambdaQueryWrapper<SelectionHistory> hqw = new LambdaQueryWrapper<>();
        hqw.eq(SelectionHistory::getUserId, userId)
           .ge(SelectionHistory::getScore, 3)
           .orderByDesc(SelectionHistory::getSelectTime)
           .last("LIMIT 20");
        List<SelectionHistory> recentHistories = historyMapper.selectList(hqw);

        Set<Long> triedDishIds = new HashSet<>();
        Set<String> preferredCategories = new LinkedHashSet<>();
        for (SelectionHistory h : recentHistories) {
            triedDishIds.add(h.getDishId());
        }
        // Batch load dishes to avoid N+1
        if (!triedDishIds.isEmpty()) {
            List<Dish> dishes = dishMapper.selectBatchIds(new ArrayList<>(triedDishIds));
            for (Dish dish : dishes) {
                preferredCategories.add(dish.getCategory());
            }
        }

        // 同时加入收藏的菜品分类
        LambdaQueryWrapper<Favorite> fqw = new LambdaQueryWrapper<>();
        fqw.eq(Favorite::getUserId, userId);
        List<Favorite> favs = favoriteMapper.selectList(fqw);
        Set<Long> favDishIds = new HashSet<>();
        for (Favorite f : favs) {
            triedDishIds.add(f.getDishId());
            favDishIds.add(f.getDishId());
        }
        if (!favDishIds.isEmpty()) {
            List<Dish> dishes = dishMapper.selectBatchIds(new ArrayList<>(favDishIds));
            for (Dish dish : dishes) {
                preferredCategories.add(dish.getCategory());
            }
        }

        // 2. 在偏好分类中推荐未尝试过的菜品
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (String category : preferredCategories) {
            LambdaQueryWrapper<Dish> dqw = new LambdaQueryWrapper<>();
            dqw.eq(Dish::getCategory, category)
               .notIn(!triedDishIds.isEmpty(), Dish::getDishId, triedDishIds)
               .last("LIMIT 4");
            List<Dish> dishes = dishMapper.selectList(dqw);
            for (Dish d : dishes) {
                Map<String, Object> map = dishToMap(d);
                map.put("reason", "喜欢" + category);
                recommendations.add(map);
                triedDishIds.add(d.getDishId());
            }
        }

        return recommendations;
    }

    // ==================== 收藏 ====================

    @Override
    public boolean addFavorite(Long userId, Long dishId) {
        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, userId).eq(Favorite::getDishId, dishId);
        if (favoriteMapper.selectCount(qw) > 0) return true; // 已存在

        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setDishId(dishId);
        f.setFavoriteTime(LocalDateTime.now());
        return favoriteMapper.insert(f) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long dishId) {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, userId).eq(Favorite::getDishId, dishId);
        return favoriteMapper.delete(qw) > 0;
    }

    @Override
    public List<Map<String, Object>> getFavorites(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return favoriteMapper.selectFavoritesByUserId(userId, offset, pageSize);
    }

    // ==================== 互动 ====================

    @Override
    public boolean scoreDish(Long userId, Long dishId, int score) {
        // 查找今天的选餐记录
        LambdaQueryWrapper<SelectionHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(SelectionHistory::getUserId, userId)
          .eq(SelectionHistory::getDishId, dishId);
        SelectionHistory record = historyMapper.selectOne(qw);

        if (record != null) {
            record.setScore(score);
            return historyMapper.updateById(record) > 0;
        } else {
            // 新建记录
            record = new SelectionHistory();
            record.setUserId(userId);
            record.setDishId(dishId);
            record.setScore(score);
            record.setSelectTime(LocalDateTime.now());
            return historyMapper.insert(record) > 0;
        }
    }

    @Override
    public boolean likeDish(Long userId, Long dishId, boolean likeStatus) {
        LambdaQueryWrapper<SelectionHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(SelectionHistory::getUserId, userId)
          .eq(SelectionHistory::getDishId, dishId);
        SelectionHistory record = historyMapper.selectOne(qw);

        if (record != null) {
            record.setLikeStatus(likeStatus);
            return historyMapper.updateById(record) > 0;
        } else {
            record = new SelectionHistory();
            record.setUserId(userId);
            record.setDishId(dishId);
            record.setLikeStatus(likeStatus);
            record.setSelectTime(LocalDateTime.now());
            return historyMapper.insert(record) > 0;
        }
    }

    // ==================== 历史 ====================

    @Override
    public List<Map<String, Object>> getHistory(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return historyMapper.selectHistoryByUserId(userId, offset, pageSize);
    }

    // ==================== 工具方法 ====================

    /** Dish 实体转 Map，字段名保持 snake_case */
    private Map<String, Object> dishToMap(Dish d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("dish_id", d.getDishId());
        map.put("dish_name", d.getDishName());
        map.put("price", d.getPrice());
        map.put("category", d.getCategory());
        map.put("description", d.getDescription());
        map.put("image_url", d.getImageUrl());
        map.put("stall_id", d.getStallId());
        return map;
    }

    @Override
    public Long getUserIdByToken(String token) {
        return jwtUtils.getUserIdFromToken(token);
    }
}
