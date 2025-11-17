<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="仓库名称" prop="warehouseName">
                <el-input v-model="searchForm.warehouseName" placeholder="请输入仓库名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="仓库编码" prop="warehouseCode">
                <el-input v-model="searchForm.warehouseCode" placeholder="请输入仓库编码" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
                    <el-option label="启用" :value="1" />
                    <el-option label="禁用" :value="0" />
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
                新增仓库
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
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="warehouseName" label="仓库名称" width="150" />
                <el-table-column prop="warehouseCode" label="仓库编码" width="150" />
                <el-table-column prop="warehouseType" label="仓库类型" width="120">
                    <template #default="{ row }">
                        <el-tag v-if="row.warehouseType" type="primary">
                            {{ row.warehouseType }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="location" label="仓库位置" width="150" />
                <el-table-column prop="capacity" label="容量(吨)" width="100" />
                <el-table-column prop="usedCapacity" label="已用容量(吨)" width="120" />
                <el-table-column prop="usageRate" label="利用率" width="120">
                    <template #default="{ row }">
                        <el-progress v-if="row.usageRate" :percentage="parseFloat(row.usageRate)"
                            :color="getUtilizationRateColor(parseFloat(row.usageRate))" :show-text="false" />
                        <span style="margin-left: 8px">{{ row.usageRate ? parseFloat(row.usageRate).toFixed(2) : '0.00'
                            }}%</span>
                    </template>
                </el-table-column>
                <el-table-column prop="manager" label="负责人" width="100" />
                <el-table-column prop="phone" label="联系电话" width="120" />
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="250" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="warning" size="small" @click="handleViewStock(row)">
                            查看库存
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
                        <el-form-item label="仓库名称" prop="warehouseName">
                            <el-input v-model="formData.warehouseName" placeholder="请输入仓库名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="仓库编码" prop="warehouseCode">
                            <el-input v-model="formData.warehouseCode" placeholder="请输入仓库编码" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="仓库类型" prop="warehouseType">
                            <el-select v-model="formData.warehouseType" placeholder="请选择仓库类型" clearable
                                style="width: 100%">
                                <el-option label="普通仓" value="普通仓" />
                                <el-option label="冷藏仓" value="冷藏仓" />
                                <el-option label="冷冻仓" value="冷冻仓" />
                                <el-option label="恒温仓" value="恒温仓" />
                                <el-option label="保税仓" value="保税仓" />
                                <el-option label="其他" value="其他" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="状态" prop="status">
                            <el-radio-group v-model="formData.status">
                                <el-radio :label="1">启用</el-radio>
                                <el-radio :label="0">禁用</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="容量(吨)" prop="capacity">
                            <el-input-number v-model="formData.capacity" :min="0" :precision="2" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="温度范围" prop="temperatureRange">
                            <el-input v-model="formData.temperatureRange" placeholder="如：-18°C ~ -15°C" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="负责人" prop="manager">
                            <el-input v-model="formData.manager" placeholder="请输入负责人" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="联系电话" prop="phone">
                            <el-input v-model="formData.phone" placeholder="请输入联系电话" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="仓库位置" prop="location">
                    <el-input v-model="formData.location" placeholder="请输入仓库位置" />
                </el-form-item>
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
    getWarehouseList,
    addWarehouse,
    updateWarehouse,
    deleteWarehouse,
    batchDeleteWarehouse,
} from "@/api/inventory";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    warehouseName: "",
    warehouseCode: "",
    status: null,
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
    warehouseName: "",
    warehouseCode: "",
    warehouseType: "",
    capacity: null,
    temperatureRange: "",
    manager: "",
    phone: "",
    location: "",
    status: 1,
    remark: "",
});

// 表单验证规则
const formRules = {
    warehouseName: [
        { required: true, message: "请输入仓库名称", trigger: "blur" },
        { min: 2, max: 50, message: "仓库名称长度在 2 到 50 个字符", trigger: "blur" },
    ],
    warehouseCode: [
        { required: true, message: "请输入仓库编码", trigger: "blur" },
        { min: 2, max: 20, message: "仓库编码长度在 2 到 20 个字符", trigger: "blur" },
    ],
    warehouseType: [{ required: false, message: "请选择仓库类型", trigger: "change" }],
    capacity: [{ required: false, message: "请输入容量", trigger: "blur" }],
    manager: [{ required: false, message: "请输入负责人", trigger: "blur" }],
    phone: [
        { required: false, message: "请输入联系电话", trigger: "blur" },
        { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: "请输入正确的手机号或固定电话", trigger: "blur" },
    ],
    location: [{ required: false, message: "请输入仓库位置", trigger: "blur" }],
    status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

// 获取仓库列表
const loadWarehouseList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getWarehouseList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取仓库列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadWarehouseList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadWarehouseList();
};

// 新增仓库
const handleAdd = () => {
    dialogTitle.value = "新增仓库";
    dialogVisible.value = true;
    resetForm();
};

// 编辑仓库
const handleEdit = (row) => {
    dialogTitle.value = "编辑仓库";
    dialogVisible.value = true;
    Object.assign(formData, row);
};

// 查看库存
const handleViewStock = (row) => {
    ElMessage.info(`查看仓库 ${row.warehouseName} 的库存详情`);
    // 这里可以跳转到库存详情页面
};

// 删除仓库
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该仓库吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteWarehouse(row.id);
        ElMessage.success("删除成功");
        loadWarehouseList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除仓库失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的仓库吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteWarehouse(ids);
        ElMessage.success("删除成功");
        loadWarehouseList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除仓库失败:", error);
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
            await updateWarehouse(formData);
            ElMessage.success("更新成功");
        } else {
            await addWarehouse(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadWarehouseList();
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
        warehouseName: "",
        warehouseCode: "",
        warehouseType: "",
        capacity: null,
        temperatureRange: "",
        manager: "",
        phone: "",
        location: "",
        status: 1,
        remark: "",
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
    loadWarehouseList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadWarehouseList();
};


// 获取利用率颜色
const getUtilizationRateColor = (rate) => {
    if (rate >= 90) return "#f56c6c";
    if (rate >= 70) return "#e6a23c";
    return "#67c23a";
};

// 初始化
onMounted(() => {
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
</style>
