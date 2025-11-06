<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="订单号" prop="orderNo">
                <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
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
            <el-button type="primary" :icon="Plus" @click="handleCreatePay">
                创建支付订单
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" border stripe>
                <el-table-column prop="orderNo" label="订单号" width="180" />
                <el-table-column prop="amount" label="支付金额" width="120">
                    <template #default="{ row }">
                        <span style="color: #f56c6c; font-weight: 600;">¥{{ row.amount }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="商品描述" min-width="200" show-overflow-tooltip />
                <el-table-column prop="openid" label="用户OpenID" width="200" show-overflow-tooltip />
                <el-table-column prop="payStatus" label="支付状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getPayStatusTagType(row.payStatus)">
                            {{ getPayStatusText(row.payStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="300" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleQueryPayResult(row)">
                            查询支付结果
                        </el-button>
                        <el-button v-if="row.payStatus === 'SUCCESS'" type="warning" size="small"
                            @click="handleRefund(row)">
                            申请退款
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

        <!-- 创建支付订单对话框 -->
        <el-dialog v-model="payDialogVisible" title="创建支付订单" width="600px" @close="handlePayDialogClose">
            <el-form ref="payFormRef" :model="payFormData" :rules="payFormRules" label-width="120px">
                <el-form-item label="订单号" prop="orderNo">
                    <el-input v-model="payFormData.orderNo" placeholder="请输入订单号" />
                </el-form-item>
                <el-form-item label="支付金额" prop="amount">
                    <el-input-number v-model="payFormData.amount" :min="0.01" :precision="2" style="width: 100%" />
                </el-form-item>
                <el-form-item label="商品描述" prop="description">
                    <el-input v-model="payFormData.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
                </el-form-item>
                <el-form-item label="用户OpenID" prop="openid">
                    <el-input v-model="payFormData.openid" placeholder="请输入用户OpenID" />
                </el-form-item>
                <el-form-item label="回调地址" prop="notifyUrl">
                    <el-input v-model="payFormData.notifyUrl" placeholder="可选，默认使用系统配置" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="payDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handlePaySubmit" :loading="paySubmitLoading">
                    确定
                </el-button>
            </template>
        </el-dialog>

        <!-- 退款对话框 -->
        <el-dialog v-model="refundDialogVisible" title="申请退款" width="500px" @close="handleRefundDialogClose">
            <el-form ref="refundFormRef" :model="refundFormData" :rules="refundFormRules" label-width="120px">
                <el-form-item label="订单号">
                    <el-input v-model="refundFormData.orderNo" disabled />
                </el-form-item>
                <el-form-item label="退款单号" prop="refundNo">
                    <el-input v-model="refundFormData.refundNo" placeholder="请输入退款单号" />
                </el-form-item>
                <el-form-item label="退款金额" prop="refundAmount">
                    <el-input-number v-model="refundFormData.refundAmount" :min="1" style="width: 100%" />
                    <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                        退款金额单位为分，例如：100表示1元
                    </div>
                </el-form-item>
                <el-form-item label="退款原因" prop="reason">
                    <el-input v-model="refundFormData.reason" type="textarea" :rows="3" placeholder="请输入退款原因" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="refundDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleRefundSubmit" :loading="refundSubmitLoading">
                    确定
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
    Search,
    Refresh,
    Plus,
} from "@element-plus/icons-vue";
import {
    createWxPayOrder,
    queryWxPayResult,
    refundWxPay,
} from "@/api/wechat";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    orderNo: "",
});

// 表格数据
const loading = ref(false);
const tableData = ref([]);

// 分页
const pagination = reactive({
    current: 1,
    size: 10,
    total: 0,
});

// 创建支付订单对话框
const payDialogVisible = ref(false);
const payFormRef = ref();
const paySubmitLoading = ref(false);
const payFormData = reactive({
    orderNo: "",
    amount: null,
    description: "",
    openid: "",
    notifyUrl: "",
});

const payFormRules = {
    orderNo: [{ required: true, message: "请输入订单号", trigger: "blur" }],
    amount: [{ required: true, message: "请输入支付金额", trigger: "blur" }],
    description: [{ required: true, message: "请输入商品描述", trigger: "blur" }],
    openid: [{ required: true, message: "请输入用户OpenID", trigger: "blur" }],
};

// 退款对话框
const refundDialogVisible = ref(false);
const refundFormRef = ref();
const refundSubmitLoading = ref(false);
const refundFormData = reactive({
    orderNo: "",
    refundNo: "",
    refundAmount: null,
    reason: "",
});

const refundFormRules = {
    refundNo: [{ required: true, message: "请输入退款单号", trigger: "blur" }],
    refundAmount: [{ required: true, message: "请输入退款金额", trigger: "blur" }],
    reason: [{ required: true, message: "请输入退款原因", trigger: "blur" }],
};

// 获取支付列表（这里可以从订单列表获取，或者创建支付记录表）
const loadPayList = async () => {
    try {
        loading.value = true;
        // TODO: 实现支付记录列表查询
        // const response = await getWxPayList(params);
        // tableData.value = response.data.list;
        // pagination.total = response.data.total;
        tableData.value = [];
        pagination.total = 0;
    } catch (error) {
        console.error("获取支付列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadPayList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadPayList();
};

// 创建支付订单
const handleCreatePay = () => {
    payDialogVisible.value = true;
    resetPayForm();
};

// 提交支付订单
const handlePaySubmit = async () => {
    try {
        await payFormRef.value.validate();
        paySubmitLoading.value = true;

        const response = await createWxPayOrder({
            orderNo: payFormData.orderNo,
            amount: payFormData.amount,
            description: payFormData.description,
            openid: payFormData.openid,
            notifyUrl: payFormData.notifyUrl || null,
        });

        ElMessage.success("支付订单创建成功");
        ElMessage.info("请在小程序中使用返回的支付参数调起支付");
        console.log("支付参数:", response.data);

        payDialogVisible.value = false;
        loadPayList();
    } catch (error) {
        console.error("创建支付订单失败:", error);
        const errorMessage = error?.response?.data?.message || error?.message || "创建支付订单失败，请稍后重试";
        ElMessage.error(errorMessage);
    } finally {
        paySubmitLoading.value = false;
    }
};

// 查询支付结果
const handleQueryPayResult = async (row) => {
    try {
        loading.value = true;
        const response = await queryWxPayResult(row.orderNo);
        if (response.data) {
            ElMessage.success("订单已支付");
        } else {
            ElMessage.warning("订单未支付");
        }
        loadPayList();
    } catch (error) {
        console.error("查询支付结果失败:", error);
        const errorMessage = error?.response?.data?.message || error?.message || "查询失败，请稍后重试";
        ElMessage.error(errorMessage);
    } finally {
        loading.value = false;
    }
};

// 申请退款
const handleRefund = (row) => {
    refundFormData.orderNo = row.orderNo;
    refundFormData.refundNo = `REF${Date.now()}`;
    refundFormData.refundAmount = null;
    refundFormData.reason = "";
    refundDialogVisible.value = true;
};

// 提交退款
const handleRefundSubmit = async () => {
    try {
        await refundFormRef.value.validate();
        refundSubmitLoading.value = true;

        await refundWxPay({
            orderNo: refundFormData.orderNo,
            refundNo: refundFormData.refundNo,
            refundAmount: refundFormData.refundAmount,
            reason: refundFormData.reason,
        });

        ElMessage.success("退款申请成功");
        refundDialogVisible.value = false;
        loadPayList();
    } catch (error) {
        console.error("申请退款失败:", error);
        const errorMessage = error?.response?.data?.message || error?.message || "申请退款失败，请稍后重试";
        ElMessage.error(errorMessage);
    } finally {
        refundSubmitLoading.value = false;
    }
};

// 重置支付表单
const resetPayForm = () => {
    Object.assign(payFormData, {
        orderNo: "",
        amount: null,
        description: "",
        openid: "",
        notifyUrl: "",
    });
    payFormRef.value?.resetFields();
};

// 关闭支付对话框
const handlePayDialogClose = () => {
    resetPayForm();
};

// 关闭退款对话框
const handleRefundDialogClose = () => {
    refundFormRef.value?.resetFields();
};

// 获取支付状态标签类型
const getPayStatusTagType = (status) => {
    const statusMap = {
        SUCCESS: "success",
        NOTPAY: "warning",
        CLOSED: "danger",
        REFUND: "info",
    };
    return statusMap[status] || "info";
};

// 获取支付状态文本
const getPayStatusText = (status) => {
    const statusMap = {
        SUCCESS: "已支付",
        NOTPAY: "未支付",
        CLOSED: "已关闭",
        REFUND: "已退款",
    };
    return statusMap[status] || "未知";
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadPayList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadPayList();
};

// 初始化
onMounted(() => {
    loadPayList();
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
</style>
