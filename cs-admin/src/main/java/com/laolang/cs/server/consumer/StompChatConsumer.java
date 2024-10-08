package com.laolang.cs.server.consumer;

import cn.hutool.core.date.DateUtil;
import com.cmpt.sys.comm.Constants;
import com.frm.redis.ModeDict;
import com.frm.redis.RedisTool;
import com.laolang.cs.chatmessage.ChatMessage;
import com.laolang.cs.chatmessage.ChatMessageRead;
import com.laolang.cs.chatmessage.ChatMessageReadService;
import com.laolang.cs.chatmessage.ChatMessageService;
import com.laolang.cs.chatuser.service.ChatUserService;
import com.laolang.cs.server.authen.SubsysTool;
import com.laolang.cs.server.protocol.Msg;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @since 2023/9/20 1:00
 */
@Slf4j
@AllArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class StompChatConsumer extends StompAbsConsumer implements Consumer<Msg> {

    private ChatMessageService chatMessageService;
    private ChatMessageReadService chatMessageReadService;
    private ChatUserService chatUserService;
    private SubsysTool subsysTool;
    private RedisTool redisTool;

    @Override
    public void accept(Msg recvMsg) {
        Map<String, Object> msgBody = recvMsg.getMsgBody();
        // 消息落地
        Date now = new Date();
        ChatMessage chatMessage = new ChatMessage()
                .setMessage(msgBody)
                .setSender(recvMsg.getSender()) // 发送者是当前 用户
                .setReceiver(recvMsg.getRcptId())
                .setRoomId(recvMsg.getRoomId());
        // 消息落地
        boolean ret = chatMessageService.persistMessage(chatMessage.setCreateTime(now), recvMsg.getMsgType());

        if (!ret) {
            // TIP 客户端
            response(recvMsg, Msg.tip("消息发送失败", recvMsg.getMsgId()));
            return;
        }

        // 取得接收者ID列表，设置未读消息
        List<Integer> rcptIdList = chatUserService.queryRcptIdList(recvMsg.getSender(), recvMsg.getRoomId());
        if (!rcptIdList.isEmpty()) {
            chatMessageReadService.saveBatch(
                    rcptIdList.stream().map(rcptId -> new ChatMessageRead()
                            .setRead(false)
                            .setMsgId(chatMessage.getId())
                            .setRcptId(rcptId)).collect(Collectors.toList())
            );
        }

        // ACK 客户端
        response(recvMsg, Msg.ack(recvMsg.getMsgId()));

        // 在线消息发送
        // 将消息发送给 群里的所有已订阅群的人
        // 对消息做一下后置处理
        if ("image".equals(recvMsg.getMsgBody().get("type"))) {
            String imgUrl = (String) recvMsg.getMsgBody().get("value");
            if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                String staticDomain = redisTool.hget(Constants.SYS_CONFIG_KEY, "STATIC_DOMAIN", ModeDict.APP_GROUP, 1);
                imgUrl = staticDomain + (imgUrl.startsWith("/") ? "" : "/") + imgUrl;
                recvMsg.getMsgBody().put("value", imgUrl);
            }
        }
        broadcast(recvMsg.setMsgId(chatMessage.getId().toString()).setCreateTime(now));

        /*是否人工座席*/
        boolean bCsUser = !chatMessage.getRoomId().equals(chatMessage.getReceiver());
        String tipAllCs = redisTool.hget(Constants.SYS_CONFIG_KEY, "TIP_ALL_CS", ModeDict.APP_GROUP, 1);
        /*是否提醒所有人工座席*/
        boolean bTipAllCs = StringUtils.isNotBlank(tipAllCs) && !"0".equals(tipAllCs);
        for (Integer receiver : rcptIdList) {
            if (bCsUser && !bTipAllCs && !receiver.equals(chatMessage.getReceiver())) {
                // 忽略当前站点的其他人工坐席
                continue;
            }
            // 通知每个聊天对手有消息待接收
            if (subsysTool.checkOnline(receiver)) {
                String clientId = subsysTool.getClientId(receiver);
                log.info("通知对手收到消息，对手clientId: " + clientId + ", msg => " + recvMsg.getMsgBody().get("value"));
                recvMsg.getMessagingTemplate().convertAndSendToUser(clientId, "/alone/cs/getResponse",
                        Msg.event(recvMsg.getSender() + "," + DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"), recvMsg.getMsgId()));
            }
        }

    }

}
