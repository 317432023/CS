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
  data() {
    return {
      currentRole: 'editorDashboard'
    }
  },
  computed: {
    ...mapGetters([
      'roles', 'roleIds', 'tenantId'
    ])
  },
  created() {
    console.log('roles:' + JSON.stringify(this.roles) + '\nroleIds:' + JSON.stringify(this.roleIds) + '\ntenantId:' + this.tenantId + ', typeof(this.tenantId)=' + typeof (this.tenantId))
    if (this.roles.includes('root') && this.tenantId === 0) {
      this.currentRole = 'adminDashboard' // this.currentRole = 'rootDashboard' // 理论上 根用户应该使用 rootDashboard 仪表视图 这里为了美观先使用 adminDashboard
    } else if (this.roles.includes('admin')) {
      this.currentRole = 'adminDashboard'
    }
  }
}
</script>
