<template>
  <div class="app-container">
    <!-- 对话框 新增/编辑 -->
    <el-dialog :title="isAdd?'新增':'编辑'" @closed="reset($refs.dialogForm)" :visible.sync="showDialog" :close-on-click-modal="false" width="600px"
               v-el-drag-dialog>
      <el-form status-icon  :model="dialogForm" :inline="true" ref="dialogForm" :rules="rule" style="font-weight: bold;" size="small" label-position="right" label-width="80px" >
        <el-form-item prop="type" label="类别Y:系统内置,N其他">
          <el-input v-model="dialogForm.configType" size="mini" placeholder="类别Y:系统内置,N其他"/>
        </el-form-item>
        <el-form-item prop="title" label="参数名称（中文）">
          <el-input v-model="dialogForm.configName" size="mini" placeholder="参数名称（中文）"/>
        </el-form-item>
        <el-form-item prop="name" label="配置键（唯一）">
          <el-input v-model="dialogForm.configKey" size="mini" placeholder="配置键（唯一）"/>
        </el-form-item>
        <el-form-item prop="value" label="参数值">
          <el-input v-model="dialogForm.configValue" size="mini" placeholder="参数值"/>
        </el-form-item>
        <el-form-item prop="remark" label="备注">
          <el-input v-model="dialogForm.remark" size="mini" placeholder="备注"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="showDialog = false">取 消</el-button>
        <el-button size="mini" type="primary" @click="submitForm()" :loading="loadingSubmit">{{isAdd?'新增':'更新'}}</el-button>
      </div>
    </el-dialog>

    <!-- 搜索区域 -->
    <el-form :inline="true">
      <el-form-item>
        <el-select multiple filterable allow-create size="mini" v-model="params.sort"
                   placeholder="排序字段">
          <el-option label="configId" value="configId"></el-option>
          <el-option label="类别Y:系统内置,N其他" value="configType"></el-option>
          <el-option label="参数名称（中文）" value="configName"></el-option>
          <el-option label="配置键（唯一）" value="configKey"></el-option>
          <el-option label="参数值" value="configValue"></el-option>
          <el-option label="备注" value="remark"></el-option>
          <el-option label="创建时间" value="createTime"></el-option>
          <el-option label="修改时间" value="updateTime"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select size="mini" v-model="params.asc" placeholder="排序方式" style="width: 80px">
          <el-option label="正序" :value="true"/>
          <el-option label="逆序" :value="false"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="params.text" prefix-icon="el-icon-search" size="mini"
                  @keyup.enter.native="queryPage()" placeholder="请输入查询内容"/>
      </el-form-item>
      <el-form-item>
        <el-button size="mini" type="success" @click="queryPage()" icon="el-icon-search">查询</el-button>
        <el-button size="mini" type="primary" @click="showAddDialog({})" icon="el-icon-plus" v-if="havePermission('system:config:add')">新增</el-button>
      </el-form-item>
    </el-form>

    <!-- 分割线 -->

    <!-- 内容区域 -->
    <el-table v-loading="loadingTable" :data="records" highlight-current-row element-loading-text="加载中..." border fit>
      <el-table-column align="center" label="configId" prop="configId"></el-table-column>
      <el-table-column align="center" label="类别Y:系统内置,N其他" prop="configType"></el-table-column>
      <el-table-column align="center" label="参数名称（中文）" prop="configName"></el-table-column>
      <el-table-column align="center" label="配置键（唯一）" prop="configKey"></el-table-column>
      <el-table-column align="center" label="参数值" prop="configValue"></el-table-column>
      <el-table-column align="center" label="备注" prop="remark"></el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime"></el-table-column>
      <el-table-column align="center" label="修改时间" prop="updateTime"></el-table-column>
      <el-table-column align="center" label="操作"  min-width="120" fixed="right"  v-if="haveAnyPermission(['system:config:mod','system:config:del'])">
        <template slot-scope="scope">
          <el-button @click="showEditDialog(scope.row.configId)" size="mini" type="success" icon="el-icon-edit" :loading="loadingEdit"  v-if="havePermission('system:config:mod')" />
          <el-button @click="removeOne(scope.row.configId)" size="mini" type="danger" icon="el-icon-delete" v-if="havePermission('system:config:del')"/>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="padding-top: 10px"
                   :current-page="params.current" :page-size="params.size"
                   @size-change="params.size=$event;queryPage()" @current-change="queryPage"
                   :page-sizes="[2, 6, 10, 20, 30, 50]"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total" background/>
  </div>
</template>

<script>
import elDragDialog from '@/directive/el-drag-dialog'
import request from '@/api/system/config'
import common from "@/mixins/common";

export default {
  name: "Config",
  mixins: [common],
  directives: { elDragDialog },
  data() {
    return {
      rule:{
        configType:[{required:true,message:"类别Y:系统内置,N其他必须填写",trigger:'blur'}],
        configName:[{required:true,message:"参数名称（中文）必须填写",trigger:'blur'}],
        configKey:[{required:true,message:"配置键（唯一）必须填写",trigger:'blur'}],
        configValue:[{required:true,message:"参数值必须填写",trigger:'blur'}],
        remark:[{required:false,message:"备注必须填写",trigger:'blur'}],
      },
      request: request,
    }
  },
  methods: {},
  created() {
  }
}
</script>

<style>

</style>
