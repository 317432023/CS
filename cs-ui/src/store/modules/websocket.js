/*

  原生 websocket.readyState
   0 - CONNECTING   连接正在建立
   1 - OPEN         连接已建立
   2 - CLOSING      正在关闭
   3 - CLOSED       已关闭或不可用

  Stomp常用代码
  // 发送消息
  stompClient.send('/topic/terminal_chart', {}, "ping")
  // 订阅（subscription是一个对象，包含一个id属性，对应这个这个客户端的订阅ID）
  var subscription = stompClient.subscribe("/queue/test", callback);
  // 终止订阅消息
  subscription.unsubscribe();
  // 获取STOMP子协议的客户端对象
  stompClient = Stomp.over(socket);
  // 心跳发送频率
  stompClient.heartbeat.outgoing = 20000;
  // 心跳接收频率
  stompClient.heartbeat.incoming = 20000;
  // 调用.connect方法连接Stomp服务端进行验证
  stompClient.connect({}, (frame)=>{
      // 成功回调
  }, (err)=>{
      // 失败回调（第一次连接失败和连接后断开连接都会调用这个函数）
  })
  // 判断是否连接
  stompClient.connected

  Stomp心跳机制
  stomp默认的心跳为10000ms,
  heartbeat.outgoing:客户端发给服务端的心跳，* 0表示它不能发送心跳 * 否则它是能保证两次心跳的最小毫秒数
  heartbeat.incoming：客户端希望服务端发送的心跳。* 0表示它不想接收心跳 * 否则它表示两次心跳期望的毫秒数
  CONNECT
  heart-beat:<cx>,<cy> 客户端

  CONNECTED:
  heart-beat:<sx>,<sy> 服务端
  对于client发送server的心跳： * 如果<cx>为0(client不能发送心跳)或者<sy>为0(server不想接收心跳),将不起任何作用。心跳频率为MAX(<cx>,<sy>)毫秒数.
  对于server发送client的心跳：心跳频率为MAX(<cy>,<sx>)毫秒数.

 */

import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { getQueryString } from '../../utils/tools'

import { MessageBox, Message } from 'element-ui'

const state = {
    url: '',
    websocket: null,
    stompClient: null,

    wsReconnect: null, // 断线重连定时器
    reConnectCount: 0, // 重连次数计数器: 0
    tokenInvalid: false // 令牌是否无效
}

const mutations = {
	$INIT_CONNECT(state) {
        const websocket = new SockJS(state.url, null, {timeout: 15000})
        state.websocket = websocket
        const stompClient = Stomp.over(websocket); // 覆盖sockjs使用stomp客户端
        stompClient.heartbeat.outgoing = 10000;    // 发送心跳的间隔(默认10000ms) 设置为0则客户端不给服务端发送心跳包
        stompClient.heartbeat.incoming = 10000;    // 接收心跳的间隔(默认10000ms) 设置为0则客户端不从服务端接收心跳包
        stompClient.debug = null                   // 关闭控制台打印
        state.stompClient = stompClient
	},
    $RESET(state) {
        state.tokenInvalid = false
        state.reConnectCount = 0
        clearTimeout(state.wsReconnect)
        state.wsReconnect = null
    },
}

const actions = {
	CONNECT({ commit, state, dispatch }, params) {
		commit('$INIT_CONNECT')

		let headers = {
			'token' : getQueryString('token', state.url),
			'userType' : getQueryString('userType', state.url),
			'tenantId' : getQueryString('tenantId', state.url)
		}

	    let {okCallBack, failCallBack} = params
		return new Promise((resolve, reject) => {
			state.stompClient.connect(headers, (frame)=>{
				// 成功回调
				console.log('连接成功！frame = > \n' + JSON.stringify(frame))
				Message({ message: '连接成功', type: 'success', duration: 1 * 1500 })
				commit('$RESET') // 一旦连上可以重置状态
                if(okCallBack) {
                    console.log('okCallBack 调用')
                    okCallBack()
                }
				resolve(1)
			}, (error)=>{
				// 失败回调（第一次连接失败和连接后断开连接都会调用这个函数）

				if((typeof error) === 'object') {
					let errDetails = JSON.stringify(error)
                    if(failCallBack) {
                        failCallBack()
                    }
					if(errDetails.includes("令牌无效") || errDetails.includes("连接未认证")) {
						console.error("连接失败，令牌无效：" + state.url)
						state.tokenInvalid = true
					} else {
						console.error('连接发生异常: error => \n' + error)
						Message({ message: '连接发生异常', type: 'error', duration: 1 * 1500 })
					}
				} else {
					console.error('发生异常: error => \n' + error)
				}
				if(state.tokenInvalid) {
					MessageBox.confirm('会话无效或已过期，请重新登录', '确认退出', {
					  confirmButtonText: '重新登录',
					  cancelButtonText: '取消',
					  type: 'warning'
					}).then(() => {
                      dispatch('security/resetToken', {}, { root: true }).then(() => {
                        location.reload()
                      })
					})
					reject(4)
                    return
				}
				console.log("连接失败：" + state.url)
				Message({ message: '连接失败', type: 'error', duration: 1 * 1500 })

				if (state.reConnectCount >= 5) {
					console.log(state.reConnectCount)
					let tipMsg = "温馨提示：您的连接已断开，请退出后重新进入。"
					MessageBox.confirm(tipMsg, '确认退出', {
					  confirmButtonText: '重新登录',
					  cancelButtonText: '取消',
					  type: 'warning'
					}).then(() => {
                      dispatch('security/resetToken', {}, { root: true }).then(() => {
                        location.reload()
                      })
					})
				} else {
					if(state.wsReconnect) {
						clearTimeout(state.wsReconnect)
					}
					state.wsReconnect = setTimeout(async function () {
                        state.reConnectCount++
                        console.log("开始重连..." + state.reConnectCount);
                        Message({message: `第${state.reConnectCount}次重新连接`, type: 'warning', duration: 1 * 1500})

                        await Promise.all([
                            dispatch('CONNECT', params)
                        ]).then(res => resolve(res)).catch(e => reject(e))

                    }, 2000);
				}
				reject()
                return
			})
		})
	},

    WEBSOCKET_CONNECT({ commit, state, dispatch }, params) {
	    let {url} = params
		return new Promise(async (resolve, reject) => {
            if (state.stompClient != null && state.stompClient.connected) {
                console.log('连接已建立成功，不再执行')
                resolve(-1)
                return
            }

            state.url = url
            if (state.stompClient != null) {
                if (state.websocket.readyState === SockJS.CONNECTING) {
                    console.log('连接正在建立，忽略本次连接')
                    resolve(0)
                    return
                } else if (state.websocket.readyState === SockJS.OPEN) {
                    console.log('连接已打开，发起重连')
                    state.stompClient.disconnect(async () => {

                        await Promise.all([
                            dispatch('CONNECT', params)
                        ]).then(res => resolve(res)).catch(e => reject(e))

                    });

                } else {
                    console.log("state.websocket.readyState = " + state.websocket.readyState)
                }
            }

            await Promise.all([
                dispatch('CONNECT', params)
            ]).then(res => resolve(res)).catch(e => reject(e))
        })
    },

    WEBSOCKET_DISCONNECT({ commit, state, dispatch }) { // 彻底断开连接
        return new Promise(async (resolve, reject) => {
			if(state.stompClient != null && ( state.stompClient.connected || state.websocket.readyState !== SockJS.CLOSED)) {
			    // 先取消订阅
                await Promise.all([
                    dispatch('WEBSOCKET_UNSUBSCRIBE')
                ]).then(res => resolve(res)).catch(e => reject(e))

				// 断开连接
				state.stompClient.disconnect(function(){
					// 有效断开的回调
					console.log("已断开连接")
					resolve()
				});
			}
		})
    },

    WEBSOCKET_UNSUBSCRIBE({ commit, state }, address) {
	    return new Promise((resolve, reject) => {

	        if (!state.stompClient || !state.stompClient.connected) {
                reject("没有连接,无法操作(取消)订阅 -> " + address);
                return
            }

	        if(address) {
                let id = ""
                for (let item in state.stompClient.subscriptions) {
                    if (item.endsWith(address)) {
                        id = item
                        break
                    }
                }
                if(id) {
                    state.stompClient.unsubscribe(id);
                    console.log("取消订阅成功 -> id:" + id + ", address:" + address)
                    resolve()
                    return
                }
                resolve(-1)
                return
            }

	        // 取消所有订阅
            for (let id in state.stompClient.subscriptions) {
                state.stompClient.unsubscribe(id);
                console.log("取消订阅成功 -> id:" + id)
            }
            resolve()
        })
    },

    WEBSOCKET_SUBSCRIBE({ commit, state, dispatch }, params) {
	    let {address, callBack} = params;
	    console.log("address=" + address)
	    console.log("callBack=" + callBack)
	    return new Promise(async (resolve, reject) => {

	        await Promise.all([
                dispatch('WEBSOCKET_UNSUBSCRIBE', address)
            ]).then(res => resolve(res)).catch(e => reject(e))

            // 生成 sub-id
            let timestamp = new Date().getTime() + address;
            console.log("订阅成功 -> " + address);
            state.stompClient.subscribe(
                address,
                (message) => {
                    //this.console(message, "message");
                    let data
                    try {
                        data = JSON.parse(message.body);
                    } catch (err) {
                        console.error(err)
                    }
                    console.log(" 订阅消息通知,信息如下 ↓↓↓↓↓↓↓");
                    console.log(data?JSON.stringify(data):message.body)
                    callBack(data)
                },
                {
                    id: timestamp,
                }
            )
            resolve()
        })
    },
}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}
