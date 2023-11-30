//package com.laolang.cs.server.authen.websocket;
//
//import com.tk.dep.ip.HutoolIpTool;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
//
//import javax.servlet.http.HttpServletRequest;
//import java.security.Principal;
//import java.util.Map;
//
///**
// * 握手之后处理器
// **/
//@Slf4j
//public class CsHandshakeHandler extends DefaultHandshakeHandler {
//
//    /**
//     * 重写定义用户信息方法
//     *
//     * @param request    握手请求对象
//     * @param wsHandler  WebSocket管理器，用于管理信息
//     * @param attributes 用于传递WebSocket会话的握手属性
//     * @return StompPrincipal 自定义用户信息
//     */
//    @Override
//    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        log.info("Websocket 的 http 连接握手");
//        //获取客户端主机名称
//        String hostName = request.getRemoteAddress().getHostName();
//        //获取客户端主机IP地址
//        String hostAddress = HutoolIpTool.getClientIpAddress((HttpServletRequest) request);
//        log.info("取得握手用户主机名：{}，IP地址：{}", hostName, hostAddress);
//        //设置认证用户
//        return (Principal) attributes.get("user");
//    }
//
//}
