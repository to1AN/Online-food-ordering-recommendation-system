package com.foodrec.admin.controller.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrendVO {
    private List<String> dates;
    private List<Integer> values;
}
