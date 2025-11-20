<template>
    <div class="dashboard-container">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :sm="12" :md="6" :lg="6">
                <el-card class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon user">
                            <el-icon>
                                <User />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ statsData.userCount }}</div>
                            <div class="stats-label">用户总数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6" :lg="6">
                <el-card class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon seed">
                            <el-icon>
                                <Grape />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ statsData.seedCount }}</div>
                            <div class="stats-label">种子种类</div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6" :lg="6">
                <el-card class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon inventory">
                            <el-icon>
                                <Box />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ statsData.inventoryCount }}</div>
                            <div class="stats-label">库存总量</div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6" :lg="6">
                <el-card class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon order">
                            <el-icon>
                                <Document />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ statsData.orderCount }}</div>
                            <div class="stats-label">订单总数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 图表区域 -->
        <el-row :gutter="20" class="chart-row">
            <el-col :xs="24" :sm="24" :md="12" :lg="12">
                <el-card class="chart-card">
                    <template #header>
                        <div class="card-header">
                            <span>销售趋势</span>
                            <el-button type="text" @click="refreshSalesChart">刷新</el-button>
                        </div>
                    </template>
                    <div class="chart-container">
                        <v-chart :option="salesChartOption" style="height: 300px;" />
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="24" :md="12" :lg="12">
                <el-card class="chart-card">
                    <template #header>
                        <div class="card-header">
                            <span>库存分布</span>
                            <el-button type="text" @click="refreshInventoryChart">刷新</el-button>
                        </div>
                    </template>
                    <div class="chart-container">
                        <v-chart :option="inventoryChartOption" style="height: 300px;" />
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 数据表格 -->
        <el-row :gutter="20" class="table-row">
            <el-col :xs="24" :sm="24" :md="12" :lg="12">
                <el-card class="table-card">
                    <template #header>
                        <div class="card-header">
                            <span>最新订单</span>
                            <el-button type="text" @click="goToOrderList">查看全部</el-button>
                        </div>
                    </template>
                    <el-table :data="recentOrders" style="width: 100%">
                        <el-table-column prop="orderNo" label="订单号" width="120" />
                        <el-table-column prop="customerName" label="客户" width="100" />
                        <el-table-column prop="amount" label="金额" width="80">
                            <template #default="{ row }">
                                <span class="amount">¥{{ row.amount }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="status" label="状态" width="80">
                            <template #default="{ row }">
                                <el-tag :type="getOrderStatusType(row.status)" size="small">
                                    {{ getOrderStatusText(row.status) }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="createTime" label="时间" width="120" />
                    </el-table>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="24" :md="12" :lg="12">
                <el-card class="table-card">
                    <template #header>
                        <div class="card-header">
                            <span>库存预警</span>
                            <el-button type="text" @click="goToInventoryList">查看全部</el-button>
                        </div>
                    </template>
                    <el-table :data="lowStockItems" style="width: 100%">
                        <el-table-column prop="seedName" label="种子名称" width="120" />
                        <el-table-column prop="currentStock" label="当前库存" width="80" />
                        <el-table-column prop="minStock" label="最低库存" width="80" />
                        <el-table-column prop="status" label="状态" width="80">
                            <template #default="{ row }">
                                <el-tag :type="row.currentStock <= row.minStock ? 'danger' : 'warning'" size="small">
                                    {{ row.currentStock <= row.minStock ? '缺货' : '预警' }} </el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import {
    getDashboardOverview,
    getRecentOrders,
    getLowStockItems,
    getDashboardSalesTrend,
    getDashboardInventoryDistribution
} from '@/api/statistics'

// 注册必要的组件
use([
    CanvasRenderer,
    LineChart,
    PieChart,
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent
])

const router = useRouter()

// 统计数据
const statsData = reactive({
    userCount: 0,
    seedCount: 0,
    inventoryCount: 0,
    orderCount: 0
})

// 最新订单
const recentOrders = ref([])

// 库存预警
const lowStockItems = ref([])

// 销售趋势图表配置
const salesChartOption = ref({
    title: {
        text: '近7天销售趋势',
        left: 'center'
    },
    tooltip: {
        trigger: 'axis'
    },
    xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
        type: 'value'
    },
    series: [{
        data: [120, 200, 150, 80, 70, 110, 130],
        type: 'line',
        smooth: true,
        areaStyle: {}
    }]
})

// 库存分布图表配置
const inventoryChartOption = ref({
    title: {
        text: '库存分布',
        left: 'center'
    },
    tooltip: {
        trigger: 'item'
    },
    series: [{
        type: 'pie',
        radius: '50%',
        data: [
            { value: 1048, name: '玉米种子' },
            { value: 735, name: '水稻种子' },
            { value: 580, name: '小麦种子' },
            { value: 484, name: '蔬菜种子' },
            { value: 300, name: '其他' }
        ],
        emphasis: {
            itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
        }
    }]
})

// 获取统计数据
const loadStatsData = async () => {
    try {
        const response = await getDashboardOverview()
        if (response.code === 200 && response.data) {
            const data = response.data
            statsData.userCount = data.totalUsers || 0
            statsData.seedCount = data.totalSeeds || 0
            statsData.inventoryCount = data.totalInventory || 0
            statsData.orderCount = data.totalOrders || 0
        } else {
            // 如果API返回失败，使用默认值0
            statsData.userCount = 0
            statsData.seedCount = 0
            statsData.inventoryCount = 0
            statsData.orderCount = 0
        }
    } catch (error) {
        console.error('加载统计数据失败:', error)
        ElMessage.warning('获取统计数据失败')
        // 使用默认值0
        statsData.userCount = 0
        statsData.seedCount = 0
        statsData.inventoryCount = 0
        statsData.orderCount = 0
    }
}

// 获取最新订单
const loadRecentOrders = async () => {
    try {
        const response = await getRecentOrders(5)
        if (response.code === 200 && response.data && response.data.data) {
            recentOrders.value = response.data.data.map(order => ({
                orderNo: order.orderNo || order.order_no,
                customerName: order.customerName || order.customer_name,
                amount: order.amount || order.paid_amount,
                status: order.status || order.order_status,
                createTime: order.createTime || order.create_time
            }))
        } else {
            // 没有数据时显示空列表
            recentOrders.value = []
        }
    } catch (error) {
        console.error('获取最新订单失败:', error)
        ElMessage.warning('获取最新订单失败')
        // 没有数据时显示空列表
        recentOrders.value = []
    }
}

// 获取库存预警
const loadLowStockItems = async () => {
    try {
        const response = await getLowStockItems(4)
        if (response.code === 200 && response.data) {
            lowStockItems.value = response.data.map(item => ({
                seedName: item.seedName || item.seed_name || item.productName,
                currentStock: item.currentStock || item.current_stock || item.quantity,
                minStock: item.minStock || item.min_stock || item.safetyStock
            }))
        } else {
            // 没有数据时显示空列表
            lowStockItems.value = []
        }
    } catch (error) {
        console.error('获取库存预警失败:', error)
        ElMessage.warning('获取库存预警失败')
        // 没有数据时显示空列表
        lowStockItems.value = []
    }
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
    const statusMap = {
        1: 'warning',
        2: 'success',
        3: 'info',
        4: 'danger'
    }
    return statusMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
    const statusMap = {
        1: '待支付',
        2: '已支付',
        3: '已发货',
        4: '已完成'
    }
    return statusMap[status] || '未知'
}

// 刷新销售图表
const refreshSalesChart = async () => {
    try {
        const response = await getDashboardSalesTrend()
        if (response.code === 200 && response.data) {
            const data = response.data
            salesChartOption.value = {
                ...salesChartOption.value,
                xAxis: {
                    type: 'category',
                    data: data.map(item => item.date || item.day)
                },
                series: [{
                    data: data.map(item => item.sales || item.amount),
                    type: 'line',
                    smooth: true,
                    areaStyle: {}
                }]
            }
        }
        ElMessage.success('销售图表刷新成功')
    } catch (error) {
        console.error('刷新销售图表失败:', error)
        ElMessage.warning('刷新销售图表失败，保持原有数据')
    }
}

// 刷新库存图表
const refreshInventoryChart = async () => {
    try {
        const response = await getDashboardInventoryDistribution()
        if (response.code === 200 && response.data) {
            const data = response.data
            inventoryChartOption.value = {
                ...inventoryChartOption.value,
                series: [{
                    type: 'pie',
                    radius: '50%',
                    data: data.map(item => ({
                        value: item.value || item.quantity || item.count,
                        name: item.name || item.category || item.seed_name
                    })),
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }]
            }
        }
        ElMessage.success('库存图表刷新成功')
    } catch (error) {
        console.error('刷新库存图表失败:', error)
        ElMessage.warning('刷新库存图表失败，保持原有数据')
    }
}

// 跳转到订单列表
const goToOrderList = () => {
    router.push('/order/list')
}

// 跳转到库存列表
const goToInventoryList = () => {
    router.push('/inventory/stock')
}

// 初始化销售图表数据
const loadSalesChartData = async () => {
    try {
        const response = await getDashboardSalesTrend()
        if (response.code === 200 && response.data) {
            const data = response.data
            salesChartOption.value = {
                ...salesChartOption.value,
                xAxis: {
                    type: 'category',
                    data: data.map(item => item.date || item.day)
                },
                series: [{
                    data: data.map(item => item.sales || item.amount),
                    type: 'line',
                    smooth: true,
                    areaStyle: {}
                }]
            }
        }
    } catch (error) {
        console.error('加载销售图表数据失败:', error)
        // 保持默认的模拟数据
    }
}

// 初始化库存图表数据
const loadInventoryChartData = async () => {
    try {
        const response = await getDashboardInventoryDistribution()
        if (response.code === 200 && response.data) {
            const data = response.data
            inventoryChartOption.value = {
                ...inventoryChartOption.value,
                series: [{
                    type: 'pie',
                    radius: '50%',
                    data: data.map(item => ({
                        value: item.value || item.quantity || item.count,
                        name: item.name || item.category || item.seed_name
                    })),
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }]
            }
        }
    } catch (error) {
        console.error('加载库存图表数据失败:', error)
        // 保持默认的模拟数据
    }
}

// 初始化
onMounted(() => {
    loadStatsData()
    loadRecentOrders()
    loadLowStockItems()
    loadSalesChartData()
    loadInventoryChartData()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
    .stats-row {
        margin-bottom: 20px;

        .stats-card {
            .stats-content {
                display: flex;
                align-items: center;

                .stats-icon {
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-right: 20px;

                    &.user {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                    }

                    &.seed {
                        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                        color: white;
                    }

                    &.inventory {
                        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                        color: white;
                    }

                    &.order {
                        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
                        color: white;
                    }

                    .el-icon {
                        font-size: 24px;
                    }
                }

                .stats-info {
                    .stats-value {
                        font-size: 28px;
                        font-weight: 600;
                        color: #333;
                        margin-bottom: 5px;
                    }

                    .stats-label {
                        font-size: 14px;
                        color: #666;
                    }
                }
            }
        }
    }

    .chart-row {
        margin-bottom: 20px;

        .chart-card {
            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .chart-container {
                width: 100%;
            }
        }
    }

    .table-row {
        .table-card {
            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .amount {
                color: #f56c6c;
                font-weight: 600;
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .dashboard-container {
        .stats-row {
            .stats-card {
                margin-bottom: 10px;
            }
        }

        .chart-row,
        .table-row {
            .el-col {
                margin-bottom: 20px;
            }
        }
    }
}
</style>
