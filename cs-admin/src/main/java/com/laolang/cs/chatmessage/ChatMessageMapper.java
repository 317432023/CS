package com.laolang.cs.chatmessage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
