<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="角色名称" prop="roleName">
                <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="角色编码" prop="roleCode">
                <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" clearable style="width: 200px" />
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
                新增角色
            </el-button>
            <el-button type="danger" :icon="Delete" :disabled="!multipleSelection.length" @click="handleBatchDelete">
                批量删除
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border stripe
                style="width: 100%">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="roleName" label="角色名称" width="120" />
                <el-table-column prop="roleCode" label="角色编码" width="250" />
                <el-table-column prop="description" label="描述" min-width="200" />
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
                        <el-button type="warning" size="small" @click="handleAssignMenu(row)">
                            分配菜单
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
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="角色名称" prop="roleName">
                            <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="角色编码" prop="roleCode">
                            <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
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
                <el-form-item label="描述" prop="description">
                    <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
                    确定
                </el-button>
            </template>
        </el-dialog>

        <!-- 分配菜单对话框 -->
        <el-dialog v-model="assignMenuDialogVisible" title="分配菜单" width="800px" @close="handleAssignMenuDialogClose">
            <el-tree ref="menuTreeRef" :data="menuTreeData" :props="menuTreeProps" show-checkbox node-key="id"
                :default-checked-keys="checkedMenuIds" check-strictly />
            <template #footer>
                <el-button @click="assignMenuDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleAssignMenuSubmit" :loading="assignMenuLoading">
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
} from "@element-plus/icons-vue";
import {
    getRoleList,
    addRole,
    updateRole,
    deleteRole,
    batchDeleteRole,
    assignMenu,
    getRoleMenus,
} from "@/api/role";
import { getMenuTree } from "@/api/menu";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    roleName: "",
    roleCode: "",
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
    roleName: "",
    roleCode: "",
    description: "",
    status: 1,
    sort: 0,
});

// 表单验证规则
const formRules = {
    roleName: [
        { required: true, message: "请输入角色名称", trigger: "blur" },
        { min: 2, max: 20, message: "角色名称长度在 2 到 20 个字符", trigger: "blur" },
    ],
    roleCode: [
        { required: true, message: "请输入角色编码", trigger: "blur" },
        { min: 2, max: 20, message: "角色编码长度在 2 到 20 个字符", trigger: "blur" },
    ],
    status: [{ required: true, message: "请选择状态", trigger: "change" }],
    sort: [{ required: true, message: "请输入排序", trigger: "blur" }],
};

// 分配菜单对话框
const assignMenuDialogVisible = ref(false);
const assignMenuLoading = ref(false);
const menuTreeRef = ref();
const menuTreeData = ref([]);
const checkedMenuIds = ref([]);
const currentRoleId = ref(null);

// 菜单树配置
const menuTreeProps = {
    children: "children",
    label: "menuName",
};

// 获取角色列表
const loadRoleList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getRoleList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取角色列表失败:", error);
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

// 获取角色菜单
const loadRoleMenus = async (roleId) => {
    try {
        const response = await getRoleMenus(roleId);
        checkedMenuIds.value = response.data;
    } catch (error) {
        console.error("获取角色菜单失败:", error);
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadRoleList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadRoleList();
};

// 新增角色
const handleAdd = () => {
    dialogTitle.value = "新增角色";
    dialogVisible.value = true;
    resetForm();
};

// 编辑角色
const handleEdit = (row) => {
    dialogTitle.value = "编辑角色";
    dialogVisible.value = true;
    Object.assign(formData, row);
};

// 删除角色
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该角色吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteRole(row.id);
        ElMessage.success("删除成功");
        loadRoleList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除角色失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的角色吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteRole(ids);
        ElMessage.success("删除成功");
        loadRoleList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除角色失败:", error);
        }
    }
};

// 分配菜单
const handleAssignMenu = async (row) => {
    currentRoleId.value = row.id;
    assignMenuDialogVisible.value = true;
    await loadRoleMenus(row.id);
};

// 提交表单
const handleSubmit = async () => {
    try {
        await formRef.value.validate();
        submitLoading.value = true;

        if (formData.id) {
            await updateRole(formData);
            ElMessage.success("更新成功");
        } else {
            await addRole(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadRoleList();
    } catch (error) {
        console.error("提交失败:", error);
    } finally {
        submitLoading.value = false;
    }
};

// 分配菜单提交
const handleAssignMenuSubmit = async () => {
    try {
        assignMenuLoading.value = true;

        const checkedKeys = menuTreeRef.value.getCheckedKeys();
        const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys();
        const menuIds = [...checkedKeys, ...halfCheckedKeys];

        await assignMenu({
            roleId: currentRoleId.value,
            menuIds: menuIds,
        });

        ElMessage.success("分配菜单成功");
        assignMenuDialogVisible.value = false;
    } catch (error) {
        console.error("分配菜单失败:", error);
    } finally {
        assignMenuLoading.value = false;
    }
};

// 重置表单
const resetForm = () => {
    Object.assign(formData, {
        id: null,
        roleName: "",
        roleCode: "",
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

// 关闭分配菜单对话框
const handleAssignMenuDialogClose = () => {
    currentRoleId.value = null;
    checkedMenuIds.value = [];
};

// 选择变化
const handleSelectionChange = (selection) => {
    multipleSelection.value = selection;
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadRoleList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadRoleList();
};

// 初始化
onMounted(() => {
    loadRoleList();
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
</style>
