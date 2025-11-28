<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="操作人" prop="username">
                <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="操作模块" prop="module">
                <el-input v-model="searchForm.module" placeholder="请输入操作模块" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="操作内容" prop="content">
                <el-input v-model="searchForm.content" placeholder="请输入操作内容" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
                    <el-option label="成功" :value="1" />
                    <el-option label="失败" :value="0" />
                </el-select>
            </el-form-item>
            <el-form-item label="IP地址" prop="ip">
                <el-input v-model="searchForm.ip" placeholder="请输入IP地址" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="操作时间">
                <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间"
                    end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 360px" />
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
            <el-button type="danger" :icon="Delete" :disabled="!multipleSelection.length" @click="handleBatchDelete">
                批量删除
            </el-button>
            <el-button type="danger" :icon="Delete" @click="handleClear">
                清空日志
            </el-button>
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <!-- 临时调试信息 -->
            <el-alert type="info" :closable="false" style="margin-bottom: 10px;">
                数据总数: {{ pagination.total }} | 当前页: {{ pagination.current }} | 每页: {{ pagination.size }} | 
                表格数据: {{ tableData.length }} 条
            </el-alert>
            
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="username" label="操作人" min-width="120" />
                <el-table-column prop="module" label="操作模块" min-width="150" />
                <el-table-column prop="content" label="操作内容" min-width="200" show-overflow-tooltip />
                <el-table-column prop="method" label="请求方法" min-width="120" show-overflow-tooltip />
                <el-table-column prop="ip" label="IP地址" min-width="140" />
                <el-table-column prop="ipRegion" label="IP地区" min-width="120" show-overflow-tooltip />
                <el-table-column prop="executeTime" label="执行时长" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getExecuteTimeType(row.executeTime)">
                            {{ row.executeTime }}ms
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '成功' : '失败' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="操作时间" min-width="160" />
                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            详情
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

        <!-- 详情对话框 -->
        <el-dialog v-model="detailVisible" title="操作日志详情" width="800px" :close-on-click-modal="false">
            <el-descriptions :column="2" border v-if="currentLog">
                <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
                <el-descriptions-item label="操作人">{{ currentLog.username }}</el-descriptions-item>
                <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
                <el-descriptions-item label="操作内容">{{ currentLog.content }}</el-descriptions-item>
                <el-descriptions-item label="请求路径" :span="2">{{ currentLog.url }}</el-descriptions-item>
                <el-descriptions-item label="请求方法">{{ currentLog.method }}</el-descriptions-item>
                <el-descriptions-item label="执行时长">
                    <el-tag :type="getExecuteTimeType(currentLog.executeTime)">
                        {{ currentLog.executeTime }}ms
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
                <el-descriptions-item label="IP地区">{{ currentLog.ipRegion || '-' }}</el-descriptions-item>
                <el-descriptions-item label="操作状态">
                    <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
                        {{ currentLog.status === 1 ? '成功' : '失败' }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
                <el-descriptions-item label="请求参数" :span="2">
                    <el-input v-model="currentLog.param" type="textarea" :rows="4" readonly />
                </el-descriptions-item>
                <el-descriptions-item label="用户代理" :span="2">
                    <el-input v-model="currentLog.userAgent" type="textarea" :rows="2" readonly />
                </el-descriptions-item>
                <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
                    <el-input v-model="currentLog.errorMessage" type="textarea" :rows="4" readonly />
                </el-descriptions-item>
            </el-descriptions>
            <template #footer>
                <el-button @click="detailVisible = false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh, Delete, Download } from '@element-plus/icons-vue';
import {
    getOperationLogList,
    getOperationLogDetail,
    deleteOperationLog,
    batchDeleteOperationLog,
    clearOperationLog
} from '@/api/operationlog';

// 表格数据
const tableData = ref([]);
const loading = ref(false);
const multipleSelection = ref([]);

// 分页信息
const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
});

// 搜索表单
const searchFormRef = ref(null);
const searchForm = reactive({
    username: '',
    module: '',
    content: '',
    status: null,
    ip: ''
});

// 日期范围
const dateRange = ref([]);

// 详情对话框
const detailVisible = ref(false);
const currentLog = ref(null);

// 获取执行时长类型
const getExecuteTimeType = (time) => {
    if (time < 500) return 'success';
    if (time < 1000) return 'warning';
    return 'danger';
};

// 获取列表数据
const fetchData = async () => {
    loading.value = true;
    try {
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm
        };

        // 添加时间范围
        if (dateRange.value && dateRange.value.length === 2) {
            params.startTime = dateRange.value[0];
            params.endTime = dateRange.value[1];
        }

        const { data } = await getOperationLogList(params);
        tableData.value = data.list || [];
        pagination.total = parseInt(data.total) || 0;
    } catch (error) {
        console.error('获取操作日志列表失败:', error);
        ElMessage.error('获取操作日志列表失败');
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    fetchData();
};

// 重置
const handleReset = () => {
    searchFormRef.value?.resetFields();
    dateRange.value = [];
    pagination.current = 1;
    fetchData();
};

// 分页大小改变
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    fetchData();
};

// 页码改变
const handleCurrentChange = (page) => {
    pagination.current = page;
    fetchData();
};

// 表格选择改变
const handleSelectionChange = (selection) => {
    multipleSelection.value = selection;
};

// 查看详情
const handleView = async (row) => {
    try {
        const { data } = await getOperationLogDetail(row.id);
        currentLog.value = data;
        detailVisible.value = true;
    } catch (error) {
        console.error('获取日志详情失败:', error);
        ElMessage.error('获取日志详情失败');
    }
};

// 删除
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除该操作日志吗？`,
            '提示',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        );

        await deleteOperationLog(row.id);
        ElMessage.success('删除成功');
        fetchData();
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除操作日志失败:', error);
            ElMessage.error('删除操作日志失败');
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    if (!multipleSelection.value.length) {
        ElMessage.warning('请选择要删除的日志');
        return;
    }

    try {
        await ElMessageBox.confirm(
            `确定要删除选中的 ${multipleSelection.value.length} 条操作日志吗？`,
            '提示',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        );

        const ids = multipleSelection.value.map(item => item.id);
        await batchDeleteOperationLog(ids);
        ElMessage.success('批量删除成功');
        fetchData();
    } catch (error) {
        if (error !== 'cancel') {
            console.error('批量删除操作日志失败:', error);
            ElMessage.error('批量删除操作日志失败');
        }
    }
};

// 清空日志
const handleClear = async () => {
    try {
        await ElMessageBox.confirm(
            '确定要清空所有操作日志吗？此操作不可恢复！',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'error'
            }
        );

        await clearOperationLog();
        ElMessage.success('清空成功');
        fetchData();
    } catch (error) {
        if (error !== 'cancel') {
            console.error('清空操作日志失败:', error);
            ElMessage.error('清空操作日志失败');
        }
    }
};

// 导出数据
const handleExport = () => {
    ElMessage.info('导出功能开发中...');
};

// 页面加载时获取数据
onMounted(() => {
    fetchData();
});
</script>

<style scoped lang="scss">
.app-container {
    padding: 20px;

    .search-form {
        background: #fff;
        padding: 20px;
        border-radius: 4px;
        margin-bottom: 20px;
    }

    .button-group {
        margin-bottom: 20px;
    }

    .page-container {
        min-height: 200px;

        .pagination-container {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            padding: 20px 0;
            margin-top: 20px;
            min-height: 32px;
            justify-content: center;
        }
    }
}

// 确保分页组件可见
:deep(.el-pagination) {
    display: flex !important;
}
</style>
