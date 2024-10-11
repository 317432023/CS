import request from '@/utils/request'

export default {
  /**
   * 查询聊天对手列表
   * @param clientId
   * @param rcptId
   * @param lastChatUserId
   * @param limit
   * @param searchText 模糊匹配用户昵称（仅rcptId无效的情况下支持）
   */
  getChatUsers(clientId, rcptId, lastChatUserId = 0, limit = 50, searchText = '') {
    return request({
      url: `/api/mbr/chat_users`,
      method: 'get',
      params: { rcptId, lastChatUserId, limit, searchText },
      headers: { 'clientId': clientId }
    })
  },

  getRecentMessages(clientId, params) {
    return request({
      url: `/api/mbr/recent_messages`,
      method: 'get',
      params, // {customerChatUserId:0,headMsgId:0,limit:15}
      headers: { 'clientId': clientId }
    })
  }
}
