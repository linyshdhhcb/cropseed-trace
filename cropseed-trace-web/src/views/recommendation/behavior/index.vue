<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="用户ID" prop="userId">
                <el-input v-model.number="searchForm.userId" placeholder="请输入用户ID" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="行为类型" prop="behaviorType">
                <el-select v-model="searchForm.behaviorType" placeholder="请选择行为类型" clearable style="width: 180px">
                    <el-option label="浏览" :value="1" />
                    <el-option label="搜索" :value="2" />
                    <el-option label="收藏" :value="3" />
                    <el-option label="加购物车" :value="4" />
                    <el-option label="购买" :value="5" />
                    <el-option label="评价" :value="6" />
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
            <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="userId" label="用户ID" width="100" />
                <el-table-column prop="behaviorType" label="行为类型" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getBehaviorTypeTag(row.behaviorType)">
                            {{ getBehaviorTypeName(row.behaviorType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="targetType" label="目标类型" width="120">
                    <template #default="{ row }">
                        <span>{{ getTargetTypeName(row.targetType) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="targetId" label="目标ID" width="100" />
                <el-table-column prop="behaviorContent" label="行为内容" min-width="200" show-overflow-tooltip />
                <el-table-column prop="behaviorWeight" label="行为权重" width="120" align="center">
                    <template #default="{ row }">
                        <el-progress :percentage="getWeightPercentage(row.behaviorWeight)" :color="getWeightColor(row.behaviorWeight)" />
                    </template>
                </el-table-column>
                <el-table-column prop="behaviorTime" label="行为时间" width="160" align="center" />
                <el-table-column prop="sessionId" label="会话ID" width="200" show-overflow-tooltip />
                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            详情
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
        <el-dialog v-model="viewDialogVisible" title="行为详情" width="600px">
            <div v-if="behaviorDetail" class="behavior-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="ID">{{ behaviorDetail.id }}</el-descriptions-item>
                    <el-descriptions-item label="用户ID">{{ behaviorDetail.userId }}</el-descriptions-item>
                    <el-descriptions-item label="行为类型">
                        <el-tag :type="getBehaviorTypeTag(behaviorDetail.behaviorType)">
                            {{ getBehaviorTypeName(behaviorDetail.behaviorType) }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="目标类型">
                        {{ getTargetTypeName(behaviorDetail.targetType) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="目标ID">{{ behaviorDetail.targetId }}</el-descriptions-item>
                    <el-descriptions-item label="行为权重">
                        {{ (behaviorDetail.behaviorWeight * 100).toFixed(2) }}%
                    </el-descriptions-item>
                    <el-descriptions-item label="行为内容" :span="2">
                        {{ behaviorDetail.behaviorContent || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="行为时间">{{ behaviorDetail.behaviorTime }}</el-descriptions-item>
                    <el-descriptions-item label="会话ID">{{ behaviorDetail.sessionId }}</el-descriptions-item>
                    <el-descriptions-item label="设备信息">{{ behaviorDetail.deviceInfo || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="IP地址">{{ behaviorDetail.ipAddress || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{ behaviorDetail.createTime }}</el-descriptions-item>
                </el-descriptions>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Search, Refresh } from "@element-plus/icons-vue";
import { getUserBehaviorList } from "@/api/recommendation";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    userId: null,
    behaviorType: null,
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
const behaviorDetail = ref(null);

// 获取行为类型名称
const getBehaviorTypeName = (type) => {
    const typeMap = {
        1: "浏览",
        2: "搜索",
        3: "收藏",
        4: "加购物车",
        5: "购买",
        6: "评价",
    };
    return typeMap[type] || "未知";
};

// 获取行为类型标签
const getBehaviorTypeTag = (type) => {
    const tagMap = {
        1: "info",
        2: "primary",
        3: "warning",
        4: "success",
        5: "success",
        6: "danger",
    };
    return tagMap[type] || "info";
};

// 获取目标类型名称
const getTargetTypeName = (type) => {
    const typeMap = {
        1: "种子",
        2: "品类",
        3: "品牌",
    };
    return typeMap[type] || "未知";
};

// 获取权重百分比
const getWeightPercentage = (weight) => {
    if (!weight) return 0;
    return Math.min(Math.round(weight * 100), 100);
};

// 获取权重颜色
const getWeightColor = (weight) => {
    if (!weight) return "#909399";
    const percentage = weight * 100;
    if (percentage >= 80) return "#67c23a";
    if (percentage >= 60) return "#e6a23c";
    if (percentage >= 40) return "#f56c6c";
    return "#909399";
};

// 获取行为列表
const loadBehaviorList = async () => {
    try {
        loading.value = true;
        const params = {
            pageNum: pagination.current,
            pageSize: pagination.size,
            ...searchForm,
        };
        const response = await getUserBehaviorList(params);
        tableData.value = response.data.list;
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取行为列表失败:", error);
        ElMessage.error("获取行为列表失败");
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadBehaviorList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadBehaviorList();
};

// 查看详情
const handleView = (row) => {
    behaviorDetail.value = row;
    viewDialogVisible.value = true;
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadBehaviorList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadBehaviorList();
};

// 初始化
onMounted(() => {
    loadBehaviorList();
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

.behavior-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }
}
</style>
