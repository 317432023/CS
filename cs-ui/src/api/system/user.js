import request from '@/utils/request'

export default {
  /* page (params) {
    return request({
      url: `system/user/query/list/${params.current}/${params.size}`,
      method: 'post',
      data: {
        asc: params.asc,
        text: params.text,
        sort: params.sort
      }
    })
  }, */
  page (params, data) {
    return request({
      url: 'system/user/query/list',
      method: 'post',
      params, // {current:1,size:6}
      data
    })
  },
  add (data) {
    return request({
      url: 'system/user/add',
      method: 'post',
      data
    })
  },
  delete (id) {
    return request({
      url: `system/user/del/${id}`,
      method: 'delete'
    })
  },
  mod (data) {
    return request({
      url: 'system/user/mod',
      method: 'put',
      data
    })
  },
  getById (id) {
    return request({
      url: `system/user/query/one/${id}`,
      method: 'get'
    })
  }/*,
  updatePwd (data) {
    return request({
      url: 'system/user/updatePwd',
      method: 'put',
      data
    })
  },
  updateInfo (data) {
    return request({
      url: 'system/user/updateInfo',
      method: 'put',
      data
    })
  } */
}
