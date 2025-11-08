import { defineStore } from "pinia";
import config from "@/common/config.js";
import { getUserInfo, logout } from "@/api/auth.js";

export const useUserStore = defineStore("user", {
  state: () => ({
    token: "",
    userInfo: null,
    initialized: false,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) =>
      state.userInfo ? state.userInfo.nickname || state.userInfo.username : "",
  },
  actions: {
    loadFromStorage() {
      if (this.initialized) return;
      const token = uni.getStorageSync(config.tokenKey);
      const userInfo = uni.getStorageSync(config.userInfoKey);
      this.token = token || "";
      this.userInfo = userInfo || null;
      this.initialized = true;
    },
    setToken(token) {
      this.token = token;
      if (token) {
        uni.setStorageSync(config.tokenKey, token);
      } else {
        uni.removeStorageSync(config.tokenKey);
      }
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo;
      if (userInfo) {
        uni.setStorageSync(config.userInfoKey, userInfo);
      } else {
        uni.removeStorageSync(config.userInfoKey);
      }
    },
    async fetchUserInfo() {
      try {
        const data = await getUserInfo();
        this.setUserInfo(data);
        return data;
      } catch (error) {
        console.error("获取用户信息失败", error);
        return Promise.reject(error);
      }
    },
    async login({ token, userInfo }) {
      if (token) {
        this.setToken(token);
      }
      if (userInfo) {
        this.setUserInfo(userInfo);
      } else {
        await this.fetchUserInfo();
      }
    },
    async logout(force = false) {
      try {
        if (!force) {
          await logout();
        }
      } catch (error) {
        console.warn("调用登出接口失败", error);
      }
      this.setToken("");
      this.setUserInfo(null);
      this.initialized = false;
    },
  },
});
