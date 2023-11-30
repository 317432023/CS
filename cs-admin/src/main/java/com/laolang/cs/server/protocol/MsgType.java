package com.laolang.cs.server.protocol;

import com.laolang.cs.server.consumer.StompChatConsumer;
import com.laolang.cs.server.consumer.StompReadConsumer;

import java.util.function.Consumer;


/**
 * 消息类型枚举
 */
public enum MsgType {

    //heartbeat("心跳消息", null), // stomp+sockjs 的 心跳由框架自身实现
    ack("确认收到", null),
    tip("提醒消息", null),
    chat("聊天内容", StompChatConsumer.class),
    read("已读消息", StompReadConsumer.class),
    ;

    public final String code;
    public final Class<Consumer<Msg>> stompClazz;

    MsgType(String code, Class stompClazz) {
        this.code = code;
        this.stompClazz = stompClazz;
    }

}
