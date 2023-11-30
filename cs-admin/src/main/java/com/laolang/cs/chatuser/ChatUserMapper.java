package com.laolang.cs.chatuser;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ChatUserMapper extends BaseMapper<ChatUser> {
}
