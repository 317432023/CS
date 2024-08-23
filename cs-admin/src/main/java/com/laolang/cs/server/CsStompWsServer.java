package com.laolang.cs.server;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.cmpt.org.entity.SysOrg;
import com.cmpt.org.service.SysOrgService;
import com.cmpt.sys.model.entity.SysUser;
import com.cmpt.sys.service.SysUserService;
import com.cmpt.ws.WebSocketUser;
import com.comm.pojo.SystemException;
import com.frm.springmvc.SpringContextHolder;
import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.chatuser.service.ChatUserService;
import com.laolang.cs.server.authen.SubsysTool;
import com.laolang.cs.server.protocol.Msg;
import com.laolang.cs.server.protocol.MsgType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;

import java.security.Principal;

/**
 * CsStompWsServer
 *
 * @date 2023/9/15 23:57
 */
@Slf4j
@Controller
public class CsStompWsServer {

    private final SimpMessagingTemplate messagingTemplate;
    private final SysUserService sysUserService;
    private final SysOrgService sysOrgService;
    private final ChatUserService chatUserService;
    private final SubsysTool subsysTool;

    public CsStompWsServer(SimpMessagingTemplate messagingTemplate, SubsysTool subsysTool, SysUserService sysUserService, SysOrgService sysOrgService, ChatUserService chatUserService) {
        this.messagingTemplate = messagingTemplate;
        this.subsysTool = subsysTool;
        this.sysUserService = sysUserService;
        this.sysOrgService = sysOrgService;
        this.chatUserService = chatUserService;
    }

    /**
     * 接收并转发聊天信息
     * @param msg<p>
     *     msgId:'',   // 消息ID
     *     msgType:'', // 消息类型
     *     roomId: 0,  // 聊天室ID
     *     msgBody: '' // 消息体
     * @param roomId 群ID=客人的聊天用户ID
     * @param principal
     */
    @MessageMapping("/cs/massRequest/{roomId}")
    public void sendMass(Msg msg, @DestinationVariable Integer roomId, Principal principal) {
        log.info("[接收消息]>>>> msg: {}", JSON.toJSONString(msg) );

        final WebSocketUser webSocketUser = (WebSocketUser)principal;
        if(webSocketUser == null) {
            log.error("非法连接");
            throw new SystemException(500, "非法连接");
        }

        MsgType msgType = msg.getMsgType();
        if(msgType == null) {
            throw new SystemException(500, "消息类型缺失");
        }

        final String clientId = webSocketUser.getName();
        try {
            // 一、检查有无权限请求 roomId
            // 当前用户
            ChatUser chatUser = subsysTool.getChatUser(clientId);

            if (chatUser.getUserType() == 0) {
                // 客人 Id
                Assert.isTrue(roomId.equals(chatUser.getId()), "400-参数不合法 roomId");
            } else {
                // 客人
                ChatUser customerUser = chatUserService.getById(roomId); // 客人ID作为群ID
                Assert.isTrue(customerUser != null, "500-找不到群组");
                Assert.isTrue(customerUser.getUserType() == 0, "400-参数不合法 roomId");

                // 客服 所属站点 必须与客人所属站点一致
                SysUser sysUser = sysUserService.getById(chatUser.getRelId());
                Assert.isTrue(sysUser != null, "500-找不到用户");
                SysOrg sysOrg = sysOrgService.getById(sysUser.getOrgId());
                Assert.isTrue(sysOrg != null, "500-找不到机构");
                Assert.isTrue(sysOrg.getOrgKey().equals(customerUser.getTenantId()), "400-参数不合法");
            }

            // 二、消息处理
            if(msgType.stompClazz == null) {
                return;
            }
            SpringContextHolder.getBean(msgType.stompClazz).accept(
                msg.setMessagingTemplate(messagingTemplate)
                    .setSender(chatUser.getId()) // 消息发送者 ID
                    .setClientId(clientId) // 消息发送者客户端会话
                    .setRoomId(roomId) // 当前群ID
                    // 以下展示需要
                    .setSenderNickName(chatUser.getNickName()) // 消息发送者昵称
            );
        } catch (Exception e) {
            log.error(ExceptionUtil.stacktraceToString(e,1000));
            // 提示 当前用户
            messagingTemplate.convertAndSendToUser(clientId, "/alone/cs/getResponse", Msg.tip("系统接收消息失败").setMsgId(msg.getMsgId()));
        }

    }

}
