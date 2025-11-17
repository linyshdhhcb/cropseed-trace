<template>
  <div class="payment-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>支付管理</span>
          <el-button type="primary" @click="refreshData">刷新</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择支付方式" clearable>
            <el-option label="微信支付" :value="1" />
            <el-option label="支付宝支付" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.orderStatus" placeholder="请选择支付状态" clearable>
            <el-option label="未支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="payableAmount" label="支付金额" width="120">
          <template #default="{ row }">
            <span class="amount">￥{{ row.payableAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.paymentMethod === 1" type="success">微信支付</el-tag>
            <el-tag v-else-if="row.paymentMethod === 2" type="primary">支付宝支付</el-tag>
            <el-tag v-else type="info">未选择</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="支付状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.orderStatus === 0" type="warning">未支付</el-tag>
            <el-tag v-else-if="row.orderStatus === 1" type="success">已支付</el-tag>
            <el-tag v-else-if="row.orderStatus === 2" type="primary">待发货</el-tag>
            <el-tag v-else-if="row.orderStatus === 3" type="info">已发货</el-tag>
            <el-tag v-else-if="row.orderStatus === 4" type="success">已完成</el-tag>
            <el-tag v-else-if="row.orderStatus === 5" type="danger">已取消</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="180">
          <template #default="{ row }">
            <span>{{ row.paymentTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="consignee" label="收货人" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
            <el-button 
              v-if="row.orderStatus === 0" 
              type="success" 
              size="small" 
              @click="handlePayment(row)"
            >
              模拟支付
            </el-button>
            <el-button 
              v-if="row.orderStatus === 0" 
              type="danger" 
              size="small" 
              @click="cancelOrder(row)"
            >
              取消订单
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 支付弹窗 -->
    <el-dialog v-model="paymentDialog" title="模拟支付" width="500px">
      <div class="payment-info">
        <p><strong>订单号：</strong>{{ currentOrder?.orderNo }}</p>
        <p><strong>支付金额：</strong><span class="amount">￥{{ currentOrder?.payableAmount }}</span></p>
        <p><strong>收货人：</strong>{{ currentOrder?.consignee }}</p>
      </div>
      
      <el-form :model="paymentForm" label-width="100px">
        <el-form-item label="支付方式">
          <el-radio-group v-model="paymentForm.paymentMethod">
            <el-radio :label="1">微信支付</el-radio>
            <el-radio :label="2">支付宝支付</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmPayment" :loading="paymentLoading">
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, payOrder, cancelOrder as cancelOrderApi } from '@/api/order'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const paymentDialog = ref(false)
const paymentLoading = ref(false)
const currentOrder = ref(null)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  paymentMethod: '',
  orderStatus: ''
})

// 支付表单
const paymentForm = reactive({
  paymentMethod: 1
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await getOrderList(params)
    
    if (response.success) {
      tableData.value = response.data.list || response.data.records || []
      pagination.total = parseInt(response.data.total) || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取订单数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchData()
}

// 重置搜索
function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  fetchData()
}

// 刷新数据
function refreshData() {
  fetchData()
}

// 分页大小改变
function handleSizeChange(val) {
  pagination.size = val
  pagination.page = 1
  fetchData()
}

// 当前页改变
function handleCurrentChange(val) {
  pagination.page = val
  fetchData()
}

// 查看详情
function viewDetail(row) {
  // 这里可以跳转到订单详情页面或打开详情弹窗
  ElMessage.info('查看订单详情功能待实现')
}

// 处理支付
function handlePayment(row) {
  currentOrder.value = row
  paymentForm.paymentMethod = 1
  paymentDialog.value = true
}

// 确认支付
async function confirmPayment() {
  if (!currentOrder.value) return
  
  paymentLoading.value = true
  try {
    const response = await payOrder(currentOrder.value.id, paymentForm.paymentMethod)
    
    if (response.success) {
      ElMessage.success('支付成功')
      paymentDialog.value = false
      fetchData() // 刷新数据
    } else {
      ElMessage.error(response.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  } finally {
    paymentLoading.value = false
  }
}

// 取消订单
async function cancelOrder(row) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await cancelOrderApi(row.id, '管理员取消')
    
    if (response.success) {
      ElMessage.success('订单已取消')
      fetchData() // 刷新数据
    } else {
      ElMessage.error(response.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消失败')
    }
  }
}
</script>

<style scoped>
.payment-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.amount {
  color: #e6a23c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.payment-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.payment-info p {
  margin: 8px 0;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
