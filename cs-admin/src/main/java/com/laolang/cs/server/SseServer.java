package com.laolang.cs.server;

import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.server.authen.SubsysTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SseServer
 *
 * @since 2023/9/22 10:54
 */
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping(path = "api/sse")
public class SseServer {

    private final static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();

    private SubsysTool subsysTool;

    /**
     * 订阅服务端发送事件(SSE)
     * <p>
     * </p>
     * @param clientId
     * @return
     */
    @GetMapping(path = "subscribe", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter subscribe(@RequestParam String clientId) {
        // 验证 clientId 的有效性
        ChatUser chatUser = subsysTool.getChatUser(clientId);

        // 关闭旧的发送器
        SseEmitter oldSseEmitter = sseCache.remove(clientId);
        if(oldSseEmitter != null) {
            oldSseEmitter.complete();
        }

        // 设置超时时间，0表示不过期。默认30秒，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        SseEmitter sseEmitter = new SseEmitter(1_60_000L); // 1 分钟内服务端未发送任何消息则超时

        try {
            // 设置前端的连接断开后的 重试连接时间间隔为5s
            sseEmitter.send(SseEmitter.event().reconnectTime(5000).data("连接成功"));
        } catch (IOException e) {
            log.error(String.format("发送sse消息失败，clientId => %s", clientId), e);
        }

        //SseEmitter.SseEventBuilder data = SseEmitter.event().name("finish").id("6666").data("哈哈");
        //sseEmitter.send(data);

        sseCache.put(clientId, sseEmitter);
        log.info("创建新的sse连接，clientId => {}", clientId);

        sseEmitter.onTimeout(() -> {
            sseCache.remove(clientId);
            log.info("发送器超时移除，clientId => {}", clientId);
        });

        sseEmitter.onCompletion(() -> { // 当 超时 或 sseEmitter.complete(); 被调用后触发
            log.info("发送器结束连接，clientId => {}", clientId);
        });

        return sseEmitter;
    }

}
