<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="昵称" prop="nickname">
                <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
                <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable style="width: 200px" />
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

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" border stripe>
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="nickname" label="昵称" width="120" />
                <el-table-column prop="avatarUrl" label="头像" width="80">
                    <template #default="{ row }">
                        <el-avatar :src="row.avatarUrl" :size="40">
                            <img src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                        </el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="openid" label="OpenID" width="200" show-overflow-tooltip />
                <el-table-column prop="phone" label="手机号" width="130" />
                <el-table-column prop="gender" label="性别" width="80">
                    <template #default="{ row }">
                        <span v-if="row.gender === 1">男</span>
                        <span v-else-if="row.gender === 2">女</span>
                        <span v-else>未知</span>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            详情
                        </el-button>
                        <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small"
                            @click="handleToggleStatus(row)">
                            {{ row.status === 1 ? '禁用' : '启用' }}
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

        <!-- 查看详情对话框 -->
        <el-dialog v-model="viewDialogVisible" title="用户详情" width="600px">
            <div v-if="userDetail" class="user-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="用户ID">{{ userDetail.id }}</el-descriptions-item>
                    <el-descriptions-item label="OpenID">{{ userDetail.openid }}</el-descriptions-item>
                    <el-descriptions-item label="UnionID">{{ userDetail.unionid || '未绑定' }}</el-descriptions-item>
                    <el-descriptions-item label="昵称">{{ userDetail.nickname }}</el-descriptions-item>
                    <el-descriptions-item label="头像">
                        <el-avatar :src="userDetail.avatarUrl" :size="50">
                            <img src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                        </el-avatar>
                    </el-descriptions-item>
                    <el-descriptions-item label="性别">
                        <span v-if="userDetail.gender === 1">男</span>
                        <span v-else-if="userDetail.gender === 2">女</span>
                        <span v-else>未知</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="手机号">{{ userDetail.phone || '未绑定' }}</el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-tag :type="userDetail.status === 1 ? 'success' : 'danger'">
                            {{ userDetail.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="国家">{{ userDetail.country || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="省份">{{ userDetail.province || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="城市">{{ userDetail.city || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="最后登录时间">{{ userDetail.lastLoginTime || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{ userDetail.createTime }}</el-descriptions-item>
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
} from "@element-plus/icons-vue";
import {
    getWxUserList,
    getWxUserDetail,
    updateWxUserStatus,
    deleteWxUser,
} from "@/api/wechat";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    nickname: "",
    phone: "",
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

// 查看详情对话框
const viewDialogVisible = ref(false);
const userDetail = ref(null);

// 获取用户列表
const loadUserList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getWxUserList(params);
        tableData.value = response.data.list;
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取用户列表失败:", error);
    } finally {
        loading.value = false;
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

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getWxUserDetail(row.id);
        userDetail.value = response.data;
        viewDialogVisible.value = true;
    } catch (error) {
        console.error("获取用户详情失败:", error);
        ElMessage.error("获取用户详情失败");
    }
};

// 切换状态
const handleToggleStatus = async (row) => {
    try {
        const newStatus = row.status === 1 ? 0 : 1;
        const statusText = newStatus === 1 ? "启用" : "禁用";
        await ElMessageBox.confirm(`确定要${statusText}该用户吗？`, "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateWxUserStatus(row.id, newStatus);
        ElMessage.success(`${statusText}成功`);
        loadUserList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("操作失败:", error);
            const errorMessage = error?.response?.data?.message || error?.message || "操作失败，请稍后重试";
            ElMessage.error(errorMessage);
        }
    }
};

// 删除
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该用户吗？删除后无法恢复！", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteWxUser(row.id);
        ElMessage.success("删除成功");
        loadUserList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除失败:", error);
            const errorMessage = error?.response?.data?.message || error?.message || "删除失败，请稍后重试";
            ElMessage.error(errorMessage);
        }
    }
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

    .page-container {
        .pagination-container {
            margin-top: 20px;
            text-align: center;
        }
    }
}

.user-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }
}
</style>
