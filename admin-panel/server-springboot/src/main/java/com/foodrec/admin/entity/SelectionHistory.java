package com.foodrec.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("selection_history")
public class SelectionHistory {
    @TableId(type = IdType.AUTO)
    private Long historyId;
    private Long userId;
    private Long dishId;
    private Integer score;
    private Boolean likeStatus;
    private LocalDateTime selectTime;
}
