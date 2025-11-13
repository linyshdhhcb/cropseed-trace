<template>
    <div class="app-container">
        <el-card v-loading="loading" class="order-detail-card">
            <!-- 订单基本信息 -->
            <el-descriptions title="订单基本信息" :column="2" border>
                <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
                <el-descriptions-item label="订单类型">
                    <el-tag :type="orderDetail.orderType === 1 ? 'primary' : 'success'">
                        {{ orderDetail.orderType === 1 ? 'B端订单' : 'C端订单' }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="客户名称">{{ orderDetail.customerName }}</el-descriptions-item>
                <el-descriptions-item label="订单状态">
                    <el-tag :type="getOrderStatusTagType(orderDetail.orderStatus)">
                        {{ getOrderStatusText(orderDetail.orderStatus) }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="订单金额">
                    <span class="amount">¥{{ formatMoney(orderDetail.totalAmount) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="支付方式">
                    {{ getPaymentMethodText(orderDetail.paymentMethod) }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ orderDetail.createTime }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ orderDetail.updateTime }}</el-descriptions-item>
            </el-descriptions>

            <!-- 收货信息 -->
            <el-descriptions title="收货信息" :column="2" border style="margin-top: 20px;">
                <el-descriptions-item label="收货人">{{ orderDetail.consignee }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ orderDetail.phone }}</el-descriptions-item>
                <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.address }}</el-descriptions-item>
            </el-descriptions>

            <!-- 商品信息 -->
            <div style="margin-top: 20px;">
                <h3>商品信息</h3>
                <el-table :data="orderDetail.orderItems" style="width: 100%; margin-top: 10px;">
                    <el-table-column prop="seedName" label="商品名称" width="200" />
                    <el-table-column prop="seedCode" label="商品编码" width="150" />
                    <el-table-column prop="quantity" label="数量" width="100" />
                    <el-table-column prop="unitPrice" label="单价" width="120">
                        <template #default="{ row }">
                            <span class="amount">¥{{ formatMoney(row.unitPrice) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="totalPrice" label="小计" width="120">
                        <template #default="{ row }">
                            <span class="amount">¥{{ formatMoney(row.totalPrice) }}</span>
                        </template>
                    </el-table-column>
                </el-table>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons" style="margin-top: 20px;">
                <el-button v-if="orderDetail.orderStatus === 1" type="success" @click="handlePay">
                    确认支付
                </el-button>
                <el-button v-if="orderDetail.orderStatus === 2" type="warning" @click="handleShip">
                    发货
                </el-button>
                <el-button v-if="orderDetail.orderStatus === 3" type="info" @click="handleComplete">
                    完成订单
                </el-button>
                <el-button v-if="[1, 2].includes(orderDetail.orderStatus)" type="danger" @click="handleCancel">
                    取消订单
                </el-button>
                <el-button @click="handleBack">
                    返回列表
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { getOrderDetail, updateOrderStatus } from "@/api/order";
import { formatMoney } from "@/utils/index";

const route = useRoute();
const router = useRouter();

// 数据
const loading = ref(false);
const orderDetail = ref({});

// 获取订单详情
const loadOrderDetail = async () => {
    try {
        loading.value = true;
        const orderId = route.params.id;
        const response = await getOrderDetail(orderId);
        orderDetail.value = response.data;
    } catch (error) {
        console.error("获取订单详情失败:", error);
        ElMessage.error("获取订单详情失败");
    } finally {
        loading.value = false;
    }
};

// 确认支付
const handlePay = async () => {
    try {
        await ElMessageBox.confirm("确定要确认支付该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(orderDetail.value.id, 2);
        ElMessage.success("确认支付成功");
        loadOrderDetail();
    } catch (error) {
        if (error !== "cancel") {
            console.error("确认支付失败:", error);
        }
    }
};

// 发货
const handleShip = async () => {
    try {
        await ElMessageBox.confirm("确定要发货该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(orderDetail.value.id, 3);
        ElMessage.success("发货成功");
        loadOrderDetail();
    } catch (error) {
        if (error !== "cancel") {
            console.error("发货失败:", error);
        }
    }
};

// 完成订单
const handleComplete = async () => {
    try {
        await ElMessageBox.confirm("确定要完成该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(orderDetail.value.id, 4);
        ElMessage.success("订单完成");
        loadOrderDetail();
    } catch (error) {
        if (error !== "cancel") {
            console.error("完成订单失败:", error);
        }
    }
};

// 取消订单
const handleCancel = async () => {
    try {
        await ElMessageBox.confirm("确定要取消该订单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateOrderStatus(orderDetail.value.id, 5);
        ElMessage.success("订单已取消");
        loadOrderDetail();
    } catch (error) {
        if (error !== "cancel") {
            console.error("取消订单失败:", error);
        }
    }
};

// 返回列表
const handleBack = () => {
    router.push("/order/list");
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
    loadOrderDetail();
});
</script>

<style lang="scss" scoped>
.app-container {
    .order-detail-card {
        .el-descriptions {
            margin-bottom: 20px;
        }
    }

    .action-buttons {
        text-align: center;

        .el-button {
            margin: 0 10px;
        }
    }
}

.amount {
    color: #f56c6c;
    font-weight: 600;
}
</style>
