import config from "./config.js";

/**
 * HTTP 请求封装
 */
class Http {
  constructor() {
    this.baseURL = config.baseURL;
    this.timeout = config.timeout;
  }

  /**
   * 获取完整的请求地址
   */
  getUrl(url) {
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return url;
    }
    return this.baseURL + url;
  }

  /**
   * 获取请求头
   */
  getHeaders() {
    const headers = {
      "Content-Type": "application/json",
    };

    // 添加 Token
    const token = uni.getStorageSync(config.tokenKey);
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    return headers;
  }

  /**
   * 统一请求方法
   */
  request(options) {
    return new Promise((resolve, reject) => {
      uni.request({
        url: this.getUrl(options.url),
        method: options.method || "GET",
        data: options.data || {},
        header: { ...this.getHeaders(), ...options.header },
        timeout: options.timeout || this.timeout,
        success: (res) => {
          // 统一处理响应
          if (res.statusCode === 200) {
            const data = res.data;

            // 业务成功
            if (data.code === 200) {
              resolve(data.data);
            } else {
              // 业务失败
              this.handleError(data);
              reject(data);
            }
          } else if (res.statusCode === 401) {
            // 未授权，跳转登录
            this.handleUnauthorized();
            reject({ code: 401, message: "未授权，请先登录" });
          } else {
            // 其他错误
            this.handleError({ code: res.statusCode, message: "网络请求失败" });
            reject({ code: res.statusCode, message: "网络请求失败" });
          }
        },
        fail: (err) => {
          console.error("请求失败:", err);
          this.handleError({ message: "网络连接失败，请检查网络设置" });
          reject(err);
        },
      });
    });
  }

  /**
   * GET 请求
   */
  get(url, params = {}) {
    return this.request({
      url,
      method: "GET",
      data: params,
    });
  }

  /**
   * POST 请求
   */
  post(url, data = {}) {
    return this.request({
      url,
      method: "POST",
      data,
    });
  }

  /**
   * PUT 请求
   */
  put(url, data = {}) {
    return this.request({
      url,
      method: "PUT",
      data,
    });
  }

  /**
   * DELETE 请求
   */
  delete(url, data = {}) {
    return this.request({
      url,
      method: "DELETE",
      data,
    });
  }

  /**
   * 文件上传
   */
  upload(url, filePath, name = "file", formData = {}) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: this.getUrl(url),
        filePath,
        name,
        formData,
        header: this.getHeaders(),
        success: (res) => {
          if (res.statusCode === 200) {
            let data;
            try {
              data =
                typeof res.data === "string" ? JSON.parse(res.data) : res.data;
            } catch (error) {
              console.error("解析上传响应失败", error);
              this.handleError({ message: "上传失败" });
              reject(error);
              return;
            }

            if (data.code === 200) {
              resolve(data.data);
            } else {
              this.handleError(data);
              reject(data);
            }
          } else {
            this.handleError({ code: res.statusCode, message: "上传失败" });
            reject({ code: res.statusCode, message: "上传失败" });
          }
        },
        fail: (err) => {
          console.error("上传失败:", err);
          this.handleError({ message: "上传失败，请重试" });
          reject(err);
        },
      });
    });
  }

  /**
   * 处理错误
   */
  handleError(error) {
    if (error && error.code === 2005) {
      this.handleUnauthorized("请先登录");
      return;
    }

    const message = error?.message || "操作失败";
    uni.showToast({
      title: message,
      icon: "none",
      duration: 2000,
    });
  }

  /**
   * 处理未授权
   */
  handleUnauthorized(message = "登录已过期，请重新登录") {
    // 清除本地缓存
    uni.removeStorageSync(config.tokenKey);
    uni.removeStorageSync(config.userInfoKey);

    // 提示用户
    uni.showToast({
      title: message,
      icon: "none",
      duration: 2000,
    });

    // 延迟跳转到登录页
    setTimeout(() => {
      uni.reLaunch({
        url: "/pages/auth/login",
      });
    }, 2000);
  }
}

// 导出实例
export default new Http();
