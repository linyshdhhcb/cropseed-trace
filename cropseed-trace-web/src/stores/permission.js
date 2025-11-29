import { defineStore } from "pinia";
import { ref } from "vue";
import { getUserRouters } from "@/api/menu";

export const usePermissionStore = defineStore("permission", () => {
  const routes = ref([]);
  const addRoutes = ref([]);

  // 生成路由
  const generateRoutes = async () => {
    try {
      const response = await getUserRouters();
      const accessedRoutes = filterAsyncRoutes(response.data);
      routes.value = accessedRoutes;
      addRoutes.value = accessedRoutes;
      return accessedRoutes;
    } catch (error) {
      console.error("获取用户路由失败:", error);
      return [];
    }
  };

  // 过滤异步路由表
  const filterAsyncRoutes = (routes, parentPath = '') => {
    const res = [];
    routes.forEach((route) => {
      const tmp = {};
      
      // 映射数据库字段到路由字段
      tmp.path = route.path || '';
      
      // 生成唯一的路由名称：使用完整路径转换
      // 例如：/system/user -> SystemUser
      const fullPath = parentPath ? `${parentPath}/${route.path}` : route.path;
      tmp.name = generateRouteName(fullPath, route.menuName);
      
      // 处理组件路径
      if (route.component && route.component !== 'Layout') {
        // 清理组件路径：移除@/、views/前缀，移除.vue后缀
        let componentPath = route.component
          .replace('@/', '')           // 移除@/
          .replace('views/', '')        // 移除views/（如果有）
          .replace('.vue', '');         // 移除.vue后缀
        
        tmp.component = loadView(componentPath);
      } else if (route.menuType === 1 || route.component === 'Layout') {
        // 目录类型没有component，使用空组件或默认组件
        // tmp.component = () => import("@/layout/components/ParentView.vue");
        // 目录通常不需要component，因为它只是菜单分组
      }
      
      // 处理meta信息
      tmp.meta = {
        title: route.menuName,
        icon: route.icon,
        hidden: route.status !== 1 // status不为1时隐藏
      };
      
      // 递归处理子路由
      if (route.children && route.children.length > 0) {
        tmp.children = filterAsyncRoutes(route.children, fullPath);
      }
      
      res.push(tmp);
    });
    return res;
  };

  // 生成唯一的路由名称
  const generateRouteName = (path, menuName) => {
    // 移除开头的斜杠，按斜杠分割，转换为驼峰命名
    // 例如：/system/user -> SystemUser, /trace -> Trace
    if (!path) return `Route_${Math.random().toString(36).substr(2, 9)}`;
    
    const parts = path.replace(/^\//, '').split('/').filter(p => p);
    if (parts.length === 0) return `Route_${Math.random().toString(36).substr(2, 9)}`;
    
    // 转换为驼峰命名，每个部分首字母大写
    const routeName = parts
      .map(part => {
        // 处理特殊字符，只保留字母数字
        const cleaned = part.replace(/[^a-zA-Z0-9]/g, '');
        return cleaned.charAt(0).toUpperCase() + cleaned.slice(1);
      })
      .join('');
    
    return routeName || `Route_${Math.random().toString(36).substr(2, 9)}`;
  };

  // 预加载所有可能的组件
  const modules = import.meta.glob('../views/**/*.vue');

  // 动态加载组件
  const loadView = (view) => {
    // 构建完整路径
    const componentPath = `../views/${view}.vue`;
    
    // 从预加载的模块中查找
    const component = modules[componentPath];
    
    if (!component) {
      console.error(`组件未找到: ${componentPath}`);
      console.log('可用的组件路径:', Object.keys(modules));
      return () => import('../views/error/404.vue');
    }
    
    return component;
  };

  // 重置路由
  const resetRoutes = () => {
    routes.value = [];
    addRoutes.value = [];
  };

  return {
    routes,
    addRoutes,
    generateRoutes,
    resetRoutes,
  };
});
