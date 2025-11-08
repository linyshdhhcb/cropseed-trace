// 全局配置文件
export default {
  // API 基础地址
  baseURL:
    process.env.NODE_ENV === "development"
      ? "http://localhost:8085"
      : "http://your-production-domain/api",

  // 请求超时时间
  timeout: 30000,

  // Token 存储键名
  tokenKey: "cropseed_token",

  // 用户信息存储键名
  userInfoKey: "cropseed_userInfo",

  // 购物车存储键名
  cartKey: "cropseed_cart",

  // 微信小程序 AppID
  wxAppId: "wx54acde2424a69af2",

  // 支付宝沙箱配置
  alipay: {
    appId: "",
  },
};
