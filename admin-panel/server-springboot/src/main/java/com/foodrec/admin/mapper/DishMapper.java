package com.foodrec.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodrec.admin.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @Select("<script>" +
            "SELECT d.dish_id, d.dish_name, d.price, d.category, d.description, d.image_url, d.stall_id, " +
            "s.stall_name, m.merchant_name " +
            "FROM dish d LEFT JOIN stall s ON d.stall_id = s.stall_id " +
            "LEFT JOIN merchant m ON s.merchant_id = m.merchant_id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (d.dish_name LIKE CONCAT('%',#{keyword},'%') OR d.category LIKE CONCAT('%',#{keyword},'%') OR s.stall_name LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "<if test='category != null and category != \"\"'>" +
            "AND d.category = #{category} " +
            "</if>" +
            "ORDER BY d.dish_id LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Map<String, Object>> selectDishWithStall(@Param("keyword") String keyword,
                                                    @Param("category") String category,
                                                    @Param("offset") int offset,
                                                    @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM dish d LEFT JOIN stall s ON d.stall_id = s.stall_id " +
            "LEFT JOIN merchant m ON s.merchant_id = m.merchant_id WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (d.dish_name LIKE CONCAT('%',#{keyword},'%') OR d.category LIKE CONCAT('%',#{keyword},'%') OR s.stall_name LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "<if test='category != null and category != \"\"'>" +
            "AND d.category = #{category} " +
            "</if>" +
            "</script>")
    long countDishWithStall(@Param("keyword") String keyword, @Param("category") String category);

    /** 随机获取一道菜品 */
    @Select("SELECT * FROM dish ORDER BY RAND() LIMIT 1")
    Dish selectRandomDish();

    /**
     * 今日好评榜：今天内评分记录中均分最高的菜品 TOP 10
     * 返回包含 dish 字段 + avg_score + praise_count
     */
    @Select("SELECT d.dish_id, d.dish_name, d.price, d.category, d.description, d.image_url, d.stall_id, " +
            "ROUND(AVG(h.score), 1) AS avg_score, COUNT(*) AS praise_count " +
            "FROM selection_history h JOIN dish d ON h.dish_id = d.dish_id " +
            "WHERE h.score >= 4 AND DATE(h.select_time) = CURDATE() " +
            "GROUP BY h.dish_id ORDER BY avg_score DESC, praise_count DESC LIMIT 10")
    List<Map<String, Object>> selectTodayPraise();
}
