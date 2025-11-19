<template>
  <div class="trace-code-generator">
    <el-form :model="form" label-width="100px">
      <el-form-item label="地区选择">
        <el-select 
          v-model="form.regionCode" 
          placeholder="选择地区" 
          style="width: 100%"
          @change="handleRegionChange"
        >
          <el-option 
            v-for="(name, code) in regions" 
            :key="code" 
            :label="`${name} (${code})`" 
            :value="code"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="生成方式">
        <el-radio-group v-model="form.generateType">
          <el-radio :label="1">单个生成</el-radio>
          <el-radio :label="2">批量生成</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.generateType === 2" label="生成数量">
        <el-input-number 
          v-model="form.count" 
          :min="1" 
          :max="100"
          controls-position="right"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item>
        <el-button 
          type="primary" 
          @click="generateCode" 
          :loading="generating"
          style="width: 100%"
        >
          {{ form.generateType === 1 ? '生成溯源码' : `批量生成 ${form.count} 个溯源码` }}
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 生成结果 -->
    <div v-if="generatedCodes.length > 0" class="generated-result">
      <el-divider>生成结果</el-divider>
      
      <div v-if="form.generateType === 1" class="single-result">
        <el-input 
          :value="generatedCodes[0]" 
          readonly
          class="result-input"
        >
          <template #append>
            <el-button @click="copyToClipboard(generatedCodes[0])">复制</el-button>
          </template>
        </el-input>
        
        <div class="actions">
          <el-button type="primary" @click="useCode(generatedCodes[0])">
            使用此溯源码
          </el-button>
        </div>
      </div>

      <div v-else class="batch-result">
        <el-tag class="result-tag">
          成功生成 {{ generatedCodes.length }} 个溯源码
        </el-tag>
        
        <div class="batch-actions">
          <el-button @click="copyAllCodes">复制全部</el-button>
          <el-button type="success" @click="exportCodes">导出</el-button>
        </div>

        <el-table :data="batchTableData" style="width: 100%; margin-top: 10px;" max-height="300">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="traceCode" label="溯源码">
            <template #default="{ row }">
              <el-button type="text" @click="useCode(row.traceCode)">
                {{ row.traceCode }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="copyToClipboard(row.traceCode)">
                复制
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { generateTraceCode, batchGenerateTraceCodes, getSupportedRegions } from '@/api/trace'
import { ElMessage } from 'element-plus'

export default {
  name: 'TraceCodeGenerator',
  emits: ['generated'],
  data() {
    return {
      generating: false,
      form: {
        regionCode: 'BJ',
        generateType: 1,
        count: 10
      },
      regions: {},
      generatedCodes: []
    }
  },
  computed: {
    batchTableData() {
      return this.generatedCodes.map(code => ({ traceCode: code }))
    }
  },
  async mounted() {
    await this.loadRegions()
  },
  methods: {
    async loadRegions() {
      try {
        const response = await getSupportedRegions()
        if (response.code === 200) {
          this.regions = response.data || {}
        }
      } catch (error) {
        console.error('加载地区列表失败', error)
        // 使用默认地区列表
        this.regions = {
          'BJ': '北京市',
          'SH': '上海市', 
          'GZ': '广州市',
          'SZ': '深圳市',
          'HZ': '杭州市',
          'CD': '成都市',
          'WH': '武汉市',
          'XA': '西安市'
        }
      }
    },

    async generateCode() {
      if (!this.form.regionCode) {
        ElMessage.error('请选择地区')
        return
      }

      this.generating = true
      try {
        if (this.form.generateType === 1) {
          // 单个生成
          const response = await generateTraceCode(this.form.regionCode)
          if (response.code === 200) {
            this.generatedCodes = [response.data]
            ElMessage.success('溯源码生成成功')
          } else {
            ElMessage.error(response.message || '生成失败')
          }
        } else {
          // 批量生成
          const response = await batchGenerateTraceCodes(this.form.regionCode, this.form.count)
          if (response.code === 200) {
            this.generatedCodes = response.data.traceCodes || []
            ElMessage.success(`成功生成 ${this.generatedCodes.length} 个溯源码`)
          } else {
            ElMessage.error(response.message || '批量生成失败')
          }
        }
      } catch (error) {
        console.error('生成溯源码失败', error)
        ElMessage.error('生成失败')
      } finally {
        this.generating = false
      }
    },

    handleRegionChange() {
      // 地区改变时清空之前的结果
      this.generatedCodes = []
    },

    useCode(traceCode) {
      this.$emit('generated', traceCode)
    },

    copyToClipboard(text) {
      try {
        navigator.clipboard.writeText(text)
        ElMessage.success('复制成功')
      } catch (error) {
        // Fallback for older browsers
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        ElMessage.success('复制成功')
      }
    },

    copyAllCodes() {
      const allCodes = this.generatedCodes.join('\n')
      this.copyToClipboard(allCodes)
    },

    exportCodes() {
      const csvContent = 'data:text/csv;charset=utf-8,' + 
        '序号,溯源码,地区\n' +
        this.generatedCodes.map((code, index) => 
          `${index + 1},${code},${this.regions[this.form.regionCode]}`
        ).join('\n')
      
      const encodedUri = encodeURI(csvContent)
      const link = document.createElement('a')
      link.setAttribute('href', encodedUri)
      link.setAttribute('download', `溯源码列表_${new Date().toISOString().slice(0, 10)}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('导出成功')
    }
  }
}
</script>

<style scoped>
.trace-code-generator {
  padding: 20px;
}

.generated-result {
  margin-top: 20px;
}

.single-result {
  text-align: center;
}

.result-input {
  margin-bottom: 15px;
}

.actions {
  margin-top: 15px;
}

.batch-result {
  text-align: center;
}

.result-tag {
  margin-bottom: 15px;
  font-size: 14px;
}

.batch-actions {
  margin-bottom: 15px;
}

.batch-actions .el-button {
  margin: 0 5px;
}
</style>
