package com.foodrec.admin.controller.admin.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private long totalUsers;
    private long totalMerchants;
    private long totalStalls;
    private long totalDishes;
    private long totalFavorites;
    private long totalHistories;
    private double avgScore;
    private long todaySelects;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> stallDistribution;
}
