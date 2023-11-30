package com.laolang.cs.chatmessage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laolang.cs.chatuser.ChatUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ChatMessageService
 *
 * @date 2023/9/11 0:42
 */
@Service@Transactional(rollbackFor = Exception.class)
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IService<ChatMessage> {

}
