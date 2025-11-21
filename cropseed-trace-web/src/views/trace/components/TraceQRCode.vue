<template>
  <el-dialog
    v-model="visible"
    title="溯源二维码"
    width="600px"
    :before-close="handleClose"
  >
    <div class="qrcode-container">
      <!-- 二维码显示区域 -->
      <div class="qrcode-display">
        <div class="qrcode-canvas" ref="qrcodeRef"></div>
        <div class="qrcode-info">
          <div class="info-item">
            <span class="label">溯源码：</span>
            <span class="value">{{ traceCode }}</span>
          </div>
          <div class="info-item" v-if="batchNo">
            <span class="label">批次号：</span>
            <span class="value">{{ batchNo }}</span>
          </div>
          <div class="info-item" v-if="productName">
            <span class="label">产品：</span>
            <span class="value">{{ productName }}</span>
          </div>
        </div>
      </div>

      <!-- 二维码内容 -->
      <div class="qrcode-content">
        <el-alert
          title="扫描说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="scan-tips">
              <p>• 使用微信扫一扫或小程序扫码功能扫描此二维码</p>
              <p>• 可查看完整的溯源信息和区块链验证</p>
              <p>• 二维码可打印或下载使用</p>
            </div>
          </template>
        </el-alert>

        <div class="content-detail">
          <div class="content-label">二维码内容：</div>
          <el-input
            v-model="qrcodeContent"
            type="textarea"
            :rows="3"
            readonly
          />
        </div>

        <!-- URL配置 -->
        <div class="url-config">
          <el-form label-width="100px" size="small">
            <el-form-item label="小程序路径">
              <el-input v-model="miniappPath" placeholder="pages/trace/index" />
            </el-form-item>
            <el-form-item label="Web查询URL">
              <el-input v-model="webUrl" placeholder="https://domain.com/trace" />
            </el-form-item>
            <el-form-item>
              <el-radio-group v-model="qrcodeType" @change="generateQRCode">
                <el-radio label="miniapp">小程序路径</el-radio>
                <el-radio label="web">Web查询链接</el-radio>
                <el-radio label="code">仅溯源码</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="downloadQRCode">
          <el-icon><Download /></el-icon>
          下载二维码
        </el-button>
        <el-button type="success" @click="printQRCode">
          <el-icon><Printer /></el-icon>
          打印二维码
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import QRCode from 'qrcode';
import { ElMessage } from 'element-plus';
import { Download, Printer } from '@element-plus/icons-vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  traceCode: {
    type: String,
    required: true
  },
  batchNo: {
    type: String,
    default: ''
  },
  productName: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue']);

const visible = ref(false);
const qrcodeRef = ref(null);
const qrcodeContent = ref('');
const qrcodeType = ref('web'); // miniapp | web | code
const miniappPath = ref('pages/trace/index');
const webUrl = ref(window.location.origin + '/trace');

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val;
  if (val) {
    nextTick(() => {
      generateQRCode();
    });
  }
});

// 生成二维码内容
const getQRCodeContent = () => {
  switch (qrcodeType.value) {
    case 'miniapp':
      // 小程序路径格式：pages/trace/index?traceCode=BJ2024000001
      return `${miniappPath.value}?traceCode=${props.traceCode}`;
    case 'web':
      // Web查询链接格式：https://domain.com/trace?traceCode=BJ2024000001
      return `${webUrl.value}?traceCode=${props.traceCode}`;
    case 'code':
      // 仅溯源码
      return props.traceCode;
    default:
      return props.traceCode;
  }
};

// 生成二维码
const generateQRCode = async () => {
  if (!qrcodeRef.value) return;

  // 清空之前的二维码
  qrcodeRef.value.innerHTML = '';
  
  qrcodeContent.value = getQRCodeContent();

  try {
    // 生成二维码到 canvas
    const canvas = document.createElement('canvas');
    await QRCode.toCanvas(canvas, qrcodeContent.value, {
      width: 260,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#ffffff'
      },
      errorCorrectionLevel: 'H'
    });
    
    qrcodeRef.value.appendChild(canvas);
  } catch (error) {
    console.error('生成二维码失败:', error);
    ElMessage.error('生成二维码失败');
  }
};

// 下载二维码
const downloadQRCode = () => {
  const canvas = qrcodeRef.value?.querySelector('canvas');
  if (!canvas) {
    ElMessage.error('二维码未生成');
    return;
  }

  // 创建下载链接
  const url = canvas.toDataURL('image/png');
  const link = document.createElement('a');
  link.download = `溯源码_${props.traceCode}.png`;
  link.href = url;
  link.click();

  ElMessage.success('二维码已下载');
};

// 打印二维码
const printQRCode = () => {
  const canvas = qrcodeRef.value?.querySelector('canvas');
  if (!canvas) {
    ElMessage.error('二维码未生成');
    return;
  }

  const printWindow = window.open('', '_blank');
  const img = canvas.toDataURL('image/png');
  
  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>打印溯源二维码</title>
      <style>
        body {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 20px;
          font-family: Arial, sans-serif;
        }
        .qrcode-print {
          text-align: center;
          page-break-after: always;
        }
        img {
          width: 300px;
          height: 300px;
          margin: 20px 0;
        }
        .info {
          margin-top: 20px;
          font-size: 14px;
        }
        .info-item {
          margin: 8px 0;
        }
        .label {
          font-weight: bold;
          color: #333;
        }
        .value {
          color: #666;
        }
        @media print {
          body {
            margin: 0;
            padding: 20px;
          }
        }
      </style>
    </head>
    <body>
      <div class="qrcode-print">
        <h2>产品溯源二维码</h2>
        <img src="${img}" alt="溯源二维码" />
        <div class="info">
          <div class="info-item">
            <span class="label">溯源码：</span>
            <span class="value">${props.traceCode}</span>
          </div>
          ${props.batchNo ? `
          <div class="info-item">
            <span class="label">批次号：</span>
            <span class="value">${props.batchNo}</span>
          </div>
          ` : ''}
          ${props.productName ? `
          <div class="info-item">
            <span class="label">产品：</span>
            <span class="value">${props.productName}</span>
          </div>
          ` : ''}
          <div class="info-item" style="margin-top: 20px; color: #999; font-size: 12px;">
            扫描二维码查看完整溯源信息
          </div>
        </div>
      </div>
    </body>
    </html>
  `);
  
  printWindow.document.close();
  printWindow.focus();
  
  // 延迟打印，确保内容加载完成
  setTimeout(() => {
    printWindow.print();
    printWindow.close();
  }, 250);
};

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false);
};
</script>

<style scoped lang="scss">
.qrcode-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.qrcode-display {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 24px;
  background: #49f268;
  border-radius: 12px;
}

.qrcode-canvas {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  
  :deep(canvas) {
    display: block;
  }
}

.qrcode-info {
  flex: 1;
  color: white;
  
  .info-item {
    margin-bottom: 12px;
    font-size: 16px;
    
    .label {
      opacity: 0.9;
      margin-right: 8px;
    }
    
    .value {
      font-weight: bold;
      font-size: 18px;
    }
  }
}

.qrcode-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  justify-content: center;
}

.scan-tips {
  p {
    margin: 6px 0;
    color: #606266;
    font-size: 13px;
  }
}

.content-detail {
  .content-label {
    margin-bottom: 8px;
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }
}

.url-config {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
