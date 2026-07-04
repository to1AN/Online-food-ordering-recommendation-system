package com.foodrec.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("merchant")
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Long merchantId;
    @NotBlank(message = "商户名称不能为空")
    private String merchantName;
    @NotBlank(message = "联系方式不能为空")
    private String contactInfo;
    private LocalDate createTime;
}
