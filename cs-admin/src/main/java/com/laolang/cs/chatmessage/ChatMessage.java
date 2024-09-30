package com.laolang.cs.chatmessage;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 聊天消息
 * ChatMessage
 *
 * @since 2023/9/11 0:53
 */
@TableName(value = "tb_chat_message",autoResultMap = true)@Getter@Setter@Accessors(chain = true)
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 4130522732500072963L;
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer sender;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer receiver;
    /**
     * 房间ID（客服系统使用访客ID作为房间ID）
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer roomId;
    @TableField(updateStrategy = FieldStrategy.NEVER, typeHandler = JacksonTypeHandler.class)
    private Map<String,Object> message;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Date createTime;

    @TableField(exist = false)
    private String senderNickName;
    /**
     * 发送者头像
     */
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String receiverNickName;
    @TableField(exist = false)
    private Boolean read;
}
