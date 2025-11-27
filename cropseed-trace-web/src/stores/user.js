import { defineStore } from "pinia";
import { ref } from "vue";
import { login, getUserInfo, logout } from "@/api/user";
import { getToken, setToken, removeToken } from "@/utils/auth";
import { usePermissionStore } from "./permission";

export const useUserStore = defineStore("user", () => {
  const token = ref(getToken());
  const userInfo = ref({
    avatar: '',
    realName: '',
    username: '用户',
    email: '',
    phone: ''
  });
  const roles = ref([]);
  const permissions = ref([]);

  // 登录
  const loginAction = async (loginForm) => {
    try {
      const response = await login(loginForm);
      const { token: accessToken } = response.data;
      token.value = accessToken;
      setToken(accessToken);
      
      // 清除之前的动态路由，确保重新加载
      const permissionStore = usePermissionStore();
      permissionStore.resetRoutes();
      
      return response;
    } catch (error) {
      throw error;
    }
  };

  // 获取用户信息
  const getUserInfoAction = async () => {
    try {
      const response = await getUserInfo();
      const {
        userInfo: info,
        roles: userRoles,
        permissions: userPermissions,
      } = response.data;
      userInfo.value = info;
      roles.value = userRoles;
      permissions.value = userPermissions;
      return response;
    } catch (error) {
      throw error;
    }
  };

  // 登出
  const logoutAction = async () => {
    try {
      await logout();
      token.value = "";
      userInfo.value = {
        avatar: '',
        realName: '',
        username: '用户',
        email: '',
        phone: ''
      };
      roles.value = [];
      permissions.value = [];
      removeToken();
      
      // 清空动态路由
      const permissionStore = usePermissionStore();
      permissionStore.resetRoutes();
    } catch (error) {
      throw error;
    }
  };

  // 重置状态
  const resetState = () => {
    token.value = "";
    userInfo.value = {
      avatar: '',
      realName: '',
      username: '用户',
      email: '',
      phone: ''
    };
    roles.value = [];
    permissions.value = [];
    removeToken();
    
    // 清空动态路由
    const permissionStore = usePermissionStore();
    permissionStore.resetRoutes();
  };

  return {
    token,
    userInfo,
    roles,
    permissions,
    loginAction,
    getUserInfoAction,
    logoutAction,
    resetState,
  };
});
