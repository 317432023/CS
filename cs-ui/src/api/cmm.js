/*
 * 认证用户公用接口调用
 * 该文件的接口规定必须是用户登录认证后才能够调用
 */
import request from '../utils/request'

/**
 * 列出当前用户有权限查看的机构列表
 */
export default {
  orgList() {
    return request({
      url: 'cmm/org_list',
      method: 'get'
    })
  }
}
