import http from "@/common/http.js";

/**
 * 微信登录授权
 * @param {String} code - 微信登录凭证
 * @param {Object} userInfo - 用户信息（可选）
 */
export function wxLogin(code, userInfo = {}) {
  const payload = {
    code,
  };

  if (
    userInfo &&
    typeof userInfo === "object" &&
    Object.keys(userInfo).length > 0
  ) {
    payload.userInfo = JSON.stringify(userInfo);
  }

  return http.post("/wx/auth/login", payload);
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return http.get("/wx/user/profile");
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return http.put("/wx/user/profile", data);
}

/**
 * 登出
 */
export function logout() {
  return http.post("/wx/auth/logout");
}
