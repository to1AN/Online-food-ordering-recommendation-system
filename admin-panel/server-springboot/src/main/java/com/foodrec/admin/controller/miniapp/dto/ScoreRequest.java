package com.foodrec.admin.controller.miniapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;
    @NotNull(message = "dishId不能为空")
    private Long dishId;
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private int score;
}
