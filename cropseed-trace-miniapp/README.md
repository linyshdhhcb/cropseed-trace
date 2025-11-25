# 农作物种质资源数字化溯源系统 - 微信小程序端

[![uni-app](https://img.shields.io/badge/uni--app-3.0+-brightgreen.svg)](https://uniapp.dcloud.io/)
[![Vue](https://img.shields.io/badge/Vue-3.0+-brightgreen.svg)](https://vuejs.org/)
[![uView UI](https://img.shields.io/badge/uView_UI-3.0+-blue.svg)](https://www.uviewui.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](../LICENSE)

## 📖 项目简介

基于 **uni-app + Vue3** 开发的微信小程序端，为农作物种质资源数字化溯源系统提供移动端购物和溯源查询功能。

系统集成**电商购物**、**溯源查询**、**在线支付**、**个人中心**等核心功能，支持微信小程序平台，提供流畅的移动端购物体验和便捷的溯源查询服务。

### 适用场景

- ✅ 毕业设计/课程设计项目
- ✅ 农产品电商小程序
- ✅ 移动端溯源查询应用
- ✅ uni-app学习参考项目
- ✅ 微信小程序开发实战

## 技术栈

### 核心框架
| 技术 | 版本 | 说明 |
|-----|-----|-----|
| uni-app | 3.0+ | 跨平台应用开发框架 |
| Vue.js | 3.0+ | 渐进式JavaScript框架 |
| uView UI | 3.0+ | uni-app生态优秀的UI框架 |
| Pinia | 2.0+ | Vue.js状态管理库 |

### 辅助工具
| 技术 | 说明 |
|-----|-----|
| uni-request | HTTP请求封装 |
| uni-simple-router | 路由管理 |
| dayjs | 时间处理工具 |

### 开发工具
- **HBuilderX**: 官方推荐IDE
- **微信开发者工具**: 小程序调试工具

## 功能特性

### 核心业务功能

#### 1. 用户认证
- ✅ 微信授权登录
- ✅ 手机号绑定
- ✅ 用户信息获取
- ✅ Token自动续期

#### 2. 首页模块
- ✅ 轮播图展示
- ✅ 种子分类导航
- ✅ 热门商品推荐
- ✅ 商品搜索功能
- ✅ 用户行为上报

#### 3. 商品浏览
- ✅ 商品列表（分类、搜索）
- ✅ 商品详情（图文介绍、规格参数）
- ✅ 库存实时显示
- ✅ 溯源码展示
- ✅ 加入购物车
- ✅ 立即购买

#### 4. 购物车管理
- ✅ 购物车列表
- ✅ 商品数量修改
- ✅ 商品删除（单个/批量）
- ✅ 全选/反选
- ✅ 库存检查
- ✅ 结算跳转

#### 5. 订单管理
- ✅ 订单创建（购物车/立即购买）
- ✅ 订单列表（全部/待支付/已支付/已发货/已完成）
- ✅ 订单详情
- ✅ 订单支付（微信支付/支付宝）
- ✅ 订单取消
- ✅ 确认收货
- ✅ 订单状态跟踪

#### 6. 支付功能
- ✅ 微信支付（模拟）
- ✅ 支付宝支付（沙箱/真实）
- ✅ 支付二维码生成
- ✅ 支付状态轮询
- ✅ 支付结果回调
- ✅ 模拟支付成功（演示用）

#### 7. 溯源查询
- ✅ 扫码查询溯源信息
- ✅ 手动输入溯源码查询
- ✅ 溯源链时间线展示
- ✅ 生产基地信息
- ✅ 区块链存证查看
- ✅ 查询记录统计

#### 8. 个人中心
- ✅ 用户信息展示
- ✅ 订单统计卡片
- ✅ 我的订单入口
- ✅ 收货地址管理
- ✅ 账号设置
- ✅ 退出登录

#### 9. 推荐系统
- ✅ 首页个性化推荐
- ✅ 商品详情页相关推荐
- ✅ 用户行为收集（浏览/搜索/购买）
- ✅ 基于用户画像的推荐

### 界面特性

- **优雅设计**: 基于uView UI的现代化组件设计
- **流畅交互**: 丰富的动画效果和交互反馈
- **响应式布局**: 适配不同尺寸的移动设备
- **加载优化**: 图片懒加载、分页加载
- **用户体验**: 下拉刷新、上拉加载、骨架屏

### 技术特性

- **组件化开发**: 可复用的业务组件
- **状态管理**: Pinia集中管理全局状态
- **请求封装**: 统一的API请求拦截和处理
- **错误处理**: 完善的异常捕获和用户提示
- **性能优化**: 虚拟列表、图片压缩、按需加载

## 📁 项目结构

```
cropseed-trace-miniapp/
├── pages/                          # 页面目录
│   ├── home/                       # 首页模块
│   │   └── index.vue               # 首页（轮播图、分类、推荐）
│   ├── product/                    # 商品模块
│   │   ├── list.vue                # 商品列表
│   │   └── detail.vue              # 商品详情
│   ├── cart/                       # 购物车模块
│   │   └── index.vue               # 购物车列表
│   ├── order/                      # 订单模块
│   │   ├── list.vue                # 订单列表
│   │   ├── detail.vue              # 订单详情
│   │   └── confirm.vue             # 订单确认页
│   ├── payment/                    # 支付模块
│   │   └── alipay.vue              # 支付宝支付页
│   ├── trace/                      # 溯源模块
│   │   └── index.vue               # 溯源查询页
│   ├── user/                       # 用户模块
│   │   ├── index.vue               # 个人中心
│   │   ├── profile.vue             # 个人信息
│   │   └── address.vue             # 地址管理
│   └── login/                      # 登录模块
│       └── index.vue               # 登录授权页
├── components/                     # 公共组件
│   ├── ProductCard/                # 商品卡片组件
│   ├── OrderItem/                  # 订单项组件
│   ├── TraceTimeline/              # 溯源时间线组件
│   └── TabBar/                     # 底部导航栏组件
├── stores/                         # Pinia状态管理
│   ├── user.js                     # 用户状态
│   ├── cart.js                     # 购物车状态
│   └── app.js                      # 应用状态
├── api/                            # API接口层
│   ├── request.js                  # 请求封装
│   ├── user.js                     # 用户相关API
│   ├── product.js                  # 商品相关API
│   ├── cart.js                     # 购物车API
│   ├── order.js                    # 订单相关API
│   ├── payment.js                  # 支付相关API
│   └── trace.js                    # 溯源相关API
├── utils/                          # 工具函数
│   ├── auth.js                     # 认证工具
│   ├── request.js                  # 请求工具
│   └── common.js                   # 通用工具
├── static/                         # 静态资源
│   ├── images/                     # 图片资源
│   ├── icons/                      # 图标资源
│   └── logo.png                    # Logo
├── uni_modules/                    # uni-app插件
│   └── uview-ui/                   # uView UI组件库
├── App.vue                         # 应用入口
├── main.js                         # 主入口文件
├── manifest.json                   # 应用配置
├── pages.json                      # 页面路由配置
├── uni.scss                        # 全局样式变量
└── package.json                    # 项目依赖
```

## 🚀 快速开始

### 环境要求

| 工具 | 版本要求 | 说明 |
|-----|---------|------|
| Node.js | >= 16.0.0 | JavaScript运行环境 |
| HBuilderX | 最新版 | 官方推荐IDE |
| 微信开发者工具 | 最新版 | 小程序调试工具 |

### 安装依赖

```bash
# 进入项目目录
cd cropseed-trace-miniapp

# 安装依赖
npm install
# 或
yarn install
```

### 开发调试

#### 方式1: 使用HBuilderX（推荐）

1. 使用HBuilderX打开项目目录
2. 点击菜单栏 **运行 > 运行到小程序模拟器 > 微信开发者工具**
3. 首次运行需要配置微信开发者工具路径
4. 等待编译完成，自动打开微信开发者工具

#### 方式2: 使用CLI

```bash
# 运行到微信小程序
npm run dev:mp-weixin

# 运行到H5
npm run dev:h5

# 运行到App
npm run dev:app
```

### 配置说明

#### 1. 修改后端API地址

```javascript
// api/request.js
const baseURL = 'http://localhost:8085/api'  // 开发环境
// const baseURL = 'https://api.yourdomain.com/api'  // 生产环境
```

#### 2. 配置微信小程序AppID

```json
// manifest.json
{
  "mp-weixin": {
    "appid": "你的微信小程序AppID",
    "setting": {
      "urlCheck": false  // 开发环境关闭URL检查
    }
  }
}
```

#### 3. 配置页面路由

```json
// pages.json
{
  "pages": [
    {
      "path": "pages/home/index",
      "style": {
        "navigationBarTitleText": "首页"
      }
    }
  ],
  "tabBar": {
    "list": [
      {
        "pagePath": "pages/home/index",
        "text": "首页"
      }
    ]
  }
}
```

### 构建发布

```bash
# 构建微信小程序
npm run build:mp-weixin

# 构建H5
npm run build:h5

# 构建App
npm run build:app
```

构建产物位于 `dist/build/mp-weixin/` 目录

## ⚙️ 配置说明

### API请求配置

```javascript
// api/request.js
const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + uni.getStorageSync('token')
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else {
          uni.showToast({
            title: res.data.message,
            icon: 'none'
          })
          reject(res.data)
        }
      },
      fail: reject
    })
  })
}
```

### 状态管理配置

```javascript
// stores/cart.js
import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartList: [],
    selectedIds: []
  }),
  
  getters: {
    totalCount: (state) => state.cartList.length,
    selectedList: (state) => {
      return state.cartList.filter(item => 
        state.selectedIds.includes(item.id)
      )
    }
  },
  
  actions: {
    async fetchCart() {
      const { data } = await getCartList()
      this.cartList = data
    }
  }
})
```

## 部署说明

### 微信小程序发布

1. **构建生产版本**
   ```bash
   npm run build:mp-weixin
   ```

2. **上传代码**
   - 使用HBuilderX：点击 **发行 > 小程序-微信**
   - 使用微信开发者工具：打开`dist/build/mp-weixin`目录，点击上传

3. **提交审核**
   - 登录微信公众平台
   - 进入版本管理，提交审核
   - 等待审核通过后发布

### H5部署

```bash
# 构建H5版本
npm run build:h5

# 将 dist/build/h5/ 目录部署到Web服务器
```

## 常见问题

### 1. 请求失败，提示不在合法域名列表

**问题**: 小程序开发工具报错"不在以下request合法域名列表中"

**解决方案**:
- 开发环境：在微信开发者工具中勾选 **设置 > 项目设置 > 不校验合法域名**
- 生产环境：在微信公众平台添加服务器域名白名单

### 2. 图片加载失败

**问题**: 图片无法显示

**解决方案**:
- 检查图片URL是否正确
- 确认图片域名在downloadFile合法域名中
- 使用HTTPS协议的图片URL

### 3. 授权登录失败

**问题**: wx.login或wx.getUserProfile调用失败

**解决方案**:
- 检查AppID配置是否正确
- 确认已在微信公众平台配置服务器域名
- 查看后端接口是否正常

### 4. 支付功能异常

**问题**: 支付调用失败或支付后未回调

**解决方案**:
- 检查微信支付商户号配置
- 确认支付回调URL配置正确
- 查看支付参数签名是否正确

### 5. 页面栈溢出

**问题**: 提示"navigateTo页面栈已达到10层"

**解决方案**:
- 使用`uni.redirectTo`替代`uni.navigateTo`
- 使用`uni.reLaunch`清空页面栈
- 合理规划页面跳转逻辑

## 技术亮点

### 1. 跨平台支持
- 一套代码，多端运行
- 支持微信小程序、H5、App
- 统一的开发体验

### 2. 完整的电商流程
- 商品浏览→加购→下单→支付→收货
- 库存实时检查
- 订单状态跟踪
- 支付集成

### 3. 溯源功能集成
- 扫码查询溯源信息
- 区块链存证展示
- 溯源链可视化
- 便捷的移动端体验

### 4. 智能推荐
- 用户行为收集
- 个性化商品推荐
- 提升用户体验

## 学习资源

### uni-app官方文档
- [uni-app官方文档](https://uniapp.dcloud.net.cn/)
- [uni-app组件](https://uniapp.dcloud.net.cn/component/)
- [uni-app API](https://uniapp.dcloud.net.cn/api/)

### uView UI
- [uView UI官方文档](https://www.uviewui.com/)
- [uView UI组件](https://www.uviewui.com/components/intro.html)

### 微信小程序
- [微信小程序官方文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [微信支付文档](https://pay.weixin.qq.com/wiki/doc/apiv3/index.shtml)

## 贡献指南

欢迎贡献代码，提出问题和建议！

## 许可证

本项目采用 **MIT 许可证** - 查看 [LICENSE](../LICENSE) 文件了解详情。

## 联系方式

- **项目维护者**: linyi
- **邮箱**: jingshuihuayue@qq.com
- **GitHub**: [cropseed-trace](https://github.com/linyshdhhcb/cropseed-trace)

## Star History

如果这个项目对你有帮助，请给个 Star ⭐️
