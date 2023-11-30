import request from '@/utils/request'

export default {

  /**
   * 个人信息
   */
  profileInfo() {
    return request({url:'profile/profileInfo',method:'get'})
  },

  /**
   * 修改密码
   * @param params
   */
  modifyPassword (params) {
    return request({
      url: 'profile/modifyPassword',
      method: 'put',
      params
    })
  },

  /**
   * 设置用户头像
   * @param data
   */
  setAvatar(data) {
    return request({
      url: 'profile/setAvatar',
      method: 'post',
      data: data
    })
  }
}
