<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="用户ID" prop="userId">
                <el-input v-model.number="searchForm.userId" placeholder="请输入用户ID" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="用户类型" prop="userType">
                <el-select v-model="searchForm.userType" placeholder="请选择用户类型" clearable style="width: 180px">
                    <el-option label="个人用户" :value="1" />
                    <el-option label="企业用户" :value="2" />
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
                <el-table-column prop="userType" label="用户类型" width="120">
                    <template #default="{ row }">
                        <el-tag :type="row.userType === 1 ? 'primary' : 'success'">
                            {{ row.userType === 1 ? '个人用户' : '企业用户' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="age" label="年龄" width="80" />
                <el-table-column prop="gender" label="性别" width="80">
                    <template #default="{ row }">
                        <span v-if="row.gender === 1">男</span>
                        <span v-else-if="row.gender === 2">女</span>
                        <span v-else>未知</span>
                    </template>
                </el-table-column>
                <el-table-column prop="region" label="地区" width="120" />
                <el-table-column prop="priceSensitivity" label="价格敏感度" width="140" align="center">
                    <template #default="{ row }">
                        <el-progress :percentage="getPercentage(row.priceSensitivity)" :color="getSensitivityColor(row.priceSensitivity)" />
                    </template>
                </el-table-column>
                <el-table-column prop="qualityRequirement" label="质量要求" width="140" align="center">
                    <template #default="{ row }">
                        <el-progress :percentage="getPercentage(row.qualityRequirement)" :color="getQualityColor(row.qualityRequirement)" />
                    </template>
                </el-table-column>
                <el-table-column prop="activityLevel" label="活跃度" width="140" align="center">
                    <template #default="{ row }">
                        <el-progress :percentage="getPercentage(row.activityLevel)" :color="getActivityColor(row.activityLevel)" />
                    </template>
                </el-table-column>
                <el-table-column prop="purchaseFrequency" label="购买频率" width="140" align="center">
                    <template #default="{ row }">
                        <el-progress :percentage="getPercentage(row.purchaseFrequency)" :color="getFrequencyColor(row.purchaseFrequency)" />
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleView(row)">
                            详情
                        </el-button>
                        <el-button type="warning" size="small" @click="handleUpdate(row)">
                            更新画像
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
        <el-dialog v-model="viewDialogVisible" title="用户画像详情" width="700px">
            <div v-if="profileDetail" class="profile-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="ID">{{ profileDetail.id }}</el-descriptions-item>
                    <el-descriptions-item label="用户ID">{{ profileDetail.userId }}</el-descriptions-item>
                    <el-descriptions-item label="用户类型">
                        <el-tag :type="profileDetail.userType === 1 ? 'primary' : 'success'">
                            {{ profileDetail.userType === 1 ? '个人用户' : '企业用户' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="年龄">{{ profileDetail.age || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="性别">
                        <span v-if="profileDetail.gender === 1">男</span>
                        <span v-else-if="profileDetail.gender === 2">女</span>
                        <span v-else>未知</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="地区">{{ profileDetail.region || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="种植经验">
                        {{ getPlantingExperienceName(profileDetail.plantingExperience) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="种植规模">
                        {{ getPlantingScaleName(profileDetail.plantingScale) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="主要作物" :span="2">
                        {{ profileDetail.mainCrops || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="购买偏好">
                        {{ getPurchasePreferenceName(profileDetail.purchasePreference) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="品牌忠诚度">
                        {{ (profileDetail.brandLoyalty * 100).toFixed(2) }}%
                    </el-descriptions-item>
                    <el-descriptions-item label="价格敏感度">
                        {{ (profileDetail.priceSensitivity * 100).toFixed(2) }}%
                    </el-descriptions-item>
                    <el-descriptions-item label="质量要求">
                        {{ (profileDetail.qualityRequirement * 100).toFixed(2) }}%
                    </el-descriptions-item>
                    <el-descriptions-item label="活跃度">
                        {{ (profileDetail.activityLevel * 100).toFixed(2) }}%
                    </el-descriptions-item>
                    <el-descriptions-item label="购买频率">
                        {{ (profileDetail.purchaseFrequency * 100).toFixed(2) }}%
                    </el-descriptions-item>
                    <el-descriptions-item label="推荐权重">
                        {{ (profileDetail.recommendationWeight * 100).toFixed(2) }}%
                    </el-descriptions-item>
                    <el-descriptions-item label="用户标签" :span="2">
                        {{ profileDetail.userTags || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{ profileDetail.createTime }}</el-descriptions-item>
                    <el-descriptions-item label="更新时间">{{ profileDetail.updateTime || '-' }}</el-descriptions-item>
                </el-descriptions>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Refresh } from "@element-plus/icons-vue";
import {
    getUserProfileList,
    getUserProfileDetail,
    updateUserProfile,
} from "@/api/recommendation";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    userId: null,
    userType: null,
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
const profileDetail = ref(null);

// 获取百分比
const getPercentage = (value) => {
    if (!value) return 0;
    return Math.min(Math.round(value * 100), 100);
};

// 获取敏感度颜色
const getSensitivityColor = (value) => {
    if (!value) return "#909399";
    const percentage = value * 100;
    if (percentage >= 80) return "#f56c6c";
    if (percentage >= 60) return "#e6a23c";
    return "#67c23a";
};

// 获取质量颜色
const getQualityColor = (value) => {
    if (!value) return "#909399";
    const percentage = value * 100;
    if (percentage >= 80) return "#67c23a";
    if (percentage >= 60) return "#e6a23c";
    return "#909399";
};

// 获取活跃度颜色
const getActivityColor = (value) => {
    if (!value) return "#909399";
    const percentage = value * 100;
    if (percentage >= 80) return "#67c23a";
    if (percentage >= 60) return "#e6a23c";
    return "#909399";
};

// 获取购买频率颜色
const getFrequencyColor = (value) => {
    if (!value) return "#909399";
    const percentage = value * 100;
    if (percentage >= 80) return "#67c23a";
    if (percentage >= 60) return "#e6a23c";
    return "#909399";
};

// 获取种植经验名称
const getPlantingExperienceName = (experience) => {
    const nameMap = {
        1: "新手",
        2: "有经验",
        3: "专家",
    };
    return nameMap[experience] || "-";
};

// 获取种植规模名称
const getPlantingScaleName = (scale) => {
    const nameMap = {
        1: "小规模",
        2: "中规模",
        3: "大规模",
    };
    return nameMap[scale] || "-";
};

// 获取购买偏好名称
const getPurchasePreferenceName = (preference) => {
    const nameMap = {
        1: "价格敏感",
        2: "质量优先",
        3: "品牌优先",
    };
    return nameMap[preference] || "-";
};

// 获取画像列表
const loadProfileList = async () => {
    try {
        loading.value = true;
        const params = {
            pageNum: pagination.current,
            pageSize: pagination.size,
            ...searchForm,
        };
        const response = await getUserProfileList(params);
        tableData.value = response.data.list;
        pagination.total = response.data.total;
    } catch (error) {
        console.error("获取画像列表失败:", error);
        ElMessage.error("获取画像列表失败");
    } finally {
        loading.value = false;
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadProfileList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadProfileList();
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getUserProfileDetail(row.userId);
        profileDetail.value = response.data;
        viewDialogVisible.value = true;
    } catch (error) {
        console.error("获取画像详情失败:", error);
        ElMessage.error("获取画像详情失败");
    }
};

// 更新画像
const handleUpdate = async (row) => {
    try {
        await ElMessageBox.confirm("确定要更新该用户的画像吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await updateUserProfile(row.userId);
        ElMessage.success("更新画像成功");
        loadProfileList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("更新画像失败:", error);
            ElMessage.error("更新画像失败");
        }
    }
};

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size;
    pagination.current = 1;
    loadProfileList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadProfileList();
};

// 初始化
onMounted(() => {
    loadProfileList();
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

.profile-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }
}
</style>
