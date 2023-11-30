package com.laolang.cs.chatuser;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmpt.org.entity.SysOrg;
import com.cmpt.org.mapper.SysOrgMapper;
import com.cmpt.sys.dao.mapper.SysUserMapper;
import com.cmpt.sys.model.entity.SysUser;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * ChatUserService
 *
 * @date 2023/9/11 0:42
 */
@AllArgsConstructor
@Service@Transactional(rollbackFor = Exception.class)
public class ChatUserService extends ServiceImpl<ChatUserMapper, ChatUser> implements IService<ChatUser> {
    private SysUserMapper sysUserMapper;
    private SysOrgMapper sysOrgMapper;

    /**
     * 取得消息接收者列表
     * @param senderId 聊天用戶發送者ID
     * @return
     */
    public List<ChatUser> queryRcptList(Integer senderId) {
        ChatUser senderChatUser = getById(senderId); // 消息發送者
        QueryWrapper<ChatUser> qw = new QueryWrapper<ChatUser>()
            .select("id","create_time","tenant_id","nick_name","user_type","avatar","rel_id",
                "(select create_time from tb_chat_message where receiver=tb_chat_user.id order by create_time desc limit 1) last_message_time"
            )
            .eq("tenant_id", senderChatUser.getTenantId())
            .eq("user_type", senderChatUser.getUserType() == 0?1:0); // 若消息發送者是客人則取客服列表否則取客人列表

        return super.list(qw);
    }

    /**
     * 同步聊天用户信息
     * @param tenantId 租户，当 userType = 1时可不填
     * @param nickName 昵称
     * @param userType 0 - 客人 ； 1 - 系统用户
     * @param relId 关联ID
     * @param avatar 头像
     */
    public ChatUser syncChatUser(String tenantId, String nickName, Integer userType, Long relId, String avatar) {
        Assert.isTrue(userType != null && (userType == 0 || userType == 1), "400 - userType 参数不合规");
        ChatUser chatUser;
        if(userType == 1) {
            Assert.isTrue(relId != null && relId > 0L, "400 - relId 参数不合规");
            SysUser sysUser = sysUserMapper.selectById(relId);
            Assert.isTrue(sysUser != null, "404 - 找不到系统用户" + relId);
            Assert.isTrue(!sysUser.getDisabled(), "403 - 系统用户已被禁用" + relId);

            // 将 tenantId 与 系统用户所属机构的 org_key 进行比对验证
            SysOrg sysOrg = sysOrgMapper.selectById(sysUser.getOrgId());
            Assert.isTrue(sysOrg != null, ()->String.format("500 - 找不到机构 %d", sysUser.getOrgId()));

            chatUser = getOne(new LambdaQueryWrapper<ChatUser>().eq(ChatUser::getUserType, userType).eq(ChatUser::getRelId, relId));
            if(chatUser == null) {
                chatUser = new ChatUser();
                chatUser.setUserType(userType);
                chatUser.setRelId(relId);
                // 其他属性设置
                chatUser.setNickName("客服#" + (StringUtils.isNotBlank(nickName)? nickName : relId)); // 此处昵称以 系统用户 昵称为准
                avatar = StringUtils.isNotBlank(avatar)?avatar.trim():(sysUser.getAvatar()==null?"":sysUser.getAvatar().trim());
                chatUser.setAvatar(avatar);
                chatUser.setTenantId(sysOrg.getOrgKey());
            }
        } else {
            Assert.isTrue(StringUtils.isNotBlank(tenantId), "400 - tenantId 参数不合规");
            Assert.isTrue(StringUtils.isNotBlank(nickName) && !nickName.trim().startsWith("客服"), "400 - nickName 参数不合规");

            chatUser = getOne(new LambdaQueryWrapper<ChatUser>().eq(ChatUser::getUserType, userType).eq(ChatUser::getTenantId, tenantId).eq(ChatUser::getNickName, nickName));
            if(chatUser == null) {
                chatUser = new ChatUser();
                chatUser.setTenantId(tenantId);
                chatUser.setUserType(userType);
                chatUser.setNickName(nickName);
                avatar = StringUtils.isNotBlank(avatar)?avatar.trim():"";
                chatUser.setAvatar(avatar);
                // 其他属性设置
                chatUser.setRelId(relId);
            }
        }

        // 若聊天用户不存在则自动创建它
        if(chatUser.getId() == null)
        {
            this.save(chatUser);
        }
        else
        {
            if(!avatar.equals(chatUser.getAvatar()))
            {
                // 更新头像
                this.update().set("avatar", avatar).eq("id", chatUser.getId());
            }
        }

        return chatUser;
    }

}
