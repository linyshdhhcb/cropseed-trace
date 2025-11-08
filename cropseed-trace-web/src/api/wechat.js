import request from "./request";

// 微信用户管理
export function getWxUserList(params) {
  return request({
    url: "/wechat/user/page",
    method: "get",
    params,
  });
}

export function getWxUserDetail(id) {
  return request({
    url: `/wechat/user/${id}`,
    method: "get",
  });
}

export function getWxUserByOpenid(openid) {
  return request({
    url: `/wechat/user/info/${openid}`,
    method: "get",
  });
}

export function updateWxUserInfo(data) {
  return request({
    url: "/wechat/user/info",
    method: "put",
    data,
  });
}

export function bindWxUserPhone(phone) {
  return request({
    url: "/wechat/user/bind-phone",
    method: "post",
    params: { phone },
  });
}

export function unbindWxUserPhone() {
  return request({
    url: "/wechat/user/unbind-phone",
    method: "post",
  });
}

export function updateWxUserStatus(id, status) {
  return request({
    url: `/wechat/user/status/${id}`,
    method: "put",
    params: { status },
  });
}

export function deleteWxUser(id) {
  return request({
    url: `/wechat/user/${id}`,
    method: "delete",
  });
}

// 微信支付管理
export function createWxPayOrder(data) {
  return request({
    url: "/wechat/pay/create",
    method: "post",
    data,
  });
}

export function queryWxPayResult(orderNo) {
  return request({
    url: `/wechat/pay/query/${orderNo}`,
    method: "get",
  });
}

export function refundWxPay(data) {
  return request({
    url: "/wechat/pay/refund",
    method: "post",
    params: data,
  });
}

export function getWxPayList(params) {
  return request({
    url: "/wechat/pay/page",
    method: "get",
    params,
  });
}

export function getWxPayDetail(id) {
  return request({
    url: `/wechat/pay/${id}`,
    method: "get",
  });
}
