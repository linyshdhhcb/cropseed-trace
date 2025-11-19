<template>
  <div class="trace-entity-form">
    <el-form 
      ref="formRef"
      :model="form" 
      :rules="rules" 
      label-width="120px"
      v-loading="loading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="实体编码" prop="entityCode">
            <el-input 
              v-model="form.entityCode" 
              placeholder="请输入实体编码"
              :disabled="!!entity"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="实体名称" prop="entityName">
            <el-input v-model="form.entityName" placeholder="请输入实体名称" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="实体类型" prop="entityType">
            <el-select v-model="form.entityType" placeholder="选择实体类型" style="width: 100%">
              <el-option label="生产基地" :value="1" />
              <el-option label="加工厂" :value="2" />
              <el-option label="仓库" :value="3" />
              <el-option label="经销商" :value="4" />
              <el-option label="零售商" :value="5" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="法人代表" prop="legalPerson">
            <el-input v-model="form.legalPerson" placeholder="请输入法人代表姓名" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="联系人" prop="contactPerson">
            <el-input v-model="form.contactPerson" placeholder="请输入联系人姓名" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入详细地址" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="经度" prop="longitude">
            <el-input-number 
              v-model="form.longitude" 
              :min="-180"
              :max="180"
              :precision="6"
              :step="0.000001"
              controls-position="right"
              style="width: 100%"
              placeholder="经度"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="纬度" prop="latitude">
            <el-input-number 
              v-model="form.latitude" 
              :min="-90"
              :max="90"
              :precision="6"
              :step="0.000001"
              controls-position="right"
              style="width: 100%"
              placeholder="纬度"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="营业执照号" prop="licenseNumber">
            <el-input v-model="form.licenseNumber" placeholder="请输入营业执照号" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="资质认证信息">
            <el-input 
              v-model="form.certificationInfo" 
              type="textarea" 
              :rows="3"
              placeholder="请输入资质认证信息（JSON格式可选）"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ entity ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { createTraceEntity, updateTraceEntity } from '@/api/trace'
import { ElMessage } from 'element-plus'

export default {
  name: 'TraceEntityForm',
  props: {
    entity: {
      type: Object,
      default: null
    }
  },
  emits: ['success', 'cancel'],
  data() {
    return {
      loading: false,
      form: {
        entityCode: '',
        entityName: '',
        entityType: null,
        legalPerson: '',
        contactPerson: '',
        contactPhone: '',
        address: '',
        longitude: null,
        latitude: null,
        licenseNumber: '',
        certificationInfo: '',
        status: 1
      },
      rules: {
        entityCode: [
          { required: true, message: '请输入实体编码', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        entityName: [
          { required: true, message: '请输入实体名称', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        entityType: [
          { required: true, message: '请选择实体类型', trigger: 'change' }
        ],
        contactPerson: [
          { required: true, message: '请输入联系人', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
        ],
        address: [
          { required: true, message: '请输入详细地址', trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    entity: {
      immediate: true,
      handler(newEntity) {
        if (newEntity) {
          this.form = { ...newEntity }
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
        if (this.entity) {
          response = await updateTraceEntity(this.entity.id, data)
        } else {
          response = await createTraceEntity(data)
        }

        if (response.code === 200) {
          ElMessage.success(this.entity ? '更新成功' : '创建成功')
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

    resetForm() {
      this.form = {
        entityCode: '',
        entityName: '',
        entityType: null,
        legalPerson: '',
        contactPerson: '',
        contactPhone: '',
        address: '',
        longitude: null,
        latitude: null,
        licenseNumber: '',
        certificationInfo: '',
        status: 1
      }
    }
  }
}
</script>

<style scoped>
.trace-entity-form {
  max-height: 70vh;
  overflow-y: auto;
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
