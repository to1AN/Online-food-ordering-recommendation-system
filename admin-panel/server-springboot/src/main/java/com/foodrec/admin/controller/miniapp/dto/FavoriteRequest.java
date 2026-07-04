package com.foodrec.admin.controller.miniapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;
    @NotNull(message = "dishId不能为空")
    private Long dishId;
}
