package com.foodrec.admin.controller.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StallSaveRequest {
    @NotBlank(message = "档口名称不能为空")
    private String stallName;
    @NotBlank(message = "位置不能为空")
    private String location;
    @NotNull(message = "所属商户不能为空")
    private Long merchantId;
}
