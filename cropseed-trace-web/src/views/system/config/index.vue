<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="配置键" prop="configKey">
                <el-input v-model="searchForm.configKey" placeholder="请输入配置键" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="配置名称" prop="configName">
                <el-input v-model="searchForm.configName" placeholder="请输入配置名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="配置类型" prop="configType">
                <el-select v-model="searchForm.configType" placeholder="请选择类型" clearable style="width: 150px">
                    <el-option label="系统配置" :value="1" />
                    <el-option label="业务配置" :value="2" />
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
                新增配置
            </el-button>
            <el-button type="danger" :icon="Delete" :disabled="!multipleSelection.length" @click="handleBatchDelete">
                批量删除
            </el-button>
            <el-button type="warning" :icon="RefreshRight" @click="handleRefreshCache">
                刷新缓存
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border stripe
                style="width: 100%">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="configName" label="配置名称" width="150" />
                <el-table-column prop="configKey" label="配置键" width="200" show-overflow-tooltip />
                <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
                <el-table-column prop="configType" label="配置类型" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.configType === 1 ? 'primary' : 'success'">
                            {{ row.configType === 1 ? '系统配置' : '业务配置' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="danger" size="small" @click="handleDelete(row)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
                    :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper" :hide-on-single-page="false"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-form-item label="配置名称" prop="configName">
                    <el-input v-model="formData.configName" placeholder="请输入配置名称" />
                </el-form-item>
                <el-form-item label="配置键" prop="configKey">
                    <el-input v-model="formData.configKey" placeholder="请输入配置键（唯一标识）" />
                </el-form-item>
                <el-form-item label="配置值" prop="configValue">
                    <el-input v-model="formData.configValue" type="textarea" :rows="4" placeholder="请输入配置值" />
                </el-form-item>
                <el-form-item label="配置类型" prop="configType">
                    <el-radio-group v-model="formData.configType">
                        <el-radio :label="1">系统配置</el-radio>
                        <el-radio :label="2">业务配置</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="描述" prop="description">
                    <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入配置描述" />
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
    RefreshRight,
} from "@element-plus/icons-vue";
import {
    getConfigList,
    addConfig,
    updateConfig,
    deleteConfig,
    batchDeleteConfig,
    refreshConfigCache,
} from "@/api/config";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    configKey: "",
    configName: "",
    configType: null,
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
    configKey: "",
    configValue: "",
    configName: "",
    configType: 1,
    description: "",
});

// 表单验证规则
const formRules = {
    configName: [
        { required: true, message: "请输入配置名称", trigger: "blur" },
        { max: 100, message: "配置名称长度不能超过100个字符", trigger: "blur" },
    ],
    configKey: [
        { required: true, message: "请输入配置键", trigger: "blur" },
        { max: 100, message: "配置键长度不能超过100个字符", trigger: "blur" },
        { pattern: /^[a-zA-Z][a-zA-Z0-9._-]*$/, message: "配置键必须以字母开头，只能包含字母、数字、下划线、点和短横线", trigger: "blur" },
    ],
    configValue: [{ required: true, message: "请输入配置值", trigger: "blur" }],
    configType: [{ required: true, message: "请选择配置类型", trigger: "change" }],
};

// 获取配置列表
const loadConfigList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getConfigList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取配置列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadConfigList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadConfigList();
};

// 新增配置
const handleAdd = () => {
    dialogTitle.value = "新增配置";
    dialogVisible.value = true;
    resetForm();
};

// 编辑配置
const handleEdit = (row) => {
    dialogTitle.value = "编辑配置";
    dialogVisible.value = true;
    Object.assign(formData, row);
};

// 删除配置
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该配置吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteConfig(row.id);
        ElMessage.success("删除成功");
        loadConfigList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除配置失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的配置吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteConfig(ids);
        ElMessage.success("删除成功");
        loadConfigList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除配置失败:", error);
        }
    }
};

// 刷新缓存
const handleRefreshCache = async () => {
    try {
        await refreshConfigCache();
        ElMessage.success("缓存刷新成功");
    } catch (error) {
        console.error("刷新缓存失败:", error);
    }
};

// 提交表单
const handleSubmit = async () => {
    try {
        await formRef.value.validate();
        submitLoading.value = true;

        if (formData.id) {
            await updateConfig(formData);
            ElMessage.success("更新成功");
        } else {
            await addConfig(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadConfigList();
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
        configKey: "",
        configValue: "",
        configName: "",
        configType: 1,
        description: "",
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
    loadConfigList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadConfigList();
};

// 初始化
onMounted(() => {
    loadConfigList();
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
