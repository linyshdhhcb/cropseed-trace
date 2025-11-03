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
            <el-table v-loading="loading" :data="tableData" style="width: 100%" border stripe>
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
                            查看详情
                        </el-button>
                        <el-button v-if="row.status === 0" type="success" size="small" @click="handleApprove(row)">
                            审核
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
                <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
                    :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="1000px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="批次号" prop="batchNo">
                            <el-input v-model="formData.batchNo" placeholder="请输入批次号" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="仓库" prop="warehouseId">
                            <el-select v-model="formData.warehouseId" placeholder="请选择仓库" style="width: 100%">
                                <el-option v-for="warehouse in warehouseList" :key="warehouse.id"
                                    :label="warehouse.warehouseName" :value="warehouse.id" />
                            </el-select>
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
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="种子批次" prop="seedBatchId">
                            <el-select v-model="formData.seedBatchId" placeholder="请选择种子批次" style="width: 100%">
                                <el-option v-for="batch in seedBatchList" :key="batch.id" :label="batch.batchNo"
                                    :value="batch.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="出库数量" prop="quantity">
                            <el-input-number v-model="formData.quantity" :min="0" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
} from "@/api/inventory";
import { getWarehouseList } from "@/api/inventory";
import { getSeedInfoList } from "@/api/seed";
import { formatMoney } from "@/utils/index";

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
    batchNo: "",
    warehouseId: null,
    seedBatchId: null,
    quantity: null,
    recipient: "",
    purpose: "",
    remark: "",
    status: 0,
});

// 表单验证规则
const formRules = {
    batchNo: [
        { required: false, message: "请输入批次号", trigger: "blur" },
    ],
    warehouseId: [{ required: true, message: "请选择仓库", trigger: "change" }],
    seedBatchId: [{ required: true, message: "请选择种子批次", trigger: "change" }],
    quantity: [{ required: true, message: "请输入出库数量", trigger: "blur" }],
};

// 仓库列表
const warehouseList = ref([]);
const seedList = ref([]);
const seedBatchList = ref([]);

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
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取出库列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 获取仓库列表
const loadWarehouseList = async () => {
    try {
        const response = await getWarehouseList();
        warehouseList.value = response.data;
    } catch (error) {
        console.error("获取仓库列表失败:", error);
    }
};

// 获取种子列表
const loadSeedList = async () => {
    try {
        const response = await getSeedInfoList({ current: 1, size: 1000 });
        seedList.value = response.data.list;
    } catch (error) {
        console.error("获取种子列表失败:", error);
    }
};

// 获取种子批次列表
const loadSeedBatchList = async () => {
    try {
        // TODO: 需要调用获取种子批次列表的API
        // const response = await getSeedBatchList({ current: 1, size: 1000 });
        // seedBatchList.value = response.data.list;
    } catch (error) {
        console.error("获取种子批次列表失败:", error);
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
        await ElMessageBox.confirm("确定要审核通过该出库单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await approveOutbound({ id: row.id });
        ElMessage.success("审核成功");
        loadOutboundList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("审核失败:", error);
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
        submitLoading.value = true;

        if (formData.id) {
            await updateOutbound(formData);
            ElMessage.success("更新成功");
        } else {
            await addOutbound(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadOutboundList();
    } catch (error) {
        console.error("提交失败:", error);
    } finally {
        submitLoading.value = false;
    }
};

// 重置表单
const resetForm = () => {
    Object.assign(formData, {
        id: null,
        batchNo: "",
        warehouseId: null,
        seedBatchId: null,
        quantity: null,
        recipient: "",
        purpose: "",
        remark: "",
        status: 0,
    });
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
    loadSeedList();
    loadSeedBatchList();
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
