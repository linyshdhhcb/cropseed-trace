<template>
  <div class="trace-entity-detail">
    <div v-if="entity" class="detail-content">
      <!-- 基础信息 -->
      <div class="info-section">
        <h3>基础信息</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>实体编码:</label>
              <span class="entity-code">{{ entity.entityCode }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>实体名称:</label>
              <span>{{ entity.entityName }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>实体类型:</label>
              <el-tag :type="getEntityTypeColor(entity.entityType)">
                {{ getEntityTypeText(entity.entityType) }}
              </el-tag>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="8">
            <div class="info-item">
              <label>状态:</label>
              <el-tag :type="entity.status ? 'success' : 'danger'">
                {{ entity.status ? '启用' : '停用' }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>法人代表:</label>
              <span>{{ entity.legalPerson || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>营业执照号:</label>
              <span>{{ entity.licenseNumber || '-' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 联系信息 -->
      <div class="info-section">
        <h3>联系信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>联系人:</label>
              <span>{{ entity.contactPerson || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>联系电话:</label>
              <span>{{ entity.contactPhone || '-' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 地址信息 -->
      <div class="info-section">
        <h3>地址信息</h3>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="info-item">
              <label>详细地址:</label>
              <span>{{ entity.address || '-' }}</span>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="12">
            <div class="info-item">
              <label>经度:</label>
              <span>{{ entity.longitude || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>纬度:</label>
              <span>{{ entity.latitude || '-' }}</span>
            </div>
          </el-col>
        </el-row>

        <!-- 地图显示（如果有经纬度） -->
        <div v-if="entity.longitude && entity.latitude" class="map-section">
          <div class="map-placeholder">
            <p>地图位置: {{ entity.longitude }}, {{ entity.latitude }}</p>
            <p class="map-note">（此处可集成地图组件显示具体位置）</p>
          </div>
        </div>
      </div>

      <!-- 资质认证信息 -->
      <div v-if="entity.certificationInfo" class="info-section">
        <h3>资质认证信息</h3>
        <div class="info-item full-width">
          <div class="certification-content">
            <el-input 
              :value="entity.certificationInfo" 
              type="textarea" 
              :rows="4" 
              readonly
            />
          </div>
        </div>
      </div>

      <!-- 系统信息 -->
      <div class="info-section">
        <h3>系统信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>创建时间:</label>
              <span>{{ formatDateTime(entity.createTime) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>更新时间:</label>
              <span>{{ formatDateTime(entity.updateTime) }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 统计信息 -->
      <div class="info-section">
        <h3>统计信息</h3>
        <div class="statistics-cards">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-item">
                  <div class="stat-number">{{ statistics.relatedRecords }}</div>
                  <div class="stat-label">关联记录</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-item">
                  <div class="stat-number">{{ statistics.activeDays }}</div>
                  <div class="stat-label">活跃天数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-item">
                  <div class="stat-number">{{ statistics.lastActiveTime }}</div>
                  <div class="stat-label">最后活跃</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-item">
                  <div class="stat-number">{{ statistics.operationCount }}</div>
                  <div class="stat-label">操作次数</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button type="primary" @click="viewRelatedRecords">查看关联记录</el-button>
        <el-button type="success" @click="editEntity">编辑实体</el-button>
        <el-button type="info" @click="exportInfo">导出信息</el-button>
      </div>
    </div>

    <div v-else class="no-data">
      <el-empty description="暂无实体详情" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'TraceEntityDetail',
  props: {
    entity: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      statistics: {
        relatedRecords: 0,
        activeDays: 0,
        lastActiveTime: '-',
        operationCount: 0
      }
    }
  },
  watch: {
    entity: {
      immediate: true,
      handler(newEntity) {
        if (newEntity) {
          this.loadStatistics()
        }
      }
    }
  },
  methods: {
    loadStatistics() {
      // 模拟统计数据
      this.statistics = {
        relatedRecords: Math.floor(Math.random() * 100),
        activeDays: Math.floor(Math.random() * 30),
        lastActiveTime: '2天前',
        operationCount: Math.floor(Math.random() * 200)
      }
    },

    viewRelatedRecords() {
      if (this.entity) {
        this.$router.push(`/trace/records?entityId=${this.entity.id}`)
      }
    },

    editEntity() {
      this.$emit('edit', this.entity)
    },

    exportInfo() {
      // 导出实体信息
      const data = {
        实体编码: this.entity.entityCode,
        实体名称: this.entity.entityName,
        实体类型: this.getEntityTypeText(this.entity.entityType),
        法人代表: this.entity.legalPerson,
        联系人: this.entity.contactPerson,
        联系电话: this.entity.contactPhone,
        详细地址: this.entity.address,
        经度: this.entity.longitude,
        纬度: this.entity.latitude,
        营业执照号: this.entity.licenseNumber,
        状态: this.entity.status ? '启用' : '停用',
        创建时间: this.formatDateTime(this.entity.createTime)
      }

      const csvContent = 'data:text/csv;charset=utf-8,\uFEFF' + 
        Object.entries(data).map(([key, value]) => `${key},${value || ''}`).join('\n')
      
      const encodedUri = encodeURI(csvContent)
      const link = document.createElement('a')
      link.setAttribute('href', encodedUri)
      link.setAttribute('download', `实体信息_${this.entity.entityCode}_${new Date().toISOString().slice(0, 10)}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      this.$message.success('导出成功')
    },

    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    getEntityTypeText(type) {
      const typeMap = {
        1: '生产基地',
        2: '加工厂',
        3: '仓库',
        4: '经销商',
        5: '零售商'
      }
      return typeMap[type] || '未知'
    },

    getEntityTypeColor(type) {
      const colorMap = {
        1: 'success',
        2: 'warning',
        3: 'info',
        4: 'primary',
        5: ''
      }
      return colorMap[type] || ''
    }
  }
}
</script>

<style scoped>
.trace-entity-detail {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 30px;
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.info-section h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 120px;
  margin-right: 10px;
}

.info-item span {
  color: #303133;
}

.info-item.full-width {
  flex-direction: column;
}

.info-item.full-width label {
  margin-bottom: 8px;
}

.entity-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409eff;
}

.map-section {
  margin-top: 20px;
}

.map-placeholder {
  background: #f0f9ff;
  border: 1px dashed #409eff;
  padding: 30px;
  text-align: center;
  border-radius: 8px;
  color: #606266;
}

.map-note {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.certification-content {
  width: 100%;
}

.statistics-cards {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  margin-bottom: 20px;
}

.stat-item {
  padding: 10px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.actions {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.actions .el-button {
  margin: 0 10px;
}

.no-data {
  text-align: center;
  padding: 40px;
}

@media (max-width: 768px) {
  .trace-entity-detail {
    padding: 10px;
  }
  
  .info-section {
    padding: 15px;
  }
  
  .actions .el-button {
    margin: 5px;
  }
}
</style>
