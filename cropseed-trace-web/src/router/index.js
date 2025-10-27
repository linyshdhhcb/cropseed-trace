import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
import NProgress from "nprogress";
import "nprogress/nprogress.css";

// 配置NProgress
NProgress.configure({ showSpinner: false });

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/index.vue"),
    meta: { title: "登录", requiresAuth: false },
  },
  {
    path: "/",
    component: () => import("@/layout/index.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/index.vue"),
        meta: { title: "仪表盘", icon: "Odometer", requiresAuth: true },
      },
      {
        path: "statistics",
        name: "Statistics",
        component: () => import("@/views/statistics/index.vue"),
        meta: { title: "数据统计", icon: "TrendCharts", requiresAuth: true },
      },
      {
        path: "system/user",
        name: "SystemUser",
        component: () => import("@/views/system/user/index.vue"),
        meta: { title: "用户管理", requiresAuth: true },
      },
      {
        path: "system/role",
        name: "SystemRole",
        component: () => import("@/views/system/role/index.vue"),
        meta: { title: "角色管理", requiresAuth: true },
      },
      {
        path: "system/menu",
        name: "SystemMenu",
        component: () => import("@/views/system/menu/index.vue"),
        meta: { title: "菜单管理", requiresAuth: true },
      },
      {
        path: "seed/category",
        name: "SeedCategory",
        component: () => import("@/views/seed/category/index.vue"),
        meta: { title: "品类管理", requiresAuth: true },
      },
      {
        path: "seed/info",
        name: "SeedInfo",
        component: () => import("@/views/seed/info/index.vue"),
        meta: { title: "种子档案", requiresAuth: true },
      },
      {
        path: "seed/batch",
        name: "SeedBatch",
        component: () => import("@/views/seed/batch/index.vue"),
        meta: { title: "批次管理", requiresAuth: true },
      },
      {
        path: "inventory/warehouse",
        name: "Warehouse",
        component: () => import("@/views/inventory/warehouse/index.vue"),
        meta: { title: "仓库管理", requiresAuth: true },
      },
      {
        path: "inventory/stock",
        name: "Inventory",
        component: () => import("@/views/inventory/stock/index.vue"),
        meta: { title: "库存台账", requiresAuth: true },
      },
      {
        path: "inventory/inbound",
        name: "Inbound",
        component: () => import("@/views/inventory/inbound/index.vue"),
        meta: { title: "入库管理", requiresAuth: true },
      },
      {
        path: "inventory/outbound",
        name: "Outbound",
        component: () => import("@/views/inventory/outbound/index.vue"),
        meta: { title: "出库管理", requiresAuth: true },
      },
      {
        path: "order/list",
        name: "OrderList",
        component: () => import("@/views/order/list/index.vue"),
        meta: { title: "订单列表", requiresAuth: true },
      },
      {
        path: "order/detail/:id",
        name: "OrderDetail",
        component: () => import("@/views/order/detail/index.vue"),
        meta: { title: "订单详情", requiresAuth: true },
      },
      {
        path: "wechat/user",
        name: "WechatUser",
        component: () => import("@/views/wechat/user/index.vue"),
        meta: { title: "微信用户", requiresAuth: true },
      },
      {
        path: "wechat/pay",
        name: "WechatPay",
        component: () => import("@/views/wechat/pay/index.vue"),
        meta: { title: "支付管理", requiresAuth: true },
      },
      {
        path: "recommendation/profile",
        name: "UserProfile",
        component: () => import("@/views/recommendation/profile/index.vue"),
        meta: { title: "用户画像", requiresAuth: true },
      },
      {
        path: "recommendation/behavior",
        name: "UserBehavior",
        component: () => import("@/views/recommendation/behavior/index.vue"),
        meta: { title: "行为分析", requiresAuth: true },
      },
      {
        path: "recommendation/result",
        name: "RecommendationResult",
        component: () => import("@/views/recommendation/result/index.vue"),
        meta: { title: "推荐结果", requiresAuth: true },
      },
      {
        path: "excel/import",
        name: "ExcelImport",
        component: () => import("@/views/excel/import/index.vue"),
        meta: { title: "数据导入", requiresAuth: true },
      },
      {
        path: "excel/export",
        name: "ExcelExport",
        component: () => import("@/views/excel/export/index.vue"),
        meta: { title: "数据导出", requiresAuth: true },
      },
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
  routes,
});

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start();

  const userStore = useUserStore();
  const token = userStore.token;

  if (to.meta.requiresAuth && !token) {
    next("/login");
    return;
  }

  if (to.path === "/login" && token) {
    next("/");
    return;
  }

  next();
});

router.afterEach(() => {
  NProgress.done();
});

export default router;
