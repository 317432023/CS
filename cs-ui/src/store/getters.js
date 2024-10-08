const getters = {
  sidebar: state => state.app.sidebar,
  device: state => state.app.device,
  token: state => state.security.token,
  avatar: state => state.security.avatar || 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
  name: state => state.security.nickname || state.security.username,
  roles: state => state.security.roles, roleIds: state => state.security.roleIds, tenantId: state => state.security.tenantId,
  // 多语言支持
  language: state => state.app.language,
  // 标签页支持
  visitedViews: state => state.tagsView.visitedViews,
  cachedViews: state => state.tagsView.cachedViews,
  // 总路由表
  permission_routes: state => state.permission.routes,
  errorLogs: state => state.errorLog.logs,
  size: state => state.app.size,

  // 增加 Stomp+SockJS websocket 支持
  stompClient: state => state.websocket.stompClient,
  connected: state => state.websocket.stompClient != null && state.websocket.stompClient.connected
}
export default getters
