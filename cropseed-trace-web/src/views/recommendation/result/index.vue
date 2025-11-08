<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="用户ID" prop="userId">
                <el-input v-model.number="searchForm.userId" placeholder="请输入用户ID" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="推荐类型" prop="recommendationType">
                <el-select v-model="searchForm.recommendationType" placeholder="请选择推荐类型" clearable style="width: 180px">
                    <el-option label="协同过滤" :value="1" />
                    <el-option label="内容推荐" :value="2" />
                    <el-option label="热门推荐" :value="3" />
                    <el-option label="个性化推荐" :value="4" />
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
            <el-button type="primary" :icon="Plus" @click="handleGenerateRecommendations">
                生成推荐结果
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="userId" label="用户ID" width="100" />
                <el-table-column prop="recommendationType" label="推荐类型" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getRecommendationTypeTag(row.recommendationType)">
                            {{ getRecommendationTypeName(row.recommendationType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="targetId" label="商品ID" width="100" />
                <el-table-column prop="recommendationScore" label="推荐分数" width="120" align="center">
                    <template #default="{ row }">
                        <el-progress :percentage="getScorePercentage(row.recommendationScore)"
                            :color="getScoreColor(row.recommendationScore)" />
                    </template>
                </el-table-column>
                <el-table-column prop="recommendationWeight" label="权重" width="100" align="center">
                    <template #default="{ row }">
                        <span>{{ (row.recommendationWeight * 100).toFixed(1) }}%</span>
                    </template>
                </el-table-column>
                <el-table-column prop="recommendationReason" label="推荐理由" min-width="200" show-overflow-tooltip />
                <el-table-column prop="isShown" label="已展示" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.isShown === 1 ? 'success' : 'info'">
                            {{ row.isShown === 1 ? '是' : '否' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="isClicked" label="已点击" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.isClicked === 1 ? 'success' : 'info'">
                            {{ row.isClicked === 1 ? '是' : '否' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="isPurchased" label="已购买" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.isPurchased === 1 ? 'success' : 'info'">
                            {{ row.isPurchased === 1 ? '是' : '否' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleRecordClick(row)">
                            记录点击
                        </el-button>
                        <el-button type="success" size="small" @click="handleRecordPurchase(row)">
                            记录购买
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

        <!-- 生成推荐对话框（选择微信用户） -->
        <el-dialog v-model="generateDialogVisible" title="选择微信用户生成推荐" width="900px" @open="onGenerateDialogOpen"
            @opened="onGenerateDialogOpened" append-to-body destroy-on-close>
            <!-- 筛选表单 -->
            <el-form :model="wxSearchForm" :inline="true" class="search-form">
                <el-form-item label="昵称">
                    <el-input v-model="wxSearchForm.nickname" placeholder="请输入昵称" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="手机号">
                    <el-input v-model="wxSearchForm.phone" placeholder="请输入手机号" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="wxSearchForm.status" placeholder="请选择状态" clearable style="width: 150px">
                        <el-option label="启用" :value="1" />
                        <el-option label="禁用" :value="0" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" :icon="Search" @click="handleWxSearch">搜索</el-button>
                    <el-button :icon="Refresh" @click="handleWxReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 微信用户表格 -->
            <el-table ref="wxTableRef" v-loading="wxLoading" :data="wxTableData" border stripe style="width: 100%"
                max-height="420" :fit="true" :scrollbar-always-on="true" @selection-change="handleWxSelectionChange">
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column prop="id" label="用户ID" width="120" />
                <el-table-column prop="nickname" label="昵称" width="160" />
                <el-table-column prop="phone" label="手机号" width="140" />
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用'
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" />
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination :current-page="wxPagination.current" :page-size="wxPagination.size"
                    :page-sizes="[10, 20, 50]" :total="wxPagination.total"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleWxSizeChange"
                    @current-change="handleWxCurrentPageChange" />
            </div>

            <template #footer>
                <span style="margin-right: 12px;" v-if="selectedWxUser">已选：{{ selectedWxUser.nickname }} (ID: {{
                    selectedWxUser.id }})</span>
                <el-button @click="generateDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="generateLoading" :disabled="!selectedWxUser"
                    @click="handleConfirmGenerate">生成</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch, onBeforeUnmount } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Refresh, Plus } from "@element-plus/icons-vue";
import {
    getRecommendationList,
    generateRecommendations,
    recordRecommendationClick,
    recordRecommendationPurchase,
} from "@/api/recommendation";
import { getWxUserList } from "@/api/wechat";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    userId: null,
    recommendationType: null,
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

// 生成推荐对话框 & 微信用户选择
const generateDialogVisible = ref(false);
const generateLoading = ref(false);
const wxSearchForm = reactive({ nickname: "", phone: "", status: null });
const wxLoading = ref(false);
const wxTableData = ref([]);
const wxPagination = reactive({ current: 1, size: 10, total: 0 });
const selectedWxUser = ref(null);
const wxTableRef = ref();

const loadWxUserList = async () => {
    try {
        wxLoading.value = true;
        const params = { current: wxPagination.current, size: wxPagination.size, ...wxSearchForm };
        const res = await getWxUserList(params);
        wxTableData.value = res.data.list || [];
        wxPagination.total = res.data.total || 0;
        if (selectedWxUser.value) {
            const exists = wxTableData.value.some(u => u.id === selectedWxUser.value.id);
            if (!exists) selectedWxUser.value = null;
        }
        await relayoutWxTable();
    } catch (e) {
        ElMessage.error("加载微信用户失败");
    } finally {
        wxLoading.value = false;
    }
};

const handleWxSearch = () => {
    wxPagination.current = 1;
    loadWxUserList();
};
const handleWxReset = () => {
    wxSearchForm.nickname = "";
    wxSearchForm.phone = "";
    wxSearchForm.status = null;
    wxPagination.current = 1;
    loadWxUserList();
};
const handleWxSizeChange = (size) => {
    wxPagination.size = size;
    wxPagination.current = 1;
    loadWxUserList();
};
const handleWxCurrentPageChange = (page) => {
    wxPagination.current = page;
    loadWxUserList();
};
const handleWxSelectionChange = (rows) => {
    if (!wxTableRef.value) return;
    if (rows.length <= 1) {
        selectedWxUser.value = rows[0] || null;
        return;
    }
    const last = rows[rows.length - 1];
    wxTableRef.value.clearSelection();
    wxTableRef.value.toggleRowSelection(last, true);
    selectedWxUser.value = last;
};

// 获取推荐类型名称
const getRecommendationTypeName = (type) => {
    const typeMap = {
        1: "协同过滤",
        2: "内容推荐",
        3: "热门推荐",
        4: "个性化推荐",
    };
    return typeMap[type] || "未知";
};

// 获取推荐类型标签
const getRecommendationTypeTag = (type) => {
    const tagMap = {
        1: "primary",
        2: "success",
        3: "warning",
        4: "danger",
    };
    return tagMap[type] || "info";
};

// 获取分数百分比
const getScorePercentage = (score) => {
    if (!score) return 0;
    return Math.min(Math.round(score * 100), 100);
};

// 获取分数颜色
const getScoreColor = (score) => {
    if (!score) return "#909399";
    const percentage = score * 100;
    if (percentage >= 80) return "#67c23a";
    if (percentage >= 60) return "#e6a23c";
    if (percentage >= 40) return "#f56c6c";
    return "#909399";
};

// 获取推荐列表
const loadRecommendationList = async () => {
    try {
        loading.value = true;
        const params = {
            pageNum: pagination.current,
            pageSize: pagination.size,
            ...searchForm,
        };
        const response = await getRecommendationList(params);
        tableData.value = response.data.list;
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取推荐列表失败:", error);
        ElMessage.error("获取推荐列表失败");
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadRecommendationList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadRecommendationList();
};

// 生成推荐结果
const handleGenerateRecommendations = () => {
    selectedWxUser.value = null;
    generateDialogVisible.value = true;
    wxPagination.current = 1;
    loadWxUserList();
    if (wxTableRef.value) {
        wxTableRef.value.clearSelection();
    }
};

// 弹窗打开后强制重新布局，避免列宽错位
const onGenerateDialogOpen = () => {
    relayoutWxTable();
    window.addEventListener("resize", relayoutWxTable);
};

onBeforeUnmount(() => {
    window.removeEventListener("resize", relayoutWxTable);
});

const onGenerateDialogOpened = () => {
    // 动画结束后再强制一次布局，避免过渡期间宽度取值不准确
    relayoutWxTable();
};

const relayoutWxTable = async () => {
    await nextTick();
    setTimeout(() => {
        if (wxTableRef.value && wxTableRef.value.doLayout) {
            wxTableRef.value.doLayout();
        }
    }, 0);
};

watch(generateDialogVisible, (open) => {
    if (open) {
        relayoutWxTable();
    } else {
        window.removeEventListener("resize", relayoutWxTable);
    }
});

// 确认生成
const handleConfirmGenerate = async () => {
    try {
        generateLoading.value = true;
        if (!selectedWxUser.value) {
            ElMessage.warning("请先选择微信用户");
            return;
        }
        await generateRecommendations(selectedWxUser.value.id);
        ElMessage.success("生成推荐结果成功");
        generateDialogVisible.value = false;
        loadRecommendationList();
    } catch (error) {
        if (error !== false) {
            console.error("生成推荐结果失败:", error);
            ElMessage.error("生成推荐结果失败");
        }
    } finally {
        generateLoading.value = false;
    }
};

// 记录点击
const handleRecordClick = async (row) => {
    try {
        await recordRecommendationClick(row.id);
        ElMessage.success("记录点击成功");
        loadRecommendationList();
    } catch (error) {
        console.error("记录点击失败:", error);
        ElMessage.error("记录点击失败");
    }
};

// 记录购买
const handleRecordPurchase = async (row) => {
    try {
        await recordRecommendationPurchase(row.id);
        ElMessage.success("记录购买成功");
        loadRecommendationList();
    } catch (error) {
        console.error("记录购买失败:", error);
        ElMessage.error("记录购买失败");
    }
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadRecommendationList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadRecommendationList();
};

// 初始化
onMounted(() => {
    loadRecommendationList();
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

/* 保证 Dialog 内 el-table 头身布局一致 */
:deep(.el-dialog__body) .el-table {
    table-layout: fixed;
}
</style>
