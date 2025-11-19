<template>
  <div class="trace-record-form">
    <el-form 
      ref="formRef"
      :model="form" 
      :rules="rules" 
      label-width="120px"
      v-loading="loading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="溯源码" prop="traceCode">
            <div class="trace-code-input">
              <el-input 
                v-model="form.traceCode" 
                placeholder="请输入或生成溯源码"
                :disabled="!!record"
              />
              <el-button 
                v-if="!record" 
                type="primary" 
                @click="showCodeGenerator = true"
                style="margin-left: 10px;"
              >
                生成
              </el-button>
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="批次ID" prop="batchId">
            <el-input-number 
              v-model="form.batchId" 
              :min="1"
              :disabled="!!record"
              controls-position="right"
              style="width: 100%"
              placeholder="溯源码关联的批次ID"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="记录类型" prop="recordType">
            <el-select v-model="form.recordType" placeholder="选择记录类型" style="width: 100%">
              <el-option label="生产记录" :value="1" />
              <el-option label="质检记录" :value="2" />
              <el-option label="流通记录" :value="3" />
              <el-option label="销售记录" :value="4" />
              <el-option label="异常记录" :value="5" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="记录阶段" prop="recordStage">
            <el-input v-model="form.recordStage" placeholder="如：种植、加工、包装等" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="相关实体" prop="entityId">
            <el-select 
              v-model="form.entityId" 
              placeholder="选择溯源实体" 
              @change="handleEntityChange"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="entity in entityList"
                :key="entity.id"
                :label="entity.entityName"
                :value="entity.id"
              >
                <span>{{ entity.entityName }}</span>
                <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                  {{ getEntityTypeText(entity.entityType) }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="操作员" prop="operatorName">
            <el-input v-model="form.operatorName" placeholder="操作人员姓名" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="操作员电话" prop="operatorPhone">
            <el-input v-model="form.operatorPhone" placeholder="联系电话" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="记录时间" prop="recordTime">
            <el-date-picker
              v-model="form.recordTime"
              type="datetime"
              placeholder="选择记录时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="位置信息" prop="location">
            <el-input v-model="form.location" placeholder="详细地址或GPS坐标" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="温度(℃)" prop="temperature">
            <el-input-number 
              v-model="form.temperature" 
              :precision="1"
              :step="0.1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="湿度(%)" prop="humidity">
            <el-input-number 
              v-model="form.humidity" 
              :min="0" 
              :max="100"
              :precision="1"
              :step="0.1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="质量等级" prop="qualityGrade">
            <el-select v-model="form.qualityGrade" placeholder="选择质量等级" style="width: 100%">
              <el-option label="优等" value="优等" />
              <el-option label="一等" value="一等" />
              <el-option label="二等" value="二等" />
              <el-option label="合格" value="合格" />
              <el-option label="不合格" value="不合格" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="数量" prop="quantity">
            <el-input-number 
              v-model="form.quantity" 
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="单位" prop="unit">
            <el-select v-model="form.unit" placeholder="选择单位" style="width: 100%">
              <el-option label="公斤" value="公斤" />
              <el-option label="吨" value="吨" />
              <el-option label="袋" value="袋" />
              <el-option label="箱" value="箱" />
              <el-option label="件" value="件" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="检测结果" prop="testResult">
            <el-input v-model="form.testResult" placeholder="检测结果描述" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="内容摘要" prop="contentSummary">
            <el-input 
              v-model="form.contentSummary" 
              type="textarea" 
              :rows="3"
              placeholder="请输入内容摘要"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="详细内容">
            <el-input 
              v-model="form.detailedContent" 
              type="textarea" 
              :rows="4"
              placeholder="请输入详细内容（JSON格式可选）"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="备注">
            <el-input 
              v-model="form.remark" 
              type="textarea" 
              :rows="2"
              placeholder="其他备注信息"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ record ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>

    <!-- 溯源码生成器 -->
    <el-dialog title="生成溯源码" v-model="showCodeGenerator" width="400px">
      <TraceCodeGenerator @generated="handleCodeGenerated" />
    </el-dialog>
  </div>
</template>

<script>
import { createTraceRecord, updateTraceRecord, getTraceEntities } from '@/api/trace'
import { ElMessage } from 'element-plus'
import TraceCodeGenerator from './TraceCodeGenerator.vue'

export default {
  name: 'TraceRecordForm',
  components: {
    TraceCodeGenerator
  },
  props: {
    record: {
      type: Object,
      default: null
    }
  },
  emits: ['success', 'cancel'],
  data() {
    return {
      loading: false,
      showCodeGenerator: false,
      entityList: [],
      form: {
        traceCode: '',
        batchId: null,
        recordType: null,
        recordStage: '',
        entityId: null,
        entityName: '',
        operatorName: '',
        operatorPhone: '',
        recordTime: new Date(),
        location: '',
        temperature: null,
        humidity: null,
        quantity: null,
        unit: '',
        qualityGrade: '',
        testResult: '',
        contentSummary: '',
        detailedContent: '',
        remark: ''
      },
      rules: {
        traceCode: [
          { required: true, message: '请输入溯源码', trigger: 'blur' }
        ],
        batchId: [
          { required: true, message: '请输入批次ID', trigger: 'blur' }
        ],
        recordType: [
          { required: true, message: '请选择记录类型', trigger: 'change' }
        ],
        recordStage: [
          { required: true, message: '请输入记录阶段', trigger: 'blur' }
        ],
        operatorName: [
          { required: true, message: '请输入操作员姓名', trigger: 'blur' }
        ],
        contentSummary: [
          { required: true, message: '请输入内容摘要', trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    record: {
      immediate: true,
      handler(newRecord) {
        if (newRecord) {
          this.form = { ...newRecord }
          if (this.form.recordTime) {
            this.form.recordTime = new Date(this.form.recordTime)
          }
        } else {
          this.resetForm()
        }
      }
    }
  },
  methods: {
    async handleSubmit() {
      try {
        const valid = await this.$refs.formRef.validate()
        if (!valid) return

        this.loading = true
        
        const data = { ...this.form }
        
        let response
        if (this.record) {
          response = await updateTraceRecord(this.record.id, data)
        } else {
          response = await createTraceRecord(data)
        }

        if (response.code === 200) {
          ElMessage.success(this.record ? '更新成功' : '创建成功')
          this.$emit('success')
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('提交失败', error)
        ElMessage.error('操作失败')
      } finally {
        this.loading = false
      }
    },

    handleCancel() {
      this.$emit('cancel')
    },

    handleCodeGenerated(traceCode) {
      this.form.traceCode = traceCode
      this.showCodeGenerator = false
    },

    resetForm() {
      this.form = {
        traceCode: '',
        batchId: null,
        recordType: null,
        recordStage: '',
        entityId: null,
        entityName: '',
        operatorName: '',
        operatorPhone: '',
        recordTime: new Date(),
        location: '',
        temperature: null,
        humidity: null,
        quantity: null,
        unit: '',
        qualityGrade: '',
        testResult: '',
        contentSummary: '',
        detailedContent: '',
        remark: ''
      }
    },

    async loadEntities() {
      try {
        const response = await getTraceEntities({ pageNum: 1, pageSize: 1000 })
        if (response.code === 200) {
          this.entityList = response.data.records || []
        }
      } catch (error) {
        console.error('加载实体列表失败', error)
      }
    },

    handleEntityChange(entityId) {
      const entity = this.entityList.find(e => e.id === entityId)
      if (entity) {
        this.form.entityName = entity.entityName
      }
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
    }
  },
  mounted() {
    this.loadEntities()
  }
}
</script>

<style scoped>
.trace-record-form {
  max-height: 70vh;
  overflow-y: auto;
}

.trace-code-input {
  display: flex;
  align-items: center;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.form-actions .el-button {
  margin-left: 10px;
}
</style>
