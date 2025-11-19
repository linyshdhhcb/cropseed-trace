<template>
  <div class="trace-entities">
    <div class="page-header">
      <h2>溯源实体管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新增实体
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-card>
        <el-form :model="searchForm" :inline="true" label-width="80px">
          <el-form-item label="实体名称">
            <el-input 
              v-model="searchForm.entityName" 
              placeholder="请输入实体名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          
          <el-form-item label="实体类型">
            <el-select v-model="searchForm.entityType" placeholder="选择实体类型" clearable style="width: 150px">
              <el-option label="生产基地" :value="1" />
              <el-option label="加工厂" :value="2" />
              <el-option label="仓库" :value="3" />
              <el-option label="经销商" :value="4" />
              <el-option label="零售商" :value="5" />
            </el-select>
          </el-form-item>

          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-card>
        <el-table 
          :data="tableData" 
          v-loading="loading" 
          stripe 
          style="width: 100%"
        >
          <el-table-column prop="entityCode" label="实体编码" width="120" />
          
          <el-table-column prop="entityName" label="实体名称" width="200" show-overflow-tooltip />

          <el-table-column prop="entityType" label="实体类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getEntityTypeColor(row.entityType)">
                {{ getEntityTypeText(row.entityType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="legalPerson" label="法人代表" width="100" />

          <el-table-column prop="contactPerson" label="联系人" width="100" />

          <el-table-column prop="contactPhone" label="联系电话" width="130" />

          <el-table-column prop="address" label="地址" show-overflow-tooltip />

          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(row)"
              />
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
              <el-button type="success" size="small" @click="editEntity(row)">编辑</el-button>
              <el-popconfirm title="确定删除此实体吗？" @confirm="deleteEntity(row)">
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            :hide-on-single-page="false"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="showCreateDialog" 
      width="800px"
      :close-on-click-modal="false"
    >
      <TraceEntityForm 
        :entity="currentEntity" 
        @success="handleFormSuccess" 
        @cancel="showCreateDialog = false"
      />
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="实体详情" v-model="showDetailDialog" width="900px">
      <TraceEntityDetail :entity="detailEntity" />
    </el-dialog>
  </div>
</template>

<script>
import { Plus, Search } from '@element-plus/icons-vue'
import { 
  getTraceEntities, 
  deleteTraceEntity, 
  updateTraceEntity 
} from '@/api/trace'
import { ElMessage } from 'element-plus'
import TraceEntityForm from './components/TraceEntityForm.vue'
import TraceEntityDetail from './components/TraceEntityDetail.vue'

export default {
  name: 'TraceEntities',
  components: {
    Plus,
    Search,
    TraceEntityForm,
    TraceEntityDetail
  },
  data() {
    return {
      loading: false,
      tableData: [],
      
      // 搜索表单
      searchForm: {
        entityName: '',
        entityType: null,
        status: null
      },

      // 分页
      pagination: {
        current: 1,
        size: 20,
        total: 0
      },

      // 对话框控制
      showCreateDialog: false,
      showDetailDialog: false,
      
      // 当前操作的实体
      currentEntity: null,
      detailEntity: null
    }
  },
  computed: {
    dialogTitle() {
      return this.currentEntity ? '编辑溯源实体' : '新增溯源实体'
    }
  },
  mounted() {
    this.loadTableData()
  },
  methods: {
    async loadTableData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.current,
          pageSize: this.pagination.size,
          ...this.searchForm
        }
        
        // 过滤空值
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null) {
            delete params[key]
          }
        })

        const response = await getTraceEntities(params)
        if (response.code === 200) {
          this.tableData = response.data.list || []
          this.pagination.total = response.data.total || 0
        } else {
          ElMessage.error(response.message || '加载数据失败')
        }
      } catch (error) {
        console.error('加载数据失败', error)
        ElMessage.error('加载数据失败')
        // 使用模拟数据
        this.tableData = [
          {
            id: 1,
            entityCode: 'FARM001',
            entityName: '北京优质农业基地',
            entityType: 1,
            legalPerson: '张农民',
            contactPerson: '李联系',
            contactPhone: '13800138001',
            address: '北京市通州区农业示范园区',
            status: 1,
            createTime: new Date()
          },
          {
            id: 2,
            entityCode: 'PROC001',
            entityName: '绿色种子加工厂',
            entityType: 2,
            legalPerson: '王加工',
            contactPerson: '赵联系',
            contactPhone: '13800138002',
            address: '北京市大兴区工业园区',
            status: 1,
            createTime: new Date()
          }
        ]
        this.pagination.total = this.tableData.length
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      this.pagination.current = 1
      this.loadTableData()
    },

    resetSearch() {
      this.searchForm = {
        entityName: '',
        entityType: null,
        status: null
      }
      this.pagination.current = 1
      this.loadTableData()
    },

    handleSizeChange() {
      this.pagination.current = 1
      this.loadTableData()
    },

    handleCurrentChange() {
      this.loadTableData()
    },

    // 查看详情
    viewDetail(entity) {
      this.detailEntity = entity
      this.showDetailDialog = true
    },

    // 编辑实体
    editEntity(entity) {
      this.currentEntity = { ...entity }
      this.showCreateDialog = true
    },

    // 删除实体
    async deleteEntity(entity) {
      try {
        const response = await deleteTraceEntity(entity.id)
        if (response.code === 200) {
          ElMessage.success('删除成功')
          this.loadTableData()
        } else {
          ElMessage.error(response.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败', error)
        ElMessage.error('删除失败')
      }
    },

    // 状态切换
    async handleStatusChange(entity) {
      try {
        const response = await updateTraceEntity(entity.id, { status: entity.status })
        if (response.code === 200) {
          ElMessage.success('状态更新成功')
        } else {
          ElMessage.error(response.message || '状态更新失败')
          // 恢复原状态
          entity.status = entity.status === 1 ? 0 : 1
        }
      } catch (error) {
        console.error('状态更新失败', error)
        ElMessage.error('状态更新失败')
        // 恢复原状态
        entity.status = entity.status === 1 ? 0 : 1
      }
    },

    // 表单提交成功
    handleFormSuccess() {
      this.showCreateDialog = false
      this.currentEntity = null
      this.loadTableData()
    },

    // 工具方法
    getEntityTypeText(type) {
      const typeMap = {
        1: '生产基地',
        2: '加工厂',
        3: '仓库',
        4: '经销商',
        5: '零售商'
      }
      return typeMap[type] || '未知'
    },

    getEntityTypeColor(type) {
      const colorMap = {
        1: 'success',
        2: 'warning',
        3: 'info',
        4: 'primary',
        5: ''
      }
      return colorMap[type] || ''
    }
  }
}
</script>

<style scoped>
.trace-entities {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.search-section {
  margin-bottom: 20px;
}

.table-section {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-section .el-form {
    flex-direction: column;
  }
}
</style>
