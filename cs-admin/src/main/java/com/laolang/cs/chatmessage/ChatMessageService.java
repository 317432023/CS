package com.laolang.cs.chatmessage;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laolang.cs.chatmessage.mapper.ChatMessageMapper;
import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.chatuser.service.ChatUserService;
import com.laolang.cs.server.protocol.MsgType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ChatMessageService
 *
 * @since 2023/9/11 0:42
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IService<ChatMessage> {
    /**
     * 消息落地
     *
     * @param chatMessage
     * @param msgType
     */
    public boolean persistMessage(ChatMessage chatMessage, MsgType msgType) {
        boolean ret = save(chatMessage);
        if (msgType == MsgType.chat) {
            // 更新接收者最后聊天时间
            ChatUserService chatUserService = SpringUtil.getBean(ChatUserService.class);
            ChatUser receiver = chatUserService.getById(chatMessage.getReceiver());
            receiver.setLastMessageTime(chatMessage.getCreateTime());
            chatUserService.updateById(receiver);
            if (!chatMessage.getRoomId().equals(receiver.getId())) {
                // 更新坐席最后聊天时间
                ChatUser sender = chatUserService.getById(chatMessage.getSender());
                sender.setLastMessageTime(chatMessage.getCreateTime());
                chatUserService.updateById(sender);
            }
        }
        return true;
    }

}
