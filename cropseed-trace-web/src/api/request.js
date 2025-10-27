import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/user";
import { getToken } from "@/utils/auth";
import NProgress from "nprogress";

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 10000,
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    NProgress.start();

    // 添加token
    const token = getToken();
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    NProgress.done();
    console.error("请求错误:", error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    NProgress.done();

    const res = response.data;

    // 如果返回的状态码为200，说明接口请求成功，可以正常拿到数据
    if (res.code === 200) {
      return res;
    } else {
      // 否则的话抛出错误
      ElMessage({
        message: res.message || "请求失败",
        type: "error",
        duration: 5000,
      });
      return Promise.reject(new Error(res.message || "请求失败"));
    }
  },
  (error) => {
    NProgress.done();
    console.error("响应错误:", error);

    let message = error.message;
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          message = "未授权，请重新登录";
          // 清除token并跳转到登录页
          const userStore = useUserStore();
          userStore.resetState();
          window.location.href = "/login";
          break;
        case 403:
          message = "拒绝访问";
          break;
        case 404:
          message = "请求的资源不存在";
          break;
        case 500:
          message = "服务器内部错误";
          break;
        default:
          message = data?.message || `连接错误${status}`;
      }
    }

    ElMessage({
      message,
      type: "error",
      duration: 5000,
    });

    return Promise.reject(error);
  }
);

export default service;
