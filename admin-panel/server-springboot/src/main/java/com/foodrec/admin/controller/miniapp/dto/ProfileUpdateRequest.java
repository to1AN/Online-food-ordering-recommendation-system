package com.foodrec.admin.controller.miniapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;
    private String username;
    private String avatar;
}
