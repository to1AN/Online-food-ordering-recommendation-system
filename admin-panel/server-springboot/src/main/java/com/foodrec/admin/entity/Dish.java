package com.foodrec.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long dishId;
    @NotBlank(message = "菜品名称不能为空")
    private String dishName;
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal price;
    @NotBlank(message = "分类不能为空")
    private String category;
    private String description;
    private String imageUrl;
    @NotNull(message = "所属档口不能为空")
    private Long stallId;
    private String status; // pending, approved, rejected
}
