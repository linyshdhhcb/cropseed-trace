<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="菜单名称" prop="menuName">
                <el-input v-model="searchForm.menuName" placeholder="请输入菜单名称" clearable style="width: 200px" />
            </el-form-item>
            <!-- <el-form-item label="菜单类型" prop="menuType">
                <el-select v-model="searchForm.menuType" placeholder="请选择菜单类型" clearable style="width: 150px">
                    <el-option label="目录" :value="1" />
                    <el-option label="菜单" :value="2" />
                    <el-option label="按钮" :value="3" />
                </el-select>
            </el-form-item> -->
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
                    <!-- <el-col :span="12">
                        <el-form-item label="菜单编码" prop="menuCode">
                            <el-input v-model="formData.menuCode" placeholder="请输入菜单编码" />
                        </el-form-item>
                    </el-col> -->
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
        <el-dialog v-model="showIconDialog" title="选择图标" width="800px">
            <!-- 图标源切换和搜索栏 -->
            <div class="icon-selector-header">
                <el-radio-group v-model="iconSource" class="icon-source-tabs">
                    <el-radio-button label="element">Element Plus</el-radio-button>
                    <el-radio-button label="iconpark">IconPark</el-radio-button>
                </el-radio-group>
                <el-input
                    v-model="iconSearchKeyword"
                    placeholder="搜索图标名称..."
                    clearable
                    style="width: 200px"
                    :prefix-icon="Search"
                />
            </div>

            <!-- Element Plus 图标 -->
            <div v-if="iconSource === 'element'" class="icon-list">
                <div
                    v-for="icon in filteredElementIcons"
                    :key="`element-${icon}`"
                    class="icon-item"
                    :class="{ active: formData.icon === icon }"
                    @click="selectIcon(icon, 'element')"
                >
                    <el-icon><component :is="icon" /></el-icon>
                    <span>{{ icon }}</span>
                </div>
                <div v-if="filteredElementIcons.length === 0" class="no-icon-tip">
                    未找到匹配的图标
                </div>
            </div>

            <!-- IconPark 图标 -->
            <div v-else class="icon-list">
                <div
                    v-for="icon in filteredIconParkIcons"
                    :key="`iconpark-${icon}`"
                    class="icon-item"
                    :class="{ active: formData.icon === icon }"
                    @click="selectIcon(icon, 'iconpark')"
                >
                    <span class="iconpark-icon">{{ icon.charAt(0) }}</span>
                    <span>{{ icon }}</span>
                </div>
                <div v-if="filteredIconParkIcons.length === 0" class="no-icon-tip">
                    未找到匹配的图标（需安装 @icon-park/vue-next）
                </div>
            </div>

            <!-- 底部操作 -->
            <template #footer>
                <div class="icon-dialog-footer">
                    <span class="icon-count">共 {{ iconSource === 'element' ? filteredElementIcons.length : filteredIconParkIcons.length }} 个图标</span>
                    <div>
                        <el-button @click="clearIcon">清除图标</el-button>
                        <el-button @click="showIconDialog = false">关闭</el-button>
                    </div>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
    Search,
    Refresh,
    Plus,
    Delete,
} from "@element-plus/icons-vue";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
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
const originalData = ref([]); // 原始完整数据
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
const iconSource = ref("element"); // 图标源：element / iconpark
const iconSearchKeyword = ref(""); // 图标搜索关键词

// 动态获取所有 Element Plus 图标
const elementIconList = Object.keys(ElementPlusIconsVue);

// IconPark 图标列表（示例，实际使用需安装 @icon-park/vue-next）
const iconParkList = [
    "Home", "User", "Setting", "Config", "Data", "Analysis",
    "ChartGraph", "ChartHistogram", "ChartLine", "ChartPie",
    "Document", "Folder", "FolderOpen", "File", "FileText",
    "Search", "Edit", "Delete", "Add", "Reduce",
    "Lock", "Unlock", "Shield", "Key", "Safe",
    "Message", "Mail", "Phone", "Chat", "Comment",
    "Bell", "Alarm", "Time", "Calendar", "Clock",
    "Location", "Map", "Navigation", "Send", "Share",
    "Download", "Upload", "Cloud", "CloudDownload", "CloudUpload",
    "Link", "Unlink", "QRCode", "Scan", "Print",
    "Camera", "Image", "Video", "Music", "Mic",
    "Star", "Heart", "Like", "Bookmark", "Flag",
    "Success", "Error", "Warning", "Info", "Help",
    "Close", "Check", "Minus", "Plus", "More",
    "Menu", "Apps", "Grid", "List", "Table",
    "Eye", "EyeOff", "Refresh", "Sync", "Loading",
    "Power", "Switch", "Terminal", "Code", "Bug",
    "Shop", "Shopping", "Cart", "Coupon", "Wallet",
    "Bank", "Money", "Finance", "Bill", "Invoice"
];

// 过滤后的 Element Plus 图标
const filteredElementIcons = computed(() => {
    if (!iconSearchKeyword.value) return elementIconList;
    const keyword = iconSearchKeyword.value.toLowerCase();
    return elementIconList.filter(icon => icon.toLowerCase().includes(keyword));
});

// 过滤后的 IconPark 图标
const filteredIconParkIcons = computed(() => {
    if (!iconSearchKeyword.value) return iconParkList;
    const keyword = iconSearchKeyword.value.toLowerCase();
    return iconParkList.filter(icon => icon.toLowerCase().includes(keyword));
});

// 获取菜单列表
const loadMenuList = async () => {
    try {
        loading.value = true;
        // 使用树形接口获取菜单数据
        const response = await getMenuTree();
        console.log('菜单树响应数据:', response.data);
        if (response.data && Array.isArray(response.data)) {
            originalData.value = response.data; // 保存原始数据
            applyFilters(); // 应用筛选
            console.log('设置表格数据:', tableData.value);
        } else {
            originalData.value = [];
            tableData.value = [];
        }
    } catch (error) {
        console.error("获取菜单列表失败:", error);
        originalData.value = [];
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
    applyFilters();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    applyFilters();
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
const selectIcon = (icon, source) => {
    // 如果是 IconPark 图标，可以加前缀区分
    formData.icon = source === 'iconpark' ? `iconpark:${icon}` : icon;
    showIconDialog.value = false;
};

// 清除图标
const clearIcon = () => {
    formData.icon = "";
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

// 递归过滤树形数据
const filterTreeData = (data, filters) => {
    if (!data || data.length === 0) return [];
    
    const result = [];
    
    for (const item of data) {
        // 检查当前节点是否匹配筛选条件
        let matches = true;
        
        // 菜单名称筛选
        if (filters.menuName && !item.menuName.toLowerCase().includes(filters.menuName.toLowerCase())) {
            matches = false;
        }
        
        // 菜单类型筛选
        if (filters.menuType !== null && filters.menuType !== undefined && item.menuType !== filters.menuType) {
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
        menuName: searchForm.menuName?.trim() || '',
        menuType: searchForm.menuType,
        status: searchForm.status
    };
    
    // 检查是否有任何筛选条件
    const hasFilters = filters.menuName || 
                      filters.menuType !== null && filters.menuType !== undefined || 
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

.icon-selector-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;

    .icon-source-tabs {
        flex-shrink: 0;
    }
}

.icon-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 8px;
    max-height: 400px;
    overflow-y: auto;
    padding: 4px;

    .icon-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 12px 8px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
            background-color: #ecf5ff;
            border-color: #409eff;
            transform: translateY(-2px);
        }

        &.active {
            background-color: #409eff;
            border-color: #409eff;
            color: #fff;

            .el-icon {
                color: #fff;
            }

            span {
                color: #fff;
            }
        }

        .el-icon {
            font-size: 24px;
            margin-bottom: 6px;
            color: #606266;
        }

        .iconpark-icon {
            width: 24px;
            height: 24px;
            line-height: 24px;
            text-align: center;
            font-size: 14px;
            font-weight: bold;
            background: #409eff;
            color: #fff;
            border-radius: 4px;
            margin-bottom: 6px;
        }

        span {
            font-size: 11px;
            color: #909399;
            text-align: center;
            word-break: break-all;
            line-height: 1.2;
        }
    }

    .no-icon-tip {
        grid-column: 1 / -1;
        text-align: center;
        padding: 40px;
        color: #909399;
        font-size: 14px;
    }
}

.icon-dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .icon-count {
        color: #909399;
        font-size: 13px;
    }
}
</style>
