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
                    <el-option label="待付款" :value="0" />
                    <el-option label="待审核" :value="1" />
                    <el-option label="待发货" :value="2" />
                    <el-option label="已发货" :value="3" />
                    <el-option label="已完成" :value="4" />
                    <el-option label="已取消" :value="5" />
                    <el-option label="退款中" :value="6" />
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
            <el-button type="info" @click="showTraceOverview = true">
                <el-icon><Link /></el-icon>
                溯源总览
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange"
                style="width: 100%" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="orderNo" label="订单号" width="220" />
                <el-table-column prop="customerName" label="客户名称" width="120">
                    <template #default="{ row }">
                        <span>{{ row.customerName || row.consignee || '-' }}</span>
                    </template>
                </el-table-column>
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
                <el-table-column prop="itemCount" label="商品数量" width="100">
                    <template #default="{ row }">
                        <span>{{ row.itemCount || (row.orderItems ? row.orderItems.length : 0) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="paymentMethod" label="支付方式" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getPaymentMethodTagType(row.paymentMethod)">
                            {{ getPaymentMethodText(row.paymentMethod) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="traceStatus" label="溯源状态" width="100">
                    <template #default="{ row }">
                        <el-tag v-if="row.traceRecordCount > 0" type="success" size="small">
                            已溯源({{ row.traceRecordCount }})
                        </el-tag>
                        <el-tag v-else type="info" size="small">未溯源</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            查看详情
                        </el-button>
                        <el-button v-if="row.orderStatus === 0" type="success" size="small" @click="handlePay(row)">
                            确认支付
                        </el-button>
                        <el-button v-if="row.orderStatus === 1" type="warning" size="small" @click="handleAudit(row)">
                            审核订单
                        </el-button>
                        <el-button v-if="row.orderStatus === 2" type="warning" size="small" @click="handleShip(row)">
                            发货
                        </el-button>
                        <el-button v-if="row.orderStatus === 3" type="info" size="small" @click="handleComplete(row)">
                            完成
                        </el-button>
                        <el-dropdown v-if="row.orderStatus >= 2" @command="handleTraceAction">
                            <el-button type="warning" size="small">
                                溯源<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item :command="{action: 'view', order: row}">查看溯源</el-dropdown-item>
                                    <el-dropdown-item :command="{action: 'manage', order: row}">管理溯源</el-dropdown-item>
                                    <el-dropdown-item v-if="row.orderStatus === 4" :command="{action: 'generate', order: row}">生成溯源</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                        <el-button v-if="[0, 1, 2].includes(row.orderStatus)" type="danger" size="small"
                            @click="handleCancel(row)">
                            取消
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination :current-page="pagination.current" :page-size="pagination.size"
                    :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- 订单详情对话框 -->
        <el-dialog v-model="detailDialogVisible" title="订单详情" width="1200px" @close="handleDetailDialogClose" class="order-detail-dialog">
            <div v-if="orderDetail" class="order-detail">
                <!-- 订单状态头部 -->
                <div class="order-header">
                    <div class="order-status-card">
                        <div class="status-info">
                            <div class="order-number">
                                <i class="el-icon-document"></i>
                                <span>订单号: {{ orderDetail.orderNo }}</span>
                            </div>
                            <div class="order-status">
                                <el-tag :type="getOrderStatusTagType(orderDetail.orderStatus)" size="large">
                                    {{ getOrderStatusText(orderDetail.orderStatus) }}
                                </el-tag>
                            </div>
                        </div>
                        <div class="order-amount">
                            <div class="amount-label">订单金额</div>
                            <div class="amount-value">￥{{ orderDetail.totalAmount }}</div>
                        </div>
                    </div>
                </div>

                <!-- 订单基本信息 -->
                <el-card class="detail-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <i class="el-icon-info"></i>
                            <span>订单信息</span>
                        </div>
                    </template>
                    <div class="info-grid">
                        <div class="info-item">
                            <div class="info-label">客户名称</div>
                            <div class="info-value">{{ orderDetail.customerName || orderDetail.consignee || '-' }}</div>
                        </div>
                        <div class="info-item">
                            <div class="info-label">订单类型</div>
                            <div class="info-value">
                                <el-tag :type="orderDetail.orderType === 1 ? 'primary' : 'success'" size="small">
                                    {{ orderDetail.orderType === 1 ? 'B端订单' : 'C端订单' }}
                                </el-tag>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-label">支付方式</div>
                            <div class="info-value">
                                <el-tag :type="getPaymentMethodTagType(orderDetail.paymentMethod)" size="small">
                                    {{ getPaymentMethodText(orderDetail.paymentMethod) }}
                                </el-tag>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-label">创建时间</div>
                            <div class="info-value">{{ orderDetail.createTime }}</div>
                        </div>
                        <div class="info-item" v-if="orderDetail.payTime">
                            <div class="info-label">支付时间</div>
                            <div class="info-value">{{ orderDetail.payTime }}</div>
                        </div>
                        <div class="info-item" v-if="orderDetail.shipTime">
                            <div class="info-label">发货时间</div>
                            <div class="info-value">{{ orderDetail.shipTime }}</div>
                        </div>
                    </div>
                </el-card>

                <!-- 商品信息 -->
                <el-card class="detail-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <i class="el-icon-goods"></i>
                            <span>商品信息</span>
                            <el-tag size="small" type="info">共 {{ orderDetail.orderItems?.length || 0 }} 件商品</el-tag>
                        </div>
                    </template>
                    <el-table :data="orderDetail.orderItems" style="width: 100%" class="order-items-table">
                        <el-table-column prop="seedName" label="商品名称" min-width="200">
                            <template #default="{ row }">
                                <div class="product-info">
                                    <div class="product-name">{{ row.seedName }}</div>
                                    <div class="product-code">编码: {{ row.seedCode }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="quantity" label="数量" width="100" align="center">
                            <template #default="{ row }">
                                <div class="quantity">{{ row.quantity }}</div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="unitPrice" label="单价" width="120" align="right">
                            <template #default="{ row }">
                                <span class="price">￥{{ row.unitPrice }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="totalPrice" label="小计" width="120" align="right">
                            <template #default="{ row }">
                                <span class="price total-price">￥{{ row.totalPrice }}</span>
                            </template>
                        </el-table-column>
                    </el-table>
                    <div class="order-summary">
                        <div class="summary-item">
                            <span>商品总金额:</span>
                            <span class="price">￥{{ orderDetail.totalAmount }}</span>
                        </div>
                    </div>
                </el-card>

                <!-- 收货信息 -->
                <el-card class="detail-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <i class="el-icon-location"></i>
                            <span>收货信息</span>
                        </div>
                    </template>
                    <div class="address-info">
                        <div class="address-main">
                            <div class="consignee">
                                <i class="el-icon-user"></i>
                                <span>{{ orderDetail.consignee }}</span>
                                <span class="phone">{{ orderDetail.phone }}</span>
                            </div>
                            <div class="address">
                                <i class="el-icon-map-location"></i>
                                <span>{{ orderDetail.address }}</span>
                            </div>
                        </div>
                    </div>
                </el-card>
            </div>
            
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="detailDialogVisible = false">关闭</el-button>
                    <el-button v-if="orderDetail?.orderStatus === 1" type="primary" @click="handleAuditInDialog">审核订单</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 溯源总览对话框 -->
        <el-dialog v-model="showTraceOverview" title="订单溯源总览" width="1200px" :close-on-click-modal="false">
            <div class="trace-overview">
                <el-tabs v-model="activeTraceTab">
                    <el-tab-pane label="溯源统计" name="stats">
                        <div class="trace-stats">
                            <el-row :gutter="20">
                                <el-col :span="6">
                                    <el-card class="stat-card">
                                        <div class="stat-item">
                                            <div class="stat-number">{{ orderTraceStats.totalOrders }}</div>
                                            <div class="stat-label">总订单数</div>
                                        </div>
                                    </el-card>
                                </el-col>
                                <el-col :span="6">
                                    <el-card class="stat-card">
                                        <div class="stat-item">
                                            <div class="stat-number">{{ orderTraceStats.tracedOrders }}</div>
                                            <div class="stat-label">已溯源订单</div>
                                        </div>
                                    </el-card>
                                </el-col>
                                <el-col :span="6">
                                    <el-card class="stat-card">
                                        <div class="stat-item">
                                            <div class="stat-number">{{ orderTraceStats.traceRecords }}</div>
                                            <div class="stat-label">溯源记录数</div>
                                        </div>
                                    </el-card>
                                </el-col>
                                <el-col :span="6">
                                    <el-card class="stat-card">
                                        <div class="stat-item">
                                            <div class="stat-number">{{ orderTraceStats.traceRate }}%</div>
                                            <div class="stat-label">溯源覆盖率</div>
                                        </div>
                                    </el-card>
                                </el-col>
                            </el-row>
                        </div>
                    </el-tab-pane>
                    <el-tab-pane label="溯源管理" name="manage">
                        <div style="text-align: center; padding: 40px;">
                            <el-button type="primary" size="large" @click="goToTraceManagement">
                                进入溯源管理系统
                            </el-button>
                        </div>
                    </el-tab-pane>
                </el-tabs>
            </div>
        </el-dialog>

        <!-- 订单溯源详情对话框 -->
        <el-dialog v-model="showOrderTraceDetail" title="订单溯源详情" width="1000px">
            <div v-if="currentOrder" class="order-trace-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
                    <el-descriptions-item label="溯源记录数">{{ currentOrder.traceRecordCount || 0 }}</el-descriptions-item>
                    <el-descriptions-item label="订单状态">
                        <el-tag :type="getOrderStatusTagType(currentOrder.orderStatus)">
                            {{ getOrderStatusText(currentOrder.orderStatus) }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="最后更新">{{ formatDateTime(currentOrder.updateTime) }}</el-descriptions-item>
                </el-descriptions>

                <div class="order-items-trace" style="margin-top: 20px;">
                    <h4>商品溯源信息</h4>
                    <el-table :data="currentOrder.orderItems || []" border>
                        <el-table-column prop="seedName" label="商品名称" />
                        <el-table-column prop="quantity" label="数量" width="80" />
                        <el-table-column prop="batchNo" label="批次号" width="120">
                            <template #default="{ row }">
                                <el-button v-if="row.batchNo" type="text" @click="viewBatchTrace(row.batchNo)">
                                    {{ row.batchNo }}
                                </el-button>
                                <span v-else>-</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="traceCode" label="溯源码" width="140">
                            <template #default="{ row }">
                                <el-button v-if="row.traceCode" type="text" @click="viewItemTrace(row.traceCode)">
                                    {{ row.traceCode }}
                                </el-button>
                                <el-tag v-else type="warning" size="small">未生成</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="150">
                            <template #default="{ row }">
                                <el-button v-if="row.traceCode" type="primary" size="small" @click="viewItemTrace(row.traceCode)">
                                    查看溯源
                                </el-button>
                                <el-button v-else type="success" size="small" @click="generateItemTrace(row)">
                                    生成溯源
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>

                <div class="trace-actions" style="margin-top: 20px; text-align: center;">
                    <el-button type="primary" @click="batchGenerateTrace">批量生成溯源</el-button>
                    <el-button type="success" @click="exportTraceReport">导出溯源报告</el-button>
                </div>
            </div>
        </el-dialog>

        <!-- 溯源链查看对话框 -->
        <el-dialog v-model="showTraceChainDialog" title="完整溯源链" width="1200px">
            <TraceChainView v-if="currentTraceCode" :trace-code="currentTraceCode" />
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
    Link,
    ArrowDown
} from "@element-plus/icons-vue";
import {
    getOrderList,
    getOrderDetail,
    updateOrderStatus,
    auditOrder,
    payOrder,
    cancelOrder,
    shipOrder,
    completeOrder,
} from "@/api/order";
import { 
    generateTraceCode,
    createTraceRecord,
    getTraceRecordsByOrderId 
} from "@/api/trace";
import TraceChainView from "@/views/trace/components/TraceChainView.vue";

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

// 溯源相关变量
const showTraceOverview = ref(false);
const showOrderTraceDetail = ref(false);
const showTraceChainDialog = ref(false);
const activeTraceTab = ref('stats');
const currentOrder = ref(null);
const currentTraceCode = ref('');

// 溯源统计数据
const orderTraceStats = reactive({
    totalOrders: 0,
    tracedOrders: 0,
    traceRecords: 0,
    traceRate: 0
});

// 获取订单列表
const loadOrderList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        console.log('请求订单列表参数:', params);
        
        const response = await getOrderList(params);
        console.log('订单列表响应:', response);
        
        if (response && response.data) {
            tableData.value = response.data.list || [];
            pagination.total = parseInt(response.data.total) || 0;
        } else {
            tableData.value = [];
            pagination.total = 0;
        }
        
        console.log('设置表格数据:', tableData.value.length, '条记录');
        
    } catch (error) {
        console.error("获取订单列表失败:", error);
        ElMessage.error('获取订单列表失败: ' + (error.response?.data?.message || error.message));
        tableData.value = [];
        pagination.total = 0;
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
        console.log('查看订单详情:', row.id);
        const response = await getOrderDetail(row.id);
        console.log('订单详情响应:', response);
        
        orderDetail.value = response.data;
        detailDialogVisible.value = true;
        
    } catch (error) {
        console.error("获取订单详情失败:", error);
        ElMessage.error('获取订单详情失败: ' + (error.response?.data?.message || error.message));
    }
};

// 确认支付
const handlePay = async (row) => {
    try {
        const { value: paymentMethod } = await ElMessageBox.prompt(
            "请选择支付方式",
            "确认支付",
            {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                inputType: "select",
                inputOptions: {
                    1: "微信支付",
                    2: "支付宝",
                    3: "银行转账"
                },
                inputValue: "1"
            }
        );
        
        console.log('调用支付API:', { id: row.id, paymentMethod: parseInt(paymentMethod) });
        await payOrder(row.id, parseInt(paymentMethod));
        ElMessage.success("确认支付成功");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("确认支付失败:", error);
            ElMessage.error('确认支付失败: ' + (error.response?.data?.message || error.message));
        }
    }
};

// 发货
const handleShip = async (row) => {
    try {
        const result = await ElMessageBox.prompt(
            "请输入物流信息（格式：物流公司|快递单号|备注）",
            "订单发货",
            {
                confirmButtonText: "确定发货",
                cancelButtonText: "取消",
                inputPlaceholder: "例如：顺丰速运|SF1234567890|小心轻放"
            }
        );
        
        const shipInfo = result.value.split('|');
        const logisticsCompany = shipInfo[0] || '顺丰速运';
        const trackingNumber = shipInfo[1] || '';
        const remark = shipInfo[2] || '';
        
        if (!trackingNumber) {
            ElMessage.error('请输入快递单号');
            return;
        }
        
        console.log('调用发货API:', { 
            id: row.id, 
            logisticsCompany, 
            trackingNumber, 
            remark 
        });
        
        await shipOrder(row.id, logisticsCompany, trackingNumber, remark);
        ElMessage.success("发货成功");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("发货失败:", error);
            ElMessage.error('发货失败: ' + (error.response?.data?.message || error.message));
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
        
        console.log('调用完成订单API:', { id: row.id });
        await completeOrder(row.id);
        ElMessage.success("订单完成");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("完成订单失败:", error);
            ElMessage.error('完成订单失败: ' + (error.response?.data?.message || error.message));
        }
    }
};

// 取消订单
const handleCancel = async (row) => {
    try {
        const { value: reason } = await ElMessageBox.prompt(
            "请输入取消原因",
            "取消订单",
            {
                confirmButtonText: "确定取消",
                cancelButtonText: "返回",
                inputPlaceholder: "请输入取消订单的原因..."
            }
        );
        
        if (!reason || reason.trim() === '') {
            ElMessage.error('请输入取消原因');
            return;
        }
        
        console.log('调用取消订单API:', { id: row.id, reason });
        await cancelOrder(row.id, reason.trim());
        ElMessage.success("订单已取消");
        loadOrderList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("取消订单失败:", error);
            ElMessage.error('取消订单失败: ' + (error.response?.data?.message || error.message));
        }
    }
};

// 审核订单
const handleAudit = async (row) => {
    console.log('开始审核订单:', row.id, '当前状态:', row.orderStatus);
    
    try {
        const result = await ElMessageBox.confirm("确定要审核通过该订单吗？", "审核订单", {
            confirmButtonText: "审核通过",
            cancelButtonText: "审核拒绝",
            distinguishCancelAndClose: true,
            type: "warning",
        });
        
        console.log('用户选择结果:', result);
        
        // 审核通过，状态变为待发货(2)
        console.log('调用API - 审核通过:', { id: row.id, status: 2 });
        const response = await updateOrderStatus(row.id, 2);
        console.log('审核通过API响应:', response);
        
        ElMessage.success("订单审核通过");
        await loadOrderList();
        
    } catch (action) {
        console.log('用户操作或错误:', action);
        
        if (action === "cancel") {
            // 审核拒绝，状态变为已取消(5)
            try {
                console.log('调用API - 审核拒绝:', { id: row.id, status: 5 });
                const response = await updateOrderStatus(row.id, 5);
                console.log('审核拒绝 API响应:', response);
                
                ElMessage.success("订单审核拒绝");
                await loadOrderList();
                
            } catch (error) {
                console.error("审核拒绝失败:", error);
                const errorMsg = error.response?.data?.message || error.message || '未知错误';
                ElMessage.error('审核拒绝失败: ' + errorMsg);
            }
        } else if (action !== "close" && action !== "cancel") {
            // 审核通过失败
            console.error("审核通过失败:", action);
            const errorMsg = action.response?.data?.message || action.message || '未知错误';
            ElMessage.error('审核通过失败: ' + errorMsg);
        }
    }
};

// 在详情弹窗中审核订单
const handleAuditInDialog = async () => {
    await handleAudit(orderDetail.value);
    // 审核后关闭弹窗并刷新列表
    detailDialogVisible.value = false;
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
        0: "warning",    // 待付款 - 橙色
        1: "primary",    // 待审核 - 蓝色
        2: "info",       // 待发货 - 灰色
        3: "success",    // 已发货 - 绿色
        4: "success",    // 已完成 - 绿色
        5: "danger",     // 已取消 - 红色
        6: "warning",    // 退款中 - 橙色
    };
    return statusMap[status] || "info";
};

// 获取订单状态文本
const getOrderStatusText = (status) => {
    const statusMap = {
        0: "待付款",
        1: "待审核",
        2: "待发货", 
        3: "已发货",
        4: "已完成",
        5: "已取消",
        6: "退款中"
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

// 溯源相关方法
const handleTraceAction = ({ action, order }) => {
    currentOrder.value = order;
    
    switch (action) {
        case 'view':
            showOrderTraceDetail.value = true;
            break;
        case 'manage':
            window.open(`#/trace/records?orderId=${order.id}&orderNo=${order.orderNo}`, '_blank');
            break;
        case 'generate':
            generateOrderTrace(order);
            break;
    }
};

const goToTraceManagement = () => {
    window.open('#/trace', '_blank');
};

const viewBatchTrace = (batchNo) => {
    window.open(`#/seed/batch?batchNo=${batchNo}`, '_blank');
};

const viewItemTrace = (traceCode) => {
    currentTraceCode.value = traceCode;
    showTraceChainDialog.value = true;
};

const generateItemTrace = async (orderItem) => {
    try {
        // 为订单商品生成溯源码
        const response = await generateTraceCode('BJ'); // 根据实际需求选择地区
        if (response.code === 200) {
            orderItem.traceCode = response.data;
            
            // 创建销售溯源记录
            const traceData = {
                traceCode: response.data,
                recordType: 4, // 销售记录
                recordStage: '销售',
                recordDescription: `订单销售 - ${orderItem.seedName}`,
                operatorName: '订单系统',
                recordTime: new Date().toISOString(),
                quantity: orderItem.quantity,
                unit: '件',
                batchId: orderItem.batchId || null,
                orderId: currentOrder.value.id
            };
            
            await createTraceRecord(traceData);
            ElMessage.success('溯源码生成成功');
        } else {
            ElMessage.error(response.message || '溯源码生成失败');
        }
    } catch (error) {
        console.error('生成溯源码失败', error);
        ElMessage.error('生成溯源码失败');
    }
};

const generateOrderTrace = async (order) => {
    try {
        if (!order.orderItems || order.orderItems.length === 0) {
            ElMessage.warning('订单无商品，无法生成溯源');
            return;
        }
        
        let successCount = 0;
        for (const item of order.orderItems) {
            if (!item.traceCode) {
                await generateItemTrace(item);
                successCount++;
            }
        }
        
        ElMessage.success(`成功生成 ${successCount} 个商品的溯源码`);
        
        // 刷新数据
        loadOrderList();
    } catch (error) {
        ElMessage.error('批量生成溯源失败');
    }
};

const batchGenerateTrace = async () => {
    if (currentOrder.value) {
        await generateOrderTrace(currentOrder.value);
    }
};

const exportTraceReport = () => {
    if (currentOrder.value) {
        const reportData = {
            订单号: currentOrder.value.orderNo,
            溯源记录数: currentOrder.value.traceRecordCount || 0,
            订单状态: getOrderStatusText(currentOrder.value.orderStatus),
            商品明细: currentOrder.value.orderItems?.map(item => ({
                商品名称: item.seedName,
                数量: item.quantity,
                批次号: item.batchNo || '-',
                溯源码: item.traceCode || '未生成'
            })) || []
        };
        
        const jsonStr = JSON.stringify(reportData, null, 2);
        const blob = new Blob([jsonStr], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `订单溯源报告_${currentOrder.value.orderNo}.json`;
        link.click();
        URL.revokeObjectURL(url);
        
        ElMessage.success('溯源报告已导出');
    }
};

const loadTraceStats = () => {
    // 模拟加载溯源统计数据
    orderTraceStats.totalOrders = tableData.value.length;
    orderTraceStats.tracedOrders = tableData.value.filter(order => (order.traceRecordCount || 0) > 0).length;
    orderTraceStats.traceRecords = tableData.value.reduce((sum, order) => sum + (order.traceRecordCount || 0), 0);
    orderTraceStats.traceRate = orderTraceStats.totalOrders > 0 
        ? Math.round((orderTraceStats.tracedOrders / orderTraceStats.totalOrders) * 100)
        : 0;
};

const formatDateTime = (dateTime) => {
    if (!dateTime) return '-';
    return new Date(dateTime).toLocaleString('zh-CN');
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

.order-detail-dialog {
    .el-dialog__body {
        padding: 0 20px 20px 20px;
        max-height: 70vh;
        overflow-y: auto;
    }
}

.order-detail {
    .order-header {
        margin-bottom: 24px;
        
        .order-status-card {
            background: #6fa3f7;
            border-radius: 12px;
            padding: 24px;
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .status-info {
                .order-number {
                    display: flex;
                    align-items: center;
                    font-size: 16px;
                    margin-bottom: 8px;
                    
                    i {
                        margin-right: 8px;
                        font-size: 18px;
                    }
                }
                
                .order-status {
                    .el-tag {
                        border: none;
                        color: white;
                        font-weight: 600;
                    }
                }
            }
            
            .order-amount {
                text-align: right;
                
                .amount-label {
                    font-size: 14px;
                    opacity: 0.9;
                    margin-bottom: 4px;
                }
                
                .amount-value {
                    font-size: 28px;
                    font-weight: 700;
                }
            }
        }
    }
    
    .detail-card {
        margin-bottom: 20px;
        border-radius: 8px;
        
        &:last-child {
            margin-bottom: 0;
        }
        
        .card-header {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
            
            i {
                color: #409eff;
                font-size: 16px;
            }
        }
    }
    
    .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 16px;
        
        .info-item {
            padding: 16px;
            background: #f8f9fa;
            border-radius: 6px;
            
            .info-label {
                font-size: 12px;
                color: #909399;
                margin-bottom: 4px;
                text-transform: uppercase;
                font-weight: 500;
            }
            
            .info-value {
                font-size: 14px;
                color: #303133;
                font-weight: 500;
            }
        }
    }
    
    .order-items-table {
        .product-info {
            .product-name {
                font-weight: 600;
                color: #303133;
                margin-bottom: 4px;
            }
            
            .product-code {
                font-size: 12px;
                color: #909399;
            }
        }
        
        .quantity {
            font-weight: 600;
            color: #409eff;
        }
        
        .price {
            color: #f56c6c;
            font-weight: 600;
            
            &.total-price {
                font-size: 16px;
            }
        }
    }
    
    .order-summary {
        margin-top: 16px;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 6px;
        text-align: right;
        
        .summary-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 16px;
            font-weight: 600;
            
            .price {
                font-size: 18px;
                color: #f56c6c;
            }
        }
    }
    
    .address-info {
        .address-main {
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
            
            .consignee {
                display: flex;
                align-items: center;
                margin-bottom: 12px;
                font-size: 16px;
                font-weight: 600;
                
                i {
                    color: #67c23a;
                    margin-right: 8px;
                }
                
                .phone {
                    margin-left: 16px;
                    color: #606266;
                    font-weight: normal;
                }
            }
            
            .address {
                display: flex;
                align-items: flex-start;
                color: #606266;
                line-height: 1.5;
                
                i {
                    color: #67c23a;
                    margin-right: 8px;
                    margin-top: 2px;
                }
            }
        }
    }
}

.dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.amount {
    color: #f56c6c;
    font-weight: 600;
}

/* 溯源相关样式 */
.trace-overview {
    .trace-stats {
        .stat-card {
            text-align: center;
            
            .stat-item {
                padding: 20px;
                
                .stat-number {
                    font-size: 28px;
                    font-weight: bold;
                    color: #409eff;
                    margin-bottom: 8px;
                }
                
                .stat-label {
                    color: #909399;
                    font-size: 14px;
                }
            }
        }
    }
}

.order-trace-detail {
    .order-items-trace {
        h4 {
            margin: 0 0 15px 0;
            color: #303133;
            font-size: 16px;
        }
    }
    
    .trace-actions {
        border-top: 1px solid #eee;
        padding-top: 20px;
    }
}
</style>
