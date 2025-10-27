import request from "./request";

// 获取角色列表
export function getRoleList(params) {
  return request({
    url: "/sys/role/page",
    method: "get",
    params,
  });
}

// 获取角色详情
export function getRoleDetail(id) {
  return request({
    url: `/sys/role/${id}`,
    method: "get",
  });
}

// 新增角色
export function addRole(data) {
  return request({
    url: "/sys/role",
    method: "post",
    data,
  });
}

// 修改角色
export function updateRole(data) {
  return request({
    url: "/sys/role",
    method: "put",
    data,
  });
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/sys/role/${id}`,
    method: "delete",
  });
}

// 批量删除角色
export function batchDeleteRole(ids) {
  return request({
    url: "/sys/role/batch",
    method: "delete",
    data: { ids },
  });
}

// 分配菜单
export function assignMenu(data) {
  return request({
    url: "/sys/role/assign-menu",
    method: "post",
    data,
  });
}

// 获取角色菜单
export function getRoleMenus(roleId) {
  return request({
    url: `/sys/role/${roleId}/menus`,
    method: "get",
  });
}
