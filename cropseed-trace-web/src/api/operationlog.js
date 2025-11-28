import request from "./request";

/**
 * 操作日志管理API
 */

// 分页查询操作日志
export function getOperationLogList(params) {
  return request({
    url: "/sys/operation-log/page",
    method: "get",
    params,
  });
}

// 获取操作日志详情
export function getOperationLogDetail(id) {
  return request({
    url: `/sys/operation-log/${id}`,
    method: "get",
  });
}

// 删除操作日志
export function deleteOperationLog(id) {
  return request({
    url: `/sys/operation-log/${id}`,
    method: "delete",
  });
}

// 批量删除操作日志
export function batchDeleteOperationLog(ids) {
  return request({
    url: "/sys/operation-log/batch",
    method: "delete",
    data: { ids },
  });
}

// 清空操作日志
export function clearOperationLog() {
  return request({
    url: "/sys/operation-log/clear",
    method: "delete",
  });
}
