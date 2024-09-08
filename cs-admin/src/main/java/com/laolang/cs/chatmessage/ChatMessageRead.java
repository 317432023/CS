package com.laolang.cs.chatmessage;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息已读情况
 * ChatMessageRead
 *
 * @since 2023/9/20 15:44
 */
@TableName("tb_chat_message_read")@Getter@Setter
@Accessors(chain = true)
public class ChatMessageRead implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long msgId;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer rcptId;
    @TableField("`read`")
    private Boolean read;
    private Date updateTime;
}
