package com.foodrec.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodrec.admin.entity.Stall;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StallMapper extends BaseMapper<Stall> {

    @Select("SELECT s.stall_id, s.stall_name, s.location, s.merchant_id, m.merchant_name " +
            "FROM stall s LEFT JOIN merchant m ON s.merchant_id = m.merchant_id " +
            "WHERE (#{keyword} IS NULL OR #{keyword} = '' OR s.stall_name LIKE CONCAT('%',#{keyword},'%') " +
            "OR s.location LIKE CONCAT('%',#{keyword},'%') OR m.merchant_name LIKE CONCAT('%',#{keyword},'%')) " +
            "ORDER BY s.stall_id LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectStallWithMerchant(@Param("keyword") String keyword,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM stall s LEFT JOIN merchant m ON s.merchant_id = m.merchant_id " +
            "WHERE (#{keyword} IS NULL OR #{keyword} = '' OR s.stall_name LIKE CONCAT('%',#{keyword},'%') " +
            "OR s.location LIKE CONCAT('%',#{keyword},'%') OR m.merchant_name LIKE CONCAT('%',#{keyword},'%'))")
    long countStallWithMerchant(@Param("keyword") String keyword);

    @Select("SELECT s.stall_name AS name, COUNT(d.dish_id) AS value " +
            "FROM stall s LEFT JOIN dish d ON s.stall_id = d.stall_id " +
            "GROUP BY s.stall_id, s.stall_name ORDER BY value DESC")
    List<Map<String, Object>> selectStallDishCount();
}
