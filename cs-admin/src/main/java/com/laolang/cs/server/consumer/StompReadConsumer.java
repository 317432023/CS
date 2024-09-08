package com.laolang.cs.server.consumer;

import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.laolang.cs.chatmessage.ChatMessageRead;
import com.laolang.cs.chatmessage.ChatMessageReadService;
import com.laolang.cs.server.protocol.Msg;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.function.Consumer;

/**
 * StompReadConsumer
 *
 * @since 2023/9/22 16:19
 */
@Slf4j
@AllArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class StompReadConsumer extends StompAbsConsumer implements Consumer<Msg> {

    private ChatMessageReadService chatMessageReadService;

    @Override
    public void accept(Msg recvMsg) {
        String value = (String)recvMsg.getMsgBody().get("value");
        boolean valid = ReUtil.isMatch("^[1-9]\\d+$", value);
        if(!valid) {
            log.warn("消息内容不合法 {}", value);
            return;
        }

        // 设置已读
        Long lastReadMsgId = Long.parseLong( value );
        chatMessageReadService.update(
            new UpdateWrapper<ChatMessageRead>()
                .eq("rcpt_id", recvMsg.getSender())
                .le("msg_id", lastReadMsgId)
                .set("`read`", true)
                .set("update_time", new Date())
        );

        // ACK 客户端
        response(recvMsg, Msg.ack(recvMsg.getMsgId()));
    }

}
