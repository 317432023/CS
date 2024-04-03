<template>
  <div class="app-container"><!-- style="height: 100%;width: 100%;"-->

    <!-- 对话框 新增/编辑 -->
    <el-dialog :title="isAdd?'新增':'编辑'" @closed="reset($refs.dialogForm)" :visible.sync="showDialog"
               :close-on-click-modal="false" width="610px"
               v-el-drag-dialog
               @dragDialog="log($event)">

      <el-form :model="dialogForm" :inline="true" ref="dialogForm" :rules="rule" style="font-weight: bold;" size="small"
               label-position="right"
               label-width="80px">

        <el-form-item label="用户名" prop="username" v-if="isAdd">
          <el-input size="mini" v-model="dialogForm.username" autocomplete="off" placeholder="请输入用户名" clearable/>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="isAdd">
          <el-input size="mini" v-model="dialogForm.password" autocomplete="off" placeholder="请输入密码" clearable/>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input size="mini" v-model="dialogForm.nickname" autocomplete="off" clearable/>
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input size="mini" v-model="dialogForm.phonenumber" autocomplete="off" clearable/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input size="mini" v-model="dialogForm.email" autocomplete="off" clearable/>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio size="mini" v-model="dialogForm.sex" :label="0">男</el-radio>
          <el-radio size="mini" v-model="dialogForm.sex" :label="1">女</el-radio>
          <el-radio size="mini" v-model="dialogForm.sex" :label="2">保密</el-radio>
        </el-form-item>

        <el-form-item label="生日" prop="birth">
          <el-date-picker style="width: 192px"
                          size="mini"
                          v-model="dialogForm.birth"
                          type="date" value-format="yyyy-MM-dd HH:mm:ss" format="yyyy-MM-dd HH:mm:ss"
                          @change="dialogForm.birth=$event"
                          placeholder="选择日期"
                          clearable>
          </el-date-picker>
        </el-form-item>

        <el-form-item label="头像" prop="avatar">
          <el-avatar :size="30" :src="dialogForm.avatar" @error="true">
            <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
          </el-avatar>
        </el-form-item>

        <el-form-item label="禁用" prop="disabled">
          <el-radio-group size="mini" v-model="dialogForm.disabled" fill="#009688" :disabled="dialogForm.userId===1">
            <el-radio-button size="mini" :label="1">是</el-radio-button>
            <el-radio-button size="mini" :label="0">否</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="所属站点" prop="orgId">
          <el-select size="mini" v-model="dialogForm.orgId" placeholder="所属站点" style="width:450px" clearable
                     @change="log($event);dialogForm.roleIds=[];loadDialogRoles($event)"
                     filterable :disabled="!isAdd">
            <el-option :label="item.name" :value="item.id" v-for="item in orgs" :key="'dialogOrg' + item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="拥有角色" prop="roleIds">
          <el-select size="mini" v-model="dialogForm.roleIds" placeholder="拥有角色" style="width:450px" clearable multiple
                     filterable>
            <el-option :label="item.name" :value="item.id" v-for="item in dialogRoles" :key="'dialogRole' + item.id"/>
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="showDialog = false">取 消</el-button>
        <el-button size="mini" type="primary" @click="submitForm()" :loading="loadingSubmit">
          {{isAdd?'新增':'更新'}}
        </el-button>
      </div>

    </el-dialog>

    <!-- 搜索区域 -->
    <el-form :inline="true" :model="queryForm" ref="queryForm" label-width="70px">
      <el-form-item label="用户名">
        <el-input size="mini" v-model="queryForm.username" placeholder="用户名" clearable class="search-input"></el-input>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input size="mini" v-model="queryForm.nickname" placeholder="昵称" clearable class="search-input"></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input size="mini" v-model="queryForm.email" placeholder="邮箱" clearable class="search-input"></el-input>
      </el-form-item>
      <el-form-item label="站点">
        <el-select size="mini" v-model="queryForm.orgId" placeholder="站点" clearable class="search-input">
          <el-option :label="item.name" :value="item.id" v-for="item in orgs" :key="'org' + item.id">
            <span style="float:left">{{item.name}}</span>
            <span style="float:right; color: #8492a6; font-size: 13px">{{item.id}}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="是否禁用">
        <el-radio-group size="mini" v-model="queryForm.disabled" fill="#009688" class="search-input">
          <el-radio-button size="mini" :label="1">禁用</el-radio-button>
          <el-radio-button size="mini" :label="0">启用</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="性别">
        <el-radio size="mini" v-model="queryForm.sex" label="0" disabled>男</el-radio>
        <el-radio size="mini" v-model="queryForm.sex" label="1" disabled>女</el-radio>
        <el-radio size="mini" v-model="queryForm.sex" label="2" disabled>保密</el-radio>
      </el-form-item>
      <el-form-item label="">
        <el-button size="mini" icon="el-icon-refresh" @click="reset()">重置</el-button>
        <el-button size="mini" type="primary" icon="el-icon-search" @click="queryPage()">查询</el-button>
        <el-button size="mini" type="success" icon="el-icon-plus"
                   v-if="havePermission('system:user:add')" @click="showAddDialog({enabled:true})">添加
        </el-button>
        <el-button size="mini" type="warning" icon="el-icon-download">下载</el-button>
      </el-form-item>
    </el-form>

    <!-- 分割线 -->
    <!--<el-divider></el-divider>-->

    <!-- 内容区域（表格）-->
    <el-table v-loading="loadingTable" element-loading-text="加载中..." highlight-current-row border fit
              :data="records"> <!--max-height="330" style="width: 100%"-->
      <el-table-column
        prop="username"
        label="用户名">
      </el-table-column>
      <el-table-column
        prop="nickname"
        label="昵称">
      </el-table-column>
      <el-table-column
        prop="sex"
        label="性别" width="60">
        <template slot-scope="scope">
          {{scope.row.sex===0?'保密':(scope.row.sex===1?'男':'女')}}
        </template>
      </el-table-column>
      <el-table-column
        prop="orgName"
        label="站点">
      </el-table-column>
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
          <el-switch :value="scope.row.disabled==='1'" disabled
                     active-color="#ff4949"
                     inactive-color="#13ce66"/>
        </template>
      </el-table-column>

      <el-table-column sortable
                       prop="createTime"
                       label="创建时间"
                       width="160">
      </el-table-column>
      <!--
      <el-table-column label="操作" min-width="120" fixed="right" min-width="175">
        <el-button type="primary" size="mini" icon="el-icon-edit"></el-button>
        <el-button type="danger" size="mini" icon="el-icon-delete"></el-button>
        <el-button type="warning" size="mini" icon="el-icon-s-tools"></el-button>
      </el-table-column>
      -->
      <el-table-column label="操作" align="center" min-width="120" max-width="120" fixed="right"
                       v-if="haveAnyPermission(['system:user:mod','system:user:del'])">
        <template slot-scope="scope">
          <el-button @click="showEditDialog(''+scope.row.userId)" size="mini" type="success" icon="el-icon-edit"
                     :loading="loadingEdit"
                     v-if="havePermission('system:user:mod') && scope.row.userId != '1'"/>
          <el-button @click="removeOne(''+scope.row.userId)" size="mini" type="danger" icon="el-icon-delete"
                     v-if="havePermission('system:user:del') && scope.row.userId != '1'"/>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination style="padding-top: 10px"
                   :current-page="params.current" :page-size="params.size"
                   @size-change="params.size=$event;queryPage()" @current-change="queryPage"
                   :page-sizes="pagesizes"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total" background/><!--small-->

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
    mixins: [common],
    directives: {elDragDialog},
    data() {
      return {
        request: request,
        dialogForm: {
          roleIds:[]
        },
        rule: {
          username: [{required: true, message: '用户名必须填写', trigger: 'blur'}],
          password: [{required: true, message: '密码必须填写', trigger: 'blur'}],
          nickname: [{required: true, message: '请填写昵称', trigger: 'blur'}],
          phonenumber: [{required: false, message: '请填写手机号', trigger: 'blur'}],
          email: [{required: false, message: '请填写邮箱', trigger: 'blur'}],
          sex: [{required: false, message: '请填写性别', trigger: 'blur'}],
          birth: [{required: false, message: '请填写生日', trigger: 'blur'}],
          avatar: [{required: false, message: '请选择头像', trigger: 'blur'}],
          orgId: [{required: true, message: '请选择站点', trigger: 'blur'}],
          roleIds: [{required: true, message: '请选择角色', trigger: 'blur'}]
        },
        orgs: [],
        // begin for 对话框
        dialogRoles: []
        // end for 对话框

      }
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
        if (res.success) {
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
    },
    created() {
      // 会先调用common#queryPage

      this.loadOrgs()
    }
  }
</script>

<style lang="less" scoped>
  .search-input {

    width: 120px;
  }
</style>
