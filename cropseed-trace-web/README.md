# 农作物种质资源数字化溯源系统 - Web 管理端

[![Vue](https://img.shields.io/badge/Vue-3.5.22-brightgreen.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element_Plus-2.4.4-409EFF.svg)](https://element-plus.org/)
[![Vite](https://img.shields.io/badge/Vite-7.1.7-646CFF.svg)](https://vitejs.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📖 项目简介

基于 **Vue3 + Element Plus + Vite** 构建的现代化 Web 管理端，为农作物种质资源数字化溯源系统提供全流程管理界面。

系统集成**区块链溯源**、**智能推荐**、**电商管理**、**库存管理**等核心功能，采用前后端分离架构，支持多端协同，是一个功能完整的农业电商+溯源解决方案。

### 适用场景

- ✅ 毕业设计/课程设计项目
- ✅ 农产品电商平台管理后台
- ✅ 区块链溯源系统应用演示
- ✅ Vue3 + Element Plus 学习参考
- ✅ 前后端分离项目实战学习

##  技术栈

### 核心框架
| 技术 | 版本 | 说明 |
|-----|-----|-----|
| Vue.js | 3.5.22 | 渐进式JavaScript框架 |
| Vite | 7.1.7 | 下一代前端构建工具 |
| Vue Router | 4.2.5 | Vue.js官方路由管理器 |
| Pinia | 2.1.7 | Vue.js状态管理库 |
| Element Plus | 2.4.4 | Vue 3组件库 |

### 辅助工具
| 技术 | 版本 | 说明 |
|-----|-----|-----|
| Axios | 1.6.2 | HTTP请求库 |
| ECharts | 5.4.3 | 数据可视化图表库 |
| Sass | 最新 | CSS预处理器 |
| unplugin-vue-components | 最新 | 组件自动导入 |
| unplugin-auto-import | 最新 | API自动导入 |

### 开发工具
- **ESLint**: 代码质量检查
- **Prettier**: 代码格式化
- **Git Hooks**: 代码提交规范

## 功能特性

### 核心业务功能

#### 1. 用户认证与权限
- ✅ 用户登录/登出
- ✅ JWT Token认证
- ✅ 基于角色的权限控制（RBAC）
- ✅ 路由权限拦截
- ✅ 菜单权限控制

#### 2. 首页Dashboard
- ✅ 实时数据统计（用户、订单、库存、销售）
- ✅ 销售趋势图表（ECharts）
- ✅ 订单状态分布
- ✅ 库存预警展示
- ✅ 近期订单列表
- ✅ 数据刷新功能

#### 3. 系统管理
- ✅ 用户管理（CRUD、状态管理）
- ✅ 角色管理（权限分配）
- ✅ 菜单管理（动态菜单）
- ✅ 部门管理（组织架构）

#### 4. 种子管理
- ✅ 种子分类管理（二级分类）
- ✅ 种子档案管理（详细信息、图片上传）
- ✅ 批次管理（批次创建、溯源码生成）
- ✅ 溯源信息查看
- ✅ 批次状态跟踪

#### 5. 库存管理
- ✅ 仓库管理（多仓库支持）
- ✅ 库存台账（实时库存查询）
- ✅ 入库管理（批量入库、先进先出）
- ✅ 出库管理（库存扣减）
- ✅ 库存预警（低库存提醒）
- ✅ 库存盘点

#### 6. 订单管理
- ✅ 订单列表（多状态筛选）
- ✅ 订单详情（完整订单信息）
- ✅ 订单状态管理（待支付、已支付、已发货等）
- ✅ 订单发货
- ✅ 订单取消/退款
- ✅ 订单统计分析

#### 7. 支付管理
- ✅ 支付订单列表
- ✅ 支付状态查询
- ✅ 模拟支付功能（演示用）
- ✅ 支付宝沙箱集成
- ✅ 微信支付模拟

#### 8. 微信管理
- ✅ 微信用户管理
- ✅ 用户画像展示
- ✅ 用户行为分析
- ✅ 购买历史统计

#### 9. 溯源管理 
- ✅ 溯源总览（统计数据、最近记录）
- ✅ 溯源记录管理（CRUD、批量上链）
- ✅ 溯源码管理（生成、验证、解析）
- ✅ 溯源实体管理（生产基地、加工厂）
- ✅ 溯源查询统计（查询次数、热门排行）
- ✅ 区块链证明（腾讯云TBaas集成）
- ✅ 溯源链可视化（时间线展示）
- ✅ 批次溯源集成（一键生成溯源码）

#### 10. 推荐系统 
- ✅ 推荐结果展示（多算法支持）
- ✅ 用户行为管理（浏览、搜索、购买等）
- ✅ 用户画像展示（价格敏感度、质量要求等）
- ✅ 推荐效果分析
- ✅ 协同过滤算法
- ✅ 基于内容的推荐
- ✅ 热门推荐/新品推荐

#### 11. 数据管理
- ✅ Excel批量导入
- ✅ Excel数据导出
- ✅ 模板下载
- ✅ 数据校验

### 界面特性

-  **响应式设计**: 支持 PC、平板、手机多端适配
- 优雅布局**: 侧边栏 + 顶部导航栏的经典后台布局
- 数据可视化**: ECharts图表、统计卡片、数据面板
-  **交互友好**: 加载动画、消息提示、确认对话框
-  **现代UI**: 基于Element Plus的现代化组件设计
- **组件化**: 高度组件化，易于维护和扩展
-  **性能优化**: 懒加载、代码分割、虚拟滚动

###  开发特性

-  **自动导入**: 组件和 Composition API 自动导入
-  **快速热更新**: Vite提供极速的HMR体验
-  **智能打包**: 自动代码分割，按需加载
-  **代码检查**: ESLint + Prettier 保证代码质量
-  **开发工具**: Vue DevTools、Network调试支持
-  **规范统一**: 统一的代码风格和文件结构

## 📁 项目结构

```
cropseed-trace-web/
├── public/                      # 静态资源
│   └── favicon.ico              # 网站图标
├── src/
│   ├── api/                     # API接口层
│   │   ├── request.js           # axios配置、拦截器
│   │   ├── user.js              # 用户相关API
│   │   ├── seed.js              # 种子相关API
│   │   ├── inventory.js         # 库存相关API
│   │   ├── order.js             # 订单相关API
│   │   ├── payment.js           # 支付相关API
│   │   ├── trace.js             # 溯源相关API 
│   │   ├── recommendation.js    # 推荐系统API 
│   │   ├── statistics.js        # 统计分析API
│   │   └── ...                  # 其他业务API
│   ├── assets/                  # 静态资源文件
│   │   ├── images/              # 图片资源
│   │   ├── icons/               # 图标文件
│   │   └── logo.png             # Logo
│   ├── components/              # 全局公共组件
│   │   ├── SvgIcon/             # SVG图标组件
│   │   ├── ImageUpload/         # 图片上传组件
│   │   ├── RichTextEditor/      # 富文本编辑器
│   │   └── ...                  # 其他公共组件
│   ├── layout/                  # 布局组件
│   │   ├── index.vue            # 主布局框架
│   │   └── components/          # 布局子组件
│   │       ├── Sidebar.vue      # 侧边栏
│   │       ├── Navbar.vue       # 顶部导航
│   │       ├── AppMain.vue      # 主内容区
│   │       └── TagsView.vue     # 标签页
│   ├── router/                  # 路由配置
│   │   ├── index.js             # 路由主文件
│   │   └── modules/             # 路由模块
│   ├── stores/                  # Pinia状态管理
│   │   ├── user.js              # 用户状态
│   │   ├── app.js               # 应用状态
│   │   ├── permission.js        # 权限状态
│   │   └── tagsView.js          # 标签页状态
│   ├── styles/                  # 样式文件
│   │   ├── index.scss           # 全局样式入口
│   │   ├── variables.scss       # 样式变量
│   │   └── element-ui.scss      # Element Plus样式覆盖
│   ├── utils/                   # 工具函数库
│   │   ├── auth.js              # Token认证工具
│   │   ├── index.js             # 通用工具函数
│   │   ├── validate.js          # 表单验证工具
│   │   └── permission.js        # 权限判断工具
│   ├── views/                   # 页面组件
│   │   ├── dashboard/           # 首页Dashboard
│   │   │   └── index.vue        # 数据统计、图表展示
│   │   ├── login/               # 登录模块
│   │   │   └── index.vue        # 登录页面
│   │   ├── system/              # 系统管理
│   │   │   ├── user/            # 用户管理
│   │   │   ├── role/            # 角色管理
│   │   │   └── menu/            # 菜单管理
│   │   ├── seed/                # 种子管理
│   │   │   ├── category/        # 分类管理
│   │   │   ├── info/            # 种子档案
│   │   │   └── batch/           # 批次管理
│   │   ├── inventory/           # 库存管理
│   │   │   ├── warehouse/       # 仓库管理
│   │   │   ├── stock/           # 库存台账
│   │   │   ├── inbound/         # 入库管理
│   │   │   └── outbound/        # 出库管理
│   │   ├── order/               # 订单管理
│   │   │   └── index.vue        # 订单列表、详情
│   │   ├── payment/             # 支付管理
│   │   │   └── index.vue        # 支付订单管理
│   │   ├── wechat/              # 微信管理
│   │   │   └── user.vue         # 微信用户管理
│   │   ├── trace/               # 溯源管理 
│   │   │   ├── index.vue        # 溯源总览
│   │   │   ├── records.vue      # 溯源记录管理
│   │   │   ├── codes.vue        # 溯源码管理
│   │   │   ├── entities.vue     # 溯源实体管理
│   │   │   ├── query.vue        # 溯源查询统计
│   │   │   └── components/      # 溯源子组件
│   │   ├── recommendation/      # 推荐系统 
│   │   │   ├── display/         # 推荐展示
│   │   │   ├── result/          # 推荐结果
│   │   │   ├── behavior/        # 用户行为
│   │   │   └── profile/         # 用户画像
│   │   ├── excel/               # 数据管理
│   │   │   ├── import.vue       # 数据导入
│   │   │   └── export.vue       # 数据导出
│   │   └── error/               # 错误页面
│   │       ├── 404.vue          # 404页面
│   │       └── 403.vue          # 403页面
│   ├── App.vue                  # 根组件
│   └── main.js                  # 应用入口文件
├── .env.development             # 开发环境变量
├── .env.production              # 生产环境变量
├── .eslintrc.js                 # ESLint配置
├── .gitignore                   # Git忽略文件
├── index.html                   # HTML模板
├── package.json                 # 项目依赖配置
├── vite.config.js               # Vite构建配置
└── README.md                    # 项目说明文档
```

## 🚀 快速开始

### 环境要求

确保您的开发环境满足以下要求：

| 工具 | 版本要求 | 说明 |
|-----|---------|------|
| Node.js | >= 16.0.0 | JavaScript运行环境 |
| npm | >= 8.0.0 | Node包管理器 |
| yarn | >= 1.22.0 | 可选的包管理器 |

### 安装依赖

```bash
# 使用 npm
npm install

# 或使用 yarn
yarn install

# 或使用 pnpm (推荐)
pnpm install
```

### 启动开发服务器

```bash
# 启动开发服务器 (http://localhost:3000)
npm run dev

# 或使用 yarn
yarn dev
```

访问 http://localhost:3000查看应用

### 构建生产版本

```bash
# 构建生产环境代码
npm run build

# 或使用 yarn
yarn build
```

构建产物将生成在 `dist/` 目录中

### 预览生产版本

```bash
# 本地预览生产构建
npm run preview

# 或使用 yarn
yarn preview
```

## ⚙️ 配置说明

### 环境变量配置

项目使用`.env`文件管理环境变量，支持多环境配置：

### Vite代理配置

开发环境下，API请求会自动代理到后端服务，避免跨域问题：

```javascript
// vite.config.js
export default defineConfig({
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8085',  // 后端服务地址
        changeOrigin: true,               // 允许跨域
        rewrite: (path) => path           // 不重写路径
      }
    }
  }
})
```

### Element Plus配置

已配置Element Plus中文语言包和自动导入：

```javascript
// main.js
import ElementPlus from 'element-plus'
import locale from 'element-plus/es/locale/lang/zh-cn'

app.use(ElementPlus, { locale })
```

##  部署说明

完整的部署文档请参考项目根目录的 `script/linux/README.md`

## 常见问题

### 1. 无法访问后端API (跨域问题)

**问题**: 控制台出现CORS错误

**解决方案**:
- 开发环境：检查 `vite.config.js` 中的代理配置
- 生产环境：确保后端启用了CORS或通过Nginx代理

### 2. 构建后页面空白

**问题**: `npm run build` 后部署，页面空白

**解决方案**:
- 检查 `vite.config.js` 中的 `base` 配置
- 确保服务器配置了SPA路由支持 (`try_files $uri $uri/ /index.html`)
- 查看浏览器控制台的错误信息

### 3. Element Plus组件显示英文

**问题**: 分页等组件显示英文文字

**解决方案**:
```javascript
// main.js 确保导入中文语言包
import locale from 'element-plus/es/locale/lang/zh-cn'
app.use(ElementPlus, { locale })
```

### 4. 图片上传失败

**问题**: 上传图片时提示失败

**解决方案**:
- 检查MinIO服务是否正常运行
- 验证后端配置的MinIO连接信息
- 查看网络请求的具体错误信息

### 5. Token过期处理

**问题**: Token过期后未跳转登录页

**解决方案**:
- 检查 `src/api/request.js` 中的响应拦截器
- 确认Token存储和读取逻辑正确

### 6. 开发环境接口请求失败

**问题**: `npm run dev` 后API请求404

**解决方案**:
- 确保后端服务已启动 (http://localhost:8085)
- 检查Vite代理配置是否正确
- 查看终端的代理日志

##  技术亮点

### 1. 区块链溯源集成
- 集成腾讯云TBaas长安链
- 真实上链存证，非模拟演示
- 完整的溯源链可视化展示
- 支持区块链数据验证和证明生成

### 2. 智能推荐系统
- 协同过滤推荐算法
- 基于用户画像的个性化推荐
- 用户行为实时收集和分析
- 多种推荐策略支持

### 3. 现代化前端架构
- Vue 3 Composition API
- Vite 快速构建
- Pinia 状态管理
- 组件和API自动导入

### 4. 完整的业务流程
- 电商全流程：商品→购物车→订单→支付→发货
- 库存管理：入库→出库→盘点→预警
- 溯源流程：批次→溯源码→区块链→查询
- 推荐系统：行为→画像→推荐→展示

## 学习资源

### Vue 3 官方文档
- [Vue 3 中文文档](https://cn.vuejs.org/)
- [Vue Router 4](https://router.vuejs.org/zh/)
- [Pinia](https://pinia.vuejs.org/zh/)

### Element Plus
- [Element Plus 官方文档](https://element-plus.org/zh-CN/)
- [Element Plus 组件](https://element-plus.org/zh-CN/component/overview.html)

### Vite
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Vite 插件](https://cn.vitejs.dev/plugins/)

### ECharts
- [ECharts 官方文档](https://echarts.apache.org/zh/index.html)
- [ECharts 示例](https://echarts.apache.org/examples/zh/index.html)

## 贡献指南

欢迎贡献代码，提出问题和建议！

## 许可证

本项目采用 **MIT 许可证** - 查看 [LICENSE](../LICENSE) 文件了解详情。

## 👨‍💻 联系方式

- **项目维护者**: linyi
- **邮箱**: jingshuihuayue@qq.com
- **GitHub**: [cropseed-trace](https://github.com/linyshdhhcb/cropseed-trace)

##  Star History

如果这个项目对你有帮助，请给个 Star ⭐️
