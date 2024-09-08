<template>
  <div class="dashboard-container">
    <component :is="currentRole" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import adminSession from './admin'
import managerSession from './manager'
import staffSession from './staff'

export default {
  name: 'Sessions',
  components: { adminSession, managerSession, staffSession },
  data() {
    return {
      currentRole: 'staffSession'
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
      this.currentRole = 'adminSession'
    } else if (this.roles.includes('MANAGER')) {
      this.currentRole = 'managerSession'
    }
  }
}
</script>
