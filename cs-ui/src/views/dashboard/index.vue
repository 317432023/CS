<template>
  <div class="dashboard-container">
    <component :is="currentRole" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import rootDashboard from './root'
import adminDashboard from './admin'
import editorDashboard from './editor'

export default {
  name: 'Dashboard',
  components: { rootDashboard, adminDashboard, editorDashboard },
  data () {
    return {
      currentRole: 'editorDashboard'
    }
  },
  computed: {
    ...mapGetters([
      'roles','roleIds','orgId'
    ])
  },
  created () {
    console.log("roles:"+JSON.stringify(this.roles) + "\nroleIds:"+JSON.stringify(this.roleIds) + "\norgId:"+this.orgId+", typeof(this.orgId)="+ typeof(this.orgId))
    if (this.roles.includes('root') && this.orgId === 0) {
      this.currentRole = 'adminDashboard' // this.currentRole = 'rootDashboard' // 理论上 根用户应该使用 rootDashboard 仪表视图 这里为了美观先使用 adminDashboard
    } else if(this.roles.includes('admin')) {
      this.currentRole = 'adminDashboard'
    }
  }
}
</script>
