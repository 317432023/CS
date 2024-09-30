package com.laolang.cs.chatuser;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * ChatUser
 *
 * @since 2023/9/11 0:39
 */
@TableName("tb_chat_user")
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(of = {"avatar"})
public class ChatUser implements Serializable {
    private static final long serialVersionUID = -913408839608339811L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    /**
     * 租户ID
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String tenantId;
    /**
     * 昵称
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String nickName;
    /**
     * 用户类型0-客人; 1-客服人员(有关联系统用户)
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer userType;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 关联用户ID 本系统客服人员 或 接入系统会员
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long relId;
    /**
     * 最后一次接收或发送消息的日期
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(timezone = "GMT+8", locale = "zh_CN", pattern = "yyyy-MM-dd HH:mm:ss")//@JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date lastMessageTime;

    @Override
    public String toString() {
        return '{' +
                "\"id\":" + id + ',' +
                "\"tenantId\":" + '\"' + tenantId + '\"' + ',' +
                "\"nickName\":" + '\"' + nickName + '\"' + ',' +
                "\"userType\":" + userType + ',' +
                "\"relId\":" + relId + ',' +
                "\"createTime\":" + '\"' + DateUtil.format(createTime, "yyyy-MM-dd HH:mm:ss") + '\"' + ',' +
                "\"lastMessageTime\":" + '\"' + DateUtil.format(createTime, "yyyy-MM-dd HH:mm:ss") + '\"' +
                '}';
    }

    @TableField(exist = false)
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    private String clientId;

}
