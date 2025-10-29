<template>
    <div class="multi-file-upload">
        <!-- 上传区域 -->
        <div class="upload-area" @click="triggerUpload" v-if="!fileList.length || fileList.length < maxCount">
            <el-upload ref="uploadRef" :action="uploadUrl" :headers="uploadHeaders" :data="uploadData"
                :before-upload="beforeUpload" :on-success="onUploadSuccess" :on-error="onUploadError"
                :show-file-list="false" :accept="accept" :multiple="maxCount > 1" :disabled="disabled">
                <div class="upload-content">
                    <el-icon class="upload-icon">
                        <Upload />
                    </el-icon>
                    <div class="upload-text">
                        {{ uploadText }}
                    </div>
                    <div class="upload-tip">
                        {{ uploadTip }}
                    </div>
                </div>
            </el-upload>
        </div>

        <!-- 文件列表 -->
        <div class="file-list" v-if="fileList.length > 0">
            <div class="file-item" v-for="(file, index) in fileList" :key="index">
                <div class="file-icon">
                    <el-icon v-if="isImage(file)">
                        <Picture />
                    </el-icon>
                    <el-icon v-else-if="isDocument(file)">
                        <Document />
                    </el-icon>
                    <el-icon v-else-if="isVideo(file)">
                        <VideoPlay />
                    </el-icon>
                    <el-icon v-else-if="isAudio(file)">
                        <Headphones />
                    </el-icon>
                    <el-icon v-else>
                        <Files />
                    </el-icon>
                </div>

                <div class="file-info">
                    <div class="file-name" :title="file.name">{{ file.name }}</div>
                    <div class="file-meta">
                        <span class="file-size">{{ formatFileSize(file.size) }}</span>
                        <span class="file-type">{{ getFileType(file) }}</span>
                    </div>
                </div>

                <div class="file-actions">
                    <el-tooltip content="预览" placement="top" v-if="canPreview(file)">
                        <el-button type="primary" size="small" :icon="View" @click="handlePreviewFile(file)" circle />
                    </el-tooltip>
                    <el-tooltip content="下载" placement="top">
                        <el-button type="success" size="small" :icon="Download" @click="downloadFile(file)" circle />
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                        <el-button type="danger" size="small" :icon="Delete" @click="deleteFile(index)" circle />
                    </el-tooltip>
                </div>

                <!-- 文件序号 -->
                <div class="file-index" v-if="maxCount > 1">{{ index + 1 }}</div>
            </div>
        </div>

        <!-- 文件预览对话框 -->
        <el-dialog v-model="previewVisible" :title="previewTitle" width="80%" center>
            <div class="preview-dialog">
                <!-- 图片预览 -->
                <img v-if="isImage(previewFile)" :src="previewFile.url" alt="预览图片" class="preview-image" />

                <!-- PDF预览 -->
                <iframe v-else-if="isPDF(previewFile)" :src="previewFile.url" class="preview-pdf"
                    frameborder="0"></iframe>

                <!-- 视频预览 -->
                <video v-else-if="isVideo(previewFile)" :src="previewFile.url" controls class="preview-video"></video>

                <!-- 音频预览 -->
                <audio v-else-if="isAudio(previewFile)" :src="previewFile.url" controls class="preview-audio"></audio>

                <!-- 文本预览 -->
                <div v-else-if="isText(previewFile)" class="preview-text">
                    <pre>{{ previewContent }}</pre>
                </div>

                <!-- 不支持预览 -->
                <div v-else class="preview-unavailable">
                    <el-icon size="64">
                        <Files />
                    </el-icon>
                    <p>该文件类型不支持预览</p>
                    <el-button type="primary" @click="downloadFile(previewFile)">
                        下载文件
                    </el-button>
                </div>

                <!-- 文件信息 -->
                <div class="preview-info" v-if="previewFile">
                    <p><strong>文件名：</strong>{{ previewFile.name }}</p>
                    <p><strong>文件大小：</strong>{{ formatFileSize(previewFile.size) }}</p>
                    <p><strong>文件类型：</strong>{{ getFileType(previewFile) }}</p>
                    <p><strong>上传时间：</strong>{{ formatDate(previewFile.uploadTime) }}</p>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Upload, Picture, Document, VideoPlay, Headphones, Files, View, Download, Delete } from "@element-plus/icons-vue";
import { uploadFile, getPresignedUrl } from "@/api/file";
import { getToken } from "@/utils/auth";

const props = defineProps({
    // 文件列表
    modelValue: {
        type: Array,
        default: () => []
    },
    // 最大上传数量
    maxCount: {
        type: Number,
        default: 5
    },
    // 文件类型限制
    accept: {
        type: String,
        default: ""
    },
    // 文件大小限制（MB）
    maxSize: {
        type: Number,
        default: 50
    },
    // 上传文件夹
    folder: {
        type: String,
        default: "files"
    },
    // 上传区域大小
    size: {
        type: String,
        default: "200px"
    },
    // 是否禁用
    disabled: {
        type: Boolean,
        default: false
    },
    // 上传提示文本
    uploadText: {
        type: String,
        default: "点击或拖拽文件到此处上传"
    },
    // 上传说明
    uploadTip: {
        type: String,
        default: "支持各种文件格式，单个文件不超过50MB"
    }
});

const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error', 'file-delete']);

// 响应式数据
const uploadRef = ref();
const fileList = ref([]);
const previewVisible = ref(false);
const previewFile = ref(null);
const previewContent = ref("");

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

const previewTitle = computed(() => {
    return previewFile.value ? `预览 - ${previewFile.value.name}` : '文件预览';
});

// 监听props变化
watch(() => props.modelValue, (newVal) => {
    fileList.value = newVal || [];
}, { immediate: true });

// 监听fileList变化
watch(fileList, (newVal) => {
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
    if (props.accept && !isValidFileType(file)) {
        ElMessage.error('文件类型不支持');
        return false;
    }

    // 数量限制验证
    if (fileList.value.length >= props.maxCount) {
        ElMessage.error(`最多只能上传 ${props.maxCount} 个文件`);
        return false;
    }

    return true;
};

// 上传成功回调
const onUploadSuccess = (response, file) => {
    if (response.code === 200) {
        const fileItem = {
            url: response.data,
            name: file.name,
            size: file.size,
            type: file.type,
            uploadTime: new Date().toISOString()
        };

        fileList.value.push(fileItem);
        emit('upload-success', fileItem);
        ElMessage.success('文件上传成功');
    } else {
        ElMessage.error(response.message || '文件上传失败');
        emit('upload-error', response);
    }
};

// 上传失败回调
const onUploadError = (error) => {
    ElMessage.error('文件上传失败');
    emit('upload-error', error);
};

// 预览文件
const handlePreviewFile = async (file) => {
    previewFile.value = file;
    previewVisible.value = true;

    // 如果是文本文件，尝试读取内容
    if (isText(file)) {
        try {
            const response = await fetch(file.url);
            previewContent.value = await response.text();
        } catch (error) {
            previewContent.value = "无法读取文件内容";
        }
    }
};

// 下载文件
const downloadFile = async (file) => {
    try {
        const response = await getPresignedUrl(file.url);
        if (response.code === 200) {
            const link = document.createElement('a');
            link.href = response.data;
            link.download = file.name;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            ElMessage.success('文件下载成功');
        }
    } catch (error) {
        ElMessage.error('文件下载失败');
    }
};

// 删除文件
const deleteFile = async (index) => {
    try {
        await ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        });

        const deletedFile = fileList.value[index];
        fileList.value.splice(index, 1);
        emit('file-delete', deletedFile);
        ElMessage.success('文件已删除');
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除文件失败');
        }
    }
};

// 文件类型判断
const isImage = (file) => {
    return file.type.startsWith('image/');
};

const isDocument = (file) => {
    const docTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument'];
    return docTypes.some(type => file.type.includes(type)) || file.name.match(/\.(doc|docx|pdf|xls|xlsx|ppt|pptx)$/i);
};

const isVideo = (file) => {
    return file.type.startsWith('video/');
};

const isAudio = (file) => {
    return file.type.startsWith('audio/');
};

const isPDF = (file) => {
    return file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
};

const isText = (file) => {
    return file.type.startsWith('text/') || file.name.match(/\.(txt|json|xml|csv|log)$/i);
};

const canPreview = (file) => {
    return isImage(file) || isPDF(file) || isVideo(file) || isAudio(file) || isText(file);
};

// 获取文件类型显示名称
const getFileType = (file) => {
    if (isImage(file)) return '图片';
    if (isPDF(file)) return 'PDF';
    if (isDocument(file)) return '文档';
    if (isVideo(file)) return '视频';
    if (isAudio(file)) return '音频';
    if (isText(file)) return '文本';
    return '文件';
};

// 验证文件类型
const isValidFileType = (file) => {
    if (!props.accept) return true;
    const acceptTypes = props.accept.split(',').map(type => type.trim());
    return acceptTypes.some(type => {
        if (type.startsWith('.')) {
            return file.name.toLowerCase().endsWith(type.toLowerCase());
        }
        return file.type.match(type.replace('*', '.*'));
    });
};

// 格式化文件大小
const formatFileSize = (size) => {
    if (size < 1024) return size + ' B';
    if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB';
    if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB';
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
};

// 格式化日期
const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('zh-CN');
};

// 清空所有文件
const clearAllFiles = () => {
    fileList.value = [];
};

// 添加文件
const addFile = (fileItem) => {
    if (fileList.value.length < props.maxCount) {
        fileList.value.push(fileItem);
    }
};

// 暴露方法给父组件
defineExpose({
    clearAllFiles,
    addFile
});
</script>

<style lang="scss" scoped>
.multi-file-upload {
    .upload-area {
        width: v-bind(size);
        min-height: 120px;
        border: 2px dashed #d9d9d9;
        border-radius: 8px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16px;

        &:hover {
            border-color: #409eff;
            background: #f0f9ff;
        }

        .upload-content {
            text-align: center;
            padding: 20px;

            .upload-icon {
                font-size: 48px;
                color: #c0c4cc;
                margin-bottom: 12px;
            }

            .upload-text {
                color: #606266;
                font-size: 16px;
                margin-bottom: 8px;
            }

            .upload-tip {
                color: #909399;
                font-size: 12px;
            }
        }
    }

    .file-list {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .file-item {
            display: flex;
            align-items: center;
            padding: 12px;
            border: 1px solid #e4e7ed;
            border-radius: 8px;
            background: #fff;
            transition: all 0.3s;
            position: relative;

            &:hover {
                border-color: #409eff;
                background: #f5f7fa;
                box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
            }

            .file-icon {
                width: 40px;
                height: 40px;
                display: flex;
                align-items: center;
                justify-content: center;
                background: #f5f7fa;
                border-radius: 8px;
                margin-right: 12px;
                color: #409eff;
                font-size: 20px;
            }

            .file-info {
                flex: 1;
                min-width: 0;

                .file-name {
                    font-size: 14px;
                    color: #303133;
                    margin-bottom: 4px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }

                .file-meta {
                    display: flex;
                    gap: 12px;
                    font-size: 12px;
                    color: #909399;

                    .file-size {
                        font-weight: 500;
                    }

                    .file-type {
                        background: #e1f3d8;
                        color: #67c23a;
                        padding: 2px 6px;
                        border-radius: 4px;
                    }
                }
            }

            .file-actions {
                display: flex;
                gap: 8px;
                opacity: 0;
                transition: opacity 0.3s;

                .el-button {
                    width: 28px;
                    height: 28px;
                    padding: 0;
                    border-radius: 50%;
                    background: rgba(255, 255, 255, 0.9);
                    border: 1px solid #e4e7ed;
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

                    &:hover {
                        background: #fff;
                        transform: scale(1.1);
                    }
                }
            }

            .file-index {
                position: absolute;
                top: 8px;
                right: 8px;
                background: rgba(64, 158, 255, 0.8);
                color: white;
                padding: 2px 6px;
                border-radius: 4px;
                font-size: 12px;
                font-weight: bold;
            }

            &:hover {
                .file-actions {
                    opacity: 1;
                }
            }
        }
    }

    .preview-dialog {
        text-align: center;

        .preview-image {
            max-width: 100%;
            max-height: 70vh;
            object-fit: contain;
            margin-bottom: 16px;
        }

        .preview-pdf {
            width: 100%;
            height: 70vh;
            border: none;
            margin-bottom: 16px;
        }

        .preview-video {
            max-width: 100%;
            max-height: 70vh;
            margin-bottom: 16px;
        }

        .preview-audio {
            width: 100%;
            margin-bottom: 16px;
        }

        .preview-text {
            text-align: left;
            background: #f5f7fa;
            padding: 16px;
            border-radius: 8px;
            margin-bottom: 16px;
            max-height: 400px;
            overflow-y: auto;

            pre {
                margin: 0;
                white-space: pre-wrap;
                word-break: break-word;
            }
        }

        .preview-unavailable {
            padding: 40px;
            color: #909399;

            p {
                margin: 16px 0;
                font-size: 16px;
            }
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
