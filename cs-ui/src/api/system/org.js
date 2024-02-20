import request from '@/utils/request'

export default {
  page(params) {
    return request({
      url: `system/org/query`,
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
      // url: `system/org/query/one/${id}`,
      url: `system/org/view`,
      method: 'get',
      params: { id }
    })
  },
  edit(data, params) {
    return request({
      url: 'system/org/edit',
      method: 'put',
      data,
      params
    })
  },
  delete(id) {
    return request({
      // url: `system/org/del/${id}`,
      url: `system/org/del`,
      method: 'delete',
      params: { id }
    })
  }
}
