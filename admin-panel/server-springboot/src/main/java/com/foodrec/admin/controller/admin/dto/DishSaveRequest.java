package com.foodrec.admin.controller.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishSaveRequest {
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
}
