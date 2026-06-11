package com.foodrec.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long dishId;
    private String dishName;
    private BigDecimal price;
    private String category;
    private String description;
    private String imageUrl;
    private Long stallId;
}
