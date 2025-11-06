<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="种子名称" prop="seedName">
                <el-input v-model="searchForm.seedName" placeholder="请输入种子名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="仓库" prop="warehouseId">
                <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable style="width: 150px">
                    <el-option v-for="warehouse in warehouseList" :key="warehouse.id" :label="warehouse.warehouseName"
                        :value="warehouse.id" />
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
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
            <el-button type="warning" :icon="Refresh" @click="handleRefresh">
                刷新
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" class="stock-table" border stripe>
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="seedName" label="种子名称" width="150" />
                <el-table-column prop="seedCode" label="种子编码" width="150" />
                <el-table-column prop="warehouseName" label="仓库" width="120" />
                <el-table-column prop="quantity" label="库存数量" width="100">
                    <template #default="{ row }">
                        <span class="stock-value">{{ row.quantity }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="lockedQuantity" label="锁定数量" width="100">
                    <template #default="{ row }">
                        <span class="locked-stock">{{ row.lockedQuantity || 0 }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="availableQuantity" label="可用数量" width="100">
                    <template #default="{ row }">
                        <span class="available-stock">{{ row.availableQuantity || 0 }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="minStock" label="最低库存" width="100">
                    <template #default="{ row }">
                        <span class="min-stock">{{ row.minStock || 0 }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="maxStock" label="最高库存" width="100">
                    <template #default="{ row }">
                        <span class="max-stock">{{ row.maxStock || 0 }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="160" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            详情
                        </el-button>
                        <!-- <el-button type="warning" size="small" @click="handleAdjust(row)">
                            调整数量
                        </el-button> -->
                        <el-button type="warning" size="small" @click="handleSetThreshold(row)">
                            设置预警
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

        <!-- 库存详情对话框 -->
        <el-dialog v-model="detailDialogVisible" title="库存详情" width="800px">
            <div v-if="stockDetail" class="stock-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="种子名称">{{ stockDetail.seedName }}</el-descriptions-item>
                    <el-descriptions-item label="种子编码">{{ stockDetail.seedCode }}</el-descriptions-item>
                    <el-descriptions-item label="批次号">{{ stockDetail.batchNo }}</el-descriptions-item>
                    <el-descriptions-item label="仓库">{{ stockDetail.warehouseName }}</el-descriptions-item>
                    <el-descriptions-item label="库存数量">{{ stockDetail.quantity }}</el-descriptions-item>
                    <el-descriptions-item label="锁定数量">{{ stockDetail.lockedQuantity || 0 }}</el-descriptions-item>
                    <el-descriptions-item label="可用数量">{{ stockDetail.availableQuantity || 0 }}</el-descriptions-item>
                    <el-descriptions-item label="最低库存">{{ stockDetail.minStock || 0 }}</el-descriptions-item>
                    <el-descriptions-item label="最高库存">{{ stockDetail.maxStock || 0 }}</el-descriptions-item>
                </el-descriptions>
            </div>
        </el-dialog>

        <!-- 调整库存对话框 -->
        <el-dialog v-model="adjustDialogVisible" title="调整库存数量" width="500px" @close="handleAdjustDialogClose">
            <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustFormRules" label-width="100px">
                <el-form-item label="当前库存">
                    <el-input v-model="adjustForm.currentQuantity" disabled />
                </el-form-item>
                <el-form-item label="调整类型" prop="adjustType">
                    <el-radio-group v-model="adjustForm.adjustType">
                        <el-radio label="increase">增加</el-radio>
                        <el-radio label="decrease">减少</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="调整数量" prop="adjustQuantity">
                    <el-input-number v-model="adjustForm.adjustQuantity" :min="1" style="width: 100%" />
                </el-form-item>
                <el-form-item label="调整原因" prop="reason">
                    <el-input v-model="adjustForm.reason" type="textarea" :rows="3" placeholder="请输入调整原因" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="adjustDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleAdjustSubmit" :loading="adjustLoading">
                    确定
                </el-button>
            </template>
        </el-dialog>

        <!-- 设置预警阈值对话框 -->
        <el-dialog v-model="thresholdDialogVisible" title="设置库存预警阈值" width="500px" @close="handleThresholdDialogClose">
            <el-form ref="thresholdFormRef" :model="thresholdForm" :rules="thresholdFormRules" label-width="120px">
                <el-form-item label="种子名称">
                    <el-input v-model="thresholdForm.seedName" disabled />
                </el-form-item>
                <el-form-item label="仓库">
                    <el-input v-model="thresholdForm.warehouseName" disabled />
                </el-form-item>
                <el-form-item label="最低库存" prop="minStock">
                    <el-input-number v-model="thresholdForm.minStock" :min="0" style="width: 100%"
                        placeholder="当库存低于此值时触发预警" />
                </el-form-item>
                <el-form-item label="最高库存" prop="maxStock">
                    <el-input-number v-model="thresholdForm.maxStock" :min="0" style="width: 100%"
                        placeholder="当库存高于此值时触发预警" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="thresholdDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleThresholdSubmit" :loading="thresholdLoading">
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
    Download,
} from "@element-plus/icons-vue";
import {
    getInventoryList,
    getInventoryDetail,
    adjustInventory,
    updateStockThreshold,
} from "@/api/inventory";
import { getWarehouseList } from "@/api/inventory";
import { formatMoney } from "@/utils/index";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    seedName: "",
    warehouseId: null,
    seedId: null,
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

// 仓库列表
const warehouseList = ref([]);

// 库存详情对话框
const detailDialogVisible = ref(false);
const stockDetail = ref(null);

// 调整库存对话框
const adjustDialogVisible = ref(false);
const adjustFormRef = ref();
const adjustLoading = ref(false);
const adjustForm = reactive({
    id: null,
    currentQuantity: 0,
    adjustType: "increase",
    adjustQuantity: 0,
    reason: "",
});

// 调整库存验证规则
const adjustFormRules = {
    adjustType: [{ required: true, message: "请选择调整类型", trigger: "change" }],
    adjustQuantity: [{ required: true, message: "请输入调整数量", trigger: "blur" }],
    reason: [{ required: true, message: "请输入调整原因", trigger: "blur" }],
};

// 设置预警阈值对话框
const thresholdDialogVisible = ref(false);
const thresholdFormRef = ref();
const thresholdLoading = ref(false);
const thresholdForm = reactive({
    id: null,
    seedName: "",
    warehouseName: "",
    minStock: 0,
    maxStock: 0,
});

// 预警阈值验证规则
const thresholdFormRules = {
    minStock: [{ required: true, message: "请输入最低库存", trigger: "blur" }],
    maxStock: [{ required: true, message: "请输入最高库存", trigger: "blur" }],
};

// 获取库存列表
const loadInventoryList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getInventoryList(params);
        tableData.value = response.data.list;
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取库存列表失败:", error);
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

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadInventoryList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadInventoryList();
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getInventoryDetail(row.seedId, row.batchId, row.warehouseId);
        stockDetail.value = response.data;
        detailDialogVisible.value = true;
    } catch (error) {
        console.error("获取库存详情失败:", error);
    }
};

// 调整库存
const handleAdjust = (row) => {
    adjustForm.id = row.id;
    adjustForm.currentQuantity = row.quantity;
    adjustForm.adjustType = "increase";
    adjustForm.adjustQuantity = 0;
    adjustForm.reason = "";
    adjustDialogVisible.value = true;
};

// 调整库存提交
const handleAdjustSubmit = async () => {
    try {
        await adjustFormRef.value.validate();
        adjustLoading.value = true;

        await adjustInventory({
            id: adjustForm.id,
            adjustType: adjustForm.adjustType,
            adjustQuantity: adjustForm.adjustQuantity,
            reason: adjustForm.reason,
        });
        ElMessage.success("库存调整成功");
        adjustDialogVisible.value = false;
        loadInventoryList();
    } catch (error) {
        console.error("调整库存失败:", error);
    } finally {
        adjustLoading.value = false;
    }
};

// 关闭调整对话框
const handleAdjustDialogClose = () => {
    adjustFormRef.value?.resetFields();
};

// 设置预警阈值
const handleSetThreshold = (row) => {
    thresholdForm.id = row.id;
    thresholdForm.seedName = row.seedName;
    thresholdForm.warehouseName = row.warehouseName;
    thresholdForm.minStock = row.minStock || 0;
    thresholdForm.maxStock = row.maxStock || 0;
    thresholdDialogVisible.value = true;
};

// 预警阈值提交
const handleThresholdSubmit = async () => {
    try {
        await thresholdFormRef.value.validate();

        // 验证最高库存应大于等于最低库存
        if (thresholdForm.maxStock < thresholdForm.minStock) {
            ElMessage.error("最高库存不能小于最低库存");
            return;
        }

        thresholdLoading.value = true;
        await updateStockThreshold(thresholdForm.id, thresholdForm.minStock, thresholdForm.maxStock);
        ElMessage.success("库存预警阈值设置成功");
        thresholdDialogVisible.value = false;
        loadInventoryList();
    } catch (error) {
        if (error !== false) {
            console.error("设置库存预警阈值失败:", error);
        }
    } finally {
        thresholdLoading.value = false;
    }
};

// 关闭预警阈值对话框
const handleThresholdDialogClose = () => {
    thresholdFormRef.value?.resetFields();
};

// 导出数据
const handleExport = () => {
    ElMessage.info("导出功能开发中...");
};

// 刷新
const handleRefresh = () => {
    loadInventoryList();
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadInventoryList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadInventoryList();
};


// 初始化
onMounted(() => {
    loadInventoryList();
    loadWarehouseList();
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

.stock-table {

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

.stock-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }
}

.stock-value {
    color: #409eff;
    font-weight: 600;
}

.min-stock {
    color: #e6a23c;
    font-weight: 600;
}

.max-stock {
    color: #67c23a;
    font-weight: 600;
}

.amount {
    color: #f56c6c;
    font-weight: 600;
}
</style>
