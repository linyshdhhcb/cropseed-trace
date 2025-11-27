import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
import { usePermissionStore } from "@/stores/permission";
import NProgress from "nprogress";
import "nprogress/nprogress.css";

// 配置NProgress
NProgress.configure({ showSpinner: false });

// 白名单路由（不需要权限）
const whiteList = ["/login"];

// 静态路由（不需要权限的基础路由）
const constantRoutes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/index.vue"),
    meta: { title: "登录", requiresAuth: false },
  },
  {
    path: "/",
    name: "Layout",
    component: () => import("@/layout/index.vue"),
    redirect: "/dashboard",
    children: [
      // 首页默认路由，所有人都可以访问
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/index.vue"),
        meta: { title: "首页", icon: "Odometer", requiresAuth: true },
      }
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/error/404.vue"),
    meta: { title: "页面不存在", requiresAuth: false },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
});

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start();

  const userStore = useUserStore();
  const permissionStore = usePermissionStore();
  const token = userStore.token;

  if (token) {
    if (to.path === "/login") {
      // 已登录用户访问登录页，重定向到首页
      next({ path: "/dashboard", replace: true });
      NProgress.done();
      return;
    }
    
    // 判断是否已经生成过路由
    const hasLoadedRoutes = permissionStore.routes.length > 0;
    
    if (!hasLoadedRoutes) {
      try {
        // 获取用户信息
        await userStore.getUserInfoAction();
        
        // 生成动态路由
        const accessRoutes = await permissionStore.generateRoutes();
        
        // 动态添加路由到Layout下
        if (accessRoutes && accessRoutes.length > 0) {
          accessRoutes.forEach((route) => {
            // 添加到Layout路由作为子路由
            router.addRoute("Layout", route);
          });
          
          // 确保addRoute已完成，重新导航
          next({ ...to, replace: true });
        } else {
          // 没有任何路由权限，直接放行
          next();
        }
      } catch (error) {
        console.error("路由生成失败:", error);
        // 清除token并跳转登录
        await userStore.resetState();
        next(`/login?redirect=${to.path}`);
        NProgress.done();
      }
    } else {
      next();
    }
  } else {
    // 未登录
    if (whiteList.indexOf(to.path) !== -1) {
      next();
    } else {
      next(`/login?redirect=${to.path}`);
      NProgress.done();
    }
  }
});

router.afterEach(() => {
  NProgress.done();
});

export default router;
