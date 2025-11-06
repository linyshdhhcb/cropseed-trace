import request from "./request";

// 仓库管理
export function getWarehouseList(params) {
  return request({
    url: "/inventory/warehouse/page",
    method: "get",
    params,
  });
}

export function getActiveWarehouseList() {
  return request({
    url: "/inventory/warehouse/active",
    method: "get",
  });
}

export function getWarehouseDetail(id) {
  return request({
    url: `/inventory/warehouse/${id}`,
    method: "get",
  });
}

export function addWarehouse(data) {
  return request({
    url: "/inventory/warehouse",
    method: "post",
    data,
  });
}

export function updateWarehouse(data) {
  return request({
    url: "/inventory/warehouse",
    method: "put",
    data,
  });
}

export function deleteWarehouse(id) {
  return request({
    url: `/inventory/warehouse/${id}`,
    method: "delete",
  });
}

export function batchDeleteWarehouse(ids) {
  return request({
    url: "/inventory/warehouse/batch",
    method: "delete",
    data: ids,
  });
}

// 库存管理
export function getInventoryList(params) {
  return request({
    url: "/inventory/stock/page",
    method: "get",
    params,
  });
}

export function getInventoryDetail(seedId, batchId, warehouseId) {
  return request({
    url: "/inventory/stock/detail",
    method: "get",
    params: { seedId, batchId, warehouseId },
  });
}

export function addInventory(data) {
  return request({
    url: "/inventory/stock",
    method: "post",
    data,
  });
}

export function updateInventory(data) {
  return request({
    url: "/inventory/stock",
    method: "put",
    data,
  });
}

export function deleteInventory(id) {
  return request({
    url: `/inventory/stock/${id}`,
    method: "delete",
  });
}

export function batchDeleteInventory(ids) {
  return request({
    url: "/inventory/stock/batch",
    method: "delete",
    data: { ids },
  });
}

// 库存调整
export function adjustInventory(params) {
  // 期望的数据结构：{ id, adjustType: 'increase' | 'decrease', adjustQuantity, reason }
  return request({
    url: "/inventory/stock/adjust",
    method: "post",
    params,
  });
}

// 更新库存预警阈值
export function updateStockThreshold(id, minStock, maxStock) {
  return request({
    url: "/inventory/stock/threshold",
    method: "put",
    params: { id, minStock, maxStock },
  });
}

// 入库管理
export function getInboundList(params) {
  return request({
    url: "/inventory/inbound/page",
    method: "get",
    params,
  });
}

export function getInboundDetail(id) {
  return request({
    url: `/inventory/inbound/${id}`,
    method: "get",
  });
}

export function addInbound(data) {
  return request({
    url: "/inventory/inbound",
    method: "post",
    data,
  });
}

export function updateInbound(data) {
  return request({
    url: "/inventory/inbound",
    method: "put",
    data,
  });
}

export function deleteInbound(id) {
  return request({
    url: `/inventory/inbound/${id}`,
    method: "delete",
  });
}

export function batchDeleteInbound(ids) {
  return request({
    url: "/inventory/inbound/batch",
    method: "delete",
    data: { ids },
  });
}

// 入库审批/确认/取消
export function approveInbound(data) {
  // data: { id, remark }
  return request({
    url: "/inventory/inbound/approve",
    method: "post",
    params: data,
  });
}

export function confirmInbound(data) {
  // data: { id, remark }
  return request({
    url: "/inventory/inbound/confirm",
    method: "post",
    params: data,
  });
}

export function cancelInbound(data) {
  // data: { id, reason }
  return request({
    url: "/inventory/inbound/cancel",
    method: "post",
    params: data,
  });
}

// 出库管理
export function getOutboundList(params) {
  return request({
    url: "/inventory/outbound/page",
    method: "get",
    params,
  });
}

export function getOutboundDetail(id) {
  return request({
    url: `/inventory/outbound/${id}`,
    method: "get",
  });
}

export function addOutbound(data) {
  return request({
    url: "/inventory/outbound",
    method: "post",
    data,
  });
}

export function updateOutbound(data) {
  return request({
    url: "/inventory/outbound",
    method: "put",
    data,
  });
}

export function deleteOutbound(id) {
  return request({
    url: `/inventory/outbound/${id}`,
    method: "delete",
  });
}

export function batchDeleteOutbound(ids) {
  return request({
    url: "/inventory/outbound/batch",
    method: "delete",
    data: { ids },
  });
}

// 出库审批/确认/取消
export function approveOutbound(data) {
  // data: { id, approved, remark }
  return request({
    url: "/inventory/outbound/approve",
    method: "post",
    params: data,
  });
}

export function confirmOutbound(data) {
  // data: { id, remark }
  return request({
    url: "/inventory/outbound/confirm",
    method: "post",
    params: data,
  });
}

export function cancelOutbound(data) {
  // data: { id, reason }
  return request({
    url: "/inventory/outbound/cancel",
    method: "post",
    params: data,
  });
}
