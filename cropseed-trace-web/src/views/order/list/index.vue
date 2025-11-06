<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="订单号" prop="orderNo">
                <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="客户名称" prop="customerName">
                <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="订单状态" prop="orderStatus">
                <el-select v-model="searchForm.orderStatus" placeholder="请选择订单状态" clearable style="width: 150px">
                    <el-option label="待支付" :value="1" />
                    <el-option label="已支付" :value="2" />
                    <el-option label="已发货" :value="3" />
                    <el-option label="已完成" :value="4" />
                    <el-option label="已取消" :value="5" />
                </el-select>
            </el-form-item>
            <el-form-item label="订单类型" prop="orderType">
                <el-select v-model="searchForm.orderType" placeholder="请选择订单类型" clearable style="width: 150px">
                    <el-option label="B端订单" :value="1" />
                    <el-option label="C端订单" :value="2" />
                </el-select>
            </el-form-item>
            <el-form-item label="创建时间" prop="createTimeRange">
                <el-date-picker v-model="searchForm.createTimeRange" type="daterange" range-separator="至"
                    start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" :icon="Search" @click="handleSearch">
                    搜索
                </el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            </el-form-item>
        </el-form>

        <!-- 操作按钮 -->
        <div class="button-group">
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
            <el-button type="warning" :icon="Refresh" @click="handleRefresh">
                刷新
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange"
                style="width: 100%">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="orderNo" label="订单号" width="150" />
                <el-table-column prop="customerName" label="客户名称" width="120" />
                <el-table-column prop="orderType" label="订单类型" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.orderType === 1 ? 'primary' : 'success'">
                            {{ row.orderType === 1 ? 'B端订单' : 'C端订单' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="orderStatus" label="订单状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getOrderStatusTagType(row.orderStatus)">
                            {{ getOrderStatusText(row.orderStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="totalAmount" label="订单金额" width="120">
                    <template #default="{ row }">
                        <span class="amount">¥{{ row.totalAmount }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="itemCount" label="商品数量" width="100" />
                <el-table-column prop="paymentMethod" label="支付方式" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getPaymentMethodTagType(row.paymentMethod)">
                            {{ getPaymentMethodText(row.paymentMethod) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="250" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            查看详情
                        </el-button>
                        <el-button v-if="row.orderStatus === 1" type="success" size="small" @click="handlePay(row)">
                            确认支付
                        </el-button>
                        <el-button v-if="row.orderStatus === 2" type="warning" size="small" @click="handleShip(row)">
                            发货
                        </el-button>
                        <el-button v-if="row.orderStatus === 3" type="info" size="small" @click="handleComplete(row)">
                            完成
                        </el-button>
                        <el-button v-if="[1, 2].includes(row.orderStatus)" type="danger" size="small"
                            @click="handleCancel(row)">
                            取消
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
                    :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- 订单详情对话框 -->
        <el-dialog v-model="detailDialogVisible" title="订单详情" width="1000px" @close="handleDetailDialogClose">
            <div v-if="orderDetail" class="order-detail">
                <!-- 订单基本信息 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <span>订单基本信息</span>
                    </template>
                    <el-row :gutter="20">
                        <el-col :span="8">
                            <div class="detail-item">
                                <label>订单号：</label>
                                <span>{{ orderDetail.orderNo }}</span>
                            </div>
                        </el-col>
                        <el-col :span="8">
                            <div class="detail-item">
                                <label>客户名称：</label>
                                <span>{{ orderDetail.customerName }}</span>
                            </div>
                        </el-col>
                        <el-col :span="8">
                            <div class="detail-item">
                                <label>订单状态：</label>
                                <el-tag :type="getOrderStatusTagType(orderDetail.orderStatus)">
                                    {{ getOrderStatusText(orderDetail.orderStatus) }}
                                </el-tag>
                            </div>
                        </el-col>
                    </el-row>
                    <el-row :gutter="20">
                        <el-col :span="8">
                            <div class="detail-item">
                                <label>订单金额：</label>
                                <span class="amount">¥{{ orderDetail.totalAmount }}</span>
                            </div>
                        </el-col>
                        <el-col :span="8">
                            <div class="detail-item">
                                <label>支付方式：</label>
                                <span>{{ getPaymentMethodText(orderDetail.paymentMethod) }}</span>
                            </div>
                        </el-col>
                        <el-col :span="8">
                            <div class="detail-item">
                                <label>创建时间：</label>
                                <span>{{ orderDetail.createTime }}</span>
                            </div>
                        </el-col>
                    </el-row>
                </el-card>

                <!-- 商品信息 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <span>商品信息</span>
                    </template>
                    <el-table :data="orderDetail.orderItems" style="width: 100%">
                        <el-table-column prop="seedName" label="商品名称" width="200" />
                        <el-table-column prop="seedCode" label="商品编码" width="150" />
                        <el-table-column prop="quantity" label="数量" width="100" />
                        <el-table-column prop="unitPrice" label="单价" width="100">
                            <template #default="{ row }">
                                <span class="amount">¥{{ row.unitPrice }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="totalPrice" label="小计" width="100">
                            <template #default="{ row }">
                                <span class="amount">¥{{ row.totalPrice }}</span>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>

                <!-- 收货信息 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <span>收货信息</span>
                    </template>
                    <el-row :gutter="20">
                        <el-col :span="12">
                            <div class="detail-item">
                                <label>收货人：</label>
                                <span>{{ orderDetail.consignee }}</span>
                            </div>
                        </el-col>
                        <el-col :span="12">
                            <div class="detail-item">
                                <label>联系电话：</label>
                                <span>{{ orderDetail.phone }}</span>
                            </div>
                        </el-col>
                    </el-row>
                    <el-row :gutter="20">
                        <el-col :span="24">
                            <div class="detail-item">
                                <label>收货地址：</label>
                                <span>{{ orderDetail.address }}</span>
                            </div>
                        </el-col>
                    </el-row>
                </el-card>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
    Search,
    Refresh,
    Download,
} from "@element-plus/icons-vue";
import {
    getOrderList,
    getOrderDetail,
    updateOrderStatus,
} from "@/api/order";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    orderNo: "",
    customerName: "",
    orderStatus: null,
    orderType: null,
    createTimeRange: null,
});

// 表格数据
const loading = ref(false);
const tableData = ref([]);
const multipleSelection = ref([]);

// 分页
const pagination = reactive({
    current: 1,
    size: 10,
    total: 0,
});

// 订单详情对话框
const detailDialogVisible = ref(false);
const orderDetail = ref(null);

// 获取订单列表
const loadOrderList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getOrderList(params);
        tableData.value = response.data.list;
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取订单列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadOrderList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadOrderList();
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getOrderDetail(row.id);
        orderDetail.value = response.data;
        detailDialogVisible.value = true;
    } catch (error) {
        console.error("获取订单详情失败:", error);
    }
};

// 确认支付
const handlePay = async (row) => {
    try {
        await ElMessageBox.confirm("确定要确认支付该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(row.id, 2);
        ElMessage.success("确认支付成功");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("确认支付失败:", error);
        }
    }
};

// 发货
const handleShip = async (row) => {
    try {
        await ElMessageBox.confirm("确定要发货该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(row.id, 3);
        ElMessage.success("发货成功");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("发货失败:", error);
        }
    }
};

// 完成订单
const handleComplete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要完成该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(row.id, 4);
        ElMessage.success("订单完成");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("完成订单失败:", error);
        }
    }
};

// 取消订单
const handleCancel = async (row) => {
    try {
        await ElMessageBox.confirm("确定要取消该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(row.id, 5);
        ElMessage.success("订单已取消");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("取消订单失败:", error);
        }
    }
};

// 导出数据
const handleExport = () => {
    ElMessage.info("导出功能开发中...");
};

// 刷新
const handleRefresh = () => {
    loadOrderList();
};

// 选择变化
const handleSelectionChange = (selection) => {
    multipleSelection.value = selection;
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadOrderList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadOrderList();
};

// 关闭详情对话框
const handleDetailDialogClose = () => {
    orderDetail.value = null;
};

// 获取订单状态标签类型
const getOrderStatusTagType = (status) => {
    const statusMap = {
        1: "warning",
        2: "success",
        3: "info",
        4: "success",
        5: "danger",
    };
    return statusMap[status] || "info";
};

// 获取订单状态文本
const getOrderStatusText = (status) => {
    const statusMap = {
        1: "待支付",
        2: "已支付",
        3: "已发货",
        4: "已完成",
        5: "已取消",
    };
    return statusMap[status] || "未知";
};

// 获取支付方式标签类型
const getPaymentMethodTagType = (method) => {
    const methodMap = {
        1: "primary",
        2: "success",
        3: "warning",
    };
    return methodMap[method] || "info";
};

// 获取支付方式文本
const getPaymentMethodText = (method) => {
    const methodMap = {
        1: "微信支付",
        2: "支付宝",
        3: "银行转账",
    };
    return methodMap[method] || "未知";
};

// 初始化
onMounted(() => {
    loadOrderList();
});
</script>

<style lang="scss" scoped>
.app-container {
    .search-form {
        background: #f5f7fa;
        padding: 20px;
        border-radius: 4px;
        margin-bottom: 20px;
    }

    .button-group {
        margin-bottom: 20px;
    }

    .page-container {
        .pagination-container {
            margin-top: 20px;
            text-align: center;
        }
    }
}

.order-detail {
    .detail-card {
        margin-bottom: 20px;

        &:last-child {
            margin-bottom: 0;
        }
    }

    .detail-item {
        margin-bottom: 10px;

        label {
            font-weight: 600;
            color: #606266;
            margin-right: 8px;
        }

        span {
            color: #303133;
        }
    }
}

.amount {
    color: #f56c6c;
    font-weight: 600;
}
</style>
