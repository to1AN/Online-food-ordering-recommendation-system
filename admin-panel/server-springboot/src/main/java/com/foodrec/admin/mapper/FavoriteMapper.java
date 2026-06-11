package com.foodrec.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodrec.admin.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("<script>" +
            "SELECT f.favorite_id, f.user_id, f.dish_id, f.favorite_time, u.username, d.dish_name " +
            "FROM favorite f LEFT JOIN user u ON f.user_id = u.user_id " +
            "LEFT JOIN dish d ON f.dish_id = d.dish_id WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (u.username LIKE CONCAT('%',#{keyword},'%') OR d.dish_name LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "ORDER BY f.favorite_time DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Map<String, Object>> selectFavoriteWithNames(@Param("keyword") String keyword,
                                                        @Param("offset") int offset,
                                                        @Param("limit") int limit);

    /** 按用户ID查询收藏列表，联表查菜品信息 */
    @Select("SELECT f.favorite_id, f.user_id, f.dish_id, f.favorite_time, " +
            "d.dish_name, d.price, d.category, d.image_url " +
            "FROM favorite f JOIN dish d ON f.dish_id = d.dish_id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.favorite_time DESC LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectFavoritesByUserId(@Param("userId") Long userId,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);
}
