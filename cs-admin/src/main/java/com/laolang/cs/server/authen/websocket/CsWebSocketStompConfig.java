package com.laolang.cs.server.authen.websocket;

import com.cmpt.ws.props.WebSocketProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@AllArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
@Order(HIGHEST_PRECEDENCE)
public class CsWebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    private WebSocketProperties webSocketProperties;
    private CsSocketChannelInterceptor socketChannelInterceptor;

    /**
     * 配置客户端连接地址
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /*
         * 1. 将 /api/chat_user/ws 路径注册为STOMP的端点，
         *    用户连接了这个端点后就可以进行websocket通讯，支持socketJs
         * 2. setAllowedOriginPatterns("*")表示可以跨域
         * 3. withSockJS()表示支持socktJS访问
         * 4. addInterceptors 添加自定义拦截器
         * 5. setHandshakeHandler 添加拦截处理，这里MyPrincipalHandshakeHandler 封装的认证用户信息
         */
        registry.addEndpoint("/api/chat_user/ws") // 1
            //.setAllowedOrigins("*")   // 2 - 跨域支持：spring5.2及以下版本使用!!!
            .setAllowedOriginPatterns("*")// 2 - 跨域支持：spring5.3及以上版本使用!!!
            //.addInterceptors(new CsHandshakeInterceptor(webSocketProperties)) // 4
            //.setHandshakeHandler(new CsHandshakeHandler()) // 5
            .withSockJS() // 3 注册一个STOMP的endpoint,并指定使用SockJS协议
            .setSessionCookieNeeded(false);
    }

    /**
     * 配置客户端发送和订阅地址
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
        te.setPoolSize(webSocketProperties.getHeartBeatThreadPoolSize());
        te.setThreadNamePrefix("stompWS-CS-heartbeat-thread-");
        te.initialize();
        registry.enableSimpleBroker("/topic", "/queue", "/mass", "/alone")// 配置一个/topic广播消息代理和“/queue”一对一消息代理，群发（mass），单聊（alone）， 用于客户端订阅地址的前缀信息，也就是客户端接收服务端消息的地址的前缀信息
            .setHeartbeatValue(
                new long[]{
                /*规定服务端发送给客户端的心跳不得大于该间隔*/webSocketProperties.getWriteHeartBeat(),
                /*规定客户端发送给服务端的心跳不得大于该间隔*/webSocketProperties.getReadHeartBeat()
                }
            )
            .setTaskScheduler(te)
        ;
        log.info("启动 CS 通讯心跳线程池大小：{},写间隔：{}s,读间隔：{}s", webSocketProperties.getHeartBeatThreadPoolSize(), webSocketProperties.getWriteHeartBeat(), webSocketProperties.getReadHeartBeat());

        // 指定用户发送（一对一）的前缀 不设置的话默认为/user/
        registry.setUserDestinationPrefix("/user/");
        registry.setPreservePublishOrder(true); // 客戶端接收到的消息有序化

        // 设置客户端给服务端发消息的地址的前缀，不设置的话，默认没有前缀
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 配置发送与接收的消息参数，可以指定消息字节大小，缓存大小，发送超时时间
     *
     * @param registration
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        /*
         * 1. setMessageSizeLimit 设置消息缓存的字节数大小 字节
         * 2. setSendBufferSizeLimit 设置websocket会话时，缓存的大小 字节
         * 3. setSendTimeLimit 设置消息发送会话超时时间，毫秒
         */
        registration.setMessageSizeLimit(10240)
            .setSendBufferSizeLimit(10240)
            .setSendTimeLimit(10000);
    }

    /**
     * 配置客户端入站通道拦截器
     * 设置输入消息通道的线程数，默认线程为1，可以自己自定义线程数，最大线程数，线程存活时间
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        /*
         * 配置消息线程池
         * 1. corePoolSize 配置核心线程池，当线程数小于此配置时，不管线程中有无空闲的线程，都会产生新线程处理任务
         * 2. maxPoolSize 配置线程池最大数，当线程池数等于此配置时，不会产生新线程
         * 3. keepAliveSeconds 线程池维护线程所允许的空闲时间，单位秒
         */
        registration.taskExecutor().corePoolSize(10)
            .maxPoolSize(20)
            .keepAliveSeconds(60);

        registration.interceptors(socketChannelInterceptor);
    }

    /**
     * 设置输出消息通道的线程数，默认线程为1，可以自己自定义线程数，最大线程数，线程存活时间
     *
     * @param registration
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(10)
            .maxPoolSize(20)
            .keepAliveSeconds(60);
    }

}
