package com.laolang.cs.server.protocol;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;
import java.util.Map;

/**
 * 私有协议
 * Msg - WebSocket 消息
 *
 * @date 2023/2/26 21:03
 */
@Getter
@Setter
@Accessors(chain = true)
public class Msg {
    @JsonIgnore
    @JSONField(serialize = false)
    private SimpMessagingTemplate messagingTemplate;
    @JsonIgnore
    @JSONField(serialize = false)
    private String clientId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // 仅可用于序列化显示
    @JSONField(serialize = false)
    private Integer sender;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // 仅可用于序列化显示
    @JSONField(serialize = false)
    private String senderNickName;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // 仅可用于序列化显示
    @JsonFormat(timezone = "GMT+8",locale = "zh_CN", pattern = "yyyy-MM-dd HH:mm:ss")//@JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JSONField(serialize = false)
    private Date createTime;
    /*
     msgId:'',   // 消息ID
     msgType:'', // 消息类型
     roomId: 0,  // 聊天室ID
     msgBody: '' // 消息体
    */
    private String msgId;
    private MsgType msgType;
    private Integer roomId;
    private Map<String,Object> msgBody;

    public static void main(String[] args) {
        Msg msg = new Msg();
        msg.setMsgId(UUID.fastUUID().toString());
        msg.setMsgType(MsgType.ack);
        System.out.println(JSON.toJSONString(msg));
    }

    // 这些类型为服务端发送的消息

    /**
     * 确认到达消息
     * @param msgId 客户端上送的消息ID
     * @return
     */
    public static Msg ack(String msgId) {
        return new Msg().setMsgId(msgId).setMsgType(MsgType.ack);
    }

    ///**
    // * 心跳消息<br>
    // *     stomp 的心跳由框架自动实现
    // * @return
    // */
    //public static Msg heartbeat() {
    //    Msg msg = new Msg();
    //    msg.setMsgId(UUID.randomUUID().toString());
    //    msg.setMsgType(MsgType.heartbeat);
    //    return msg;
    //}

    /**
     * 提醒消息
     * @return
     */
    public static Msg tip(String message) {
        return msg(message, MsgType.tip, null);
    }

    /**
     * 提醒消息
     * @return
     */
    public static Msg tip(String message, String msgId) {
        return msg(message, MsgType.tip, msgId);
    }

    /**
     * 任意消息
     * @return
     */
    public static Msg msg(String message, MsgType msgType, String msgId) {
        Msg msg = new Msg();
        if(StringUtils.isNotBlank(msgId)) {
            msg.setMsgId(msgId);
        } else {
            msg.setMsgId(UUID.fastUUID().toString());
        }
        msg.setMsgType(msgType);
        msg.setMsgBody(Dict.create().set("type","text").set("value",message));
        return msg;
    }

}
