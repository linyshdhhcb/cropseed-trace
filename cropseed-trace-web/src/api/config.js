import request from "./request";

// 获取配置分页列表
export function getConfigList(params) {
  return request({
    url: "/sys/config/page",
    method: "get",
    params,
  });
}

// 获取所有配置列表
export function getAllConfigs() {
  return request({
    url: "/sys/config/list",
    method: "get",
  });
}

// 获取配置详情
export function getConfigDetail(id) {
  return request({
    url: `/sys/config/${id}`,
    method: "get",
  });
}

// 根据配置键获取配置值
export function getConfigValueByKey(configKey) {
  return request({
    url: `/sys/config/key/${configKey}`,
    method: "get",
  });
}

// 根据配置键获取配置信息
export function getConfigByKey(configKey) {
  return request({
    url: `/sys/config/info/${configKey}`,
    method: "get",
  });
}

// 根据配置类型获取配置列表
export function getConfigsByType(configType) {
  return request({
    url: `/sys/config/type/${configType}`,
    method: "get",
  });
}

// 新增配置
export function addConfig(data) {
  return request({
    url: "/sys/config",
    method: "post",
    data,
  });
}

// 修改配置
export function updateConfig(data) {
  return request({
    url: "/sys/config",
    method: "put",
    data,
  });
}

// 删除配置
export function deleteConfig(id) {
  return request({
    url: `/sys/config/${id}`,
    method: "delete",
  });
}

// 批量删除配置
export function batchDeleteConfig(ids) {
  return request({
    url: "/sys/config/batch",
    method: "delete",
    data: ids,
  });
}

// 刷新配置缓存
export function refreshConfigCache() {
  return request({
    url: "/sys/config/refresh-cache",
    method: "post",
  });
}
