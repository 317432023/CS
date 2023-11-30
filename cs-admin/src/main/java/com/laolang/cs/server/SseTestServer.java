package com.laolang.cs.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sse 测试服务
 *
 * 项目中涉及到部分请求，后端处理时间较长，使用常规Http请求，页面等待时间太长，对用户不友好，故考虑使用长链接进行消息推送，可选方案有WebSocket、SSE，WebSocket可实现双工通信，SSE仅支持服务端向客户端推送消息，根据实际使用场景，SSE即可满足，故选用SSE。
 * 一、SSE是什么？
 *
 * SSE技术是基于单工通信模式，只是单纯的客户端向服务端发送请求，服务端不会主动发送给客户端。服务端采取的策略是抓住这个请求不放，等数据更新的时候才返回给客户端，当客户端接收到消息后，再向服务端发送请求，周而复始。
 *
 *     注意：因为EventSource对象是SSE的客户端，可能会有浏览器对其不支持，但谷歌、火狐、360是可以的，IE不可以。
 *     优点：SSE和WebSocket相比，最大的优势是便利，服务端不需要其他的类库，开发难度较低，SSE和轮询相比它不用处理很多请求，不用每次建立新连接，延迟较低。
 *     缺点：如果客户端有很多，那就要保持很多长连接，这会占用服务器大量内存和连接数
 *     sse 规范：在 html5 的定义中，服务端 sse，一般需要遵循以下要求：
 *     Content-Type: text/event-stream;charset=UTF-8
 *     Cache-Control: no-cache
 *     Connection: keep-alive
 * Nginx 配置：
 * proxy_set_header Host $http_host;  ##proxy_set_header用来重定义发往后端服务器的请求头
 * proxy_set_header X-Real-IP $remote_addr;
 * proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 * proxy_set_header X-Forwarded-Proto $scheme;
 * proxy_buffering off;
 * proxy_http_version  1.1;
 * proxy_read_timeout 600s; ##设置SSE长链接保持时间为 600s
 */
@Slf4j
@Controller
@RequestMapping(path = "api/sse_test")
public class SseTestServer {

    private final static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();

    /**
     * 连接sse服务
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping(path = "subscribe", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter subscribe(String id) throws IOException {
        // 设置超时时间，0表示不过期。默认30秒，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        SseEmitter sseEmitter = new SseEmitter(1_60_000L); // 1_60_000L = 1 分钟
        // 设置前端的重试时间间隔为1s
        // send(): 发送数据，如果传入的是一个非SseEventBuilder对象，那么传递参数会被封装到 data 中
        sseEmitter.send(SseEmitter.event().reconnectTime(1000).data("连接成功"));
        sseCache.put(id, sseEmitter);
        log.info("创建新的sse连接，当前用户：{}", id);

        System.out.println("add " + id);
        // 如果传入的是一个非SseEventBuilder对象，那么传递参数会被封装到 data 中
        sseEmitter.send("你好", MediaType.APPLICATION_JSON);
        SseEmitter.SseEventBuilder data = SseEmitter.event().name("finish").id("6666").data("哈哈");
        sseEmitter.send(data);
        // onTimeout(): 超时回调触发
        sseEmitter.onTimeout(() -> {
            System.out.println(id + "超时");
            sseCache.remove(id);
        });
        // onCompletion(): 结束之后的回调触发
        sseEmitter.onCompletion(() -> System.out.println("结束连接 id=" +id));
        return sseEmitter;
    }

    /**
     * 请求该方法向用户发送消息
     * http://127.0.0.1:8080/sse/push?id=7777&content=%E4%BD%A0%E5%93%88aaaaaa
     * @param id
     * @param content
     * @return
     * @throws IOException
     */
    @ResponseBody
    @GetMapping(path = "push")
    public String push(String id, String content) throws IOException {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            sseEmitter.send(content);
        }
        return "over";
    }

    /**
     * 请求该方法断开连接
     * @param id
     * @return
     */
    @ResponseBody
    @GetMapping(path = "over")
    public String over(String id) {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            // complete(): 表示执行完毕，会断开连接
            sseEmitter.complete();
            sseCache.remove(id);
        }
        return "over";
    }
}
