package com.foodrec.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodrec.admin.entity.SelectionHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SelectionHistoryMapper extends BaseMapper<SelectionHistory> {

    @Select("<script>" +
            "SELECT h.history_id, h.user_id, h.dish_id, h.score, h.like_status, h.select_time, " +
            "u.username, d.dish_name " +
            "FROM selection_history h LEFT JOIN user u ON h.user_id = u.user_id " +
            "LEFT JOIN dish d ON h.dish_id = d.dish_id WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (u.username LIKE CONCAT('%',#{keyword},'%') OR d.dish_name LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "ORDER BY h.select_time DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Map<String, Object>> selectHistoryWithNames(@Param("keyword") String keyword,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    /** 按用户ID查询选餐历史，联表查菜品信息 */
    @Select("SELECT h.history_id, h.user_id, h.dish_id, h.score, h.like_status, h.select_time, " +
            "d.dish_name, d.price, d.category, d.image_url " +
            "FROM selection_history h JOIN dish d ON h.dish_id = d.dish_id " +
            "WHERE h.user_id = #{userId} " +
            "ORDER BY h.select_time DESC LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectHistoryByUserId(@Param("userId") Long userId,
                                                     @Param("offset") int offset,
                                                     @Param("limit") int limit);
}
