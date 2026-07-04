package com.foodrec.admin.controller.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScoreDistributionVO {
    private List<Integer> distribution;
}
