package com.laolang.cs.server.consumer;

import com.laolang.cs.server.protocol.Msg;

/**
 * StompAbsConsumer
 *
 * @date 2023/9/20 0:57
 */
public abstract class StompAbsConsumer {

    /**
     * 响应消息
     * @param recvMsg
     * @param sendMsg
     */
    protected void response(Msg recvMsg, Msg sendMsg) {
        recvMsg.getMessagingTemplate().convertAndSendToUser(recvMsg.getClientId(), "/alone/cs/getResponse", sendMsg);
    }

    /**
     * 消息扩散
     * @param recvMsg
     */
    protected void broadcast(Msg recvMsg) {
        recvMsg.getMessagingTemplate().convertAndSend("/mass/cs/getResponse" + "/" + recvMsg.getRoomId(), recvMsg);
    }

}
