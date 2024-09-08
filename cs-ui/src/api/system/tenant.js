import request from '@/utils/request'

export default {
  page(params) {
    return request({
      url: `system/tenant/query`,
      method: 'get',
      params: {
        pageNum: params.current,
        pageSize: params.size,
        orderByColumn: params.sort,
        isAsc: 'desc'
      }
    })
  },
  getById(id) {
    return request({
      // url: `system/tenant/query/one/${id}`,
      url: `system/tenant/view`,
      method: 'get',
      params: { id }
    })
  },
  edit(data, params) {
    return request({
      url: 'system/tenant/edit',
      method: 'post',
      data,
      params
    })
  },
  delete(id) {
    return request({
      // url: `system/tenant/del/${id}`,
      url: `system/tenant/del`,
      method: 'delete',
      params: { id }
    })
  }
}
