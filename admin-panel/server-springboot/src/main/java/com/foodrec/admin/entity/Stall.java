package com.foodrec.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("stall")
public class Stall {
    @TableId(type = IdType.AUTO)
    private Long stallId;
    private String stallName;
    private String location;
    private Long merchantId;
}
