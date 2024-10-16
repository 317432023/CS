package com.laolang.cs.chatmessage.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cmpt.sys.comm.Constants;
import com.cmpt.sys.model.entity.SysUser;
import com.cmpt.sys.service.SysUserService;
import com.comm.pojo.R;
import com.frm.redis.ModeDict;
import com.frm.redis.RedisTool;
import com.frm.springmvc.SpringContextHolder;
import com.laolang.cs.chatmessage.ChatMessage;
import com.laolang.cs.chatmessage.ChatMessageRead;
import com.laolang.cs.chatmessage.ChatMessageReadService;
import com.laolang.cs.chatmessage.ChatMessageService;
import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.chatuser.service.ChatUserService;
import com.laolang.cs.server.authen.SubsysTool;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ChatMessageController
 *
 * @since 2023/9/11 0:44
 */
@Tag(name = "会员-客服接口")
@AllArgsConstructor
@RestController
@RequestMapping("/api/mbr")
public class ChatMessageController {
    private ChatMessageService service;
    private ChatMessageReadService readService;
    private ChatUserService chatUserService;
    private SubsysTool subsysTool;
    private Environment environment;
    private SysUserService sysUserService;

    /**
     * 取自身聊天用户信息
     *
     * @return
     */
    @Operation(summary = "取自身聊天用户信息")
    @Parameter(name = "clientId", description = "客戶端ID", in = ParameterIn.HEADER, required = true, example = "1024")
    @Parameter(name = "igResPrefix", description = "返回资源是否去掉前缀，默认false", in = ParameterIn.QUERY, example = "true")
    @GetMapping("self")
    public R<Map<String, Object>> self(@RequestHeader String clientId, @RequestParam(required = false, defaultValue = "false") Boolean igResPrefix) {
        ChatUser senderChatUser = subsysTool.getChatUser(clientId); // 消息发送者
        Map<String, Object> chatUserInfo = new HashMap<>(); // nickName
        chatUserInfo.put("chatUserId", senderChatUser.getId());
        chatUserInfo.put("nickName", senderChatUser.getNickName());
        chatUserInfo.put("tenantId", senderChatUser.getTenantId());
        chatUserInfo.put("userType", senderChatUser.getUserType());
        String imgUrl = senderChatUser.getAvatar() != null ? senderChatUser.getAvatar().trim() : "";
        if (igResPrefix == null || !igResPrefix) {
            if (StringUtils.isNotBlank(imgUrl) && !imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                RedisTool redisTool = SpringContextHolder.getBean("redisTool", RedisTool.class);
                String staticDomain = redisTool.hget(Constants.SYS_CONFIG_KEY, "STATIC_DOMAIN", ModeDict.APP_GROUP, 1);
                imgUrl = staticDomain + (imgUrl.startsWith("/") ? "" : "/") + imgUrl;
            }
        }
        chatUserInfo.put("avatar", imgUrl); // 头像
        return R.success(chatUserInfo);
    }

    /**
     * 取未读消息数目(当前用户)
     *
     * @return
     */
    @Operation(summary = "取未读消息数目(当前用户)")
    @Parameter(name = "clientId", description = "客戶端ID", in = ParameterIn.HEADER, required = true, example = "1024")
    @GetMapping("unread_message_num")
    public R<Long> unreadMessageNum(@RequestHeader String clientId) {
        ChatUser chatUser = subsysTool.getChatUser(clientId);
        QueryWrapper<ChatMessageRead> qw = new QueryWrapper<>();
        qw.eq("`read`", false);
        qw.eq("rcpt_id", chatUser.getId());
        long count = readService.count(qw);
        return R.success(count);
    }

    /**
     * 取聊天用户名单(在线情况)
     *
     */
    @Operation(summary = "取聊天用户名单(在线情况)")
    @Parameter(name = "clientId", description = "客戶端ID", in = ParameterIn.HEADER, required = true, example = "1024")
    @Parameter(name = "rcptId", description = "聊天用户ID", in = ParameterIn.QUERY, example = "0")
    @Parameter(name = "igResPrefix", description = "返回资源是否去掉前缀，默认false", in = ParameterIn.QUERY, example = "true")
    @Parameter(name = "lastChatUserId", description = "上一个聊天用户ID，默认0", in = ParameterIn.QUERY, example = "0")
    @Parameter(name = "limit", description = "限制返回记录数，默认50", in = ParameterIn.QUERY, example = "0")
    @GetMapping("chat_users")
    public R<List<Map<String, Object>>> onlineChatUsers(@RequestHeader String clientId,
                                                        @RequestParam(required = false, defaultValue = "0") Integer rcptId,
                                                        @RequestParam(required = false, defaultValue = "false") Boolean igResPrefix,
                                                        @RequestParam(required = false, defaultValue = "0") Integer lastChatUserId,
                                                        @RequestParam(required = false, defaultValue = "50") Integer limit,
                                                        /*当rcptId无效的情况下支持*/@RequestParam(required = false, defaultValue = "") String searchText
    ) {
        ChatUser senderChatUser = subsysTool.getChatUser(clientId); // 消息發送者
        List<ChatUser> rcptList;
        if (rcptId == null || rcptId <= 0) {
            rcptList = chatUserService.queryRcptList(senderChatUser.getId(), lastChatUserId, limit, searchText);
        } else {
            rcptList = new ArrayList<>();
            ChatUser user = chatUserService.queryRcpt(senderChatUser.getId(), rcptId);
            if (user != null) {
                rcptList.add(user);
            }
        }
        Date date = DateUtil.parse("1970", "yyyy");
        rcptList.forEach(e -> {
            if (e.getLastMessageTime() == null) e.setLastMessageTime(date);
        });
        //rcptList.stream().sorted(Comparator.comparing(ChatUser::getLastMessageTime).reversed().thenComparing(ChatUser::getId));

        List<Map<String, Object>> rcptMapList = new ArrayList<>();
        Map<Integer, Boolean> onlineCache = new HashMap<>();
        RedisTool redisTool = SpringContextHolder.getBean("redisTool", RedisTool.class);
        String staticDomain = redisTool.hget(Constants.SYS_CONFIG_KEY, "STATIC_DOMAIN", ModeDict.APP_GROUP, 1);
        for (ChatUser rcpt : rcptList) {
            Map<String, Object> onlineChatUserMap = new HashMap<>(); // nickName
            onlineChatUserMap.put("rcptId", rcpt.getId());
            onlineChatUserMap.put("nickName", rcpt.getNickName());

            String avatar;
            if (rcpt.getUserType() == 0) {
                avatar = rcpt.getAvatar();
            } else {
                SysUser sysUser = sysUserService.getById(rcpt.getRelId());
                avatar = sysUser.getAvatar();
            }

            String imgUrl = avatar != null ? avatar.trim() : "";
            if (igResPrefix == null || !igResPrefix) {
                if (StringUtils.isNotBlank(imgUrl) && !imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                    imgUrl = staticDomain + (imgUrl.startsWith("/") ? "" : "/") + imgUrl;
                }
            }

            onlineChatUserMap.put("avatar", imgUrl); // 头像

            Boolean online;
            if (onlineCache.containsKey(rcpt.getId())) {
                online = onlineCache.get(rcpt.getId());
            } else {
                online = subsysTool.checkOnline(rcpt.getId());
                onlineCache.put(rcpt.getId(), online);
            }
            onlineChatUserMap.put("online", online); // 是否在线
            onlineChatUserMap.put("lastMessageTime", DateUtil.format(rcpt.getLastMessageTime(), "yyyy-MM-dd HH:mm:ss")); // 最后一次发送或接收消息的时间

            // 未读消息数目
            QueryWrapper<ChatMessageRead> qw = new QueryWrapper<>();
            qw.eq("`read`", false);
            qw.eq("rcpt_id", senderChatUser.getId());
            qw.exists("select 1 from tb_chat_message where id=msg_id and sender=" + rcpt.getId());
            long count = readService.count(qw);
            onlineChatUserMap.put("unreadMessageNum", count);

            rcptMapList.add(onlineChatUserMap);
        }

        return R.success(rcptMapList);
    }

    /**
     * 取最近聊天历史
     *
     * @param clientId           socket连接令牌
     * @param customerChatUserId 聊天用戶ID 客服可以使用该参数；会员忽略此参数
     * @param headMsgId          已取得的最早消息ID
     * @param limit              条数，默认15条
     * @return
     */
    @Operation(summary = "取最近聊天历史")
    @Parameter(name = "clientId", description = "客戶端ID", in = ParameterIn.HEADER, required = true, example = "1024")
    @Parameter(name = "customerChatUserId", description = "聊天用戶ID(客服可以使用该参数；会员忽略此参数)", in = ParameterIn.QUERY, example = "0")
    @Parameter(name = "headMsgId", description = "已取得的最早消息ID", in = ParameterIn.QUERY, example = "0")
    @Parameter(name = "limit", description = "最多返回記錄數", in = ParameterIn.QUERY, example = "15")
    @Parameter(name = "igResPrefix", description = "返回资源是否去掉前缀，默认false", in = ParameterIn.QUERY, example = "true")
    @GetMapping("recent_messages")
    public R<List<ChatMessage>> recentMessages(@RequestHeader String clientId, @RequestParam(required = false, defaultValue = "false") Boolean igResPrefix,
                                               @RequestParam(required = false, defaultValue = "0") Integer customerChatUserId,
                                               @RequestParam(required = false, defaultValue = "0") Long headMsgId,
                                               @RequestParam(required = false, defaultValue = "15") Integer limit) {
        ChatUser chatUser = subsysTool.getChatUser(clientId);
        int userType = chatUser.getUserType();
        List<ChatMessage> chatMessageList;

        boolean h2Flag = "h2".equalsIgnoreCase(environment.getProperty("spring.sql.init.platform", "mysql")) ||
                "h2".equalsIgnoreCase(environment.getProperty("spring.datasource.platform", "mysql"));

        // 是否已读SQL语句 根据数据不同语句不通 由于 h2 数据库不支持 ifnull 函数改用case...end
        String readSQL = h2Flag ?
                "(case `read` when is null then false else `read` end)" :
                "ifnull(`read`,false)";
        String[] selectColumns = new String[]{
                "id",
                "sender",
                "receiver",
                "room_id",
                "message",
                "create_time",
                "(select nick_name from tb_chat_user cu where cu.id=sender) senderNickName",
                "(select nick_name from tb_chat_user cu where cu.id=receiver) receiverNickName",
                "(case sender when " + chatUser.getId() + " then 1 else (select " + readSQL + " from tb_chat_message_read cmr where cmr.msg_id=tb_chat_message.id and cmr.rcpt_id=" + chatUser.getId() + ") end) as `read`"
        };
        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        qw.select(selectColumns);
        if (userType == 0) { // 客人
            qw.eq("room_id", chatUser.getId()); // 群ID = 客人 ID
        } else { // 客服
            if (customerChatUserId != null && customerChatUserId > 0) {
                ChatUser customerChatUser = chatUserService.getById(customerChatUserId);
                Assert.isTrue(customerChatUser.getUserType() == 0, "400-仅能查询客人用户的聊天历史");
                Assert.isTrue(customerChatUser.getTenantId().equals(chatUser.getTenantId()), "403-无权限查询该用户历史");
                qw.eq("room_id", customerChatUserId); // 群ID = 客人 ID
            } else {
                qw.exists("select 1 from tb_chat_user cu where cu.id=room_id and cu.tenant_id='" + chatUser.getTenantId() + "'");
            }
        }

        if (headMsgId != null && headMsgId > 0) {
            qw.lt("id", headMsgId);
            qw.orderByDesc("id"); // 按 ID 倒序排序 优先返回 ID 较大 即最近的记录
        } else {
            qw.lt("create_time", new Date());
            qw.orderByDesc("create_time"); // 按 create_time 倒序排序 优先返回 最近的记录
        }

        qw.last("limit " + (limit > 50 ? 50 : (limit <= 0 ? 15 : limit))); // 分页
        chatMessageList = service.list(qw);
        Collections.reverse(chatMessageList); // 反转顺序

        RedisTool redisTool = SpringContextHolder.getBean("redisTool", RedisTool.class);
        String staticDomain = redisTool.hget(Constants.SYS_CONFIG_KEY, "STATIC_DOMAIN", ModeDict.APP_GROUP, 1);
        if (igResPrefix == null || !igResPrefix) {
            // 图片如有必要加资源域名前缀
            for (ChatMessage chatMessage : chatMessageList) {
                if (chatMessage.getMessage() != null && "image".equals(chatMessage.getMessage().get("type"))) {
                    String imgUrl = (String) chatMessage.getMessage().get("value");
                    if (StringUtils.isNotBlank(imgUrl) && !imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                        imgUrl = staticDomain + (imgUrl.startsWith("/") ? "" : "/") + imgUrl;
                        chatMessage.getMessage().put("value", imgUrl);
                    }
                }
            }
        }

        return R.success(chatMessageList);
    }

}
