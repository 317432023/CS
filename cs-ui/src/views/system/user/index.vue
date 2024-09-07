<template>
  <div class="app-container"><!-- style="height: 100%;width: 100%;"-->

    <!-- 对话框 新增/编辑 -->
    <el-dialog
      v-el-drag-dialog
      :title="isAdd?'新增':'编辑'"
      :visible.sync="showDialog"
      :close-on-click-modal="false"
      width="610px"
      @closed="reset($refs.dialogForm)"
      @dragDialog="log($event)"
    >

      <el-form
        ref="dialogForm"
        :model="dialogForm"
        :inline="true"
        :rules="rule"
        style="font-weight: bold;"
        size="small"
        label-position="right"
        label-width="80px"
      >

        <el-form-item v-if="isAdd" label="用户名" prop="username">
          <el-input v-model="dialogForm.username" size="mini" autocomplete="off" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item v-if="isAdd" label="密码" prop="password">
          <el-input v-model="dialogForm.password" size="mini" autocomplete="off" placeholder="请输入密码" clearable />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="dialogForm.nickname" size="mini" autocomplete="off" clearable />
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="dialogForm.phonenumber" size="mini" autocomplete="off" clearable />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="dialogForm.email" size="mini" autocomplete="off" clearable />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio v-model="dialogForm.sex" size="mini" :label="0">男</el-radio>
          <el-radio v-model="dialogForm.sex" size="mini" :label="1">女</el-radio>
          <el-radio v-model="dialogForm.sex" size="mini" :label="2">保密</el-radio>
        </el-form-item>

        <el-form-item label="生日" prop="birth">
          <el-date-picker
            v-model="dialogForm.birth"
            style="width: 192px"
            size="mini"
            type="date"
            value-format="yyyy-MM-dd HH:mm:ss"
            format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择日期"
            clearable
            @change="dialogForm.birth=$event"
          />
        </el-form-item>

        <el-form-item label="头像" prop="avatar">
          <el-avatar :size="30" :src="dialogForm.avatar" @error="true">
            <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png">
          </el-avatar>
        </el-form-item>

        <el-form-item label="禁用" prop="disabled">
          <el-radio-group v-model="dialogForm.disabled" size="mini" fill="#009688" :disabled="dialogForm.userId===1">
            <el-radio-button size="mini" :label="1">是</el-radio-button>
            <el-radio-button size="mini" :label="0">否</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="所属站点" prop="orgId">
          <el-select
            v-model="dialogForm.orgId"
            size="mini"
            placeholder="所属站点"
            style="width:450px"
            clearable
            filterable
            :disabled="!isAdd"
            @change="log($event);dialogForm.roleIds=[];loadDialogRoles($event)"
          >
            <el-option v-for="item in orgs" :key="'dialogOrg' + item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="拥有角色" prop="roleIds">
          <el-select
            v-model="dialogForm.roleIds"
            size="mini"
            placeholder="拥有角色"
            style="width:450px"
            clearable
            multiple
            filterable
          >
            <el-option v-for="item in dialogRoles" :key="'dialogRole' + item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="showDialog = false">取 消</el-button>
        <el-button size="mini" type="primary" :loading="loadingSubmit" @click="submitForm()">
          {{ isAdd?'新增':'更新' }}
        </el-button>
      </div>

    </el-dialog>

    <!-- 搜索区域 -->
    <el-form ref="queryForm" :inline="true" :model="queryForm" label-width="70px">
      <el-form-item label="用户名">
        <el-input v-model="queryForm.username" size="mini" placeholder="用户名" clearable class="search-input" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="queryForm.nickname" size="mini" placeholder="昵称" clearable class="search-input" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="queryForm.email" size="mini" placeholder="邮箱" clearable class="search-input" />
      </el-form-item>
      <el-form-item label="站点">
        <el-select v-model="queryForm.orgId" size="mini" placeholder="站点" clearable class="search-input">
          <el-option v-for="item in orgs" :key="'org' + item.id" :label="item.name" :value="item.id">
            <span style="float:left">{{ item.name }}</span>
            <span style="float:right; color: #8492a6; font-size: 13px">{{ item.id }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="是否禁用">
        <el-radio-group v-model="queryForm.disabled" size="mini" fill="#009688" class="search-input">
          <el-radio-button size="mini" :label="1">禁用</el-radio-button>
          <el-radio-button size="mini" :label="0">启用</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="性别">
        <el-radio v-model="queryForm.sex" size="mini" label="0" disabled>男</el-radio>
        <el-radio v-model="queryForm.sex" size="mini" label="1" disabled>女</el-radio>
        <el-radio v-model="queryForm.sex" size="mini" label="2" disabled>保密</el-radio>
      </el-form-item>
      <el-form-item label="">
        <el-button size="mini" icon="el-icon-refresh" @click="reset()">重置</el-button>
        <el-button size="mini" type="primary" icon="el-icon-search" @click="queryPage()">查询</el-button>
        <el-button
          v-if="havePermission('system:user:add')"
          size="mini"
          type="success"
          icon="el-icon-plus"
          @click="showAddDialog({enabled:true})"
        >添加
        </el-button>
        <el-button size="mini" type="warning" icon="el-icon-download">下载</el-button>
      </el-form-item>
    </el-form>

    <!-- 分割线 -->
    <!--<el-divider></el-divider>-->

    <!-- 内容区域（表格）-->
    <el-table
      v-loading="loadingTable"
      element-loading-text="加载中..."
      highlight-current-row
      border
      fit
      :data="records"
    > <!--max-height="330" style="width: 100%"-->
      <el-table-column
        prop="username"
        label="用户名"
      />
      <el-table-column
        prop="nickname"
        label="昵称"
      />
      <el-table-column
        prop="sex"
        label="性别"
        width="60"
      >
        <template slot-scope="scope">
          {{ scope.row.sex===0?'保密':(scope.row.sex===1?'男':'女') }}
        </template>
      </el-table-column>
      <el-table-column
        prop="orgName"
        label="站点"
      />
      <!--<el-table-column
        prop="phonenumber"
        label="手机号"
        width="120">
      </el-table-column>
      <el-table-column
        prop="email"
        label="邮箱"
        width="160">
      </el-table-column>
      <el-table-column
        prop="avatar"
        label="头像"
        width="100">
      </el-table-column>
      <el-table-column
        prop="salt"
        label="盐"
        width="180">
      </el-table-column>-->
      <el-table-column align="center" label="禁用" prop="disabled" width="70">
        <template slot-scope="scope">
          <el-switch
            :value="scope.row.disabled==='1'"
            disabled
            active-color="#ff4949"
            inactive-color="#13ce66"
          />
        </template>
      </el-table-column>

      <el-table-column
        sortable
        prop="createTime"
        label="创建时间"
        width="160"
      />
      <!--
      <el-table-column label="操作" min-width="120" fixed="right" min-width="175">
        <el-button type="primary" size="mini" icon="el-icon-edit"></el-button>
        <el-button type="danger" size="mini" icon="el-icon-delete"></el-button>
        <el-button type="warning" size="mini" icon="el-icon-s-tools"></el-button>
      </el-table-column>
      -->
      <el-table-column
        v-if="haveAnyPermission(['system:user:mod','system:user:del'])"
        label="操作"
        align="center"
        min-width="120"
        max-width="120"
        fixed="right"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            v-if="havePermission('system:user:mod') && scope.row.userId != '1'"
            type="success"
            icon="el-icon-edit"
            :loading="loadingEdit"
            @click="showEditDialog(''+scope.row.userId)"
          />
          <el-button
            v-if="havePermission('system:user:del') && scope.row.userId != '1'"
            size="mini"
            type="danger"
            icon="el-icon-delete"
            @click="removeOne(''+scope.row.userId)"
          />
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      style="padding-top: 10px"
      :current-page="params.current"
      :page-size="params.size"
      :page-sizes="pagesizes"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      background
      @size-change="params.size=$event;queryPage()"
      @current-change="queryPage"
    /><!--small-->

  </div>
</template>

<script>
import elDragDialog from '@/directive/el-drag-dialog'

import request from '@/api/system/user'
import common from '@/mixins/common'

import roleApi from '@/api/system/role'
import cmmApi from '@/api/cmm'

export default {
  name: 'Admin',
  directives: { elDragDialog },
  mixins: [common],
  data() {
    return {
      request: request,
      dialogForm: {
        roleIds: []
      },
      rule: {
        username: [{ required: true, message: '用户名必须填写', trigger: 'blur' }],
        password: [{ required: true, message: '密码必须填写', trigger: 'blur' }],
        nickname: [{ required: true, message: '请填写昵称', trigger: 'blur' }],
        phonenumber: [{ required: false, message: '请填写手机号', trigger: 'blur' }],
        email: [{ required: false, message: '请填写邮箱', trigger: 'blur' }],
        sex: [{ required: false, message: '请填写性别', trigger: 'blur' }],
        birth: [{ required: false, message: '请填写生日', trigger: 'blur' }],
        avatar: [{ required: false, message: '请选择头像', trigger: 'blur' }],
        orgId: [{ required: true, message: '请选择站点', trigger: 'blur' }],
        roleIds: [{ required: true, message: '请选择角色', trigger: 'blur' }]
      },
      orgs: [],
      // begin for 对话框
      dialogRoles: []
      // end for 对话框

    }
  },
  created() {
    // 会先调用common#queryPage

    this.loadOrgs()
  },
  methods: {
    log(value) {
      console.log(value)
    },

    loadOrgs() {
      cmmApi.orgList().then(res => {
        this.orgs = res.data
      })
    },
    async getRoles(orgId) {
      const res = await roleApi.page({
        current: 1,
        size: 1000,
        orgId: orgId
      }).then(res => res).catch(err => err)
      if (res.code === 0 || res.success) {
        return res.data.records
      }
      return null
    },
    async loadDialogRoles(orgId) {
      this.dialogRoles = orgId ? await this.getRoles(orgId) : []
    },

    // begin Overrides 覆盖 common.js 中的方法
    // 显示添加模态框
    showAddDialog(form) {
      this.dialogRoles = []
      this.isAdd = true
      this.dialogForm = form
      this.showDialog = true
    },
    // 编辑数据
    showEditDialog(id) {
      this.log(id)
      this.isAdd = false
      this.request.getById(id).then(res => {
        // 取用户所属站点
        const orgId = res.data.orgId
        this.loadDialogRoles(orgId)
        this.dialogForm = res.data
        this.showDialog = true
      })
    }

    // end Overrides 覆盖 common.js 中的方法
  }
}
</script>

<style lang="less" scoped>
  .search-input {

    width: 120px;
  }
</style>
