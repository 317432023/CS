package com.laolang.cs.chatmessage.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.comm.pojo.R;
import com.frm.redis.ModeDict;
import com.frm.redis.RedisTool;
import com.frm.springmvc.SpringContextHolder;
import com.laolang.cs.chatmessage.ChatMessage;
import com.laolang.cs.chatmessage.ChatMessageRead;
import com.laolang.cs.chatmessage.ChatMessageReadService;
import com.laolang.cs.chatmessage.ChatMessageService;
import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.chatuser.ChatUserService;
import com.laolang.cs.server.authen.SubsysTool;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ChatMessageController
 *
 * @date 2023/9/11 0:44
 */
@Api(tags = {"会员-客服接口"})
@AllArgsConstructor
@RestController
@RequestMapping("/api/mbr")
public class ChatMessageController {
    private ChatMessageService service;
    private ChatMessageReadService readService;
    private ChatUserService chatUserService;
    private SubsysTool subsysTool;

    /**
     * 取未读消息数目(当前用户)
     * @return
     */
    @ApiOperation(value="取未读消息数目(当前用户)")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "header", name = "clientId", value = "客戶端ID", dataType = "string"),
    })
    @GetMapping("unread_message_num")
    public R<Integer> unreadMessageNum(@RequestHeader String clientId) {
        ChatUser chatUser = subsysTool.getChatUser(clientId);
        QueryWrapper<ChatMessageRead> qw = new QueryWrapper<>();
        qw.eq("`read`", false);
        qw.eq("rcpt_id", chatUser.getId());
        int count = readService.count(qw);
        return R.success(count);
    }

    /**
     * 取聊天用户名单(在线情况)
     * @return
     */
    @ApiOperation(value="取聊天用户名单(在线情况)")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "header", name = "clientId", value = "客戶端ID", dataType = "string"),
    })
    @GetMapping("chat_users")
    public R<List<Map<String, Object>>> onlineChatUsers(@RequestHeader String clientId) {
        ChatUser senderChatUser = subsysTool.getChatUser(clientId); // 消息發送者
        List<ChatUser> rcptList = chatUserService.queryRcptList(senderChatUser.getId());
        Date date = DateUtil.parse("1970", "yyyy");
        rcptList.forEach(e-> {if(e.getLastMessageTime()==null)e.setLastMessageTime(date);});
        //rcptList.stream().sorted(Comparator.comparing(ChatUser::getLastMessageTime).reversed().thenComparing(ChatUser::getId));

        List<Map<String, Object>> rcptMapList = new ArrayList<>();
        Map<Integer, Boolean> onlineCache = new HashMap<>();
        RedisTool redisTool = SpringContextHolder.getBean("redisTool", RedisTool.class);
        String staticDomain = redisTool.hget("system_config", "STATIC_DOMAIN", ModeDict.APP_GROUP, 1);
        for (ChatUser rcpt : rcptList) {
            Map<String, Object> onlineChatUserMap = new HashMap<>(); // nickName
            onlineChatUserMap.put("rcptId", rcpt.getId());
            onlineChatUserMap.put("nickName", rcpt.getNickName());

            String imgUrl = rcpt.getAvatar() != null?rcpt.getAvatar().trim():"";
            if(StringUtils.isNotBlank(imgUrl) && !imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                imgUrl = staticDomain + (imgUrl.startsWith("/") ? "" : "/") + imgUrl;
            }

            onlineChatUserMap.put("avatar", imgUrl); // 头像

            Boolean online;
            if(onlineCache.containsKey(rcpt.getId()))
            {
                online = onlineCache.get(rcpt.getId());
            }
            else
            {
                online = subsysTool.checkOnline(rcpt.getId());
                onlineCache.put(rcpt.getId(), online);
            }
            onlineChatUserMap.put("online", online); // 是否在线
            onlineChatUserMap.put("lastMessageTime", rcpt.getLastMessageTime()); // 最后一次发送或接收消息的时间
            rcptMapList.add(onlineChatUserMap);
        }

        return R.success(rcptMapList);
    }

    /**
     * 取最近聊天历史
     * @param clientId socket连接令牌
     * @param customerChatUserId 聊天用戶ID
     * @param headMsgId 已取得的最早消息ID
     * @param limit 条数，默认15条
     * @return
     */
    @ApiOperation(value="取最近聊天历史")
    @ApiResponses( value = {
      @ApiResponse(code = 0/200, message = "OK"),
      @ApiResponse(code = 1, message = "FAIL"),
    })
    @ApiImplicitParams({
      @ApiImplicitParam(paramType="header", name = "clientId", value = "客戶端ID", dataType = "string"),
      @ApiImplicitParam(paramType="query", name = "customerChatUserId", value = "聊天用戶ID", dataType = "int"),
      @ApiImplicitParam(paramType="query", name = "headMsgId", value = "已取得的最早消息ID", dataType = "bigint"),
      @ApiImplicitParam(paramType="query", name = "limit", value = "最多返回記錄數", dataType = "int"),
    })
    @GetMapping("recent_messages")
    public R<List<ChatMessage>> recentMessages(@RequestHeader String clientId,
                                               @RequestParam(required = false, defaultValue = "0") Integer customerChatUserId,
                                               @RequestParam(required = false, defaultValue = "0") Long headMsgId,
                                               @RequestParam(required = false, defaultValue = "15") Integer limit) {
        ChatUser chatUser = subsysTool.getChatUser(clientId);
        int userType = chatUser.getUserType();
        List<ChatMessage> chatMessageList;
        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        String[] selectColumns = new String[]{
            "id",
            "sender",
            "receiver",
            "message",
            "create_time",
            "(select nick_name from tb_chat_user cu where cu.id=sender) senderNickName",
            userType==1?"(select nick_name from tb_chat_user cu where cu.id=receiver) receiverNickName":"NULL receiverNickName",
            //"if(sender="+chatUser.getId()+",true,ifnull((select `read` from tb_chat_message_read cmr where cmr.msg_id=tb_chat_message.id and cmr.rcpt_id="+chatUser.getId()+"),false) ) as `read`",
        };
        qw.select(selectColumns);
        if(userType == 0) { // 客人
            qw.eq("receiver", chatUser.getId()); // 群ID = 客人 ID
        } else { // 客服
            if(customerChatUserId != null && customerChatUserId > 0) {
                ChatUser customerChatUser = chatUserService.getById(customerChatUserId);
                Assert.isTrue(customerChatUser.getUserType() == 0, "400-仅能查询客人用户的聊天历史");
                Assert.isTrue(customerChatUser.getTenantId().equals(chatUser.getTenantId()), "403-无权限查询该用户历史");
                qw.eq("receiver", customerChatUserId); // 群ID = 客人 ID
            } else {
                qw.exists("select 1 from tb_chat_user cu where cu.id=receiver and cu.tenant_id='"+chatUser.getTenantId()+"'");
            }
        }

        if(headMsgId != null && headMsgId > 0) {
            qw.lt("id", headMsgId);
            qw.orderByDesc("id"); // 按 ID 倒序排序 优先返回 ID 较大 即最近的记录
        } else {
            qw.lt("create_time", new Date());
            qw.orderByDesc("create_time"); // 按 create_time 倒序排序 优先返回 最近的记录
        }

        qw.last("limit "+(limit > 50?50:(limit<=0?15:limit))); // 分页
        chatMessageList = service.list(qw);
        Collections.reverse(chatMessageList); // 反转顺序

        // 图片如有必要加资源域名前缀
        for (ChatMessage chatMessage : chatMessageList) {
            if(chatMessage.getMessage() != null && "image".equals(chatMessage.getMessage().get("type"))) {
                String imgUrl = (String)chatMessage.getMessage().get("value");
                if(StringUtils.isNotBlank(imgUrl) && !imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                    RedisTool redisTool = SpringContextHolder.getBean("redisTool", RedisTool.class);
                    String staticDomain = redisTool.hget("system_config", "STATIC_DOMAIN", ModeDict.APP_GROUP, 1);
                    imgUrl = staticDomain + (imgUrl.startsWith("/") ? "" : "/") + imgUrl;
                    chatMessage.getMessage().put("value", imgUrl);
                }
            }
        }

        return R.success(chatMessageList);
    }

}
