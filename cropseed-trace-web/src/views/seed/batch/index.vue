<template>
    <div class="app-container">
        <!-- 搜索表单 -->
        <el-form ref="searchFormRef" :model="searchForm" :inline="true" class="search-form">
            <el-form-item label="批次号" prop="batchNo">
                <el-input v-model="searchForm.batchNo" placeholder="请输入批次号" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="种子" prop="seedId">
                <el-select v-model="searchForm.seedId" placeholder="请选择种子" clearable style="width: 200px">
                    <el-option v-for="seed in seedList" :key="seed.id" :label="seed.seedName" :value="seed.id" />
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
                新增批次
            </el-button>
            <el-button type="danger" :icon="Delete" :disabled="!multipleSelection.length" @click="handleBatchDelete">
                批量删除
            </el-button>
            <el-button type="success" :icon="Download" @click="handleExport">
                导出数据
            </el-button>
        </div>

        <!-- 数据表格 -->
        <el-card class="page-container">
            <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange"
                style="width: 100%" border stripe>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="batchNo" label="批次号" width="150" align="center">
                    <template #default="{ row }">
                        <el-button type="text" @click="handleView(row)" class="batch-no-link">
                            {{ row.batchNo }}
                        </el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="traceCode" label="溯源码" width="140" align="center">
                    <template #default="{ row }">
                        <div v-if="row.traceCode" class="trace-code-cell">
                            <el-button type="text" @click="viewTraceChain(row.traceCode)">
                                {{ row.traceCode }}
                            </el-button>
                        </div>
                        <div v-else>
                            <el-button type="primary" size="small" @click="generateTraceCode(row)">
                                生成溯源码
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="seedName" label="种子名称" width="150" show-overflow-tooltip />
                <el-table-column prop="producerName" label="生产商" width="150" show-overflow-tooltip />
                <el-table-column prop="productionDate" label="生产日期" width="120" align="center" />
                <el-table-column prop="expiryDate" label="过期日期" width="120" align="center" />
                <el-table-column prop="qualityStatus" label="质检状态" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.qualityStatus === 1 ? 'success' : 'danger'">
                            {{ row.qualityStatus === 1 ? '合格' : '不合格' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
                <el-table-column prop="updateTime" label="更新时间" width="160" />
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button type="info" size="small" @click="handleView(row)">
                            查看
                        </el-button>
                        <el-button v-if="row.traceCode" type="success" size="small" @click="manageTrace(row)">
                            溯源
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
                <!-- 基础信息 -->
                <el-divider content-position="left">基础信息</el-divider>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="批次号" prop="batchNo">
                            <el-input v-model="formData.batchNo" placeholder="请输入批次号" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="种子" prop="seedId">
                            <el-select v-model="formData.seedId" placeholder="请选择种子" style="width: 100%">
                                <el-option v-for="seed in seedList" :key="seed.id" :label="seed.seedName"
                                    :value="seed.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <!-- <el-col :span="12">
                        <el-form-item label="溯源码" prop="traceCode">
                            <el-input v-model="formData.traceCode" placeholder="请输入溯源码（可选）" />
                        </el-form-item>
                    </el-col> -->
                    <el-col :span="12">
                        <el-form-item label="单位" prop="unit">
                            <el-input v-model="formData.unit" placeholder="如：千克、斤" />
                        </el-form-item>
                    </el-col>
                </el-row>

                <!-- 生产信息 -->
                <el-divider content-position="left">生产信息</el-divider>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="生产商" prop="producerId">
                            <el-select 
                                v-model="formData.producerId" 
                                placeholder="请选择生产商" 
                                style="width: 100%"
                                filterable
                                @change="handleProducerChange"
                            >
                                <el-option 
                                    v-for="entity in producerList" 
                                    :key="entity.id" 
                                    :label="entity.entityName" 
                                    :value="entity.id"
                                >
                                    <span>{{ entity.entityName }}</span>
                                    <span style="color: #8492a6; font-size: 12px; margin-left: 10px">
                                        {{ entity.entityCode }}
                                    </span>
                                </el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="生产地点" prop="productionLocation">
                            <el-input v-model="formData.productionLocation" placeholder="自动从生产商获取" readonly />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="生产日期" prop="productionDate">
                            <el-date-picker v-model="formData.productionDate" type="date" placeholder="请选择生产日期"
                                style="width: 100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="收获日期" prop="harvestDate">
                            <el-date-picker v-model="formData.harvestDate" type="date" placeholder="请选择收获日期"
                                style="width: 100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="加工日期" prop="processingDate">
                            <el-date-picker v-model="formData.processingDate" type="date" placeholder="请选择加工日期"
                                style="width: 100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="过期日期" prop="expiryDate">
                            <el-date-picker v-model="formData.expiryDate" type="date" placeholder="请选择过期日期"
                                style="width: 100%" value-format="YYYY-MM-DD" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="生产设备" prop="productionEquipment">
                            <el-input v-model="formData.productionEquipment" placeholder="请输入生产设备" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="加工方式" prop="processingMethod">
                            <el-select v-model="formData.processingMethod" placeholder="请选择或输入加工方式" 
                                style="width: 100%" filterable allow-create>
                                <el-option label="干燥加工" value="干燥加工" />
                                <el-option label="筛选分级" value="筛选分级" />
                                <el-option label="包衣处理" value="包衣处理" />
                                <el-option label="清选去杂" value="清选去杂" />
                                <el-option label="脱粒加工" value="脱粒加工" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="种子来源" prop="seedSource">
                            <el-input v-model="formData.seedSource" placeholder="请输入种子来源" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="亲本品种" prop="parentVariety">
                            <el-input v-model="formData.parentVariety" placeholder="请输入亲本品种" />
                        </el-form-item>
                    </el-col>
                </el-row>

                <!-- 质量信息 -->
                <el-divider content-position="left">质量信息</el-divider>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="质检状态" prop="qualityStatus">
                            <el-radio-group v-model="formData.qualityStatus">
                                <el-radio :label="1">合格</el-radio>
                                <el-radio :label="0">不合格</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="初始质量等级" prop="initialQualityGrade">
                            <el-select v-model="formData.initialQualityGrade" placeholder="请选择质量等级" style="width: 100%">
                                <el-option label="优等" value="优等" />
                                <el-option label="一等" value="一等" />
                                <el-option label="二等" value="二等" />
                                <el-option label="三等" value="三等" />
                                <el-option label="合格" value="合格" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="8">
                        <el-form-item label="含水率(%)" prop="moistureContent">
                            <el-input-number v-model="formData.moistureContent" :min="0" :max="100" :precision="2" 
                                placeholder="含水率" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="发芽率(%)" prop="germinationRate">
                            <el-input-number v-model="formData.germinationRate" :min="0" :max="100" :precision="2"
                                placeholder="发芽率" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="纯度(%)" prop="purity">
                            <el-input-number v-model="formData.purity" :min="0" :max="100" :precision="2"
                                placeholder="纯度" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="质检报告" prop="qualityReport">
                    <MultiImageUpload 
                        v-model="formData.qualityReport" 
                        :max-count="1"
                        upload-text="上传质检报告图片"
                    />
                </el-form-item>

                <!-- 包装信息 -->
                <el-divider content-position="left">包装与储存</el-divider>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="包装类型" prop="packagingType">
                            <el-input v-model="formData.packagingType" placeholder="如：真空袋、编织袋" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="包装规格" prop="packagingSpecification">
                            <el-input v-model="formData.packagingSpecification" placeholder="如：10kg/袋" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="储存条件" prop="storageCondition">
                    <el-input v-model="formData.storageCondition" placeholder="请输入储存条件要求" />
                </el-form-item>

                <!-- 操作员与认证 -->
                <el-divider content-position="left">操作员与认证</el-divider>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="操作员姓名" prop="initialOperatorName">
                            <el-input v-model="formData.initialOperatorName" placeholder="请输入操作员姓名" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="操作员电话" prop="initialOperatorPhone">
                            <el-input v-model="formData.initialOperatorPhone" placeholder="请输入操作员电话" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="认证信息" prop="certification">
                            <el-select v-model="formData.certification" placeholder="请选择认证类型" 
                                style="width: 100%" multiple collapse-tags>
                                <el-option label="有机认证" value="有机认证" />
                                <el-option label="绿色食品" value="绿色食品" />
                                <el-option label="无公害农产品" value="无公害农产品" />
                                <el-option label="GAP认证" value="GAP认证" />
                                <el-option label="ISO9001" value="ISO9001" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="可追溯等级" prop="traceabilityLevel">
                            <el-select v-model="formData.traceabilityLevel" placeholder="请选择可追溯等级" style="width: 100%">
                                <el-option label="基础" :value="1" />
                                <el-option label="详细" :value="2" />
                                <el-option label="完整" :value="3" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>

                <!-- 备注 -->
                <el-form-item label="备注" prop="remarks">
                    <el-input v-model="formData.remarks" type="textarea" :rows="3" placeholder="请输入备注" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
                    确定
                </el-button>
            </template>
        </el-dialog>

        <!-- 查看详情对话框 -->
        <el-dialog v-model="viewDialogVisible" title="批次详情" width="1000px" class="batch-detail-dialog">
            <div v-if="batchDetail" class="batch-detail">
                <!-- 基础信息 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">基础信息</span>
                        </div>
                    </template>
                    <el-descriptions :column="2" border size="default">
                        <el-descriptions-item label="批次号" label-class-name="label-bold">
                            <el-tag type="primary" size="large">{{ batchDetail.batchNo }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="种子ID" label-class-name="label-bold">
                            {{ batchDetail.seedId || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="种子名称" label-class-name="label-bold">
                            {{ batchDetail.seedName || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="单位" label-class-name="label-bold">
                            {{ batchDetail.unit || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="溯源码" :span="2" label-class-name="label-bold">
                            <span v-if="batchDetail.traceCode" class="trace-code-value">
                                <code>{{ batchDetail.traceCode }}</code>
                                <el-button type="primary" link size="small" @click="viewTraceChain(batchDetail.traceCode)" style="margin-left: 10px;">
                                    查看溯源链
                                </el-button>
                            </span>
                            <el-tag v-else type="warning">未生成</el-tag>
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 生产信息 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">生产信息</span>
                        </div>
                    </template>
                    <el-descriptions :column="2" border size="default">
                        <el-descriptions-item label="生产商ID" label-class-name="label-bold" v-if="batchDetail.producerId">
                            {{ batchDetail.producerId }}
                        </el-descriptions-item>
                        <el-descriptions-item label="生产商名称" label-class-name="label-bold" :span="batchDetail.producerId ? 1 : 2">
                            {{ batchDetail.producerName || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="生产地点" label-class-name="label-bold" :span="2">
                            {{ batchDetail.productionLocation || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="生产日期" label-class-name="label-bold">
                            {{ batchDetail.productionDate || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="收获日期" label-class-name="label-bold">
                            {{ batchDetail.harvestDate || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="加工日期" label-class-name="label-bold">
                            {{ batchDetail.processingDate || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="过期日期" label-class-name="label-bold">
                            {{ batchDetail.expiryDate || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="生产设备" label-class-name="label-bold">
                            {{ batchDetail.productionEquipment || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="加工方式" label-class-name="label-bold">
                            {{ batchDetail.processingMethod || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="种子来源" label-class-name="label-bold">
                            {{ batchDetail.seedSource || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="亲本品种" label-class-name="label-bold">
                            {{ batchDetail.parentVariety || '-' }}
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 质量信息 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">质量信息</span>
                        </div>
                    </template>
                    <el-descriptions :column="3" border size="default">
                        <el-descriptions-item label="质检状态" label-class-name="label-bold">
                            <el-tag :type="batchDetail.qualityStatus === 1 ? 'success' : 'danger'" size="large">
                                {{ batchDetail.qualityStatus === 1 ? '合格' : '不合格' }}
                            </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="初始质量等级" label-class-name="label-bold" :span="2">
                            <el-tag v-if="batchDetail.initialQualityGrade" type="success">{{ batchDetail.initialQualityGrade }}</el-tag>
                            <span v-else>-</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="含水率" label-class-name="label-bold">
                            <span v-if="batchDetail.moistureContent" class="quality-value">💧 {{ batchDetail.moistureContent }}%</span>
                            <span v-else>-</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="发芽率" label-class-name="label-bold">
                            <span v-if="batchDetail.germinationRate" class="quality-value">🌱 {{ batchDetail.germinationRate }}%</span>
                            <span v-else>-</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="纯度" label-class-name="label-bold">
                            <span v-if="batchDetail.purity" class="quality-value">✅ {{ batchDetail.purity }}%</span>
                            <span v-else>-</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="质检报告" label-class-name="label-bold" :span="3">
                            <el-link v-if="batchDetail.qualityReport" :href="batchDetail.qualityReport" target="_blank" type="primary">
                                <el-icon><Document /></el-icon> 查看报告
                            </el-link>
                            <span v-else>-</span>
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 包装与储存 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">包装与储存</span>
                        </div>
                    </template>
                    <el-descriptions :column="2" border size="default">
                        <el-descriptions-item label="包装类型" label-class-name="label-bold">
                            {{ batchDetail.packagingType || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="包装规格" label-class-name="label-bold">
                            {{ batchDetail.packagingSpecification || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="储存条件" label-class-name="label-bold" :span="2">
                            {{ batchDetail.storageCondition || '-' }}
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 操作员与认证 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">操作员与认证</span>
                        </div>
                    </template>
                    <el-descriptions :column="2" border size="default">
                        <el-descriptions-item label="操作员姓名" label-class-name="label-bold">
                            {{ batchDetail.initialOperatorName || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="操作员电话" label-class-name="label-bold">
                            {{ batchDetail.initialOperatorPhone || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="认证信息" label-class-name="label-bold">
                            <el-tag v-if="batchDetail.certification" type="success">{{ batchDetail.certification }}</el-tag>
                            <span v-else>-</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="可追溯等级" label-class-name="label-bold">
                            <el-tag v-if="batchDetail.traceabilityLevel === 1" type="info" size="large">基础</el-tag>
                            <el-tag v-else-if="batchDetail.traceabilityLevel === 2" type="warning" size="large">详细</el-tag>
                            <el-tag v-else-if="batchDetail.traceabilityLevel === 3" type="success" size="large">完整</el-tag>
                            <span v-else>-</span>
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 备注信息 -->
                <el-card class="detail-card" shadow="never" v-if="batchDetail.remarks">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">备注信息</span>
                        </div>
                    </template>
                    <div class="remarks-content">
                        {{ batchDetail.remarks }}
                    </div>
                </el-card>

                <!-- 系统信息 -->
                <el-card class="detail-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">系统信息</span>
                        </div>
                    </template>
                    <el-descriptions :column="2" border size="default">
                        <el-descriptions-item label="创建时间" label-class-name="label-bold">
                            {{ batchDetail.createTime || '-' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="更新时间" label-class-name="label-bold">
                            {{ batchDetail.updateTime || '-' }}
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>
                
                <!-- 溯源操作区域 -->
                <div v-if="batchDetail.traceCode" class="trace-actions" style="margin-top: 20px; text-align: center;">
                    <el-button type="primary" @click="viewTraceChain(batchDetail.traceCode)">
                        查看溯源链
                    </el-button>
                    <el-button type="success" @click="addTraceRecord(batchDetail)">
                        添加溯源记录
                    </el-button>
                    <el-button type="info" @click="verifyTraceIntegrity(batchDetail.traceCode)">
                        验证完整性
                    </el-button>
                </div>
                <div v-else class="no-trace" style="margin-top: 20px; text-align: center;">
                    <el-alert
                        title="该批次尚未生成溯源码"
                        type="warning"
                        show-icon
                        :closable="false"
                    />
                    <el-button type="primary" style="margin-top: 15px;" @click="generateTraceCode(batchDetail)">
                        立即生成溯源码
                    </el-button>
                </div>
            </div>
        </el-dialog>

        <!-- 溯源链查看对话框 -->
        <el-dialog v-model="showTraceChainDialog" title="完整溯源链" width="1200px">
            <TraceChainView v-if="currentTraceCode" :trace-code="currentTraceCode" />
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import {
    Search,
    Refresh,
    Plus,
    Delete,
    Download,
    Link,
    Document
} from "@element-plus/icons-vue";
import {
    getSeedBatchList,
    addSeedBatch,
    updateSeedBatch,
    deleteSeedBatch,
    batchDeleteSeedBatch,
    getSeedBatchDetail,
    updateBatchTraceCode,
} from "@/api/seed";
import { getSeedInfoList } from "@/api/seed";
import { 
    generateTraceCode as apiGenerateTraceCode,
    verifyTraceIntegrity as apiVerifyTraceIntegrity,
    getTraceEntities
} from "@/api/trace";
import TraceChainView from "@/views/trace/components/TraceChainView.vue";
import MultiImageUpload from "@/components/MultiImageUpload.vue";

// 路由
const router = useRouter();

// 搜索表单
const searchFormRef = ref();
const searchForm = reactive({
    batchNo: "",
    seedId: null,
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
    batchNo: "",
    seedId: null,
    productionDate: "",
    expiryDate: "",
    qualityReport: [],  // MultiImageUpload需要数组格式
    qualityStatus: 1,
    remarks: "",
    traceCode: "",
    producerId: null,
    producerName: "",
    productionLocation: "",
    harvestDate: "",
    processingDate: "",
    storageCondition: "",
    certification: [],  // 多选下拉框需要数组格式
    traceabilityLevel: 1,
    unit: "",
    initialQualityGrade: "",
    moistureContent: null,
    germinationRate: null,
    purity: null,
    packagingType: "",
    packagingSpecification: "",
    initialOperatorName: "",
    initialOperatorPhone: "",
    productionEquipment: "",
    processingMethod: "",
    seedSource: "",
    parentVariety: "",
});

// 表单验证规则
const formRules = {
    batchNo: [
        { required: true, message: "请输入批次号", trigger: "blur" },
        { min: 2, max: 50, message: "批次号长度在 2 到 50 个字符", trigger: "blur" },
    ],
    seedId: [{ required: true, message: "请选择种子", trigger: "change" }],
    producerId: [{ required: true, message: "请选择生产商", trigger: "change" }],
    productionDate: [{ required: true, message: "请选择生产日期", trigger: "change" }],
    expiryDate: [{ required: true, message: "请选择过期日期", trigger: "change" }],
    qualityStatus: [{ required: true, message: "请选择质检状态", trigger: "change" }],
};

// 种子列表
const seedList = ref([]);

// 生产商列表（溯源实体-生产商类型）
const producerList = ref([]);

// 查看详情对话框
const viewDialogVisible = ref(false);
const batchDetail = ref(null);

// 溯源相关变量
const showTraceChainDialog = ref(false);
const currentTraceCode = ref('');

// 获取批次列表
const loadBatchList = async () => {
    try {
        loading.value = true;
        const params = {
            current: pagination.current,
            size: pagination.size,
            ...searchForm,
        };
        const response = await getSeedBatchList(params);
        tableData.value = response.data.list;
        pagination.total = parseInt(response.data.total) || 0;
    } catch (error) {
        console.error("获取批次列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 获取种子列表
const loadSeedList = async () => {
    try {
        const response = await getSeedInfoList({ current: 1, size: 1000 });
        seedList.value = response.data.list;
    } catch (error) {
        console.error("获取种子列表失败:", error);
    }
};

// 获取生产商列表（溯源实体类型为生产商）
const loadProducerList = async () => {
    try {
        const response = await getTraceEntities({ 
            current: 1, 
            size: 1000,
            entityType: 1  // 1-生产商
        });
        producerList.value = response.data.list || [];
        console.log("生产商列表加载成功:", producerList.value.length);
    } catch (error) {
        console.error("获取生产商列表失败:", error);
        producerList.value = [];
    }
};

// 处理生产商选择变化
const handleProducerChange = (producerId) => {
    if (producerId) {
        const selectedProducer = producerList.value.find(p => p.id === producerId);
        if (selectedProducer) {
            // 自动填充生产商名称和地点
            formData.producerName = selectedProducer.entityName;
            if (selectedProducer.address) {
                formData.productionLocation = selectedProducer.address;
            }
            console.log("已选择生产商:", selectedProducer.entityName);
        }
    } else {
        formData.producerName = "";
    }
};

// 搜索
const handleSearch = () => {
    pagination.current = 1;
    loadBatchList();
};

// 重置搜索
const handleReset = () => {
    searchFormRef.value.resetFields();
    pagination.current = 1;
    loadBatchList();
};

// 新增批次
const handleAdd = () => {
    dialogTitle.value = "新增批次";
    dialogVisible.value = true;
    resetForm();
};

// 编辑批次
const handleEdit = (row) => {
    dialogTitle.value = "编辑批次";
    dialogVisible.value = true;
    Object.assign(formData, row);
    
    // 1. 处理认证信息：字符串转数组
    if (formData.certification && typeof formData.certification === 'string') {
        formData.certification = formData.certification.split(',').filter(item => item.trim());
    } else if (!formData.certification) {
        formData.certification = [];
    }
    
    // 2. 处理质检报告：字符串URL转MultiImageUpload数组格式
    if (formData.qualityReport && typeof formData.qualityReport === 'string') {
        formData.qualityReport = [{
            url: formData.qualityReport,
            name: '质检报告.jpg'
        }];
    } else if (!formData.qualityReport || !Array.isArray(formData.qualityReport)) {
        formData.qualityReport = [];
    }
};

// 查看详情
const handleView = async (row) => {
    try {
        const response = await getSeedBatchDetail(row.id);
        batchDetail.value = response.data;
        viewDialogVisible.value = true;
    } catch (error) {
        console.error("获取批次详情失败:", error);
    }
};

// 删除批次
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm("确定要删除该批次吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        await deleteSeedBatch(row.id);
        ElMessage.success("删除成功");
        loadBatchList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除批次失败:", error);
        }
    }
};

// 批量删除
const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm("确定要删除选中的批次吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
        });
        const ids = multipleSelection.value.map((item) => item.id);
        await batchDeleteSeedBatch(ids);
        ElMessage.success("删除成功");
        loadBatchList();
    } catch (error) {
        if (error !== "cancel") {
            console.error("批量删除批次失败:", error);
        }
    }
};

// 导出数据
const handleExport = () => {
    ElMessage.info("导出功能开发中...");
};

// 提交表单
const handleSubmit = async () => {
    try {
        await formRef.value.validate();
        submitLoading.value = true;

        // 处理数据转换
        const submitData = { ...formData };
        
        // 1. 认证信息：数组转字符串
        if (Array.isArray(submitData.certification)) {
            submitData.certification = submitData.certification.join(',');
        }
        
        // 2. 质检报告：MultiImageUpload返回数组，取第一个url
        if (Array.isArray(submitData.qualityReport)) {
            submitData.qualityReport = submitData.qualityReport.length > 0 
                ? submitData.qualityReport[0].url 
                : '';
        }

        if (formData.id) {
            await updateSeedBatch(submitData);
            ElMessage.success("更新成功");
        } else {
            await addSeedBatch(submitData);
            ElMessage.success("新增成功");
        }

        dialogVisible.value = false;
        loadBatchList();
    } catch (error) {
        console.error("提交失败:", error);
        ElMessage.error(error.message || "提交失败");
    } finally {
        submitLoading.value = false;
    }
};

// 重置表单
const resetForm = () => {
    Object.assign(formData, {
        id: null,
        batchNo: "",
        seedId: null,
        productionDate: "",
        expiryDate: "",
        qualityReport: [],  // MultiImageUpload需要数组
        qualityStatus: 1,
        remarks: "",
        traceCode: null,
        producerId: null,
        producerName: "",
        productionLocation: "",
        harvestDate: "",
        processingDate: "",
        storageCondition: "",
        certification: [],  // 多选需要数组
        traceabilityLevel: 1,
        unit: "",
        initialQualityGrade: "",
        moistureContent: null,
        germinationRate: null,
        purity: null,
        packagingType: "",
        packagingSpecification: "",
        initialOperatorName: "",
        initialOperatorPhone: "",
        productionEquipment: "",
        processingMethod: "",
        seedSource: "",
        parentVariety: "",
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
    loadBatchList();
};

// 当前页变化
const handleCurrentChange = (current) => {
    pagination.current = current;
    loadBatchList();
};

// 获取状态标签类型
const getStatusTagType = (status) => {
    const statusMap = {
        1: "success",
        2: "info",
        3: "danger",
    };
    return statusMap[status] || "info";
};

// 获取状态文本
const getStatusText = (status) => {
    const statusMap = {
        1: "在库",
        2: "出库",
        3: "过期",
    };
    return statusMap[status] || "未知";
};

// 溯源相关方法
const generateTraceCode = async (batch) => {
    try {
        // 先生成溯源码
        const response = await apiGenerateTraceCode('BJ'); // 默认使用北京地区，实际应根据业务需求
        if (response.code === 200) {
            const traceCode = response.data;
            
            // 调用后端API更新批次的溯源码
            const updateResponse = await updateBatchTraceCode(batch.id, traceCode);
            if (updateResponse.code === 200) {
                // 更新本地数据
                batch.traceCode = traceCode;
                ElMessage.success(`溯源码生成并保存成功: ${traceCode}`);
            } else {
                ElMessage.error(updateResponse.message || '溯源码保存失败');
            }
        } else {
            ElMessage.error(response.message || '溯源码生成失败');
        }
    } catch (error) {
        console.error('生成溯源码失败', error);
        ElMessage.error('生成溯源码失败');
    }
};

const viewTraceChain = (traceCode) => {
    currentTraceCode.value = traceCode;
    showTraceChainDialog.value = true;
};

const manageTrace = (batch) => {
    if (batch.traceCode) {
        // 使用路由导航跳转到溯源管理页面，传递批次信息
        router.push({
            path: '/trace/records',
            query: {
                batchId: batch.id,
                traceCode: batch.traceCode
            }
        });
    } else {
        ElMessage.warning('请先生成溯源码');
    }
};

const addTraceRecord = (batch) => {
    if (batch.traceCode) {
        // 使用路由导航跳转到新增溯源记录页面
        router.push({
            path: '/trace/records',
            query: {
                action: 'create',
                batchId: batch.id,
                traceCode: batch.traceCode
            }
        });
    } else {
        ElMessage.warning('请先生成溯源码');
    }
};

const verifyTraceIntegrity = async (traceCode) => {
    try {
        const response = await apiVerifyTraceIntegrity(traceCode);
        if (response.code === 200) {
            if (response.data) {
                ElMessage.success('数据验证通过，完整性良好');
            } else {
                ElMessage.warning('数据验证失败，可能存在篡改');
            }
        } else {
            ElMessage.error(response.message || '验证失败');
        }
    } catch (error) {
        console.error('验证失败', error);
        ElMessage.error('验证失败');
    }
};

// 初始化
onMounted(() => {
    loadBatchList();
    loadSeedList();
    loadProducerList();  // 加载生产商列表
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

// 批次详情对话框样式
.batch-detail-dialog {
    :deep(.el-dialog__body) {
        max-height: 70vh;
        overflow-y: auto;
        padding: 10px 20px;
    }
}

.batch-detail {
    .detail-card {
        margin-bottom: 20px;
        border: 1px solid #ebeef5;
        
        &:last-child {
            margin-bottom: 0;
        }
    }
    
    .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
    }
    
    :deep(.label-bold) {
        font-weight: 600;
        color: #606266;
        background-color: #fafafa;
    }
    
    .trace-code-value {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        
        code {
            font-family: 'Courier New', Consolas, monospace;
            font-size: 14px;
            font-weight: bold;
            color: #409eff;
            background: #ecf5ff;
            padding: 4px 12px;
            border-radius: 4px;
        }
    }
    
    .quality-value {
        display: inline-flex;
        align-items: center;
        gap: 5px;
        color: #409eff;
        font-weight: 500;
        font-size: 14px;
    }
    
    .remarks-content {
        padding: 15px;
        background: #f5f7fa;
        border-radius: 6px;
        line-height: 1.8;
        color: #606266;
        white-space: pre-wrap;
        word-break: break-word;
    }
}

.trace-code-cell {
    .el-button--text {
        font-family: 'Courier New', monospace;
        font-weight: bold;
        color: #409eff;
    }
}

.batch-no-link {
    font-weight: 600;
    color: #409eff;
    
    &:hover {
        color: #66b1ff;
    }
}

.trace-management-content {
    min-height: 600px;
}

.amount {
    color: #f56c6c;
    font-weight: 600;
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
