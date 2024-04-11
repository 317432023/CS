import request from '@/utils/request'

export default {
    /**
     * 查询聊天对手列表
     * @param clientId
     */
    getChatUsers(clientId, rcptId) {
        return request({
            url: `/api/mbr/chat_users`,
            method: 'get',
            params: { rcptId },
            headers: {'clientId': clientId}
        })
    },
    getRecentMessages(clientId, params) {
        return request({
            url: `/api/mbr/recent_messages`,
            method: 'get',
            params, // {customerChatUserId:0,headMsgId:0,limit:15}
            headers: {'clientId': clientId}
        })
    }
}
