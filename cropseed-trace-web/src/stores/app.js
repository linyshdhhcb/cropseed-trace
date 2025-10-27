import { defineStore } from "pinia";
import { ref } from "vue";

export const useAppStore = defineStore("app", () => {
  // 侧边栏状态
  const sidebarCollapsed = ref(false);

  // 主题模式
  const theme = ref("light");

  // 页面加载状态
  const loading = ref(false);

  // 面包屑导航
  const breadcrumbs = ref([]);

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  };

  // 设置侧边栏状态
  const setSidebarCollapsed = (collapsed) => {
    sidebarCollapsed.value = collapsed;
  };

  // 切换主题
  const toggleTheme = () => {
    theme.value = theme.value === "light" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", theme.value);
  };

  // 设置加载状态
  const setLoading = (status) => {
    loading.value = status;
  };

  // 设置面包屑
  const setBreadcrumbs = (crumbs) => {
    breadcrumbs.value = crumbs;
  };

  return {
    sidebarCollapsed,
    theme,
    loading,
    breadcrumbs,
    toggleSidebar,
    setSidebarCollapsed,
    toggleTheme,
    setLoading,
    setBreadcrumbs,
  };
});
