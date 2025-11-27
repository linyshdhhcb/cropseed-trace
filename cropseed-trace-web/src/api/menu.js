import request from "./request";

// 获取菜单列表
export function getMenuList(params) {
  return request({
    url: "/sys/menu/list",
    method: "get",
    params,
  });
}

// 获取菜单树
export function getMenuTree() {
  return request({
    url: "/sys/menu/tree",
    method: "get",
  });
}

// 获取菜单详情
export function getMenuDetail(id) {
  return request({
    url: `/sys/menu/${id}`,
    method: "get",
  });
}

// 新增菜单
export function addMenu(data) {
  return request({
    url: "/sys/menu",
    method: "post",
    data,
  });
}

// 修改菜单
export function updateMenu(data) {
  return request({
    url: "/sys/menu",
    method: "put",
    data,
  });
}

// 删除菜单
export function deleteMenu(id) {
  return request({
    url: `/sys/menu/${id}`,
    method: "delete",
  });
}

// 批量删除菜单
export function batchDeleteMenu(ids) {
  return request({
    url: "/sys/menu/batch",
    method: "delete",
    data: { ids },
  });
}

// 获取用户路由菜单
export function getUserRouters() {
  return request({
    url: "/sys/menu/getUserRouters",
    method: "get",
  });
}
