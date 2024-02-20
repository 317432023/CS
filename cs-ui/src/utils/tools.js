/*
 * @description 工具方法
 * @author ga 收集自互联网
 */

const tools = {
  /**
   * 获取url参数方法
   * @param name 参数名 必填
   * @param url 地址 可选，若未填写则使用当前url地址
   * @returns {*}
   */
  getQueryString: (name, url) => {
    let reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)', 'i')
    const search = url ? url.substring(url.indexOf('?') + 1) : window.location.search.substring(1) // 获取url中"?"符后的字符串
    let r = search.match(reg) // 正则匹配
    let context = ''
    if (r != null) context = decodeURIComponent(r[2])
    reg = null
    r = null
    return context == null || context === 'undefined' ? '' : context
  },

  generateRandomString: length => [...Array(length)].map(() => Math.random().toString(36)[2]).join(''),

  uuid: () => this.generateRandomString(32)

}

export function getQueryString(name, url) {
  return tools.getQueryString(name, url)
}

export function generateRandomString(length) {
  return tools.generateRandomString(length)
}

export default tools
