package com.laolang.cs.server.authen;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cmpt.org.entity.SysOrg;
import com.cmpt.org.service.SysOrgService;
import com.cmpt.sys.security.domain.AdminUserDetails;
import com.cmpt.ws.props.WebSocketProperties;
import com.comm.pojo.R;
import com.comm.pojo.SystemException;
import com.frm.redis.ModeDict;
import com.frm.redis.RedisTool;
import com.laolang.cs.chatuser.ChatUser;
import com.laolang.cs.chatuser.service.ChatUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

import static com.comm.iface.Constants.LOGIN_TOKEN_PREFIX;

/**
 * 取子系统用户信息
 * GetInfoTool
 *
 * @date 2023/9/11 23:21
 */
@Slf4j
@AllArgsConstructor
@Component
//@PropertySources({
//    @PropertySource("classpath:sys.properties")
//})
public final class SubsysTool {
    /**
     * 接入子系统头令牌Key
     */
    public static final String SubsysTokenHeaderKey = "X-Token";

    private ChatUserService chatUserService;
    private RedisTool redisTool;
    //private Environment environment;
    private WebSocketProperties webSocketProperties;

    /**
     * 鉴权/认证
     * @param token 令牌
     * @param tenantId 租户ID，当 userType = 1时，忽略自动 tenantId
     * @param userType 0-客人 1-客服
     * @return
     */
    public ChatUser authenticate(String token, String tenantId, Integer userType) {
        Assert.isTrue(StringUtils.isNotBlank(token), "400 - token 参数不合规");
        if(userType == null) {
            userType = 0;
        } else Assert.isTrue(userType == 0 || userType == 1, "400 - userTye 参数不合规");

        String nickName,avatar;
        Long relId;
        if(userType == 0) {
            log.info("客人连接");
            Assert.isTrue(StringUtils.isNotBlank(tenantId), "400 - tenantId 参数不合规");
            // 根据token和tenantId查询对应的应用地址验证令牌的用户信息
            R r = SubsysTool.getInfo(token, tenantId);
            Map<String, Object> dataMap = (Map<String, Object>) r.getData();
            Map<String, Object> userInfo = (Map<String, Object>) dataMap.get("userInfo");

            nickName = (String) userInfo.get("nickname");
            avatar = (String) userInfo.get("avatar");
            relId = Long.parseLong(userInfo.get("id").toString());
        } else {
            log.info("客服人员连接，拉取用户信息 当前令牌token=" + token);
            AdminUserDetails userDetails = null;
            String stringAdminUserDetails = redisTool.getString(LOGIN_TOKEN_PREFIX + token, ModeDict.APP);
            if (stringAdminUserDetails != null) {
                userDetails = JSON.parseObject(stringAdminUserDetails, AdminUserDetails.class);
            }
            if (userDetails != null) {
                nickName = userDetails.getNickname();
                relId = userDetails.getId();
                avatar = "";
            } else throw new SystemException(401, "令牌无效");
        }
        ChatUser chatUser = chatUserService.syncChatUser(tenantId, nickName, userType, relId, avatar);

        // 生成客服clientId
        StringBuilder strBuf = new StringBuilder(100);
        strBuf.append(token).append('#');
        if (userType == 0) {
            strBuf.append(tenantId).append('#');
        }
        strBuf.append(userType);
        String clientId = strBuf.toString();

        // 放进 chatUser 对象中
        chatUser.setClientId(clientId);

        return chatUser;
    }

    /**
     * 根据令牌和租户ID取得用户信息
     * @param token
     * @param tenantId
     * @return 示例：
     * {
     *   "code" : 200,
     *   "message" : "",
     *   "data" : {
     *     "platform" : 0,
     *     "userKey" : "",
     *     "keyExtra" : "",
     *     "memberId" : "100002",
     *     "salt" : "",
     *     "userInfo" : {
     *       "createBy" : "",
     *       "gender" : 0,
     *       "createTime" : "1677406941000",
     *       "updateBy" : "",
     *       "nickname" : "nwp",
     *       "pid" : 0,
     *       "disabled" : false,
     *       "updateTime" : "1677406941000",
     *       "id" : 100002,
     *       "avatar" : "/upload/faces/01.jpg",
     *       "orgId" : 6,
     *       "username" : "nwp"
     *     }
     *   },
     *   "success" : true
     * }
     */
    public static R getInfo(String token, String tenantId) {
        // 根据 tenantId 提取 远程接口地址，格式如：http://127.0.0.1:9090/api/mbr/getInfo
        SysOrgService sysOrgService = SpringUtil.getBean(SysOrgService.class);
        Assert.isTrue(sysOrgService != null, "500-系统内部异常，请联系管理");
        SysOrg sysOrg = sysOrgService.getOne(new QueryWrapper<SysOrg>().eq("org_key", tenantId));
        Assert.isTrue(sysOrg != null, ()->String.format("404-找不到机构%s，请联系管理", tenantId));
        String getInfoUrl = sysOrg.getDomain();
        if(StringUtils.isBlank(getInfoUrl) && sysOrg.getPid() != null && sysOrg.getPid() > 0) {
            SysOrg pSysOrg = sysOrgService.getById(sysOrg.getPid());
            getInfoUrl = pSysOrg.getDomain();
        }
        Assert.isTrue(StringUtils.isNotBlank(getInfoUrl), ()->String.format("500-机构%s认证地址未配置，请联系管理", tenantId));

        // 请求 远程接口地址 带上请求头 X-Token = ${token}，得到如下 json
        HttpResponse response = HttpUtil.createGet(getInfoUrl + "?t=" + System.currentTimeMillis())
            .header(SubsysTokenHeaderKey, token).setFollowRedirects(true)
            //.setConnectionTimeout(5000).setReadTimeout(5000)
            .timeout(10000)
            .execute()
        ;
        if(response.getStatus() == 200) {
            String body = response.body();
            R r = JSON.parseObject(body, R.class);
            return r;
        }
        throw new SystemException(500, "请求失败");
    }

    /**
     * 检查用户是否在线
     * @param chatUserId
     * @return
     */
    public boolean checkOnline(Integer chatUserId) {
        return redisTool.hasKey(webSocketProperties.getOnlinePrefix() + chatUserId, ModeDict.APP);
    }

    public String getClientId(Integer chatUserId) {
        return redisTool.getString(webSocketProperties.getOnlinePrefix() + chatUserId, ModeDict.APP);
    }

    /**
     * 检查用户是否在线
     * @param clientId
     * @return
     */
    public Integer checkOnline(String clientId) {
        String userId = redisTool.get(webSocketProperties.getConnPrefix()+ clientId, ModeDict.APP); // 当前的用户
        if(StringUtils.isBlank(userId)) {
            throw new SystemException(401, "未登录或会话已失效");
        }
        return Integer.parseInt(userId);
    }

    public void renew(String clientId, String userId) {
        int onlineDuration = webSocketProperties.getOnlineDuration();

        /**
         * 在线用户，数据结构：用户ID=>用户令牌
         */
        if(redisTool.hasKey(webSocketProperties.getOnlinePrefix() + userId, ModeDict.APP)) {
            String oldClientId = redisTool.getString(webSocketProperties.getOnlinePrefix() + userId, ModeDict.APP);
            if(!oldClientId.equals(clientId)) {
                // 移除旧的连接
                redisTool.del(ModeDict.APP, webSocketProperties.getConnPrefix() + oldClientId);
            }
        }
        redisTool.setString(webSocketProperties.getOnlinePrefix() + userId, clientId, ModeDict.APP, onlineDuration*60L);

        /**
         * 数据结构：用户令牌=>用户ID
         */
        redisTool.set(webSocketProperties.getConnPrefix() + clientId, userId, ModeDict.APP);
    }

    /**
     * 根据客户端令牌取聊天用户信息
     * @param clientId
     * @return
     */
    public ChatUser getChatUser(String clientId) {
        Integer chatUserId = this.checkOnline(clientId);

        ChatUser chatUser = null;
        if(redisTool.hasKey(webSocketProperties.getChatUserCachePrefix() + chatUserId, ModeDict.APP)) {
            chatUser = redisTool.get(webSocketProperties.getChatUserCachePrefix() + chatUserId, ModeDict.APP);
        }
        if(chatUser == null) {
            chatUser = chatUserService.getById(chatUserId);
            /**
             * 缓存数据结构：用户ID=>用户信息
             */
            redisTool.set(webSocketProperties.getChatUserCachePrefix() + chatUserId, chatUser, ModeDict.APP);
        }

        if(chatUser == null) {
            throw new SystemException(404, "找不到用户");
        }

        return chatUser;
    }

}
