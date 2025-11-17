<template>
    <div class="app-container">
        <!-- 推荐算法选择 -->
        <el-card class="algorithm-selector">
            <template #header>
                <div class="card-header">
                    <span>推荐算法</span>
                </div>
            </template>
            <el-radio-group v-model="selectedAlgorithm" @change="handleAlgorithmChange">
                <el-radio-button label="collaborative">协同过滤</el-radio-button>
                <el-radio-button label="content">内容推荐</el-radio-button>
                <el-radio-button label="popular">热门推荐</el-radio-button>
                <el-radio-button label="personalized">个性化推荐</el-radio-button>
                <el-radio-button label="hybrid">混合推荐</el-radio-button>
            </el-radio-group>
        </el-card>

        <!-- 用户选择 -->
        <el-card class="user-selector" v-if="selectedAlgorithm !== 'popular'">
            <template #header>
                <div class="card-header">
                    <span>选择用户</span>
                    <el-button type="primary" size="small" @click="showUserDialog = true">
                        选择用户
                    </el-button>
                </div>
            </template>
            <div v-if="selectedUser" class="selected-user">
                <el-tag type="success" size="large">
                    {{ selectedUser.nickname }} (ID: {{ selectedUser.id }})
                </el-tag>
                <el-button type="text" @click="selectedUser = null">清除</el-button>
            </div>
            <div v-else class="no-user">
                <el-text type="info">请选择一个用户来查看个性化推荐</el-text>
            </div>
        </el-card>

        <!-- 推荐结果展示 -->
        <el-card class="recommendation-results">
            <template #header>
                <div class="card-header">
                    <span>推荐结果</span>
                    <el-button type="primary" :loading="loading" @click="loadRecommendations">
                        刷新推荐
                    </el-button>
                </div>
            </template>
            
            <div v-loading="loading" class="results-container">
                <div v-if="recommendations.length === 0 && !loading" class="empty-state">
                    <el-empty description="暂无推荐结果" />
                </div>
                
                <div v-else class="recommendation-grid">
                    <div 
                        v-for="item in recommendations" 
                        :key="item.targetId" 
                        class="recommendation-card"
                        @click="handleRecommendationClick(item)"
                    >
                        <div class="card-image">
                            <img 
                                :src="item.targetImage || '/static/no-image-available.png'" 
                                :alt="item.targetName"
                                @error="handleImageError"
                            />
                            <div class="score-badge">
                                {{ (item.recommendationScore * 100).toFixed(1) }}%
                            </div>
                        </div>
                        <div class="card-content">
                            <h3 class="product-name">{{ item.targetName || '商品名称' }}</h3>
                            <div class="product-price">￥{{ item.targetPrice || '0.00' }}</div>
                            <div class="recommendation-info">
                                <el-tag :type="getAlgorithmTagType(item.recommendationType)" size="small">
                                    {{ item.recommendationTypeName }}
                                </el-tag>
                                <span class="weight">权重: {{ (item.recommendationWeight * 100).toFixed(1) }}%</span>
                            </div>
                            <div class="recommendation-reason">
                                {{ item.recommendationReason }}
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 分页组件 -->
                <div class="pagination-container">
                    <el-pagination
                        :current-page="pagination.current"
                        :page-size="pagination.size"
                        :page-sizes="[8, 12, 16, 24]"
                        :total="pagination.total"
                        layout="total, sizes, prev, pager, next, jumper"
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                    />
                </div>
            </div>
        </el-card>

        <!-- 用户选择对话框 -->
        <el-dialog v-model="showUserDialog" title="选择用户" width="800px" @open="loadUsers">
            <el-table 
                ref="userTableRef" 
                v-loading="userLoading" 
                :data="users" 
                @selection-change="handleUserSelection"
                max-height="400"
            >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="用户ID" width="100" />
                <el-table-column prop="nickname" label="昵称" width="150" />
                <el-table-column prop="phone" label="手机号" width="140" />
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" />
            </el-table>
            
            <template #footer>
                <el-button @click="showUserDialog = false">取消</el-button>
                <el-button type="primary" :disabled="!tempSelectedUser" @click="confirmUserSelection">
                    确定
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
    collaborativeFilteringRecommend,
    contentBasedRecommend,
    popularRecommend,
    personalizedRecommend,
    hybridRecommend,
    recordRecommendationClick
} from '@/api/recommendation'
import { getWxUserList } from '@/api/wechat'

// 响应式数据
const selectedAlgorithm = ref('popular')
const selectedUser = ref(null)
const recommendations = ref([])
const loading = ref(false)
const showUserDialog = ref(false)
const userLoading = ref(false)
const users = ref([])
const tempSelectedUser = ref(null)
const userTableRef = ref()

// 分页数据
const pagination = reactive({
    current: 1,
    size: 12,
    total: 0
})

// 算法标签类型映射 - 与后端RecommendationVO保持一致
const getAlgorithmTagType = (type) => {
    const typeMap = {
        1: 'primary',   // 协同过滤
        2: 'success',   // 内容推荐  
        3: 'warning',   // 热门推荐
        4: 'danger'     // 个性化推荐
    }
    return typeMap[type] || 'info'
}

// 获取推荐类型名称
const getRecommendationTypeName = (type) => {
    const typeMap = {
        1: '协同过滤',
        2: '内容推荐', 
        3: '热门推荐',
        4: '个性化推荐'
    }
    return typeMap[type] || '未知'
}

// 处理算法切换
const handleAlgorithmChange = () => {
    if (selectedAlgorithm.value === 'popular') {
        selectedUser.value = null
    }
    // 重置分页
    pagination.current = 1
    pagination.total = 0
    loadRecommendations()
}

// 加载推荐结果
const loadRecommendations = async () => {
    if (selectedAlgorithm.value !== 'popular' && !selectedUser.value) {
        ElMessage.warning('请先选择用户')
        return
    }
    
    loading.value = true
    try {
        let response
        const userId = selectedUser.value?.id
        
        // 根据分页加载推荐结果
        const params = {
            pageNum: pagination.current,
            pageSize: pagination.size,
            userId: userId,
            recommendationType: getRecommendationTypeByAlgorithm(selectedAlgorithm.value)
        }
        
        switch (selectedAlgorithm.value) {
            case 'collaborative':
                response = await collaborativeFilteringRecommend(userId, pagination.size)
                break
            case 'content':
                response = await contentBasedRecommend(userId, pagination.size)
                break
            case 'popular':
                response = await popularRecommend(pagination.size)
                break
            case 'personalized':
                response = await personalizedRecommend(userId, pagination.size)
                break
            case 'hybrid':
                response = await hybridRecommend(userId, pagination.size)
                break
            default:
                response = { data: [] }
        }
        
        recommendations.value = response.data || []
        // 如果API返回了分页信息，更新总数
        if (response.total !== undefined) {
            pagination.total = parseInt(response.total) || 0
        } else {
            // 如果没有分页信息，根据返回结果估算
            pagination.total = recommendations.value.length
        }
        
        if (recommendations.value.length === 0) {
            ElMessage.info('暂无推荐结果')
        }
    } catch (error) {
        console.error('获取推荐失败:', error)
        ElMessage.error('获取推荐失败')
        recommendations.value = []
        pagination.total = 0
    } finally {
        loading.value = false
    }
}

// 根据算法名称获取推荐类型编码
const getRecommendationTypeByAlgorithm = (algorithm) => {
    const typeMap = {
        'collaborative': 1,  // 协同过滤
        'content': 2,       // 内容推荐
        'popular': 3,       // 热门推荐
        'personalized': 4,  // 个性化推荐
        'hybrid': 5         // 混合推荐
    }
    return typeMap[algorithm] || null
}

// 分页大小变化
const handleSizeChange = (size) => {
    pagination.size = size
    pagination.current = 1
    loadRecommendations()
}

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current
    loadRecommendations()
}

// 加载用户列表
const loadUsers = async () => {
    userLoading.value = true
    try {
        const response = await getWxUserList({ current: 1, size: 100 })
        users.value = response.data.list || []
    } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
    } finally {
        userLoading.value = false
    }
}

// 处理用户选择
const handleUserSelection = (selection) => {
    tempSelectedUser.value = selection.length > 0 ? selection[0] : null
    
    // 单选逻辑
    if (selection.length > 1) {
        userTableRef.value.clearSelection()
        userTableRef.value.toggleRowSelection(selection[selection.length - 1], true)
        tempSelectedUser.value = selection[selection.length - 1]
    }
}

// 确认用户选择
const confirmUserSelection = () => {
    selectedUser.value = tempSelectedUser.value
    showUserDialog.value = false
    tempSelectedUser.value = null
    loadRecommendations()
}

// 处理推荐点击
const handleRecommendationClick = async (item) => {
    try {
        if (item.id) {
            await recordRecommendationClick(item.id)
            ElMessage.success('已记录点击行为')
        }
    } catch (error) {
        console.warn('记录点击失败:', error)
    }
}

// 处理图片加载错误
const handleImageError = (event) => {
    event.target.src = '/static/no-image-available.png'
}

// 初始化
onMounted(() => {
    loadRecommendations()
})
</script>

<style lang="scss" scoped>
.app-container {
    padding: 20px;
}

.algorithm-selector,
.user-selector,
.recommendation-results {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.selected-user {
    display: flex;
    align-items: center;
    gap: 10px;
}

.no-user {
    padding: 20px;
    text-align: center;
}

.results-container {
    min-height: 200px;
}

.recommendation-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    padding: 10px 0;
}

.recommendation-card {
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    background: white;

    &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
    }
}

.card-image {
    position: relative;
    height: 200px;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .score-badge {
        position: absolute;
        top: 10px;
        right: 10px;
        background: rgba(0, 0, 0, 0.7);
        color: white;
        padding: 4px 8px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: bold;
    }
}

.card-content {
    padding: 15px;
}

.product-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.product-price {
    font-size: 18px;
    font-weight: bold;
    color: #e6a23c;
    margin-bottom: 10px;
}

.recommendation-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .weight {
        font-size: 12px;
        color: #909399;
    }
}

.recommendation-reason {
    font-size: 13px;
    color: #606266;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.empty-state {
    padding: 40px;
    text-align: center;
}

.pagination-container {
    margin-top: 30px;
    text-align: center;
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
    min-height: 50px;
}
</style>
