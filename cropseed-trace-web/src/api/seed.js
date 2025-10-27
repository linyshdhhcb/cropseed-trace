import request from "./request";

// 品类管理
export function getCategoryList(params) {
  return request({
    url: "/seed/category/list",
    method: "get",
    params,
  });
}

export function getCategoryTree() {
  return request({
    url: "/seed/category/tree",
    method: "get",
  });
}

export function addCategory(data) {
  return request({
    url: "/seed/category",
    method: "post",
    data,
  });
}

export function updateCategory(data) {
  return request({
    url: "/seed/category",
    method: "put",
    data,
  });
}

export function deleteCategory(id) {
  return request({
    url: `/seed/category/${id}`,
    method: "delete",
  });
}

export function batchDeleteCategory(ids) {
  return request({
    url: "/seed/category/batch",
    method: "delete",
    data: { ids },
  });
}

// 种子档案管理
export function getSeedInfoList(params) {
  return request({
    url: "/seed/info/page",
    method: "get",
    params,
  });
}

export function getSeedInfoDetail(id) {
  return request({
    url: `/seed/info/${id}`,
    method: "get",
  });
}

export function addSeedInfo(data) {
  return request({
    url: "/seed/info",
    method: "post",
    data,
  });
}

export function updateSeedInfo(data) {
  return request({
    url: "/seed/info",
    method: "put",
    data,
  });
}

export function deleteSeedInfo(id) {
  return request({
    url: `/seed/info/${id}`,
    method: "delete",
  });
}

export function batchDeleteSeedInfo(ids) {
  return request({
    url: "/seed/info/batch",
    method: "delete",
    data: { ids },
  });
}

// 种子批次管理
export function getSeedBatchList(params) {
  return request({
    url: "/seed/batch/page",
    method: "get",
    params,
  });
}

export function getSeedBatchDetail(id) {
  return request({
    url: `/seed/batch/${id}`,
    method: "get",
  });
}

export function addSeedBatch(data) {
  return request({
    url: "/seed/batch",
    method: "post",
    data,
  });
}

export function updateSeedBatch(data) {
  return request({
    url: "/seed/batch",
    method: "put",
    data,
  });
}

export function deleteSeedBatch(id) {
  return request({
    url: `/seed/batch/${id}`,
    method: "delete",
  });
}

export function batchDeleteSeedBatch(ids) {
  return request({
    url: "/seed/batch/batch",
    method: "delete",
    data: { ids },
  });
}
