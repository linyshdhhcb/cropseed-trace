<template>
    <div class="app-container">
        <!-- 时间筛选 -->
        <el-card class="filter-card" shadow="never">
            <el-form :model="filterForm" :inline="true" class="filter-form">
                <el-form-item label="时间范围">
                    <el-date-picker v-model="filterForm.dateRange" type="daterange" range-separator="至"
                        start-placeholder="开始日期" end-placeholder="结束日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
                        @change="handleDateRangeChange" />
                </el-form-item>
                <el-form-item label="统计维度">
                    <el-select v-model="filterForm.dimension" @change="handleDimensionChange" style="width: 80px">
                        <el-option label="按日" value="day" />
                        <el-option label="按周" value="week" />
                        <el-option label="按月" value="month" />
                        <el-option label="按年" value="year" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" :icon="Search" @click="handleRefresh">
                        刷新数据
                    </el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <!-- 核心指标卡片 -->
        <el-row :gutter="20" class="metrics-row">
            <el-col :span="6">
                <el-card class="metric-card" shadow="hover">
                    <div class="metric-content">
                        <div class="metric-icon total-orders">
                            <el-icon>
                                <Document />
                            </el-icon>
                        </div>
                        <div class="metric-info">
                            <div class="metric-value">{{ statisticsData.totalOrders }}</div>
                            <div class="metric-label">总订单数</div>
                            <div class="metric-trend" :class="getTrendClass(statisticsData.ordersTrend)">
                                <el-icon>
                                    <component :is="getTrendIcon(statisticsData.ordersTrend)" />
                                </el-icon>
                                {{ Math.abs(statisticsData.ordersTrend || 0) }}%
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card class="metric-card" shadow="hover">
                    <div class="metric-content">
                        <div class="metric-icon total-revenue">
                            <el-icon>
                                <Money />
                            </el-icon>
                        </div>
                        <div class="metric-info">
                            <div class="metric-value">{{ formatMoney(statisticsData.totalRevenue) }}</div>
                            <div class="metric-label">总销售额</div>
                            <div class="metric-trend" :class="getTrendClass(statisticsData.revenueTrend)">
                                <el-icon>
                                    <component :is="getTrendIcon(statisticsData.revenueTrend)" />
                                </el-icon>
                                {{ Math.abs(statisticsData.revenueTrend || 0) }}%
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card class="metric-card" shadow="hover">
                    <div class="metric-content">
                        <div class="metric-icon total-users">
                            <el-icon>
                                <User />
                            </el-icon>
                        </div>
                        <div class="metric-info">
                            <div class="metric-value">{{ statisticsData.totalUsers }}</div>
                            <div class="metric-label">总用户数</div>
                            <div class="metric-trend" :class="getTrendClass(statisticsData.usersTrend)">
                                <el-icon>
                                    <component :is="getTrendIcon(statisticsData.usersTrend)" />
                                </el-icon>
                                {{ Math.abs(statisticsData.usersTrend || 0) }}%
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card class="metric-card" shadow="hover">
                    <div class="metric-content">
                        <div class="metric-icon total-seeds">
                            <el-icon>
                                <Grape />
                            </el-icon>
                        </div>
                        <div class="metric-info">
                            <div class="metric-value">{{ statisticsData.totalSeeds }}</div>
                            <div class="metric-label">种子品种数</div>
                            <div class="metric-trend" :class="getTrendClass(statisticsData.seedsTrend)">
                                <el-icon>
                                    <component :is="getTrendIcon(statisticsData.seedsTrend)" />
                                </el-icon>
                                {{ Math.abs(statisticsData.seedsTrend || 0) }}%
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 图表区域 -->
        <el-row :gutter="20" class="charts-row">
            <!-- 销售趋势图 -->
            <el-col :span="12">
                <el-card class="chart-card" shadow="never">
                    <template #header>
                        <div class="chart-header">
                            <span>销售趋势</span>
                            <el-button-group>
                                <el-button v-for="type in salesChartTypes" :key="type.value"
                                    :type="salesChartType === type.value ? 'primary' : 'default'" size="small"
                                    @click="handleSalesChartTypeChange(type.value)">
                                    {{ type.label }}
                                </el-button>
                            </el-button-group>
                        </div>
                    </template>
                    <div class="chart-container">
                        <v-chart :option="salesChartOption" style="height: 300px; width: 100%"
                            v-loading="salesChartLoading" />
                    </div>
                </el-card>
            </el-col>

            <!-- 订单状态分布 -->
            <el-col :span="12">
                <el-card class="chart-card" shadow="never">
                    <template #header>
                        <span>订单状态分布</span>
                    </template>
                    <div class="chart-container">
                        <v-chart :option="orderStatusChartOption" style="height: 300px; width: 100%"
                            v-loading="orderStatusChartLoading" />
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20" class="charts-row">
            <!-- 用户增长趋势 -->
            <el-col :span="12">
                <el-card class="chart-card" shadow="never">
                    <template #header>
                        <span>用户增长趋势</span>
                    </template>
                    <div class="chart-container">
                        <v-chart :option="userGrowthChartOption" style="height: 300px; width: 100%"
                            v-loading="userGrowthChartLoading" />
                    </div>
                </el-card>
            </el-col>

            <!-- 种子品类销售排行 -->
            <el-col :span="12">
                <el-card class="chart-card" shadow="never">
                    <template #header>
                        <span>种子品类销售排行</span>
                    </template>
                    <div class="chart-container">
                        <v-chart :option="categoryRankingChartOption" style="height: 300px; width: 100%"
                            v-loading="categoryRankingChartLoading" />
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 详细数据表格 -->
        <el-card class="table-card" shadow="never">
            <template #header>
                <div class="table-header">
                    <span>详细数据</span>
                    <el-button type="primary" :icon="Download" @click="handleExportData">
                        导出数据
                    </el-button>
                </div>
            </template>
            <el-table v-loading="tableLoading" :data="tableData" style="width: 100%" stripe>
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="orders" label="订单数" width="100" />
                <el-table-column prop="revenue" label="销售额" width="120">
                    <template #default="{ row }">
                        <span class="amount">¥{{ formatMoney(row.revenue) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="users" label="新增用户" width="100" />
                <el-table-column prop="seeds" label="种子品种" width="100" />
                <el-table-column prop="conversionRate" label="转化率" width="100">
                    <template #default="{ row }">
                        <span class="rate">{{ row.conversionRate }}%</span>
                    </template>
                </el-table-column>
                <el-table-column prop="avgOrderValue" label="客单价" width="120">
                    <template #default="{ row }">
                        <span class="amount">¥{{ formatMoney(row.avgOrderValue) }}</span>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage } from "element-plus";
import {
    Search,
    Download,
    Document,
    Money,
    User,
    Grape,
    TrendCharts,
    ArrowUp,
    ArrowDown,
} from "@element-plus/icons-vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { LineChart, BarChart, PieChart } from "echarts/charts";
import {
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import { getStatisticsData, getChartData, getTableData } from "@/api/statistics";
import { formatMoney } from "@/utils/index";

// 注册ECharts组件
use([
    CanvasRenderer,
    LineChart,
    BarChart,
    PieChart,
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent,
]);

// 筛选表单
const filterForm = reactive({
    dateRange: [],
    dimension: "day",
});

// 统计数据
const statisticsData = ref({
    totalOrders: 0,
    totalRevenue: 0,
    totalUsers: 0,
    totalSeeds: 0,
    ordersTrend: 0,
    revenueTrend: 0,
    usersTrend: 0,
    seedsTrend: 0,
});

// 表格数据
const tableLoading = ref(false);
const tableData = ref([]);

// 销售趋势图
const salesChartLoading = ref(false);
const salesChartType = ref("revenue");
const salesChartTypes = [
    { label: "销售额", value: "revenue" },
    { label: "订单数", value: "orders" },
    { label: "用户数", value: "users" },
];

// 其他图表加载状态
const orderStatusChartLoading = ref(false);
const userGrowthChartLoading = ref(false);
const categoryRankingChartLoading = ref(false);

// 销售趋势图配置
const salesChartOption = computed(() => ({
    title: {
        text: salesChartType.value === "revenue" ? "销售额趋势" :
            salesChartType.value === "orders" ? "订单数趋势" : "用户数趋势",
        left: "center",
        textStyle: {
            fontSize: 14,
            fontWeight: "normal",
        },
    },
    tooltip: {
        trigger: "axis",
        axisPointer: {
            type: "cross",
        },
    },
    legend: {
        data: ["当前", "同期"],
        bottom: 0,
    },
    grid: {
        left: "3%",
        right: "4%",
        bottom: "15%",
        containLabel: true,
    },
    xAxis: {
        type: "category",
        boundaryGap: false,
        data: salesChartData.value.labels,
    },
    yAxis: {
        type: "value",
        axisLabel: {
            formatter: salesChartType.value === "revenue" ? "¥{value}" : "{value}",
        },
    },
    series: [
        {
            name: "当前",
            type: "line",
            data: salesChartData.value.current,
            smooth: true,
            itemStyle: {
                color: "#409eff",
            },
        },
        {
            name: "同期",
            type: "line",
            data: salesChartData.value.previous,
            smooth: true,
            itemStyle: {
                color: "#67c23a",
            },
        },
    ],
}));

// 销售趋势图数据
const salesChartData = ref({
    labels: [],
    current: [],
    previous: [],
});

// 订单状态分布图配置
const orderStatusChartOption = computed(() => ({
    title: {
        text: "订单状态分布",
        left: "center",
        textStyle: {
            fontSize: 14,
            fontWeight: "normal",
        },
    },
    tooltip: {
        trigger: "item",
        formatter: "{a} <br/>{b}: {c} ({d}%)",
    },
    legend: {
        orient: "vertical",
        left: "left",
        data: orderStatusData.value.map(item => item.name),
    },
    series: [
        {
            name: "订单状态",
            type: "pie",
            radius: "50%",
            center: ["50%", "60%"],
            data: orderStatusData.value,
            emphasis: {
                itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: "rgba(0, 0, 0, 0.5)",
                },
            },
        },
    ],
}));

// 订单状态数据
const orderStatusData = ref([]);

// 用户增长趋势图配置
const userGrowthChartOption = computed(() => ({
    title: {
        text: "用户增长趋势",
        left: "center",
        textStyle: {
            fontSize: 14,
            fontWeight: "normal",
        },
    },
    tooltip: {
        trigger: "axis",
    },
    legend: {
        data: ["新增用户", "活跃用户"],
        bottom: 0,
    },
    grid: {
        left: "3%",
        right: "4%",
        bottom: "15%",
        containLabel: true,
    },
    xAxis: {
        type: "category",
        data: userGrowthData.value.labels,
    },
    yAxis: {
        type: "value",
    },
    series: [
        {
            name: "新增用户",
            type: "bar",
            data: userGrowthData.value.newUsers,
            itemStyle: {
                color: "#409eff",
            },
        },
        {
            name: "活跃用户",
            type: "bar",
            data: userGrowthData.value.activeUsers,
            itemStyle: {
                color: "#67c23a",
            },
        },
    ],
}));

// 用户增长数据
const userGrowthData = ref({
    labels: [],
    newUsers: [],
    activeUsers: [],
});

// 种子品类销售排行图配置
const categoryRankingChartOption = computed(() => ({
    title: {
        text: "种子品类销售排行",
        left: "center",
        textStyle: {
            fontSize: 14,
            fontWeight: "normal",
        },
    },
    tooltip: {
        trigger: "axis",
        axisPointer: {
            type: "shadow",
        },
    },
    grid: {
        left: "3%",
        right: "4%",
        bottom: "3%",
        containLabel: true,
    },
    xAxis: {
        type: "value",
    },
    yAxis: {
        type: "category",
        data: categoryRankingData.value.labels,
    },
    series: [
        {
            name: "销售额",
            type: "bar",
            data: categoryRankingData.value.values,
            itemStyle: {
                color: "#409eff",
            },
        },
    ],
}));

// 种子品类销售排行数据
const categoryRankingData = ref({
    labels: [],
    values: [],
});

// 获取统计数据
const loadStatisticsData = async () => {
    try {
        const response = await getStatisticsData({
            startDate: filterForm.dateRange[0],
            endDate: filterForm.dateRange[1],
            dimension: filterForm.dimension,
        });
        statisticsData.value = response.data;
    } catch (error) {
        console.error("获取统计数据失败:", error);
    }
};

// 获取图表数据
const loadChartData = async () => {
    try {
        // 销售趋势图
        salesChartLoading.value = true;
        const salesResponse = await getChartData({
            chartType: "sales",
            startDate: filterForm.dateRange[0],
            endDate: filterForm.dateRange[1],
            dimension: filterForm.dimension,
        });
        salesChartData.value = salesResponse.data;

        // 订单状态分布
        orderStatusChartLoading.value = true;
        const orderStatusResponse = await getChartData({
            chartType: "orderStatus",
            startDate: filterForm.dateRange[0],
            endDate: filterForm.dateRange[1],
        });
        orderStatusData.value = orderStatusResponse.data;

        // 用户增长趋势
        userGrowthChartLoading.value = true;
        const userGrowthResponse = await getChartData({
            chartType: "userGrowth",
            startDate: filterForm.dateRange[0],
            endDate: filterForm.dateRange[1],
            dimension: filterForm.dimension,
        });
        userGrowthData.value = userGrowthResponse.data;

        // 种子品类销售排行
        categoryRankingChartLoading.value = true;
        const categoryRankingResponse = await getChartData({
            chartType: "categoryRanking",
            startDate: filterForm.dateRange[0],
            endDate: filterForm.dateRange[1],
        });
        categoryRankingData.value = categoryRankingResponse.data;
    } catch (error) {
        console.error("获取图表数据失败:", error);
    } finally {
        salesChartLoading.value = false;
        orderStatusChartLoading.value = false;
        userGrowthChartLoading.value = false;
        categoryRankingChartLoading.value = false;
    }
};

// 获取表格数据
const loadTableData = async () => {
    try {
        tableLoading.value = true;
        const response = await getTableData({
            tableType: "statistics",
            startDate: filterForm.dateRange[0],
            endDate: filterForm.dateRange[1],
            dimension: filterForm.dimension,
        });
        tableData.value = response.data;
    } catch (error) {
        console.error("获取表格数据失败:", error);
    } finally {
        tableLoading.value = false;
    }
};

// 时间范围变化
const handleDateRangeChange = () => {
    loadAllData();
};

// 统计维度变化
const handleDimensionChange = () => {
    loadAllData();
};

// 销售图表类型变化
const handleSalesChartTypeChange = (type) => {
    salesChartType.value = type;
    loadChartData();
};

// 刷新数据
const handleRefresh = () => {
    loadAllData();
};

// 导出数据
const handleExportData = () => {
    ElMessage.info("导出功能开发中...");
};

// 加载所有数据
const loadAllData = () => {
    loadStatisticsData();
    loadChartData();
    loadTableData();
};

// 获取趋势图标
const getTrendIcon = (trend) => {
    return (trend || 0) >= 0 ? ArrowUp : ArrowDown;
};

// 获取趋势样式类
const getTrendClass = (trend) => {
    return (trend || 0) >= 0 ? "trend-up" : "trend-down";
};

// 初始化
onMounted(() => {
    // 设置默认时间范围为最近30天
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30);

    filterForm.dateRange = [
        startDate.toISOString().split('T')[0],
        endDate.toISOString().split('T')[0]
    ];

    loadAllData();
});
</script>

<style lang="scss" scoped>
.app-container {
    .filter-card {
        margin-bottom: 20px;

        .filter-form {
            margin-bottom: 0;
        }
    }

    .metrics-row {
        margin-bottom: 20px;

        .metric-card {
            .metric-content {
                display: flex;
                align-items: center;

                .metric-icon {
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-right: 20px;

                    .el-icon {
                        font-size: 24px;
                        color: #fff;
                    }

                    &.total-orders {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    }

                    &.total-revenue {
                        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                    }

                    &.total-users {
                        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                    }

                    &.total-seeds {
                        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
                    }
                }

                .metric-info {
                    flex: 1;

                    .metric-value {
                        font-size: 24px;
                        font-weight: 600;
                        color: #303133;
                        margin-bottom: 4px;
                    }

                    .metric-label {
                        font-size: 14px;
                        color: #909399;
                        margin-bottom: 8px;
                    }

                    .metric-trend {
                        display: flex;
                        align-items: center;
                        font-size: 12px;
                        font-weight: 500;

                        &.trend-up {
                            color: #67c23a;
                        }

                        &.trend-down {
                            color: #f56c6c;
                        }

                        .el-icon {
                            margin-right: 4px;
                        }
                    }
                }
            }
        }
    }

    .charts-row {
        margin-bottom: 20px;

        .chart-card {
            .chart-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .chart-container {
                height: 300px;
            }
        }
    }

    .table-card {
        .table-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .amount {
            color: #f56c6c;
            font-weight: 600;
        }

        .rate {
            color: #67c23a;
            font-weight: 600;
        }
    }
}
</style>
