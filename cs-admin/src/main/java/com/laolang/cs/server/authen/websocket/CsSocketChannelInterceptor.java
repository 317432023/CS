package com.laolang.cs.server.authen.websocket;

import com.cmpt.ws.WebSocketUser;
import com.cmpt.ws.props.WebSocketProperties;
import com.comm.pojo.SystemException;
import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.server.authen.SubsysTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * CsSocketChannelInterceptor
 *
 * @since 2023/5/13 16:14
 */
@Slf4j
@AllArgsConstructor
@Component
public class CsSocketChannelInterceptor implements ChannelInterceptor {
    private WebSocketProperties webSocketProperties;
    private SubsysTool subsysTool;

    /**
     * 发送消息到通道前
     *
     * @param message
     * @param channel
     * @return
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 获取连接头信息
        StompHeaderAccessor accessor = Objects.requireNonNull(MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class));//StompHeaderAccessor.wrap(message);

        WebSocketUser webSocketUser = null;
        String token, userType, tenantId; // 令牌 用户类型 租户

        if (accessor.getCommand() == null) {
            if (Objects.requireNonNull(accessor.getMessageType()) == SimpMessageType.HEARTBEAT) { // 心跳
                webSocketUser = (WebSocketUser) accessor.getUser();
                if (webSocketUser != null) {
                    log.info("收到心跳 clientId => " + webSocketUser.getName() + ", userId => " + webSocketUser.getUserId());
                    // 更新在线会话
                    subsysTool.renew(webSocketUser.getName(), webSocketUser.getUserId());
                }
            }
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT:
                // 获取头中的token
                token = accessor.getFirstNativeHeader(webSocketProperties.getTokenName());
                if (StringUtils.isNotBlank(token)) {
                    accessor.removeNativeHeader(webSocketProperties.getTokenName());
                }
                userType = accessor.getFirstNativeHeader("userType");
                tenantId = accessor.getFirstNativeHeader("tenantId");

                // validate and convert to a Principal based on your own requirements e.g.
                // authenticationManager.authenticate(JwtAuthentication(token))
                if (StringUtils.isNotBlank(token)) {
                    // 鉴权
                    try {
                        ChatUser chatUser = subsysTool.authenticate(token, tenantId, StringUtils.isNotBlank(userType) ? Integer.parseInt(userType) : null);
                        webSocketUser = new WebSocketUser(chatUser.getClientId(), chatUser.getId().toString());
                        accessor.setUser(webSocketUser);
                        // not documented anywhere but necessary otherwise NPE in StompSubProtocolHandler!
                        accessor.setLeaveMutable(true);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        //throw e;
                    }
                }

                if (webSocketUser == null) {
                    throw new SystemException(401, "连接未认证，原因：" + (StringUtils.isBlank(token) ? "缺少令牌" : "令牌无效 " + token));
                }

                break;
            case DISCONNECT:

                break;
            case SUBSCRIBE:
            case SEND:

                break;
            default:
                break;

        }

        if (log.isDebugEnabled()) {
            token = webSocketUser != null ? webSocketUser.getName() : null;
            log.debug("MessageType => " + accessor.getMessageType().name() + " , token => " + token);
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    /**
     * 发送消息到通道后
     *
     * @param message
     * @param channel
     * @return
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        ChannelInterceptor.super.postSend(message, channel, sent);
    }

    /**
     * 发送完成后
     *
     * @param message
     * @param channel
     * @return
     */
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        return ChannelInterceptor.super.preReceive(channel);
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        return ChannelInterceptor.super.postReceive(message, channel);
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        ChannelInterceptor.super.afterReceiveCompletion(message, channel, ex);
    }
}
