package com.foodrec.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@TableName("stall")
public class Stall {
    @TableId(type = IdType.AUTO)
    private Long stallId;
    @NotBlank(message = "档口名称不能为空")
    private String stallName;
    @NotBlank(message = "位置不能为空")
    private String location;
    @NotNull(message = "所属商户不能为空")
    private Long merchantId;
}
