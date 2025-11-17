<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="批次号" prop="batchNo">
                <el-input v-model="searchForm.batchNo" placeholder="请输入批次号" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="仓库" prop="warehouseId">
                <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable style="width: 150px">
                    <el-option v-for="warehouse in warehouseList" :key="warehouse.id" :label="warehouse.warehouseName"
                        :value="warehouse.id" />
                </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
                    <el-option label="待审核" :value="0" />
                    <el-option label="已审核" :value="1" />
                    <el-option label="已出库" :value="2" />
                </el-select>
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
            <el-button type="primary" :icon="Plus" @click="handleAdd">
                新增出库
            </el-button>
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" class="outbound-table" border stripe>
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="batchNo" label="批次号" width="150" />
                <el-table-column prop="warehouseName" label="仓库" width="120" />
                <el-table-column prop="quantity" label="出库数量" width="100" />
                <el-table-column prop="recipient" label="接收方" width="120" />
                <el-table-column prop="purpose" label="用途" width="120" />
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getStatusTagType(row.status)">
                            {{ getStatusText(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="operator" label="操作人" width="100" />
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            详情
                        </el-button>
                        <el-button v-if="row.status === 0" type="success" size="small" @click="handleApprove(row)">
                            审批
                        </el-button>
                        <el-button v-if="row.status === 1" type="warning" size="small" @click="handleOutbound(row)">
                            出库
                        </el-button>
                        <el-button v-if="[0, 1].includes(row.status)" type="danger" size="small"
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

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="选择库存" prop="inventoryId">
                            <el-select v-model="formData.inventoryId" placeholder="请选择库存" style="width: 100%"
                                @change="handleInventoryChange">
                                <el-option v-for="inventory in inventoryList" :key="inventory.id"
                                    :label="`${inventory.seedName} - ${inventory.warehouseName} (可用:${inventory.availableQuantity || 0})`"
                                    :value="inventory.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="出库数量" prop="quantity">
                            <el-input-number v-model="formData.quantity" :min="1"
                                :max="selectedInventory && selectedInventory.availableQuantity > 0 ? selectedInventory.availableQuantity : undefined"
                                style="width: 100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="种子名称">
                            <el-input v-model="formData.seedName" disabled />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="批次号">
                            <el-input v-model="formData.batchNo" disabled />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="仓库">
                            <el-input v-model="formData.warehouseName" disabled />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="可用数量">
                            <el-input v-model="formData.availableQuantity" disabled />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="接收方" prop="recipient">
                            <el-input v-model="formData.recipient" placeholder="请输入接收方" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="用途" prop="purpose">
                            <el-input v-model="formData.purpose" placeholder="请输入用途" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="备注" prop="remarks">
                    <el-input v-model="formData.remarks" type="textarea" :rows="3" placeholder="请输入备注" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
                    确定
                </el-button>
            </template>
        </el-dialog>

        <!-- 查看详情对话框 -->
        <el-dialog v-model="viewDialogVisible" title="出库详情" width="800px">
            <div v-if="outboundDetail" class="outbound-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="批次号">{{ outboundDetail.batchNo }}</el-descriptions-item>
                    <el-descriptions-item label="仓库">{{ outboundDetail.warehouseName }}</el-descriptions-item>
                    <el-descriptions-item label="接收方">{{ outboundDetail.recipient }}</el-descriptions-item>
                    <el-descriptions-item label="用途">{{ outboundDetail.purpose }}</el-descriptions-item>
                    <el-descriptions-item label="出库数量">{{ outboundDetail.quantity }}</el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-tag :type="getStatusTagType(outboundDetail.status)">
                            {{ getStatusText(outboundDetail.status) }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="操作人">{{ outboundDetail.operator }}</el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{ outboundDetail.createTime }}</el-descriptions-item>
                    <el-descriptions-item label="更新时间">{{ outboundDetail.updateTime }}</el-descriptions-item>
                </el-descriptions>
                <el-descriptions :column="1" border style="margin-top: 20px;">
                    <el-descriptions-item label="备注">{{ outboundDetail.remark }}</el-descriptions-item>
                </el-descriptions>
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
    Plus,
    Download,
} from "@element-plus/icons-vue";
import {
    getOutboundList,
    addOutbound,
    updateOutbound,
    deleteOutbound,
    getOutboundDetail,
    approveOutbound,
    confirmOutbound,
    cancelOutbound,
    getInventoryList,
    getActiveWarehouseList,
} from "@/api/inventory";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    batchNo: "",
    warehouseId: null,
    status: null,
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

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const submitLoading = ref(false);

// 表单数据
const formData = reactive({
    id: null,
    inventoryId: null,
    seedId: null,
    batchId: null,
    warehouseId: null,
    seedName: "",
    batchNo: "",
    warehouseName: "",
    availableQuantity: 0,
    quantity: null,
    recipient: "",
    purpose: "",
    remarks: "",
});

// 表单验证规则
const formRules = {
    inventoryId: [{ required: true, message: "请选择库存", trigger: "change" }],
    quantity: [
        { required: true, message: "请输入出库数量", trigger: "blur" },
        { type: "number", min: 1, message: "出库数量必须大于0", trigger: "blur" }
    ],
    recipient: [
        { max: 255, message: "接收方长度不能超过255个字符", trigger: "blur" }
    ],
    purpose: [
        { max: 255, message: "用途长度不能超过255个字符", trigger: "blur" }
    ],
    remarks: [
        { max: 500, message: "备注长度不能超过500个字符", trigger: "blur" }
    ],
};

// 库存列表
const inventoryList = ref([]);
const selectedInventory = ref(null);

// 仓库列表
const warehouseList = ref([]);

// 查看详情对话框
const viewDialogVisible = ref(false);
const outboundDetail = ref(null);

// 获取出库列表
const loadOutboundList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getOutboundList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取出库列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 获取仓库列表
const loadWarehouseList = async () => {
    try {
        const response = await getActiveWarehouseList();
        warehouseList.value = response.data || [];
    } catch (error) {
        console.error("获取仓库列表失败:", error);
        warehouseList.value = [];
    }
};

// 获取库存列表
const loadInventoryList = async () => {
    try {
        const response = await getInventoryList({ current: 1, size: 1000 });
        // 只显示有可用库存的记录
        inventoryList.value = response.data.list.filter(item => (item.availableQuantity || 0) > 0);
    } catch (error) {
        console.error("获取库存列表失败:", error);
        inventoryList.value = [];
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadOutboundList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadOutboundList();
};

// 库存选择变更
const handleInventoryChange = (inventoryId) => {
    const inventory = inventoryList.value.find(item => item.id === inventoryId);
    if (inventory) {
        selectedInventory.value = inventory;
        formData.seedId = inventory.seedId;
        formData.batchId = inventory.batchId;
        formData.warehouseId = inventory.warehouseId;
        formData.seedName = inventory.seedName;
        formData.batchNo = inventory.batchNo;
        formData.warehouseName = inventory.warehouseName;
        formData.availableQuantity = inventory.availableQuantity || 0;
    }
};

// 新增出库
const handleAdd = () => {
    dialogTitle.value = "新增出库";
    dialogVisible.value = true;
    resetForm();
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getOutboundDetail(row.id);
        outboundDetail.value = response.data;
        viewDialogVisible.value = true;
    } catch (error) {
        console.error("获取出库详情失败:", error);
    }
};

// 审核
const handleApprove = async (row) => {
    try {
        const { value } = await ElMessageBox.prompt(
            "请输入审批备注（可选）",
            "审批出库单",
            {
                confirmButtonText: "通过",
                cancelButtonText: "不通过",
                distinguishCancelAndClose: true,
                inputPlaceholder: "请输入审批备注",
                inputType: "textarea",
                inputValidator: () => true,
            }
        );

        // 用户点击了"通过"按钮
        await approveOutbound({ id: row.id, approved: true, remark: value || "" });
        ElMessage.success("审批通过");
        loadOutboundList();
    } catch (error) {
        if (error === "cancel") {
            // 用户点击了"不通过"按钮
            try {
                const { value } = await ElMessageBox.prompt(
                    "请输入不通过原因（必填）",
                    "审批不通过",
                    {
                        confirmButtonText: "确定",
                        cancelButtonText: "取消",
                        inputPlaceholder: "请输入不通过原因",
                        inputType: "textarea",
                        inputValidator: (val) => {
                            if (!val || val.trim() === "") {
                                return "请输入不通过原因";
                            }
                            return true;
                        },
                    }
                );
                await approveOutbound({ id: row.id, approved: false, remark: value });
                ElMessage.success("审批不通过");
                loadOutboundList();
            } catch (innerError) {
                if (innerError !== "cancel") {
                    console.error("审批不通过失败:", innerError);
                    const errorMessage = innerError?.response?.data?.message || innerError?.message || "操作失败，请稍后重试";
                    ElMessage.error(errorMessage);
                }
            }
        } else if (error !== "close") {
            console.error("审核失败:", error);
            const errorMessage = error?.response?.data?.message || error?.message || "操作失败，请稍后重试";
            ElMessage.error(errorMessage);
        }
    }
};

// 出库
const handleOutbound = async (row) => {
    try {
        await ElMessageBox.confirm("确定要确认出库吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await confirmOutbound({ id: row.id });
        ElMessage.success("出库成功");
        loadOutboundList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("出库失败:", error);
        }
    }
};

// 取消
const handleCancel = async (row) => {
    try {
        await ElMessageBox.confirm("确定要取消该出库单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await cancelOutbound({ id: row.id });
        ElMessage.success("取消成功");
        loadOutboundList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("取消失败:", error);
        }
    }
};

// 导出数据
const handleExport = () => {
    ElMessage.info("导出功能开发中...");
};


// 提交表单
const handleSubmit = async () => {
    try {
        await formRef.value.validate();

        // 验证必填字段
        if (!formData.seedId || !formData.batchId || !formData.warehouseId) {
            ElMessage.error("请选择库存");
            return;
        }

        if (!formData.quantity || formData.quantity <= 0) {
            ElMessage.error("请输入有效的出库数量");
            return;
        }

        // 验证出库数量不能超过可用数量
        if (formData.quantity > formData.availableQuantity) {
            ElMessage.error(`出库数量不能超过可用数量${formData.availableQuantity}`);
            return;
        }

        submitLoading.value = true;

        // 构建提交数据，确保所有字段都正确传递
        const submitData = {
            seedId: formData.seedId,
            batchId: formData.batchId,
            warehouseId: formData.warehouseId,
            quantity: formData.quantity,
            recipient: formData.recipient && formData.recipient.trim() ? formData.recipient.trim() : null,
            purpose: formData.purpose && formData.purpose.trim() ? formData.purpose.trim() : null,
            remarks: formData.remarks && formData.remarks.trim() ? formData.remarks.trim() : null,
        };

        if (formData.id) {
            submitData.id = formData.id;
            await updateOutbound(submitData);
            ElMessage.success("更新成功");
        } else {
            await addOutbound(submitData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadOutboundList();
        loadInventoryList(); // 刷新库存列表
    } catch (error) {
        console.error("提交失败:", error);
        // 显示错误信息
        const errorMessage = error?.response?.data?.message || error?.message || "操作失败，请稍后重试";
        ElMessage.error(errorMessage);
    } finally {
        submitLoading.value = false;
    }
};

// 重置表单
const resetForm = () => {
    Object.assign(formData, {
        id: null,
        inventoryId: null,
        seedId: null,
        batchId: null,
        warehouseId: null,
        seedName: "",
        batchNo: "",
        warehouseName: "",
        availableQuantity: 0,
        quantity: null,
        recipient: "",
        purpose: "",
        remarks: "",
    });
    selectedInventory.value = null;
    formRef.value?.resetFields();
};

// 关闭对话框
const handleDialogClose = () => {
    resetForm();
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadOutboundList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadOutboundList();
};

// 获取状态标签类型
const getStatusTagType = (status) => {
    const statusMap = {
        0: "warning",  // 待审核
        1: "info",     // 已审核
        2: "success",  // 已出库
    };
    return statusMap[status] || "info";
};

// 获取状态文本
const getStatusText = (status) => {
    const statusMap = {
        0: "待审核",
        1: "已审核",
        2: "已出库",
    };
    return statusMap[status] || "未知";
};

// 初始化
onMounted(() => {
    loadOutboundList();
    loadWarehouseList();
    loadInventoryList();
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

.outbound-table {

    :deep(.el-table__header),
    :deep(.el-table__body) {
        width: 100% !important;
        table-layout: fixed;
    }

    :deep(.el-table__header-wrapper),
    :deep(.el-table__body-wrapper) {
        table-layout: fixed;
    }

    :deep(.el-table__cell) {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}

.outbound-items {
    .total-amount {
        text-align: right;
        margin-top: 10px;
        font-size: 16px;
        font-weight: 600;
    }
}

.outbound-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }
}

.amount {
    color: #f56c6c;
    font-weight: 600;
}
</style>
