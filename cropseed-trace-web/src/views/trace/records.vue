<template>
  <div class="trace-records">
    <div class="page-header">
      <h2>溯源记录管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新增记录
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-card>
        <el-form :model="searchForm" :inline="true" label-width="80px">
          <el-form-item label="溯源码">
            <el-input 
              v-model="searchForm.traceCode" 
              placeholder="请输入溯源码"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          
          <el-form-item label="批次ID">
            <el-input 
              v-model="searchForm.batchId" 
              placeholder="请输入批次ID"
              clearable
              style="width: 150px"
            />
          </el-form-item>

          <el-form-item label="记录类型">
            <el-select v-model="searchForm.recordType" placeholder="选择记录类型" clearable style="width: 150px">
              <el-option label="生产记录" :value="1" />
              <el-option label="质检记录" :value="2" />
              <el-option label="流通记录" :value="3" />
              <el-option label="销售记录" :value="4" />
              <el-option label="异常记录" :value="5" />
            </el-select>
          </el-form-item>

          <el-form-item label="上链状态">
            <el-select v-model="searchForm.blockchainStatus" placeholder="选择上链状态" clearable style="width: 150px">
              <el-option label="未上链" :value="0" />
              <el-option label="上链中" :value="1" />
              <el-option label="已上链" :value="2" />
              <el-option label="上链失败" :value="3" />
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

    <!-- 操作按钮区域 -->
    <div class="operation-section">
      <el-button type="success" @click="batchUpload" :loading="batchUploading">
        <el-icon><Upload /></el-icon>
        批量上链
      </el-button>
      <el-button type="warning" @click="exportRecords">
        <el-icon><Download /></el-icon>
        导出数据
      </el-button>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-card>
        <el-table 
          :data="tableData" 
          v-loading="loading" 
          stripe 
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column prop="traceCode" label="溯源码" width="140" fixed="left">
            <template #default="{ row }">
              <el-button type="text" @click="viewTraceChain(row.traceCode)">
                {{ row.traceCode }}
              </el-button>
            </template>
          </el-table-column>

          <el-table-column prop="batchId" label="批次ID" width="100" />

          <el-table-column prop="recordType" label="记录类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getRecordTypeColor(row.recordType)">
                {{ getRecordTypeText(row.recordType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="recordStage" label="记录阶段" width="100" />

          <el-table-column prop="operatorName" label="操作员" width="100" />

          <el-table-column prop="recordTime" label="记录时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.recordTime) }}
            </template>
          </el-table-column>

          <el-table-column prop="location" label="位置" width="150" show-overflow-tooltip />

          <el-table-column prop="contentSummary" label="内容摘要" show-overflow-tooltip />

          <el-table-column prop="blockchainStatus" label="上链状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getBlockchainStatusType(row.blockchainStatus)">
                {{ getBlockchainStatusText(row.blockchainStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
              <el-button type="success" size="small" @click="editRecord(row)">编辑</el-button>
              <el-button 
                v-if="row.blockchainStatus !== 2" 
                type="warning" 
                size="small" 
                @click="uploadToBlockchain(row)"
              >
                上链
              </el-button>
              <el-popconfirm title="确定删除此记录吗？" @confirm="deleteRecord(row)">
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
      <TraceRecordForm 
        :record="currentRecord" 
        @success="handleFormSuccess" 
        @cancel="showCreateDialog = false"
      />
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="溯源记录详情" v-model="showDetailDialog" width="900px">
      <TraceRecordDetail :record="detailRecord" />
    </el-dialog>

    <!-- 溯源链对话框 -->
    <el-dialog title="完整溯源链" v-model="showChainDialog" width="1200px">
      <TraceChainView :trace-code="currentTraceCode" />
    </el-dialog>
  </div>
</template>

<script>
import { Plus, Search, Upload, Download } from '@element-plus/icons-vue'
import { 
  getTraceRecordsPage, 
  deleteTraceRecord, 
  batchUploadToBlockchain,
  uploadRecordToBlockchain
} from '@/api/trace'
import { ElMessage, ElMessageBox } from 'element-plus'
import TraceRecordForm from './components/TraceRecordForm.vue'
import TraceRecordDetail from './components/TraceRecordDetail.vue'
import TraceChainView from './components/TraceChainView.vue'

export default {
  name: 'TraceRecords',
  components: {
    Plus,
    Search,
    Upload,
    Download,
    TraceRecordForm,
    TraceRecordDetail,
    TraceChainView
  },
  data() {
    return {
      loading: false,
      batchUploading: false,
      tableData: [],
      selectedRows: [],
      
      // 搜索表单
      searchForm: {
        traceCode: '',
        batchId: '',
        recordType: null,
        blockchainStatus: null
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
      showChainDialog: false,
      
      // 当前操作的记录
      currentRecord: null,
      detailRecord: null,
      currentTraceCode: ''
    }
  },
  computed: {
    dialogTitle() {
      return this.currentRecord ? '编辑溯源记录' : '新增溯源记录'
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

        const response = await getTraceRecordsPage(params)
        if (response.code === 200) {
          this.tableData = response.data.list || []
          this.pagination.total = response.data.total || 0
        } else {
          ElMessage.error(response.message || '加载数据失败')
        }
      } catch (error) {
        console.error('加载数据失败', error)
        ElMessage.error('加载数据失败')
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
        traceCode: '',
        batchId: '',
        recordType: null,
        blockchainStatus: null
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

    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 查看详情
    viewDetail(record) {
      this.detailRecord = record
      this.showDetailDialog = true
    },

    // 查看溯源链
    viewTraceChain(traceCode) {
      this.currentTraceCode = traceCode
      this.showChainDialog = true
    },

    // 编辑记录
    editRecord(record) {
      this.currentRecord = { ...record }
      this.showCreateDialog = true
    },

    // 删除记录
    async deleteRecord(record) {
      try {
        const response = await deleteTraceRecord(record.id)
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

    // 单个上链
    async uploadToBlockchain(record) {
      try {
        const response = await uploadRecordToBlockchain(record.id)
        if (response.code === 200) {
          ElMessage.success('上链请求已提交，请稍后查看上链状态')
          // 延迟刷新以便看到状态变化
          setTimeout(() => {
            this.loadTableData()
          }, 1000)
        } else {
          ElMessage.error(response.message || '上链失败')
        }
      } catch (error) {
        console.error('上链失败', error)
        ElMessage.error('上链失败')
      }
    },

    // 批量上链
    async batchUpload() {
      if (this.selectedRows.length === 0) {
        // 如果没有选择记录，则处理所有未上链的记录
        try {
          await ElMessageBox.confirm(
            '将处理所有未上链的记录，是否继续？',
            '批量上链确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
        } catch {
          return
        }
      }

      this.batchUploading = true
      try {
        const response = await batchUploadToBlockchain(100)
        if (response.code === 200) {
          ElMessage.success(`批量上链完成，成功处理 ${response.data} 条记录`)
          this.loadTableData()
        } else {
          ElMessage.error(response.message || '批量上链失败')
        }
      } catch (error) {
        console.error('批量上链失败', error)
        ElMessage.error('批量上链失败')
      } finally {
        this.batchUploading = false
      }
    },

    // 导出数据
    exportRecords() {
      ElMessage.info('导出功能开发中...')
    },

    // 表单提交成功
    handleFormSuccess() {
      this.showCreateDialog = false
      this.currentRecord = null
      this.loadTableData()
    },

    // 工具方法
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    getRecordTypeText(type) {
      const typeMap = {
        1: '生产记录',
        2: '质检记录', 
        3: '流通记录',
        4: '销售记录',
        5: '异常记录'
      }
      return typeMap[type] || '未知'
    },

    getRecordTypeColor(type) {
      const colorMap = {
        1: 'success',
        2: 'warning',
        3: 'info',
        4: 'primary',
        5: 'danger'
      }
      return colorMap[type] || ''
    },

    getBlockchainStatusType(status) {
      const statusMap = {
        0: 'info',    // 未上链
        1: 'warning', // 上链中
        2: 'success', // 上链成功
        3: 'danger'   // 上链失败
      }
      return statusMap[status] || 'info'
    },

    getBlockchainStatusText(status) {
      const statusMap = {
        0: '未上链',
        1: '上链中',
        2: '已上链',
        3: '上链失败'
      }
      return statusMap[status] || '未知'
    }
  }
}
</script>

<style scoped>
.trace-records {
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

.operation-section {
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
  
  .operation-section {
    text-align: center;
  }
}
</style>
