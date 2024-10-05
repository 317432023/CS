<template>
  <div class="session-staff-container" :style="{ height: (scrollHeight + 15) + 'px' }">
    <el-row id="chatDialog" :gutter="20">
      <el-col id="userList" :span="6" :style="{ height: scrollHeight + 'px'}">
        <div v-for="(user,index) in userList" :key="user.rcptId" :ref="'user'+user.rcptId" :style="{textAlign: 'center'}" @click="chatUserSelected(user)">
          <br>
          <el-badge v-if="user.unreadMessageNum > 0" is-dot>
            <el-avatar shape="square" :size="64" fit="fill" :src="user.avatar" :class="{gray: !user.online}" />
          </el-badge>
          <el-avatar v-else shape="square" :size="64" fit="fill" :src="user.avatar" :class="{gray: !user.online}" />

          <span class="title">{{ user.nickName }}</span>
          <span style="font: italic normal 12px/30px arial, sans-serif; color: grey;">{{ (user.lastMessageTime && user.lastMessageTime !== '1970-01-01 00:00:00')?user.lastMessageTime.substring(5):'                   ' }}</span>
        </div>
        <br>
      </el-col>
      <el-col v-if="chatToUser != null" id="chatMain" :span="18">
        <el-container :style="{ height: scrollHeight + 'px' }">
          <el-header style="text-align: center; font-size: 12px; line-height: 60px; font-weight:bold;">
            <span class="title">与 {{ chatToUser.nickName }} 聊天&nbsp;<el-button @click="handleLoadHis">加载历史</el-button></span>
          </el-header>
          <el-main id="chat-cnt" ref="mainscroll" @scroll="handleScroll">
            <el-row v-for="(msg,index) in msgList" :key="msg.id" :ref="'msg_'+msg.id">
              <el-col :span="4"><el-avatar v-if="msg.sender === msg.roomId" shape="square" :size="48" fit="fill" :src="getAvatar(msg.sender)" /><span v-else style="visibility:hidden">.</span></el-col>
              <el-col :span="16">
                <div :style="{textAlign:msg.sender !== msg.roomId?'right':'left'}">
                  <span style="font-weight: bold;color: lightsteelblue;">{{ msg.senderNickName }}</span><br>
                  <span v-if="msg.message.type==='text'">
                    {{ msg.message.value }}
                  </span>
                  <span v-else-if="msg.message.type === 'image'">
                    <el-image :src="msg.message.value">
                      <div slot="placeholder" class="image-slot">
                        加载中<span class="dot">...</span>
                      </div>
                    </el-image>
                  </span>
                  <div style="font: italic normal 12px/30px arial, sans-serif; color: grey;">{{ msg.createTime }}</div>
                </div>
              </el-col>
              <el-col v-if="msg.sender === msg.roomId" :span="4">&nbsp;</el-col><el-col v-else :span="4"><el-avatar shape="square" :size="48" fit="fill" :src="$store.getters.avatar" style="float:right" /></el-col>
            </el-row>
          </el-main>
          <el-footer id="input-cont0">
            <el-container id="input-cont">
              <el-main id="chat-input">
                <el-input id="input" v-model="textarea" type="textarea" :rows="5" placeholder="请输入内容" />
              </el-main>
              <el-footer id="btn-send-div">
                <el-button type="primary" @click="sendMsg($event)">发送</el-button>&nbsp;
                <el-upload
                  style="display:inline;"
                  name="myFileName"
                  :on-success="handleUploadSuccess"
                  :before-upload="beforeUpload"
                  :action="uploadUrl"
                >
                  <el-button size="small" type="primary" icon="el-icon-picture" class="share-button" />
                </el-upload>&nbsp;
              </el-footer>
            </el-container>
          </el-footer>
        </el-container>
      </el-col>
    </el-row>
    <div style="display: none">
      <audio ref="audioPlayer" src="@/assets/audio/cs.mp3" controls />
    </div>
  </div>
</template>
<script>

import request from '@/api/cs/mbr'
import open from '@/api/cs/open'
import { generateRandomString } from '@/utils/tools'

export default {
  name: 'StaffSession',
  data() {
    return {
      textarea: '',
      chatToUser: null, // 当前聊天用户
      users: [],
      scrollHeight: 100,
      msgList: [],
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadUrl: ''
    }
  },
  computed: {
    clientId() {
      return this.$store.getters.token + '#1'
    },
    userList() {
      return [...this.users].sort((a, b) => Date.parse(b.lastMessageTime) - Date.parse(a.lastMessageTime))
    },
    curUser() {
      return '客服#' + this.$store.getters.name
    },
    stompClient() {
      return this.$store.getters.stompClient
    }
  },
  created() {
    open.getUploadUrl().then(res => {
      this.uploadUrl = res.data
      console.log(this.uploadUrl)
    }).catch(e => {
      console.error('getUploadUrl failed')
    })
  },
  async mounted() {
    console.log('mounted')
    const oh = 80
    this.scrollHeight = document.documentElement.clientHeight - 20 - oh
    window.onresize = () => {
      if (this.scrollHeight) {
        this.scrollHeight = document.documentElement.clientHeight - 20 - oh
      }
    }

    // 监听滚动事件
    // this.$refs.mainscroll.addEventListener('scroll', this.handleScroll)

    // 关闭连接
    if (this.$store.getters.connected) {
      await this.$store.dispatch('websocket/WEBSOCKET_DISCONNECT')
    }
    // 打开连接
    this.connect(this.$store.getters.token, 1).then(res => {
      console.log('res => ' + res + ', this.$store.getters.connected=' + this.$store.getters.connected)
      if (res === 1 && this.$store.getters.connected) {
        console.log(this.$store.getters.name + ' 首次连接成功')
      }
    }).catch(e => {
      console.error('connect failed')
    })
  },
  /**
   * 组件销毁
   */
  beforeDestroy() {
    // 组件销毁前移除事件监听
    // this.$refs.mainscroll.removeEventListener('scroll', this.handleScroll)
    // 断开连接 (先取消订阅)
    this.$store.dispatch('websocket/WEBSOCKET_DISCONNECT') // 断开链接
  },
  methods: {
    handleScroll(event) {
      const target = event.target
      if (target.scrollTop === 0) {
        // 滚动条到达顶部，执行你需要的操作
        console.log('滚动条到达顶部')
      }
    },
    scrollToTop() {
      this.$nextTick(() => {
        const scrollEl = document.getElementById('chat-cnt')
        scrollEl.scrollTop = 1
      })
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const scrollEl = document.getElementById('chat-cnt')
        scrollEl.scrollTop = scrollEl.scrollHeight
      })
    },
    handleLoadHis() {
      console.log('按下按钮')
      if (this.msgList.length === 0) {
        return
      }
      const headMsgId = this.msgList[0].id
      console.log(headMsgId)
      console.log('this.chatToUser:' + JSON.stringify(this.chatToUser))
      request.getRecentMessages(this.clientId, { customerChatUserId: this.chatToUser.rcptId, headMsgId: headMsgId }).then((res) => {
        // console.log('加载聊天历史：' + JSON.stringify(res.data))
        if (res.data.length > 0) {
          this.msgList = [...res.data, ...this.msgList]
          this.$message.success('已加载成功')
          const obj = res.data[res.data.length - 1]
          if (!obj.read) {
            // console.log("发送已读：" + obj.id)
            this.$sendRead(obj.id)
          }
        } else {
          this.$message.info('已加载全部聊天历史')
        }
      })
    },
    reduceChatUsers(userAry, clientId, lastChatUserId, limit) {
      request.getChatUsers(clientId, 0, lastChatUserId, limit).then(res => {
        // console.log("取得聊天对手 => " + JSON.stringify(res.data))
        if (res.data.length > 0) {
          lastChatUserId = res.data[res.data.length - 1].rcptId
          for (const v of res.data) { userAry.push(v) }
          // this.reduceChatUsers(userAry, clientId, lastChatUserId, limit)
        }
      })
    },
    async getChatUser(clientId, rcptId) {
      const [user, e] = await request.getChatUsers(clientId, rcptId).then(res => {
        // console.log("取得聊天对手 => " + JSON.stringify(res.data))
        return res.data.length > 0 ? res.data[0] : null
      }).catch(e => e)
      return user
    },
    /**
     * 连接websocket
     * @param token 接入系统令牌
     * @param userType 用户类型，当 userType = 1时，忽略 appId 与 stationId
     * @param appId 应用ID eg. FT1
     * @param stationId 站点ID eg. 6
     */
    connect(token, userType, appId, stationId) {
      token = token ? encodeURIComponent(token) : ''
      appId = appId ? encodeURIComponent(appId) : ''
      stationId = stationId ? encodeURIComponent(stationId) : ''
      const tenantId = (appId && stationId) ? `${appId}@${stationId}` : ''

      const wsUrl = `${this.baseUrl}/api/chat_user/ws?userType=${userType}&token=${token}&tenantId=${tenantId}`

      return new Promise((resolve, reject) => {
        const that = this
        this.$store.dispatch('websocket/WEBSOCKET_CONNECT', { url: wsUrl, okCallBack: function() {
          // 订阅一对一消息 ack tip
          that.$store.dispatch('websocket/WEBSOCKET_SUBSCRIBE', { address: '/user/alone/cs/getResponse', callBack: data => that.showMsg(data) })
          if (that.chatToUser) {
            that.refreshChatRoom()
          } else {
            // 取得聊天用户列表更新到 users 中
            const userAry = []
            that.reduceChatUsers(userAry, that.clientId, 0, 20)
            that.users = userAry
          }
        } }).then(res => resolve(res)).catch(e => reject(e))
      })
    },
    refreshChatRoom() {
      if (!this.chatToUser) return

      this.msgList = []
      // 查询 this.chatUser.rcptId 的消息历史 然后填进 this.msgList 数组
      request.getRecentMessages(this.clientId, { customerChatUserId: this.chatToUser.rcptId }).then((res) => {
        // console.log("取得聊天历史：" + JSON.stringify(this.msgList))
        this.msgList = res.data
        this.scrollToBottom() // 聊天内容滚动到底部
        if (res.data.length > 0) {
          const obj = res.data[res.data.length - 1]
          if (!obj.read) {
            // console.log("发送已读：" + obj.id)
            this.$sendRead(obj.id)
          }
        }
      })

      // 订阅群消息 chat
      if (this.$store.getters.connected) {
        this.$store.dispatch('websocket/WEBSOCKET_SUBSCRIBE', { address: '/mass/cs/getResponse/' + this.chatToUser.rcptId, callBack: data => this.showMsg(data) })
      }
    },
    /**
     * 切换聊天对象
     */
    chatUserSelected(user) {
      if (this.chatToUser && this.chatToUser.rcptId === user.rcptId) {
        return
      }
      this.chatToUser = user // 当前选中的用户
      console.log('当前选中的用户=' + JSON.stringify(this.chatToUser))

      const that = this
      let obj = that.$refs['user' + user.rcptId][0]

      obj.style.cssText = 'text-align:center;background:skyblue'

      this.users.forEach((item, ix, ary) => {
        if (item.rcptId !== user.rcptId) {
          obj = that.$refs['user' + item.rcptId][0]
          obj.style.cssText = 'text-align:center;'
        }
      })

      this.refreshChatRoom()
    },

    /**
     * 显示消息
     */
    showMsg(msg) {
      // 接收的消息格式如: {"msgId":"by98v5jxix5drsv96iiv17fkx4gbgyri","msgType":"chat","roomId":1,"msgBody":{"type":"text","value":"你好！"},"sender":xx,"createTime": "2024-04-11 23:22:04"}
      if (msg && typeof msg === 'object') {
        switch (msg.msgType) {
          case 'chat':
            // 经过转换后插入消息对象数组 this.msgList 中 (由vue绑定自动显示)
            const obj = {
              id: msg.msgId,
              sender: msg.sender,
              receiver: msg.rcptId,
              roomId: msg.roomId,
              message: msg.msgBody,
              createTime: msg.createTime,
              senderNickName: msg.senderNickName
              // receiverNickName:null,
            }
            if (this.chatToUser) {
              if (this.chatToUser.rcptId === msg.roomId) { // 当前群消息
                this.msgList.push(obj) // （在末尾）向数组添加新元素
                this.scrollToBottom() // 聊天内容滚动到底部
                if ('客服#' + obj.senderNickName === this.curUser) { // 收到自己发送的消息
                  if (obj.message.type === 'text' && this.textarea === obj.message.value) {
                    this.textarea = '' // 发送成功并接收到自己发送的文本消息则清空文本框
                  }
                } else { // 收到对方的消息
                  this.chatToUser.lastMessageTime = obj.createTime // 更新最后一次消息时间
                  console.log(`发送消息已读 ${obj.id}`)
                  this.$sendRead(obj.id)
                }
              }
            } else {
              if (obj.sender === obj.roomId) { // 收到对方的消息
                // eslint-disable-next-line no-unused-vars
                let foundUser = this.users.find(element => element.rcptId === obj.sender)
                if (!foundUser) {
                  foundUser = this.getChatUser(this.clientId, obj.sender)
                  this.users.push(foundUser)
                }
                foundUser.lastMessageTime = obj.createTime
              }
            }
            break
          case 'ack':
            console.log(`消息 ${msg.msgId} 发送成功`)
            // ignore
            break
          case 'tip':
            this.$message.error(msg.msgBody.value)
            break
          case 'event':
            // 消息事件 一对一消息 目前仅代表收到消息
            const evtStr = msg.msgBody.value
            const ary = evtStr.split(',')
            const userId = ary[0]
            const lastMessageTime = ary[1]
            const a = this.users.filter(obj => (obj.rcptId + '') === userId)
            if (a.length > 0) {
              a[0].lastMessageTime = lastMessageTime
              // 收到消息提醒
              this.$message.info('收到一条新消息，来自【' + a[0].nickName + '】')
              this.$refs.audioPlayer.play()
            } else {
              const that = this
              // 往users数组中插入一个用户
              request.getChatUsers(that.clientId, userId).then(res => {
                // console.log("取得聊天对手 => " + JSON.stringify(res.data))
                if (res.data.length > 0) {
                  const user = res.data[0]
                  that.users.push(user)
                  // 收到消息提醒
                  that.$message.info('收到一条新消息，来自【' + user.nickName + '】')
                  that.$refs.audioPlayer.play()
                }
              })
            }
            break
          default:
            break
        }
      } else {
        console.warn(msg)
      }
    },

    sendMsg(event) {
      // console.log(event.target)
      const msgTxt = this.textarea.trim()
      if (msgTxt === '') {
        return
      }
      const msgBody = { 'type': 'text', 'value': msgTxt }
      this.$sendMsg(msgBody)
    },
    $sendMsg(msgBody) {
      if (!msgBody || (msgBody.type !== 'text' && msgBody.type !== 'image')) {
        return
      }
      let roomId = 0
      if (this.chatToUser) {
        roomId = this.chatToUser.rcptId
      }
      if (roomId === 0) {
        console.log('聊天用户未指定')
        return
      }
      const token = this.$store.getters.token
      const connected = this.$store.getters.connected
      if (connected) {
        // 发送的消息格式如：
        const msg = {
          msgId: generateRandomString(32), // 消息ID
          msgType: 'chat', // 消息类型 ack tip chat read
          roomId, // 聊天室ID
          msgBody // 消息体
        }
        // console.log(JSON.stringify(msg))
        this.stompClient.send('/app/cs/massRequest/' + roomId, { 'X-Token': token }, JSON.stringify(msg))
      }
    },
    $sendRead(recvMsgId) {
      if (!recvMsgId) {
        return
      }
      let roomId = 0
      if (this.chatToUser) {
        roomId = this.chatToUser.rcptId
      }
      if (roomId === 0) {
        console.log('聊天用户未指定')
        return
      }
      const token = this.$store.getters.token
      const connected = this.$store.getters.connected
      if (connected) {
        const msg = {
          msgId: generateRandomString(32), // 消息ID
          msgType: 'read',
          roomId,
          msgBody: { type: '', value: recvMsgId + '' }
        }
        this.stompClient.send('/app/cs/massRequest/' + roomId, { 'X-Token': token }, JSON.stringify(msg))
      }
    },

    getAvatar(rcptId) {
      for (let i = 0, len = this.users.length; i < len; i++) {
        console.log(JSON.stringify(this.users[i]))
        if (this.users[i].rcptId === rcptId) {
          return this.users[i].avatar
        }
      }
      return null
    },
    beforeUpload(file) {
      const isImg = file.type === 'image/jpeg' || file.type === 'image/png'
      if (!isImg) {
        this.$message.error('上传头像图片只能是 图片 jpeg或png格式!')
      }

      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
      }
      return isImg && isLt2M
    },
    handleUploadSuccess(res, file) {
      // 上传文件成功res=》{"code":200,"data":{"resPrefix":"","res":["/upload/jpeg/2023/11/29/e10c3a6c-0b0f-4d69-9106-c832d0598e3a.jpeg"]},"msg":"","success":true}
      if (res.code !== 200 && res.code !== 0) {
        this.$message.error('上传失败')
        return
      }
      const imgUrlAry = res.data.res
      const imgUrl = imgUrlAry[0]
      const msgBody = { 'type': 'image', 'value': imgUrl }
      this.$sendMsg(msgBody)
    }
  }
}
</script>
<style lang="scss" scoped>
    .session-staff-container {
        padding: 5px;
        background-color: rgb(240, 242, 245);
        position: relative;
        /*border: 1px dashed red;*/

        #chatDialog {width:700px; border: 1px solid skyblue; border-radius:5px; position: absolute; top:50%; left: 50%; transform:translate(-50%,-50%);  /*向左和向上移动自身宽度一半*/}
        .title {display:block;}
        #userList {overflow-y:auto; border-right: 1px solid skyblue;}
        #input-cont0 {height:200px !important;}
        #input-cont {width:500px; height:200px;}
        #chat-input{overflow-y: hidden; padding-left:0; padding-right:40px;}
        #input {margin:0;height:100px;}
        #btn-send-div {height:60px; line-height:60px;}

        #chat-cnt {overflow-y:auto;  border-top: 1px solid skyblue; border-bottom: 1px solid skyblue;}
        #chat-cnt .el-row{padding: 2px 0;}
        .gray {
		  -webkit-filter: grayscale(100%);
		  -webkit-filter: grayscale(1);/* Webkit */
		  filter: grayscale(100%);
		  filter: url("data:image/svg+xml;utf8,<svg%20xmlns='http://www.w3.org/2000/svg'><filter%20id='grayscale'><feColorMatrix%20type='matrix'%20values='0.3333%200.3333%200.3333%200%200%200.3333%200.3333%200.3333%200%200%200.3333%200.3333%200.3333%200%200%200%200%200%201%200'/></filter></svg>#grayscale");
		  filter: gray;/* IE6-9 */
		}
    }

</style>
