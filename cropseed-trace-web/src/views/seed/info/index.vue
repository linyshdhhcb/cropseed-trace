<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="种子名称" prop="seedName">
                <el-input v-model="searchForm.seedName" placeholder="请输入种子名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="品类" prop="categoryId">
                <el-select v-model="searchForm.categoryId" placeholder="请选择品类" clearable style="width: 150px">
                    <el-option v-for="category in categoryList" :key="category.id" :label="category.categoryName"
                        :value="category.id" />
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
                新增种子
            </el-button>
            <el-button type="danger" :icon="Delete" :disabled="!multipleSelection.length" @click="handleBatchDelete">
                批量删除
            </el-button>
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
            <el-button type="warning" :icon="Upload" @click="handleImport">
                导入数据
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange"
                style="width: 100%" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="seedName" label="种子名称" width="180" show-overflow-tooltip />
                <el-table-column prop="imageUrl" label="图片" width="100" align="center">
                    <template #default="{ row }">
                        <el-image :src="row.imageUrl" style="width: 50px; height: 50px" />
                    </template>
                </el-table-column>
                <el-table-column prop="categoryName" label="品类" width="120" align="center" />
                <el-table-column prop="variety" label="品种" width="120" align="center" />
                <el-table-column prop="originPlace" label="产地" width="120" align="center" />
                <el-table-column prop="unitPrice" label="单价(元)" width="100" align="center">
                    <template #default="{ row }">
                        <span v-if="row.unitPrice">{{ row.unitPrice }}</span>
                        <span v-else class="text-gray-400">-</span>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '上架' : '下架' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="info" size="small" @click="handleView(row)">
                            查看
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
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="handleDialogClose">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="种子名称" prop="seedName">
                            <el-input v-model="formData.seedName" placeholder="请输入种子名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="种子编码" prop="seedCode">
                            <el-input v-model="formData.seedCode" placeholder="请输入种子编码" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="品类" prop="categoryId">
                            <el-select v-model="formData.categoryId" placeholder="请选择品类" style="width: 100%">
                                <el-option v-for="category in categoryList" :key="category.id"
                                    :label="category.categoryName" :value="category.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="品种" prop="variety">
                            <el-input v-model="formData.variety" placeholder="请输入品种" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="产地" prop="originPlace">
                            <el-input v-model="formData.originPlace" placeholder="请输入产地" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="单价" prop="unitPrice">
                            <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="24">
                        <el-form-item label="特性描述" prop="characteristics">
                            <el-input 
                                v-model="formData.characteristics" 
                                type="textarea" 
                                :rows="4"
                                :maxlength="2000"
                                show-word-limit
                                placeholder="请输入特性描述，最多2000字符" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="24">
                        <el-form-item label="规格参数" prop="specifications">
                            <el-input 
                                v-model="formData.specifications" 
                                type="textarea" 
                                :rows="4"
                                :maxlength="2000"
                                show-word-limit
                                placeholder="请输入规格参数，最多2000字符" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="种子图片" prop="imageUrl">
                            <MultiImageUpload v-model="formData.imageList" :max-count="1" folder="seed-images"
                                :max-size="5" size="120px" @upload-success="onImageUploadSuccess" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="状态" prop="status">
                            <el-radio-group v-model="formData.status">
                                <el-radio :label="1">上架</el-radio>
                                <el-radio :label="0">下架</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
                    确定
                </el-button>
            </template>
        </el-dialog>

        <!-- 查看详情对话框 -->
        <el-dialog v-model="viewDialogVisible" title="种子详情" width="900px">
            <div v-if="seedDetail" class="seed-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="种子名称">{{ seedDetail.seedName }}</el-descriptions-item>
                    <el-descriptions-item label="种子编码">{{ seedDetail.seedCode }}</el-descriptions-item>
                    <el-descriptions-item label="品类">{{ seedDetail.categoryName }}</el-descriptions-item>
                    <el-descriptions-item label="品种">{{ seedDetail.variety || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="产地">{{ seedDetail.originPlace || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="单价">{{ seedDetail.unitPrice ? `￥${seedDetail.unitPrice}` : '-' }}</el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-tag :type="seedDetail.status === 1 ? 'success' : 'danger'">
                            {{ seedDetail.status === 1 ? '上架' : '下架' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="种子图片">
                        <div v-if="seedDetail.imageUrl" class="image-preview">
                            <el-image 
                                :src="seedDetail.imageUrl" 
                                style="width: 120px; height: 120px; border-radius: 8px;"
                                fit="cover"
                                :preview-src-list="[seedDetail.imageUrl]"
                                preview-teleported />
                        </div>
                        <span v-else class="text-gray-400">暂无图片</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="特性描述" :span="2">
                        <div class="text-content">
                            {{ seedDetail.characteristics || '暂无描述' }}
                        </div>
                    </el-descriptions-item>
                    <el-descriptions-item label="规格参数" :span="2">
                        <div class="text-content">
                            {{ seedDetail.specifications || '暂无参数' }}
                        </div>
                    </el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{ seedDetail.createTime }}</el-descriptions-item>
                    <el-descriptions-item label="更新时间">{{ seedDetail.updateTime }}</el-descriptions-item>
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
    Plus,
    Delete,
    Download,
    Upload,
} from "@element-plus/icons-vue";
import {
    getSeedInfoList,
    addSeedInfo,
    updateSeedInfo,
    deleteSeedInfo,
    batchDeleteSeedInfo,
    getSeedInfoDetail,
    getCategoryTree,
} from "@/api/seed";
import MultiImageUpload from "@/components/MultiImageUpload.vue";

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    seedName: "",
    categoryId: null,
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
    seedName: "",
    seedCode: "",
    categoryId: null,
    variety: "",
    originPlace: "",
    characteristics: "",
    specifications: "",
    imageList: [],
    unitPrice: 0,
    status: 1,
});

// 表单验证规则
const formRules = {
    seedName: [
        { required: true, message: "请输入种子名称", trigger: "blur" },
        { min: 2, max: 50, message: "种子名称长度在 2 到 50 个字符", trigger: "blur" },
    ],
    seedCode: [{ required: true, message: "请输入种子编码", trigger: "blur" }],
    categoryId: [{ required: true, message: "请选择品类", trigger: "change" }],
    variety: [{ required: true, message: "请输入品种", trigger: "blur" }],
    originPlace: [{ required: true, message: "请输入产地", trigger: "blur" }],
    status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

// 品类列表
const categoryList = ref([]);

// 查看详情对话框
const viewDialogVisible = ref(false);
const seedDetail = ref(null);

// 图片上传成功回调
const onImageUploadSuccess = (file) => {
    ElMessage.success("种子图片上传成功");
};

// 获取种子列表
const loadSeedList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getSeedInfoList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取种子列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 获取品类列表
const loadCategoryList = async () => {
    try {
        const response = await getCategoryTree();
        // 将树形数据转换为扁平数组
        const flattenTree = (nodes) => {
            let result = [];
            nodes.forEach(node => {
                result.push(node);
                if (node.children && node.children.length > 0) {
                    result = result.concat(flattenTree(node.children));
                }
            });
            return result;
        };
        categoryList.value = flattenTree(response.data);
    } catch (error) {
        console.error("获取品类列表失败:", error);
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadSeedList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadSeedList();
};

// 新增种子
const handleAdd = () => {
    dialogTitle.value = "新增种子";
    dialogVisible.value = true;
    resetForm();
};

// 编辑种子
const handleEdit = (row) => {
    dialogTitle.value = "编辑种子";
    dialogVisible.value = true;

    // 复制数据到表单
    Object.assign(formData, row);

    // 处理图片数据：将imageUrl转换为imageList
    if (row.imageUrl) {
        formData.imageList = [{
            url: row.imageUrl,
            name: "种子图片",
            size: 0,
            uploadTime: new Date().toISOString()
        }];
    } else {
        formData.imageList = [];
    }
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getSeedInfoDetail(row.id);
        seedDetail.value = response.data;
        viewDialogVisible.value = true;
    } catch (error) {
        console.error("获取种子详情失败:", error);
    }
};

// 删除种子
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该种子吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteSeedInfo(row.id);
        ElMessage.success("删除成功");
        loadSeedList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除种子失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的种子吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteSeedInfo(ids);
        ElMessage.success("删除成功");
        loadSeedList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除种子失败:", error);
        }
    }
};

// 导出数据
const handleExport = () => {
    ElMessage.info("导出功能开发中...");
};

// 导入数据
const handleImport = () => {
    ElMessage.info("导入功能开发中...");
};

// 提交表单
const handleSubmit = async () => {
    try {
        await formRef.value.validate();
        submitLoading.value = true;

        // 处理图片数据：将imageList转换为imageUrl
        const submitData = {
            ...formData,
            imageUrl: formData.imageList.length > 0 ? formData.imageList[0].url : ""
        };

        if (formData.id) {
            await updateSeedInfo(submitData);
            ElMessage.success("更新成功");
        } else {
            await addSeedInfo(submitData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadSeedList();
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
        seedName: "",
        seedCode: "",
        categoryId: null,
        variety: "",
        originPlace: "",
        characteristics: "",
        specifications: "",
        imageList: [],
        unitPrice: 0,
        status: 1,
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
    loadSeedList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadSeedList();
};

// 初始化
onMounted(() => {
    loadSeedList();
    loadCategoryList();
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

.seed-detail {
    .el-descriptions {
        margin-bottom: 20px;
    }

    .image-preview {
        display: flex;
        align-items: center;
        justify-content: flex-start;
        
        .el-image {
            border: 1px solid #dcdfe6;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
                border-color: #409eff;
                transform: scale(1.05);
            }
        }
    }

    .text-content {
        max-height: 120px;
        overflow-y: auto;
        padding: 8px 12px;
        background-color: #f8f9fa;
        border-radius: 4px;
        line-height: 1.6;
        white-space: pre-wrap;
        word-break: break-word;
        font-size: 14px;
        color: #606266;
        
        &::-webkit-scrollbar {
            width: 6px;
        }
        
        &::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 3px;
        }
        
        &::-webkit-scrollbar-thumb {
            background: #c1c1c1;
            border-radius: 3px;
            
            &:hover {
                background: #a8a8a8;
            }
        }
    }
}

.text-gray-400 {
    color: #9ca3af;
}

:deep(.el-table) {
    .el-table__header th {
        background-color: #f8f9fa;
        font-weight: 600;
    }

    .el-table__body td {
        padding: 12px 0;
    }

    .el-table__row:hover>td {
        background-color: #f5f7fa !important;
    }
}
</style>
