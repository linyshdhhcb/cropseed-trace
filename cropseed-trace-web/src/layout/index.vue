<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar-container">
        <div class="logo-container">
          <img src="@/assets/logo.png" alt="logo" class="logo" />
          <span v-if="!appStore.sidebarCollapsed" class="logo-text">种质溯源系统</span>
        </div>
        <el-menu :default-active="activeMenu" :collapse="appStore.sidebarCollapsed" :unique-opened="true"
          :collapse-transition="false" mode="vertical" background-color="#304156" text-color="#bfcbd9"
          active-text-color="#409EFF" router>
          <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path" />
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="navbar">
          <div class="navbar-left">
            <el-button type="text" :icon="appStore.sidebarCollapsed ? 'Expand' : 'Fold'"
              @click="appStore.toggleSidebar" />
            <breadcrumb />
          </div>
          <div class="navbar-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo.avatar" />
                <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
                <el-icon>
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容 -->
        <el-main class="main-container">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import SidebarItem from './components/SidebarItem.vue'
import Breadcrumb from './components/Breadcrumb.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 侧边栏宽度
const sidebarWidth = computed(() => appStore.sidebarCollapsed ? '64px' : '200px')

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 路由配置
const routes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    meta: { title: '仪表盘', icon: 'Odometer' }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    meta: { title: '数据统计', icon: 'TrendCharts' }
  },
  {
    path: '/system',
    name: 'System',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      { path: 'user', name: 'SystemUser', meta: { title: '用户管理' } },
      { path: 'role', name: 'SystemRole', meta: { title: '角色管理' } },
      { path: 'menu', name: 'SystemMenu', meta: { title: '菜单管理' } }
    ]
  },
  {
    path: '/seed',
    name: 'Seed',
    meta: { title: '种子管理', icon: 'Grape' },
    children: [
      { path: 'category', name: 'SeedCategory', meta: { title: '品类管理' } },
      { path: 'info', name: 'SeedInfo', meta: { title: '种子档案' } },
      { path: 'batch', name: 'SeedBatch', meta: { title: '批次管理' } }
    ]
  },
  {
    path: '/inventory',
    name: 'Inventory',
    meta: { title: '库存管理', icon: 'Box' },
    children: [
      { path: 'warehouse', name: 'Warehouse', meta: { title: '仓库管理' } },
      { path: 'stock', name: 'Inventory', meta: { title: '库存台账' } },
      { path: 'inbound', name: 'Inbound', meta: { title: '入库管理' } },
      { path: 'outbound', name: 'Outbound', meta: { title: '出库管理' } }
    ]
  },
  {
    path: '/order',
    name: 'Order',
    meta: { title: '订单管理', icon: 'Document' },
    children: [
      { path: 'list', name: 'OrderList', meta: { title: '订单列表' } }
    ]
  },
  {
    path: '/wechat',
    name: 'Wechat',
    meta: { title: '微信管理', icon: 'ChatDotRound' },
    children: [
      { path: 'user', name: 'WechatUser', meta: { title: '微信用户' } },
      { path: 'pay', name: 'WechatPay', meta: { title: '支付管理' } }
    ]
  },
  {
    path: '/recommendation',
    name: 'Recommendation',
    meta: { title: '推荐系统', icon: 'TrendCharts' },
    children: [
      { path: 'profile', name: 'UserProfile', meta: { title: '用户画像' } },
      { path: 'behavior', name: 'UserBehavior', meta: { title: '行为分析' } },
      { path: 'result', name: 'RecommendationResult', meta: { title: '推荐结果' } }
    ]
  },
  {
    path: '/excel',
    name: 'Excel',
    meta: { title: '数据管理', icon: 'Document' },
    children: [
      { path: 'import', name: 'ExcelImport', meta: { title: '数据导入' } },
      { path: 'export', name: 'ExcelExport', meta: { title: '数据导出' } }
    ]
  }
]

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await userStore.logoutAction()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

onMounted(() => {
  // 获取用户信息
  if (userStore.token) {
    userStore.getUserInfoAction()
  }
})
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;

  .el-container {
    height: 100%;
    width: 100%;
  }
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.28s;

  .logo-container {
    display: flex;
    align-items: center;
    padding: 20px;
    color: #fff;

    .logo {
      width: 32px;
      height: 32px;
      margin-right: 12px;
    }

    .logo-text {
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
    }
  }

  .el-menu {
    border-right: none;
  }
}

.navbar {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .navbar-left {
    display: flex;
    align-items: center;

    .el-button {
      margin-right: 20px;
      font-size: 18px;
    }
  }

  .navbar-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;

      .username {
        margin: 0 8px;
        color: #606266;
      }
    }
  }
}

.main-container {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
  width: 100%;
  height: 100%;
}
</style>
