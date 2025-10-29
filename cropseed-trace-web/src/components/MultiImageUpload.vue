<template>
    <div class="image-upload">
        <!-- 上传区域 -->
        <div class="upload-area" @click="triggerUpload" v-if="!imageList.length || imageList.length < maxCount">
            <el-upload ref="uploadRef" :action="uploadUrl" :headers="uploadHeaders" :data="uploadData"
                :before-upload="beforeUpload" :on-success="onUploadSuccess" :on-error="onUploadError"
                :show-file-list="false" accept="image/*" :multiple="maxCount > 1">
                <div class="upload-content">
                    <el-icon class="upload-icon">
                        <Plus />
                    </el-icon>
                    <div class="upload-text">
                        {{ maxCount === 1 ? '点击上传图片' : `点击上传图片 (${imageList.length}/${maxCount})` }}
                    </div>
                </div>
            </el-upload>
        </div>

        <!-- 图片列表 -->
        <div class="image-list" v-if="imageList.length > 0">
            <div class="image-item" v-for="(image, index) in imageList" :key="index">
                <img :src="image.url" :alt="image.name" class="preview-image" @click="previewImage(image)" />
                <div class="image-actions">
                    <el-tooltip content="预览" placement="top">
                        <el-button type="primary" size="small" :icon="View" @click="previewImage(image)" circle />
                    </el-tooltip>
                    <el-tooltip content="下载" placement="top">
                        <el-button type="success" size="small" :icon="Download" @click="downloadImage(image)" circle />
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                        <el-button type="danger" size="small" :icon="Delete" @click="deleteImage(index)" circle />
                    </el-tooltip>
                </div>
                <!-- 图片序号 -->
                <div class="image-index" v-if="maxCount > 1">{{ index + 1 }}</div>
            </div>
        </div>

        <!-- 图片预览对话框 -->
        <el-dialog v-model="previewVisible" title="图片预览" width="80%" center>
            <div class="preview-dialog">
                <img :src="previewUrl" alt="预览图片" class="preview-dialog-image" />
                <div class="preview-info" v-if="previewImageInfo">
                    <p><strong>文件名：</strong>{{ previewImageInfo.name }}</p>
                    <p><strong>文件大小：</strong>{{ formatFileSize(previewImageInfo.size) }}</p>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, View, Download, Delete } from "@element-plus/icons-vue";
import { uploadFile, getPresignedUrl } from "@/api/file";
import { getToken } from "@/utils/auth";

const props = defineProps({
    // 图片列表
    modelValue: {
        type: Array,
        default: () => []
    },
    // 最大上传数量
    maxCount: {
        type: Number,
        default: 1
    },
    // 上传文件夹
    folder: {
        type: String,
        default: "images"
    },
    // 文件大小限制（MB）
    maxSize: {
        type: Number,
        default: 5
    },
    // 上传区域大小
    size: {
        type: String,
        default: "120px"
    },
    // 是否禁用
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error', 'image-delete']);

// 响应式数据
const uploadRef = ref();
const imageList = ref([]);
const previewVisible = ref(false);
const previewUrl = ref("");
const previewImageInfo = ref(null);

// 计算属性
const uploadUrl = computed(() => {
    return '/api/file/upload';
});

const uploadHeaders = computed(() => {
    return {
        'Authorization': 'Bearer ' + getToken()
    };
});

const uploadData = computed(() => {
    return {
        folder: props.folder
    };
});

// 监听props变化
watch(() => props.modelValue, (newVal) => {
    imageList.value = newVal || [];
}, { immediate: true });

// 监听imageList变化
watch(imageList, (newVal) => {
    emit('update:modelValue', newVal);
}, { deep: true });

// 触发上传
const triggerUpload = () => {
    if (props.disabled) return;
    uploadRef.value?.$refs['upload-inner']?.handleClick();
};

// 上传前验证
const beforeUpload = (file) => {
    // 文件大小验证
    if (file.size > props.maxSize * 1024 * 1024) {
        ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`);
        return false;
    }

    // 文件类型验证
    if (!file.type.startsWith('image/')) {
        ElMessage.error('请上传图片文件');
        return false;
    }

    // 数量限制验证
    if (imageList.value.length >= props.maxCount) {
        ElMessage.error(`最多只能上传 ${props.maxCount} 张图片`);
        return false;
    }

    return true;
};

// 上传成功回调
const onUploadSuccess = (response, file) => {
    if (response.code === 200) {
        const imageItem = {
            url: response.data,
            name: file.name,
            size: file.size,
            uploadTime: new Date().toISOString()
        };

        imageList.value.push(imageItem);
        emit('upload-success', imageItem);
        ElMessage.success('图片上传成功');
    } else {
        ElMessage.error(response.message || '图片上传失败');
        emit('upload-error', response);
    }
};

// 上传失败回调
const onUploadError = (error) => {
    ElMessage.error('图片上传失败');
    emit('upload-error', error);
};

// 预览图片
const previewImage = (image) => {
    previewUrl.value = image.url;
    previewImageInfo.value = image;
    previewVisible.value = true;
};

// 下载图片
const downloadImage = async (image) => {
    try {
        const response = await getPresignedUrl(image.url);
        if (response.code === 200) {
            const link = document.createElement('a');
            link.href = response.data;
            link.download = image.name;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            ElMessage.success('图片下载成功');
        }
    } catch (error) {
        ElMessage.error('图片下载失败');
    }
};

// 删除图片
const deleteImage = async (index) => {
    try {
        await ElMessageBox.confirm('确定要删除该图片吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        });

        const deletedImage = imageList.value[index];
        imageList.value.splice(index, 1);
        emit('image-delete', deletedImage);
        ElMessage.success('图片已删除');
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除图片失败');
        }
    }
};

// 格式化文件大小
const formatFileSize = (size) => {
    if (size < 1024) return size + ' B';
    if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB';
    if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB';
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
};

// 清空所有图片
const clearAllImages = () => {
    imageList.value = [];
};

// 添加图片
const addImage = (imageItem) => {
    if (imageList.value.length < props.maxCount) {
        imageList.value.push(imageItem);
    }
};

// 暴露方法给父组件
defineExpose({
    clearAllImages,
    addImage
});
</script>

<style lang="scss" scoped>
.image-upload {
    .upload-area {
        width: v-bind(size);
        height: v-bind(size);
        border: 2px dashed #d9d9d9;
        border-radius: 8px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;

        &:hover {
            border-color: #409eff;
            background: #f0f9ff;
        }

        .upload-content {
            text-align: center;

            .upload-icon {
                font-size: 32px;
                color: #c0c4cc;
                margin-bottom: 8px;
            }

            .upload-text {
                color: #606266;
                font-size: 14px;
            }
        }
    }

    .image-list {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;

        .image-item {
            width: v-bind(size);
            height: v-bind(size);
            border-radius: 8px;
            overflow: hidden;
            position: relative;
            border: 1px solid #e4e7ed;

            .preview-image {
                width: 100%;
                height: 100%;
                object-fit: cover;
                cursor: pointer;
                transition: transform 0.3s;

                &:hover {
                    transform: scale(1.05);
                }
            }

            .image-actions {
                position: absolute;
                top: 4px;
                right: 4px;
                display: flex;
                gap: 4px;
                opacity: 0;
                transition: opacity 0.3s;

                .el-button {
                    width: 24px;
                    height: 24px;
                    padding: 0;
                    border-radius: 50%;
                    background: rgba(0, 0, 0, 0.6);
                    border: none;
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
                    color: white;

                    &:hover {
                        background: rgba(0, 0, 0, 0.8);
                        transform: scale(1.1);
                    }

                    // 不同类型按钮的颜色
                    &.el-button--primary {
                        background: rgba(64, 158, 255, 0.8);

                        &:hover {
                            background: rgba(64, 158, 255, 1);
                        }
                    }

                    &.el-button--success {
                        background: rgba(103, 194, 58, 0.8);

                        &:hover {
                            background: rgba(103, 194, 58, 1);
                        }
                    }

                    &.el-button--danger {
                        background: rgba(245, 108, 108, 0.8);

                        &:hover {
                            background: rgba(245, 108, 108, 1);
                        }
                    }
                }
            }

            .image-index {
                position: absolute;
                bottom: 4px;
                left: 4px;
                background: rgba(0, 0, 0, 0.6);
                color: white;
                padding: 2px 6px;
                border-radius: 4px;
                font-size: 12px;
                font-weight: bold;
            }

            &:hover {
                .image-actions {
                    opacity: 1;
                }
            }
        }
    }

    .preview-dialog {
        text-align: center;

        .preview-dialog-image {
            max-width: 100%;
            max-height: 70vh;
            object-fit: contain;
            margin-bottom: 16px;
        }

        .preview-info {
            text-align: left;
            background: #f5f7fa;
            padding: 16px;
            border-radius: 8px;
            margin-top: 16px;

            p {
                margin: 8px 0;
                color: #606266;
            }
        }
    }
}
</style>
