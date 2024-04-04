import request from '@/utils/request'

export default {
  /**
   * 取上传地址
   */
  getUploadUrl() {
    return request({
      url: `/api/open/getUploadUrl`,
      method: 'get'
    })
  }
}
