import request from "./request";

// 用户登录
export function login(data) {
  return request({
    url: "/auth/login",
    method: "post",
    data,
  });
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: "/auth/userinfo",
    method: "get",
  });
}

// 用户登出
export function logout() {
  return request({
    url: "/auth/logout",
    method: "post",
  });
}

// 修改密码
export function changePassword(data) {
  return request({
    url: "/sys/user/change-password",
    method: "put",
    data,
  });
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: "/sys/user/page",
    method: "get",
    params,
  });
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/sys/user/${id}`,
    method: "get",
  });
}

// 新增用户
export function addUser(data) {
  return request({
    url: "/sys/user",
    method: "post",
    data,
  });
}

// 修改用户
export function updateUser(data) {
  return request({
    url: "/sys/user",
    method: "put",
    data,
  });
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/sys/user/${id}`,
    method: "delete",
  });
}

// 批量删除用户
export function batchDeleteUser(ids) {
  return request({
    url: "/sys/user/batch",
    method: "delete",
    data: { ids },
  });
}
