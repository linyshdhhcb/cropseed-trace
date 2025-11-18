<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="品类名称" prop="categoryName">
                <el-input v-model="searchForm.categoryName" placeholder="请输入品类名称" clearable style="width: 200px" />
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
                新增品类
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
            <el-table ref="tableRef" v-loading="loading" :data="tableData" @selection-change="handleSelectionChange"
                style="width: 100%" row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                :default-expand-all="false" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="categoryName" label="品类名称" width="200" />
                <el-table-column prop="categoryCode" label="品类编码" width="150" />
                <el-table-column prop="level" label="级别" width="80" />
                <el-table-column prop="description" label="描述" min-width="200" />
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sort" label="排序" width="80" />
                <el-table-column label="操作" width="240" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="success" size="small" @click="handleAddChild(row)">
                            新增子品类
                        </el-button>
                        <el-button type="danger" size="small" @click="handleDelete(row)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="上级品类" prop="parentId">
                            <el-tree-select v-model="formData.parentId" :data="categoryTreeData"
                                :props="categoryTreeProps" placeholder="请选择上级品类" check-strictly clearable />
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
                        <el-form-item label="品类名称" prop="categoryName">
                            <el-input v-model="formData.categoryName" placeholder="请输入品类名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="品类编码" prop="categoryCode">
                            <el-input v-model="formData.categoryCode" placeholder="请输入品类编码" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="排序" prop="sort">
                            <el-input-number v-model="formData.sort" :min="0" :max="999" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="描述" prop="description">
                    <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入品类描述" />
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
    getCategoryList,
    addCategory,
    updateCategory,
    deleteCategory,
    batchDeleteCategory,
    getCategoryTree,
} from "@/api/seed";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    categoryName: "",
    status: null,
});

// 表格数据
const loading = ref(false);
const tableData = ref([]);
const originalData = ref([]); // 原始完整数据
const multipleSelection = ref([]);
const tableRef = ref();

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const submitLoading = ref(false);

// 表单数据
const formData = reactive({
    id: null,
    parentId: null,
    categoryName: "",
    categoryCode: "",
    description: "",
    status: 1,
    sort: 0,
});

// 表单验证规则
const formRules = {
    categoryName: [
        { required: true, message: "请输入品类名称", trigger: "blur" },
        { min: 2, max: 20, message: "品类名称长度在 2 到 20 个字符", trigger: "blur" },
    ],
    categoryCode: [
        { required: true, message: "请输入品类编码", trigger: "blur" },
        { min: 2, max: 20, message: "品类编码长度在 2 到 20 个字符", trigger: "blur" },
    ],
    status: [{ required: true, message: "请选择状态", trigger: "change" }],
    sort: [{ required: true, message: "请输入排序", trigger: "blur" }],
};

// 品类树数据
const categoryTreeData = ref([]);
const categoryTreeProps = {
    children: "children",
    label: "categoryName",
    value: "id",
};

// 获取品类列表
const loadCategoryList = async () => {
    try {
        loading.value = true;
        const response = await getCategoryTree();
        originalData.value = response.data; // 保存原始数据
        applyFilters(); // 应用筛选
    } catch (error) {
        console.error("获取品类列表失败:", error);
        originalData.value = [];
        tableData.value = [];
    } finally {
        loading.value = false;
    }
};

// 获取品类树
const loadCategoryTree = async () => {
    try {
        const response = await getCategoryTree();
        categoryTreeData.value = response.data;
    } catch (error) {
        console.error("获取品类树失败:", error);
    }
};

// 搜索
const handleSearch = () => {
    applyFilters();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    applyFilters();
};

// 新增品类
const handleAdd = () => {
    dialogTitle.value = "新增品类";
    dialogVisible.value = true;
    resetForm();
};

// 新增子品类
const handleAddChild = (row) => {
    dialogTitle.value = "新增子品类";
    dialogVisible.value = true;
    resetForm();
    formData.parentId = row.id;
};

// 编辑品类
const handleEdit = (row) => {
    dialogTitle.value = "编辑品类";
    dialogVisible.value = true;
    Object.assign(formData, row);
};

// 删除品类
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该品类吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteCategory(row.id);
        ElMessage.success("删除成功");
        loadCategoryList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除品类失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的品类吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteCategory(ids);
        ElMessage.success("删除成功");
        loadCategoryList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除品类失败:", error);
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
            await updateCategory(formData);
            ElMessage.success("更新成功");
        } else {
            await addCategory(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadCategoryList();
        loadCategoryTree();
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
        parentId: null,
        categoryName: "",
        categoryCode: "",
        description: "",
        status: 1,
        sort: 0,
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

// 递归过滤树形数据
const filterTreeData = (data, filters) => {
    if (!data || data.length === 0) return [];
    
    const result = [];
    
    for (const item of data) {
        // 检查当前节点是否匹配筛选条件
        let matches = true;
        
        // 品类名称筛选
        if (filters.categoryName && !item.categoryName.toLowerCase().includes(filters.categoryName.toLowerCase())) {
            matches = false;
        }
        
        // 状态筛选
        if (filters.status !== null && filters.status !== undefined && item.status !== filters.status) {
            matches = false;
        }
        
        // 递归处理子节点
        const filteredChildren = item.children ? filterTreeData(item.children, filters) : [];
        
        // 如果当前节点匹配或有子节点匹配，则保留该节点
        if (matches || filteredChildren.length > 0) {
            const newItem = { ...item };
            if (filteredChildren.length > 0) {
                newItem.children = filteredChildren;
            } else {
                // 如果没有匹配的子节点，移除children属性以避免展开
                delete newItem.children;
            }
            result.push(newItem);
        }
    }
    
    return result;
};

// 应用筛选条件
const applyFilters = () => {
    const filters = {
        categoryName: searchForm.categoryName?.trim() || '',
        status: searchForm.status
    };
    
    // 检查是否有任何筛选条件
    const hasFilters = filters.categoryName || 
                      filters.status !== null && filters.status !== undefined;
    
    if (hasFilters) {
        // 应用筛选
        tableData.value = filterTreeData(originalData.value, filters);
    } else {
        // 没有筛选条件，显示所有数据
        tableData.value = originalData.value;
    }
};

// 初始化
onMounted(() => {
    loadCategoryList();
    loadCategoryTree();
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
