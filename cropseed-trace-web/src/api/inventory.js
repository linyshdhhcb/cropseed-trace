import request from "./request";

// 仓库管理
export function getWarehouseList(params) {
  return request({
    url: "/inventory/warehouse/page",
    method: "get",
    params,
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
    data: { ids },
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

export function getInventoryDetail(id) {
  return request({
    url: `/inventory/stock/${id}`,
    method: "get",
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
