<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="批次号" prop="batchNo">
                <el-input v-model="searchForm.batchNo" placeholder="请输入批次号" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="种子" prop="seedId">
                <el-select v-model="searchForm.seedId" placeholder="请选择种子" clearable style="width: 200px">
                    <el-option v-for="seed in seedList" :key="seed.id" :label="seed.seedName" :value="seed.id" />
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
                新增批次
            </el-button>
            <el-button type="danger" :icon="Delete" :disabled="!multipleSelection.length" @click="handleBatchDelete">
                批量删除
            </el-button>
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange"
                style="width: 100%" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="batchNo" label="批次号" width="150" align="center" />
                <el-table-column prop="seedName" label="种子名称" width="180" show-overflow-tooltip />
                <el-table-column prop="productionDate" label="生产日期" width="120" align="center" />
                <el-table-column prop="expiryDate" label="过期日期" width="120" align="center" />
                <el-table-column prop="qualityStatus" label="质检状态" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.qualityStatus === 1 ? 'success' : 'danger'">
                            {{ row.qualityStatus === 1 ? '合格' : '不合格' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
                <el-table-column prop="updateTime" label="更新时间" width="160" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="info" size="small" @click="handleView(row)">
                            查看
                        </el-button>
                        <el-button type="danger" size="small" @click="handleDelete(row)">
                            删除
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
                        <el-form-item label="批次号" prop="batchNo">
                            <el-input v-model="formData.batchNo" placeholder="请输入批次号" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="种子" prop="seedId">
                            <el-select v-model="formData.seedId" placeholder="请选择种子" style="width: 100%">
                                <el-option v-for="seed in seedList" :key="seed.id" :label="seed.seedName"
                                    :value="seed.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="质检报告" prop="qualityReport">
                            <el-input v-model="formData.qualityReport" placeholder="请输入质检报告URL" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="质检状态" prop="qualityStatus">
                            <el-radio-group v-model="formData.qualityStatus">
                                <el-radio :label="1">合格</el-radio>
                                <el-radio :label="0">不合格</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="生产日期" prop="productionDate">
                            <el-date-picker v-model="formData.productionDate" type="date" placeholder="请选择生产日期"
                                style="width: 100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="过期日期" prop="expiryDate">
                            <el-date-picker v-model="formData.expiryDate" type="date" placeholder="请选择过期日期"
                                style="width: 100%" />
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
        <el-dialog v-model="viewDialogVisible" title="批次详情" width="800px">
            <div v-if="batchDetail" class="batch-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="批次号">{{ batchDetail.batchNo }}</el-descriptions-item>
                    <el-descriptions-item label="种子名称">{{ batchDetail.seedName }}</el-descriptions-item>
                    <el-descriptions-item label="生产日期">{{ batchDetail.productionDate }}</el-descriptions-item>
                    <el-descriptions-item label="过期日期">{{ batchDetail.expiryDate }}</el-descriptions-item>
                    <el-descriptions-item label="质检报告">{{ batchDetail.qualityReport }}</el-descriptions-item>
                    <el-descriptions-item label="质检状态">
                        <el-tag :type="batchDetail.qualityStatus === 1 ? 'success' : 'danger'">
                            {{ batchDetail.qualityStatus === 1 ? '合格' : '不合格' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="质检报告">{{ batchDetail.qualityReport }}</el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{ batchDetail.createTime }}</el-descriptions-item>
                    <el-descriptions-item label="更新时间">{{ batchDetail.updateTime }}</el-descriptions-item>
                </el-descriptions>
                <el-descriptions :column="1" border style="margin-top: 20px;">
                    <el-descriptions-item label="备注">{{ batchDetail.remarks }}</el-descriptions-item>
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
    Delete,
    Download,
} from "@element-plus/icons-vue";
import {
    getSeedBatchList,
    addSeedBatch,
    updateSeedBatch,
    deleteSeedBatch,
    batchDeleteSeedBatch,
    getSeedBatchDetail,
} from "@/api/seed";
import { getSeedInfoList } from "@/api/seed";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    batchNo: "",
    seedId: null,
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

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const submitLoading = ref(false);

// 表单数据
const formData = reactive({
    id: null,
    batchNo: "",
    seedId: null,
    productionDate: "",
    expiryDate: "",
    qualityReport: "",
    qualityStatus: 1,
    remarks: "",
});

// 表单验证规则
const formRules = {
    batchNo: [
        { required: true, message: "请输入批次号", trigger: "blur" },
        { min: 2, max: 50, message: "批次号长度在 2 到 50 个字符", trigger: "blur" },
    ],
    seedId: [{ required: true, message: "请选择种子", trigger: "change" }],
    productionDate: [{ required: true, message: "请选择生产日期", trigger: "change" }],
    expiryDate: [{ required: true, message: "请选择过期日期", trigger: "change" }],
    qualityStatus: [{ required: true, message: "请选择质检状态", trigger: "change" }],
};

// 种子列表
const seedList = ref([]);

// 查看详情对话框
const viewDialogVisible = ref(false);
const batchDetail = ref(null);

// 获取批次列表
const loadBatchList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getSeedBatchList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取批次列表失败:", error);
    } finally {
        loading.value = false;
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

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadBatchList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadBatchList();
};

// 新增批次
const handleAdd = () => {
    dialogTitle.value = "新增批次";
    dialogVisible.value = true;
    resetForm();
};

// 编辑批次
const handleEdit = (row) => {
    dialogTitle.value = "编辑批次";
    dialogVisible.value = true;
    Object.assign(formData, row);
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getSeedBatchDetail(row.id);
        batchDetail.value = response.data;
        viewDialogVisible.value = true;
    } catch (error) {
        console.error("获取批次详情失败:", error);
    }
};

// 删除批次
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该批次吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteSeedBatch(row.id);
        ElMessage.success("删除成功");
        loadBatchList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除批次失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的批次吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteSeedBatch(ids);
        ElMessage.success("删除成功");
        loadBatchList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除批次失败:", error);
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
            await updateSeedBatch(formData);
            ElMessage.success("更新成功");
        } else {
            await addSeedBatch(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadBatchList();
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
        seedId: null,
        productionDate: "",
        expiryDate: "",
        qualityReport: "",
        qualityStatus: 1,
        remarks: "",
    });
    formRef.value?.resetFields();
};

// 关闭对话框
const handleDialogClose = () => {
    resetForm();
};

// 选择变化
const handleSelectionChange = (selection) => {
    multipleSelection.value = selection;
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadBatchList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadBatchList();
};

// 获取状态标签类型
const getStatusTagType = (status) => {
    const statusMap = {
        1: "success",
        2: "info",
        3: "danger",
    };
    return statusMap[status] || "info";
};

// 获取状态文本
const getStatusText = (status) => {
    const statusMap = {
        1: "在库",
        2: "出库",
        3: "过期",
    };
    return statusMap[status] || "未知";
};

// 初始化
onMounted(() => {
    loadBatchList();
    loadSeedList();
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

.batch-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }
}

.amount {
    color: #f56c6c;
    font-weight: 600;
}

:deep(.el-table) {
    .el-table__header th {
        background-color: #f8f9fa;
        font-weight: 600;
    }

    .el-table__body td {
        padding: 12px 0;
    }

    .el-table__row:hover>td {
        background-color: #f5f7fa !important;
    }
}
</style>
