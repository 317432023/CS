package com.laolang.cs.server.consumer;

import com.frm.redis.ModeDict;
import com.frm.redis.RedisTool;
import com.frm.springmvc.SpringContextHolder;
import com.laolang.cs.chatmessage.ChatMessage;
import com.laolang.cs.chatmessage.ChatMessageRead;
import com.laolang.cs.chatmessage.ChatMessageReadService;
import com.laolang.cs.chatmessage.ChatMessageService;
import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.chatuser.ChatUserService;
import com.laolang.cs.server.protocol.Msg;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * StompChatConsumer
 *
 * @date 2023/9/20 1:00
 */
@Slf4j
@AllArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class StompChatConsumer extends StompAbsConsumer implements Consumer<Msg> {

    private ChatMessageService chatMessageService;
    private ChatMessageReadService chatMessageReadService;
    private ChatUserService chatUserService;

    @Override
    public void accept(Msg recvMsg) {
        Map<String, Object> msgBody = recvMsg.getMsgBody();
        // 消息落地
        Date now = new Date();
        ChatMessage chatMessage = new ChatMessage()
            .setMessage(msgBody)
            .setSender(recvMsg.getSender()) // 发送者是当前 用户
            .setReceiver(recvMsg.getRoomId()); // 接收者是一个 群ID = 客人ID
        boolean ret = chatMessageService.save(chatMessage.setCreateTime(now));

        // 取得接收者ID列表，设置未读消息
        List<Integer> rcptIdList = chatUserService.queryRcptIdList(recvMsg.getSender(), recvMsg.getRoomId());
        if(rcptIdList.size() > 0) {
            chatMessageReadService.saveBatch(
                rcptIdList.stream().map(rcptId -> new ChatMessageRead()
                    .setRead(false)
                    .setMsgId(chatMessage.getId())
                    .setRcptId(rcptId)).collect(Collectors.toList())
            );
        }

        if(!ret) {
            //  TIP 客户端
            response(recvMsg, Msg.tip("消息发送失败").setMsgId(recvMsg.getMsgId()));
            return;
        }

        // ACK 客户端
        response(recvMsg, Msg.ack(recvMsg.getMsgId()));

        // 在线消息发送
        // 将消息发送给 群里的所有已订阅群的人
        // 对消息做一下后置处理
        if("image".equals(recvMsg.getMsgBody().get("type"))) {
            String imgUrl = (String)recvMsg.getMsgBody().get("value");
            if(!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                RedisTool redisTool = SpringContextHolder.getBean("redisTool", RedisTool.class);
                String staticDomain = redisTool.hget("system_config", "STATIC_DOMAIN", ModeDict.APP_GROUP, 1);
                imgUrl = staticDomain + (imgUrl.startsWith("/") ? "" : "/") + imgUrl;
                recvMsg.getMsgBody().put("value", imgUrl);
            }
        }
        broadcast(recvMsg.setCreateTime(now));
    }

}
