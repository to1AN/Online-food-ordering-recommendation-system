package com.foodrec.admin.controller.miniapp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class LoginResponse {
    private String token;
    private Map<String, Object> userInfo;
}
