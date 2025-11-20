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
                    <el-option label="已入库" :value="2" />
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
                新增入库
            </el-button>
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" class="inbound-table" border stripe>
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="batchNo" label="批次号" width="150" />
                <el-table-column prop="warehouseName" label="仓库" width="120" />
                <el-table-column prop="quantity" label="入库数量" width="100" />
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getStatusTagType(row.status)">
                            {{ getStatusText(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="operator" label="操作人" width="100" />
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="190" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            详情
                        </el-button>
                        <el-button v-if="row.status === 0" type="success" size="small" @click="handleApprove(row)">
                            审核
                        </el-button>
                        <el-button v-if="row.status === 1" type="warning" size="small" @click="handleInbound(row)">
                            入库
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
                        <el-form-item label="供应商" prop="supplier">
                            <el-input v-model="formData.supplier" placeholder="请输入供应商" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="质量等级" prop="qualityGrade">
                            <el-input v-model="formData.qualityGrade" placeholder="请输入质量等级" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="存储条件" prop="storageCondition">
                            <el-input v-model="formData.storageCondition" placeholder="请输入存储条件" />
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
                        <el-form-item label="入库数量" prop="quantity">
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
        <el-dialog v-model="viewDialogVisible" title="入库详情" width="800px">
            <div v-if="inboundDetail" class="inbound-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="批次号">{{ inboundDetail.batchNo }}</el-descriptions-item>
                    <el-descriptions-item label="仓库">{{ inboundDetail.warehouseName }}</el-descriptions-item>
                    <el-descriptions-item label="供应商">{{ inboundDetail.supplier }}</el-descriptions-item>
                    <el-descriptions-item label="质量等级">{{ inboundDetail.qualityGrade }}</el-descriptions-item>
                    <el-descriptions-item label="存储条件">{{ inboundDetail.storageCondition }}</el-descriptions-item>
                    <el-descriptions-item label="入库数量">{{ inboundDetail.quantity }}</el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-tag :type="getStatusTagType(inboundDetail.status)">
                            {{ getStatusText(inboundDetail.status) }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="操作人">{{ inboundDetail.operator }}</el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{ inboundDetail.createTime }}</el-descriptions-item>
                    <el-descriptions-item label="更新时间">{{ inboundDetail.updateTime }}</el-descriptions-item>
                </el-descriptions>
                <el-descriptions :column="1" border style="margin-top: 20px;">
                    <el-descriptions-item label="备注">{{ inboundDetail.remark }}</el-descriptions-item>
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
    getInboundList,
    addInbound,
    updateInbound,
    deleteInbound,
    getInboundDetail,
    approveInbound,
    confirmInbound,
    cancelInbound,
    getActiveWarehouseList,
} from "@/api/inventory";
import { getSeedInfoList, getSeedBatchList } from "@/api/seed";
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
    supplier: "",
    qualityGrade: "",
    storageCondition: "",
    remark: "",
    status: 0,
});

// 表单验证规则
const formRules = {
    batchNo: [
        { required: true, message: "请输入批次号", trigger: "blur" },
    ],
    warehouseId: [{ required: true, message: "请选择仓库", trigger: "change" }],
    seedBatchId: [{ required: true, message: "请选择种子批次", trigger: "change" }],
    quantity: [{ required: true, message: "请输入入库数量", trigger: "blur" }],
};

// 仓库列表
const warehouseList = ref([]);
const seedList = ref([]);
const seedBatchList = ref([]);

// 查看详情对话框
const viewDialogVisible = ref(false);
const inboundDetail = ref(null);

// 获取入库列表
const loadInboundList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getInboundList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取入库列表失败:", error);
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
        ElMessage.error("获取仓库列表失败");
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
        const response = await getSeedBatchList({ current: 1, size: 1000 });
        seedBatchList.value = response.data.list || [];
    } catch (error) {
        console.error("获取种子批次列表失败:", error);
        ElMessage.error("获取种子批次列表失败");
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadInboundList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadInboundList();
};

// 新增入库
const handleAdd = () => {
    dialogTitle.value = "新增入库";
    dialogVisible.value = true;
    resetForm();
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getInboundDetail(row.id);
        inboundDetail.value = response.data;
        viewDialogVisible.value = true;
    } catch (error) {
        console.error("获取入库详情失败:", error);
    }
};

// 审核
const handleApprove = async (row) => {
    try {
        await ElMessageBox.confirm("确定要审核通过该入库单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await approveInbound({ id: row.id });
        ElMessage.success("审核成功");
        loadInboundList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("审核失败:", error);
        }
    }
};

// 入库
const handleInbound = async (row) => {
    try {
        await ElMessageBox.confirm("确定要确认入库吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await confirmInbound({ id: row.id });
        ElMessage.success("入库成功");
        loadInboundList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("入库失败:", error);
        }
    }
};

// 取消
const handleCancel = async (row) => {
    try {
        await ElMessageBox.confirm("确定要取消该入库单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await cancelInbound({ id: row.id });
        ElMessage.success("取消成功");
        loadInboundList();
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

        // 验证批次号
        if (!formData.batchNo?.trim()) {
            ElMessage.error("请输入批次号");
            submitLoading.value = false;
            return;
        }

        // 构造提交数据，匹配后端 DTO 结构
        const submitData = {
            batchNo: formData.batchNo.trim(),
            seedBatchId: formData.seedBatchId,
            warehouseId: formData.warehouseId,
            quantity: formData.quantity,
            supplier: formData.supplier || null,
            qualityGrade: formData.qualityGrade || null,
            storageCondition: formData.storageCondition || null,
            remark: formData.remark || null,
        };

        if (formData.id) {
            await updateInbound(submitData);
            ElMessage.success("更新成功");
        } else {
            await addInbound(submitData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadInboundList();
    } catch (error) {
        console.error("提交失败:", error);
        if (error.response?.data?.message) {
            ElMessage.error(error.response.data.message);
        } else {
            ElMessage.error("提交失败，请检查数据");
        }
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
        supplier: "",
        qualityGrade: "",
        storageCondition: "",
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
    loadInboundList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadInboundList();
};

// 获取状态标签类型
const getStatusTagType = (status) => {
    const statusMap = {
        0: "warning",  // 待审核
        1: "info",     // 已审核
        2: "success",  // 已入库
    };
    return statusMap[status] || "info";
};

// 获取状态文本
const getStatusText = (status) => {
    const statusMap = {
        0: "待审核",
        1: "已审核",
        2: "已入库",
    };
    return statusMap[status] || "未知";
};

// 初始化
onMounted(() => {
    loadInboundList();
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

.inbound-table {

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

.inbound-items {
    .total-amount {
        text-align: right;
        margin-top: 10px;
        font-size: 16px;
        font-weight: 600;
    }
}

.inbound-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }
}

.amount {
    color: #f56c6c;
    font-weight: 600;
}
</style>
