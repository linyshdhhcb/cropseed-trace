import request from './request'

/**
 * 溯源记录管理API
 */

// 创建溯源记录
export function createTraceRecord(data) {
  return request({
    url: '/trace/records',
    method: 'post',
    data
  })
}

// 获取溯源链
export function getTraceChain(traceCode) {
  return request({
    url: `/trace/records/chain/${traceCode}`,
    method: 'get'
  })
}

// 分页查询溯源记录
export function getTraceRecordsPage(params) {
  return request({
    url: '/trace/records/page',
    method: 'get',
    params
  })
}

// 根据ID查询记录
export function getTraceRecordById(id) {
  return request({
    url: `/trace/records/${id}`,
    method: 'get'
  })
}

// 验证溯源数据完整性
export function verifyTraceIntegrity(traceCode) {
  return request({
    url: `/trace/records/verify/${traceCode}`,
    method: 'post'
  })
}

// 获取区块链证明
export function getTraceProof(traceCode) {
  return request({
    url: `/trace/records/proof/${traceCode}`,
    method: 'get'
  })
}

// 单个记录上链
export function uploadRecordToBlockchain(id) {
  return request({
    url: `/trace/records/${id}/upload`,
    method: 'post'
  })
}

// 批量上链
export function batchUploadToBlockchain(limit = 100) {
  return request({
    url: '/trace/records/batch-upload',
    method: 'post',
    params: { limit }
  })
}

// 更新溯源记录
export function updateTraceRecord(id, data) {
  return request({
    url: `/trace/records/${id}`,
    method: 'put',
    data
  })
}

// 删除溯源记录
export function deleteTraceRecord(id) {
  return request({
    url: `/trace/records/${id}`,
    method: 'delete'
  })
}

/**
 * 溯源码管理API
 */

// 生成溯源码
export function generateTraceCode(regionCode) {
  return request({
    url: '/trace/codes/generate',
    method: 'post',
    params: { regionCode }
  })
}

// 验证溯源码
export function validateTraceCode(traceCode) {
  return request({
    url: '/trace/codes/validate',
    method: 'post',
    params: { traceCode }
  })
}

// 解析溯源码
export function parseTraceCode(traceCode) {
  return request({
    url: `/trace/codes/parse/${traceCode}`,
    method: 'get'
  })
}

// 批量生成溯源码
export function batchGenerateTraceCodes(regionCode, count) {
  return request({
    url: '/trace/codes/batch-generate',
    method: 'post',
    params: { regionCode, count }
  })
}

// 获取支持的地区列表
export function getSupportedRegions() {
  return request({
    url: '/trace/codes/regions',
    method: 'get'
  })
}

// 检查溯源码使用状态
export function checkTraceCodeUsage(traceCode) {
  return request({
    url: `/trace/codes/check/${traceCode}`,
    method: 'get'
  })
}

/**
 * 消费者溯源查询API
 */

// 溯源查询
export function queryTraceInfo(traceCode, params = {}) {
  return request({
    url: `/trace/query/${traceCode}`,
    method: 'get',
    params
  })
}

// 简化溯源查询
export function querySimpleTraceInfo(traceCode) {
  return request({
    url: `/trace/query/simple/${traceCode}`,
    method: 'get'
  })
}

// 批量查询
export function batchQueryTraceInfo(traceCodes) {
  return request({
    url: '/trace/query/batch',
    method: 'post',
    data: traceCodes
  })
}

// 查询统计信息
export function getQueryStatistics(params = {}) {
  return request({
    url: '/trace/query/statistics',
    method: 'get',
    params
  })
}

/**
 * 溯源实体管理API
 */

// 获取实体列表
export function getTraceEntities(params = {}) {
  return request({
    url: '/trace/entities/page',
    method: 'get',
    params
  })
}

// 创建实体
export function createTraceEntity(data) {
  return request({
    url: '/trace/entities',
    method: 'post',
    data
  })
}

// 更新实体
export function updateTraceEntity(id, data) {
  return request({
    url: `/trace/entities/${id}`,
    method: 'put',
    data
  })
}

// 删除实体
export function deleteTraceEntity(id) {
  return request({
    url: `/trace/entities/${id}`,
    method: 'delete'
  })
}

// 根据类型和编码查询实体
export function getEntityByTypeAndCode(entityType, entityCode) {
  return request({
    url: '/trace/entities/search',
    method: 'get',
    params: { entityType, entityCode }
  })
}

// 根据订单ID获取溯源记录
export function getTraceRecordsByOrderId(orderId) {
  return request({
    url: `/trace/records/order/${orderId}`,
    method: 'get'
  })
}

// 上传溯源记录到区块链
export function uploadTraceRecord(recordId) {
  return request({
    url: `/trace/records/${recordId}/upload`,
    method: 'post'
  })
}
