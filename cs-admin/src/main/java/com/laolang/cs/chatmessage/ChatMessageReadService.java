package com.laolang.cs.chatmessage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laolang.cs.chatmessage.mapper.ChatMessageReadMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ChatMessageReadService
 *
 * @date 2023/9/11 0:42
 */
@Service@Transactional(rollbackFor = Exception.class)
public class ChatMessageReadService extends ServiceImpl<ChatMessageReadMapper, ChatMessageRead> implements IService<ChatMessageRead> {

}
