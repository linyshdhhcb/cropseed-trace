<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="用户名" prop="username">
                <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable style="width: 200px" />
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
                新增用户
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
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="username" label="用户名" min-width="120" />
                <el-table-column prop="realName" label="真实姓名" min-width="120" />
                <el-table-column prop="email" label="邮箱" min-width="180" />
                <el-table-column prop="phone" label="手机号" min-width="130" />
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="160" />
                <el-table-column label="操作" width="250" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="warning" size="small" @click="handleResetPassword(row)">
                            重置密码
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
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="用户名" prop="username">
                            <el-input v-model="formData.username" placeholder="请输入用户名" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="真实姓名" prop="realName">
                            <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="formData.email" placeholder="请输入邮箱" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="手机号" prop="phone">
                            <el-input v-model="formData.phone" placeholder="请输入手机号" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="密码" prop="password" v-if="!formData.id">
                            <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
                        </el-form-item>
                        <el-col :span="12">
                            <el-form-item label="确认密码" prop="confirmPassword" v-if="!formData.id">
                                <el-input v-model="formData.confirmPassword" type="password" placeholder="请确认密码"
                                    show-password />
                            </el-form-item>
                        </el-col>
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
                        <el-form-item label="角色" prop="roleIds">
                            <el-select v-model="formData.roleIds" multiple placeholder="请选择角色" style="width: 100%">
                                <el-option v-for="role in roleList" :key="role.id" :label="role.roleName"
                                    :value="role.id" />
                            </el-select>
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

        <!-- 重置密码对话框 -->
        <el-dialog v-model="resetPasswordDialogVisible" title="重置密码" width="400px">
            <el-form ref="resetPasswordFormRef" :model="resetPasswordForm" :rules="resetPasswordRules"
                label-width="100px">
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码"
                        show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请确认新密码"
                        show-password />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleResetPasswordSubmit" :loading="resetPasswordLoading">
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
    getUserList,
    addUser,
    updateUser,
    deleteUser,
    batchDeleteUser,
    changePassword,
} from "@/api/user";
import { getRoleList } from "@/api/role";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    username: "",
    realName: "",
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
    username: "",
    realName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    status: 1,
    roleIds: [],
    remark: "",
});

// 表单验证规则
const formRules = {
    username: [
        { required: true, message: "请输入用户名", trigger: "blur" },
        { min: 2, max: 20, message: "用户名长度在 2 到 20 个字符", trigger: "blur" },
    ],
    realName: [
        { required: true, message: "请输入真实姓名", trigger: "blur" },
        { min: 2, max: 20, message: "真实姓名长度在 2 到 20 个字符", trigger: "blur" },
    ],
    email: [
        { required: true, message: "请输入邮箱", trigger: "blur" },
        { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
    ],
    phone: [
        { required: true, message: "请输入手机号", trigger: "blur" },
        { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" },
    ],
    password: [
        { required: true, message: "请输入密码", trigger: "blur" },
        { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" },
    ],
    confirmPassword: [
        { required: true, message: "请确认密码", trigger: "blur" },
        {
            validator: (rule, value, callback) => {
                if (value !== formData.password) {
                    callback(new Error("两次输入密码不一致"));
                } else {
                    callback();
                }
            },
            trigger: "blur",
        },
    ],
    status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

// 角色列表
const roleList = ref([]);

// 重置密码对话框
const resetPasswordDialogVisible = ref(false);
const resetPasswordFormRef = ref();
const resetPasswordLoading = ref(false);
const resetPasswordForm = reactive({
    userId: null,
    newPassword: "",
    confirmPassword: "",
});

// 重置密码验证规则
const resetPasswordRules = {
    newPassword: [
        { required: true, message: "请输入新密码", trigger: "blur" },
        { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" },
    ],
    confirmPassword: [
        { required: true, message: "请确认新密码", trigger: "blur" },
        {
            validator: (rule, value, callback) => {
                if (value !== resetPasswordForm.newPassword) {
                    callback(new Error("两次输入密码不一致"));
                } else {
                    callback();
                }
            },
            trigger: "blur",
        },
    ],
};

// 获取用户列表
const loadUserList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getUserList(params);
        tableData.value = response.data.list;
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取用户列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 获取角色列表
const loadRoleList = async () => {
    try {
        const response = await getRoleList();
        roleList.value = response.data;
    } catch (error) {
        console.error("获取角色列表失败:", error);
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadUserList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadUserList();
};

// 新增用户
const handleAdd = () => {
    dialogTitle.value = "新增用户";
    dialogVisible.value = true;
    resetForm();
};

// 编辑用户
const handleEdit = (row) => {
    dialogTitle.value = "编辑用户";
    dialogVisible.value = true;
    Object.assign(formData, row);
};

// 删除用户
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该用户吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteUser(row.id);
        ElMessage.success("删除成功");
        loadUserList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除用户失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的用户吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteUser(ids);
        ElMessage.success("删除成功");
        loadUserList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除用户失败:", error);
        }
    }
};

// 导出数据
const handleExport = () => {
    ElMessage.info("导出功能开发中...");
};

// 重置密码
const handleResetPassword = (row) => {
    resetPasswordForm.userId = row.id;
    resetPasswordDialogVisible.value = true;
    resetPasswordForm.newPassword = "";
    resetPasswordForm.confirmPassword = "";
};

// 提交表单
const handleSubmit = async () => {
    try {
        await formRef.value.validate();
        submitLoading.value = true;

        if (formData.id) {
            await updateUser(formData);
            ElMessage.success("更新成功");
        } else {
            await addUser(formData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadUserList();
    } catch (error) {
        console.error("提交失败:", error);
    } finally {
        submitLoading.value = false;
    }
};

// 重置密码提交
const handleResetPasswordSubmit = async () => {
    try {
        await resetPasswordFormRef.value.validate();
        resetPasswordLoading.value = true;

        await changePassword({
            userId: resetPasswordForm.userId,
            newPassword: resetPasswordForm.newPassword,
        });

        ElMessage.success("密码重置成功");
        resetPasswordDialogVisible.value = false;
    } catch (error) {
        console.error("重置密码失败:", error);
    } finally {
        resetPasswordLoading.value = false;
    }
};

// 重置表单
const resetForm = () => {
    Object.assign(formData, {
        id: null,
        username: "",
        realName: "",
        email: "",
        phone: "",
        password: "",
        confirmPassword: "",
        status: 1,
        roleIds: [],
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
    loadUserList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadUserList();
};

// 初始化
onMounted(() => {
    loadUserList();
    loadRoleList();
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
