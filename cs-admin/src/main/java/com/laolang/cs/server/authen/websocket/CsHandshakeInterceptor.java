//package com.laolang.cs.server.authen.websocket;
//
//import cn.hutool.extra.spring.SpringUtil;
//import com.cmpt.ws.WebSocketUser;
//import com.cmpt.ws.props.WebSocketProperties;
//import com.laolang.cs.chatuser.ChatUser;
//import com.laolang.cs.server.authen.SubsysTool;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Map;
//
///**
// * 握手之前拦截器
// * eg. 取得握手之前 URL 路径 http|ws[s]://localhost:8080/api/chat_user/ws?userType=0&token=xxx&tenantId=FT1@6
// * 经 STOMP 转化为 ws://localhost:8080/api/chat_user/ws/579/rogzlf2d/websocket?userType=0&token=xxx&tenantId=FT1@6
// * !! 注意： Springboot 须开启异步请求支持
// */
//@Slf4j
//public class CsHandshakeInterceptor implements HandshakeInterceptor {
//    private WebSocketProperties webSocketProperties;
//
//    public CsHandshakeInterceptor(WebSocketProperties webSocketProperties) {
//        this.webSocketProperties = webSocketProperties;
//    }
//
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        log.info("Websocket 的 http 连接握手之前");
//        HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
//        WebSocketUser user = null;
//        String token = req.getParameter(webSocketProperties.getTokenName());
//        String tenantId = req.getParameter("tenantId");
//        String userType = req.getParameter("userType");
//
//        // 鉴权
//        SubsysTool getInfoTool = SpringUtil.getBean(SubsysTool.class);
//        try {
//            ChatUser chatUser = getInfoTool.authenticate(token, tenantId, StringUtils.isNotBlank(userType)?Integer.parseInt(userType):null);
//            user = new WebSocketUser(chatUser.getClientId(), chatUser.getId().toString());
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            //throw e;
//        }
//
//        // 若认证失败返回false拒绝握手
//        //if (user == null) {
//        //    return false;
//        //}
//
//        // 保存认证用户
//        attributes.put("user", user);
//        return true;
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
//
//    }
//}
