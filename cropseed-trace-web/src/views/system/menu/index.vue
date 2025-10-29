<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="菜单名称" prop="menuName">
                <el-input v-model="searchForm.menuName" placeholder="请输入菜单名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="菜单类型" prop="menuType">
                <el-select v-model="searchForm.menuType" placeholder="请选择菜单类型" clearable style="width: 150px">
                    <el-option label="目录" :value="1" />
                    <el-option label="菜单" :value="2" />
                    <el-option label="按钮" :value="3" />
                </el-select>
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
                新增菜单
            </el-button>
            <el-button type="danger" :icon="Delete" :disabled="!multipleSelection.length" @click="handleBatchDelete">
                批量删除
            </el-button>
            <el-button type="success" :icon="Refresh" @click="handleExpandAll">
                展开菜单
            </el-button>
            <el-button type="warning" :icon="Refresh" @click="handleCollapseAll">
                折叠全部
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table ref="tableRef" v-loading="loading" :data="tableData" @selection-change="handleSelectionChange"
                @expand-change="handleExpandChange" style="width: 100%" row-key="id"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" :default-expand-all="false"
                :expand-row-keys="expandedRowKeys" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="menuName" label="菜单名称" width="150" />
                <el-table-column prop="menuType" label="菜单类型" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getMenuTypeTagType(row.menuType)">
                            {{ getMenuTypeText(row.menuType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="icon" label="图标" width="80">
                    <template #default="{ row }">
                        <el-icon v-if="row.icon">
                            <component :is="row.icon" />
                        </el-icon>
                    </template>
                </el-table-column>
                <el-table-column prop="path" label="路由路径" width="200" />
                <el-table-column prop="component" label="组件路径" width="200" />
                <el-table-column prop="perms" label="权限标识" width="200" />
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sort" label="排序" width="80" />
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="250" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="success" size="small" @click="handleAddChild(row)">
                            新增子菜单
                        </el-button>
                        <el-button type="danger" size="small" @click="handleDelete(row)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="上级菜单" prop="parentId">
                            <el-tree-select v-model="formData.parentId" :data="menuTreeData" :props="menuTreeProps"
                                placeholder="请选择上级菜单" check-strictly clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="菜单类型" prop="menuType">
                            <el-radio-group v-model="formData.menuType" @change="handleMenuTypeChange">
                                <el-radio :label="1">目录</el-radio>
                                <el-radio :label="2">菜单</el-radio>
                                <el-radio :label="3">按钮</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="菜单名称" prop="menuName">
                            <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="菜单编码" prop="menuCode">
                            <el-input v-model="formData.menuCode" placeholder="请输入菜单编码" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20" v-if="formData.menuType !== 3">
                    <el-col :span="12">
                        <el-form-item label="路由路径" prop="path">
                            <el-input v-model="formData.path" placeholder="请输入路由路径" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="组件路径" prop="component">
                            <el-input v-model="formData.component" placeholder="请输入组件路径" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="菜单图标" prop="icon">
                            <el-input v-model="formData.icon" placeholder="请输入图标名称">
                                <template #append>
                                    <el-button @click="showIconDialog = true">选择图标</el-button>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="权限标识" prop="perms">
                            <el-input v-model="formData.perms" placeholder="请输入权限标识" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="状态" prop="status">
                            <el-radio-group v-model="formData.status">
                                <el-radio :label="1">启用</el-radio>
                                <el-radio :label="0">禁用</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="排序" prop="sort">
                            <el-input-number v-model="formData.sort" :min="0" :max="999" />
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

        <!-- 图标选择对话框 -->
        <el-dialog v-model="showIconDialog" title="选择图标" width="600px">
            <div class="icon-list">
                <div v-for="icon in iconList" :key="icon" class="icon-item" @click="selectIcon(icon)">
                    <el-icon>
                        <component :is="icon" />
                    </el-icon>
                    <span>{{ icon }}</span>
                </div>
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
} from "@element-plus/icons-vue";
import {
    getMenuList,
    addMenu,
    updateMenu,
    deleteMenu,
    batchDeleteMenu,
    getMenuTree,
} from "@/api/menu";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    menuName: "",
    menuType: null,
    status: null,
});

// 表格数据
const loading = ref(false);
const tableData = ref([]);
const multipleSelection = ref([]);
const tableRef = ref();
const expandedRowKeys = ref([]);

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const submitLoading = ref(false);

// 表单数据
const formData = reactive({
    id: null,
    parentId: null,
    menuName: "",
    menuCode: "",
    menuType: 1,
    path: "",
    component: "",
    icon: "",
    perms: "",
    status: 1,
    sort: 0,
    remark: "",
});

// 表单验证规则
const formRules = {
    menuName: [
        { required: true, message: "请输入菜单名称", trigger: "blur" },
        { min: 2, max: 20, message: "菜单名称长度在 2 到 20 个字符", trigger: "blur" },
    ],
    menuCode: [
        { required: true, message: "请输入菜单编码", trigger: "blur" },
        { min: 2, max: 20, message: "菜单编码长度在 2 到 20 个字符", trigger: "blur" },
    ],
    menuType: [{ required: true, message: "请选择菜单类型", trigger: "change" }],
    status: [{ required: true, message: "请选择状态", trigger: "change" }],
    sort: [{ required: true, message: "请输入排序", trigger: "blur" }],
    perms: [{ required: true, message: "请输入权限标识", trigger: "blur" }],
};

// 菜单树数据
const menuTreeData = ref([]);
const menuTreeProps = {
    children: "children",
    label: "menuName",
    value: "id",
};

// 图标选择
const showIconDialog = ref(false);
const iconList = ref([
    "House", "User", "Setting", "Document", "Folder", "Files", "FolderOpened",
    "Edit", "Delete", "View", "Hide", "Show", "Refresh", "Search", "Plus",
    "Minus", "Check", "Close", "Warning", "Info", "Success", "Error",
    "Question", "Star", "StarFilled", "Location", "Phone", "Message",
    "ChatDotRound", "ChatLineRound", "ChatSquare", "ChatLineSquare",
    "Bell", "BellFilled", "Notification", "NotificationFilled",
    "Lock", "Unlock", "Key", "Shield", "ShieldCheck", "ShieldClose",
    "CircleCheck", "CircleClose", "CircleCheckFilled", "CircleCloseFilled",
    "WarningFilled", "InfoFilled", "SuccessFilled", "ErrorFilled",
    "QuestionFilled", "StarFilled", "LocationFilled", "PhoneFilled",
    "MessageFilled", "ChatDotRoundFilled", "ChatLineRoundFilled",
    "ChatSquareFilled", "ChatLineSquareFilled", "BellFilled",
    "NotificationFilled", "LockFilled", "UnlockFilled", "KeyFilled",
    "ShieldFilled", "ShieldCheckFilled", "ShieldCloseFilled"
]);

// 获取菜单列表
const loadMenuList = async () => {
    try {
        loading.value = true;
        // 使用树形接口获取菜单数据
        const response = await getMenuTree();
        console.log('菜单树响应数据:', response.data);
        if (response.data && Array.isArray(response.data)) {
            tableData.value = response.data;
            console.log('设置表格数据:', tableData.value);
        } else {
            tableData.value = [];
        }
    } catch (error) {
        console.error("获取菜单列表失败:", error);
        tableData.value = [];
    } finally {
        loading.value = false;
    }
};

// 获取菜单树
const loadMenuTree = async () => {
    try {
        const response = await getMenuTree();
        menuTreeData.value = response.data;
    } catch (error) {
        console.error("获取菜单树失败:", error);
    }
};

// 搜索
const handleSearch = () => {
    loadMenuList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    loadMenuList();
};

// 新增菜单
const handleAdd = () => {
    dialogTitle.value = "新增菜单";
    dialogVisible.value = true;
    resetForm();
};

// 新增子菜单
const handleAddChild = (row) => {
    dialogTitle.value = "新增子菜单";
    dialogVisible.value = true;
    resetForm();
    formData.parentId = row.id;
};

// 编辑菜单
const handleEdit = (row) => {
    dialogTitle.value = "编辑菜单";
    dialogVisible.value = true;
    Object.assign(formData, row);
};

// 删除菜单
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该菜单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteMenu(row.id);
        ElMessage.success("删除成功");
        loadMenuList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除菜单失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的菜单吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteMenu(ids);
        ElMessage.success("删除成功");
        loadMenuList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除菜单失败:", error);
        }
    }
};

// 展开全部
const handleExpandAll = () => {
    // 递归展开所有行
    const expandAll = (rows) => {
        rows.forEach(row => {
            tableRef.value.toggleRowExpansion(row, true);
            if (row.children && row.children.length > 0) {
                expandAll(row.children);
            }
        });
    };
    expandAll(tableData.value);
};

// 折叠全部
const handleCollapseAll = () => {
    // 递归折叠所有行
    const collapseAll = (rows) => {
        rows.forEach(row => {
            tableRef.value.toggleRowExpansion(row, false);
            if (row.children && row.children.length > 0) {
                collapseAll(row.children);
            }
        });
    };
    collapseAll(tableData.value);
};

// 菜单类型变化
const handleMenuTypeChange = (value) => {
    if (value === 1) {
        // 目录
        formData.component = "Layout";
        formData.path = "";
    } else if (value === 2) {
        // 菜单
        formData.component = "";
        formData.path = "";
    } else if (value === 3) {
        // 按钮
        formData.component = "";
        formData.path = "";
    }
};

// 选择图标
const selectIcon = (icon) => {
    formData.icon = icon;
    showIconDialog.value = false;
};

// 提交表单
const handleSubmit = async () => {
    try {
        await formRef.value.validate();
        submitLoading.value = true;

        if (formData.id) {
            await updateMenu(formData);
            ElMessage.success("更新成功");
        } else {
            await addMenu(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadMenuList();
        loadMenuTree();
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
        menuName: "",
        menuCode: "",
        menuType: 1,
        path: "",
        component: "",
        icon: "",
        perms: "",
        status: 1,
        sort: 0,
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

// 展开变化
const handleExpandChange = (row, expandedRows) => {
    // 更新展开状态
    if (expandedRows.includes(row)) {
        // 展开
        if (!expandedRowKeys.value.includes(row.id)) {
            expandedRowKeys.value.push(row.id);
        }
    } else {
        // 折叠
        const index = expandedRowKeys.value.indexOf(row.id);
        if (index > -1) {
            expandedRowKeys.value.splice(index, 1);
        }
    }
};

// 获取菜单类型标签类型
const getMenuTypeTagType = (type) => {
    const typeMap = {
        1: "primary",
        2: "success",
        3: "warning",
    };
    return typeMap[type] || "info";
};

// 获取菜单类型文本
const getMenuTypeText = (type) => {
    const typeMap = {
        1: "目录",
        2: "菜单",
        3: "按钮",
    };
    return typeMap[type] || "未知";
};

// 初始化
onMounted(() => {
    loadMenuList();
    loadMenuTree();
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

.icon-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 10px;
    max-height: 400px;
    overflow-y: auto;

    .icon-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 10px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
            background-color: #f5f7fa;
            border-color: #409eff;
        }

        .el-icon {
            font-size: 20px;
            margin-bottom: 5px;
        }

        span {
            font-size: 12px;
            color: #606266;
        }
    }
}
</style>
