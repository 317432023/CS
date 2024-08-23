package com.laolang.cs.chatuser.controller;

import com.laolang.cs.chatuser.service.ChatUserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ChatUserController
 *
 * @since 2023/9/11 0:44
 */
@AllArgsConstructor
@RestController
@RequestMapping("chat_user")
public class ChatUserController {
    private ChatUserService service;


}
