# 农作物种质资源数字化溯源系统 - Web 管理端

## 项目简介

基于 Vue3 + Element Plus + Vite 构建的现代化 Web 管理端，为农作物种质资源数字化溯源系统提供完整的管理界面。

## 技术栈

- **前端框架**: Vue 3.5.22
- **构建工具**: Vite 7.1.7
- **UI 组件库**: Element Plus 2.4.4
- **状态管理**: Pinia 2.1.7
- **路由管理**: Vue Router 4.2.5
- **HTTP 客户端**: Axios 1.6.2
- **图表库**: ECharts 5.4.3
- **样式预处理**: Sass
- **开发工具**:
  - unplugin-vue-components (自动导入组件)
  - unplugin-auto-import (自动导入 API)

## 功能特性

### 🎯 核心功能

- **用户认证**: 登录、登出、权限控制
- **仪表盘**: 数据统计、图表展示、实时监控
- **系统管理**: 用户管理、角色管理、菜单管理
- **种子管理**: 品类管理、种子档案、批次管理
- **库存管理**: 仓库管理、库存台账、入库出库
- **订单管理**: 订单列表、订单详情、状态跟踪
- **微信管理**: 微信用户、支付管理
- **推荐系统**: 用户画像、行为分析、推荐结果
- **数据管理**: Excel 导入导出

### 🎨 界面特性

- **响应式设计**: 支持 PC、平板、手机多端适配
- **主题切换**: 支持明暗主题切换
- **国际化**: 支持多语言切换
- **动画效果**: 流畅的页面切换和交互动画
- **组件化**: 高度组件化，易于维护和扩展

### 🔧 开发特性

- **TypeScript 支持**: 完整的类型定义
- **自动导入**: 组件和 API 自动导入
- **热更新**: 开发时热更新
- **代码分割**: 按需加载，优化性能
- **ESLint**: 代码质量检查
- **Prettier**: 代码格式化

## 项目结构

```
cropseed-trace-web/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── request.js     # axios配置
│   │   └── user.js        # 用户相关API
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   ├── layout/           # 布局组件
│   │   ├── index.vue     # 主布局
│   │   └── components/   # 布局子组件
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义
│   ├── stores/           # 状态管理
│   │   ├── user.js       # 用户状态
│   │   └── app.js        # 应用状态
│   ├── styles/           # 样式文件
│   │   └── index.scss    # 全局样式
│   ├── utils/            # 工具函数
│   │   ├── auth.js       # 认证工具
│   │   ├── index.js      # 通用工具
│   │   └── validate.js   # 验证工具
│   ├── views/            # 页面组件
│   │   ├── dashboard/    # 仪表盘
│   │   ├── login/        # 登录页
│   │   ├── system/       # 系统管理
│   │   ├── seed/         # 种子管理
│   │   ├── inventory/    # 库存管理
│   │   ├── order/        # 订单管理
│   │   ├── wechat/       # 微信管理
│   │   ├── recommendation/ # 推荐系统
│   │   ├── excel/        # 数据管理
│   │   └── error/        # 错误页面
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── package.json          # 依赖配置
├── vite.config.js        # Vite配置
└── README.md             # 项目说明
```

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

### 安装依赖

```bash
npm install
# 或
yarn install
```

### 开发环境

```bash
npm run dev
# 或
yarn dev
```

### 构建生产版本

```bash
npm run build
# 或
yarn build
```

### 预览生产版本

```bash
npm run preview
# 或
yarn preview
```

## 配置说明

### 环境变量

- `VITE_API_BASE_URL`: API 基础 URL
- `VITE_APP_TITLE`: 应用标题
- `VITE_APP_VERSION`: 应用版本

### 代理配置

开发环境下，API 请求会自动代理到后端服务：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8085',
      changeOrigin: true
    }
  }
}
```

## 开发指南

### 添加新页面

1. 在 `src/views/` 下创建页面组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/layout/index.vue` 中添加菜单项

### 添加新 API

1. 在 `src/api/` 下创建 API 文件
2. 使用 `src/api/request.js` 发送请求
3. 在组件中导入并使用

### 添加新组件

1. 在 `src/components/` 下创建组件
2. 组件会自动导入，无需手动注册
3. 使用 `<script setup>` 语法

### 状态管理

使用 Pinia 进行状态管理：

```javascript
// 创建store
export const useUserStore = defineStore("user", () => {
  const token = ref("");
  const login = () => {
    /* ... */
  };
  return { token, login };
});

// 使用store
const userStore = useUserStore();
```

## 部署说明

### Docker 部署

```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8085;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: LinYi
- 邮箱: linyi@example.com
- 项目地址: https://github.com/linyi/cropseed-trace-web
