# 基于区块链的农作物种质资源数字化溯源系统

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 项目概述

> 我是一名计算机专业的大学生，你是否有因为遇到竞赛、课程设计、毕业设计等要求你开发的项目需要加上所谓的创新亮点，不能和普通项目一样，需要加上AI啊、区块链啊、云计算啊等等听起来就是高大上的、前沿的技术在你的项目中，实际上我们自己写的项目都是玩具一个，只能是用了完成任务，但是又是不得不需要的，因此我开发了这个基于区块链的项目，我这个区块链没有使用常见的Hyperledger Fabric、FISCO BCOS、Web3.js等等（因为部署之类的添麻烦了，不熟悉的起步非常麻烦😣😣😣），而是使用国产的各大云服务厂商提供的区块链，我选择了腾讯云的TBaas，因为用户有1年的免费试用（这个才是重点😎😎😎），而且文档也全面，我们学生使用感觉绰绰有余。

CropSeed Trace（基于区块链的农作物种质资源数字化溯源系统）是一个集区块链溯源、电商平台、智能推荐于一体的农业种业管理系统。系统采用前后端分离架构，包含Web管理端和微信小程序端，实现从种子生产到消费者购买的全链条数字化管理。

## 核心特性

- **区块链溯源**: 基于腾讯云TBaaS长安链的不可篡改数据存证
- **完整电商平台**: Web管理系统和微信小程序的B2C/B2B电商解决方案  
- **智能推荐系统**: 基于协同过滤算法的个性化商品推荐
- **库存管理**: 实时库存跟踪与自动补货预警
- **支付集成**: 支付宝沙箱环境的安全支付处理
- **多端支持**: 响应式Web界面和原生微信小程序

## 系统运行页面

### Web端

<div align="center">
<table>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/1.png" width="400"/>
<br/>
图1
</td>
<td align="center" width="50%">
<img src="doc/img/web/2.png" width="400"/>
<br/>
图2
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/3.png" width="400"/>
<br/>
图3
</td>
<td align="center" width="50%">
<img src="doc/img/web/4.png" width="400"/>
<br/>
图4
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/5.png" width="400"/>
<br/>
图5
</td>
<td align="center" width="50%">
<img src="doc/img/web/6.png" width="400"/>
<br/>
图6
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/7.png" width="400"/>
<br/>
图7
</td>
<td align="center" width="50%">
<img src="doc/img/web/8.png" width="400"/>
<br/>
图8
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/9.png" width="400"/>
<br/>
图9
</td>
<td align="center" width="50%">
<img src="doc/img/web/10.png" width="400"/>
<br/>
图10
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/11.png" width="400"/>
<br/>
图11
</td>
<td align="center" width="50%">
<img src="doc/img/web/12.png" width="400"/>
<br/>
图12
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/13.png" width="400"/>
<br/>
图13
</td>
<td align="center" width="50%">
<img src="doc/img/web/14.png" width="400"/>
<br/>
图14
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/web/15.png" width="400"/>
<br/>
图15
</td>
<td align="center" width="50%">
<img src="doc/img/web/16.png" width="400"/>
<br/>
图16
</td>
</tr>
</table>
</div>

### 小程序端

<div align="center">
<table>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/0.png" width="300"/>
<br/>
图17
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/1.png" width="300"/>
<br/>
图18
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/2.png" width="300"/>
<br/>
图19
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/3.png" width="300"/>
<br/>
图20
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/4.png" width="300"/>
<br/>
图21
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/5.png" width="300"/>
<br/>
图22
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/6.png" width="300"/>
<br/>
图23
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/7.png" width="300"/>
<br/>
图24
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/8.png" width="300"/>
<br/>
图25
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/9.png" width="300"/>
<br/>
图26
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/10.png" width="300"/>
<br/>
图27
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/11.png" width="300"/>
<br/>
图28
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/12.png" width="300"/>
<br/>
图29
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/13.png" width="300"/>
<br/>
图30
</td>
</tr>
<tr>
<td align="center" width="50%">
<img src="doc/img/uniapp/14.png" width="300"/>
<br/>
图31
</td>
<td align="center" width="50%">
<img src="doc/img/uniapp/15.png" width="300"/>
<br/>
图32
</td>
</tr>
</table>
</div>


## 技术架构

### 技术栈

| 分类 | 技术选型 |
|------|---------|
| 后端框架 | Spring Boot 3.x |
| 开发语言 | Java 21 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| ORM框架 | MyBatis-Plus |
| Web前端 | Vue.js 3 + Vite |
| UI组件库 | Element Plus |
| 移动端 | uni-app（微信小程序） |
| 区块链 | 腾讯云TBaaS（长安链） |
| 文件存储 | MinIO |
| 构建工具 | Maven |
| 支付集成 | 支付宝沙箱 |

### 架构特点

- **前后端分离**: RESTful API设计，前后端独立开发部署
- **微服务架构**: 模块化设计，易于扩展和维护
- **区块链集成**: 真实区块链网络集成，数据不可篡改
- **分布式缓存**: Redis缓存提升系统性能
- **高并发处理**: 分布式锁机制防止超卖问题

## 快速开始

### 🚀 Docker部署（推荐）

使用Docker可以一键部署整个系统，无需手动配置环境：

```bash
# 1. 构建项目
mvn clean package -DskipTests    # 后端
npm run build                     # 前端

# 2. 准备部署文件
cp target/*.jar script/linux/jar/
cp -r dist script/linux/

# 3. 修改配置
cd script/linux
vim config/application.yml       # 配置MySQL、Redis、MinIO等

# 4. 启动服务
./start.sh
```

**访问地址：**
- 前端：http://localhost:8086
- 后端：http://localhost:8085

📖 详细部署文档请查看：[Linux Docker部署指南](script/linux/README.md)

---

### 💻 本地开发环境

#### 环境要求

- Java 21+
- MySQL 8.0+  
- Redis 6.0+
- MinIO（对象存储）
- Node.js 16+
- Maven 3.8+

#### 安装步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/linyshdhhcb/cropseed-trace.git
   cd cropseed-trace
   ```

2. **数据库初始化**
   ```bash
   # 创建数据库
   mysql -u root -p
   CREATE DATABASE cropseed_trace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   # 导入数据表结构和初始数据
   mysql -u root -p cropseed_trace < sql/init.sql
   ```

3. **后端配置启动**
   ```bash
   # 进入后端项目
   cd cropseed-trace-api
   
   # 配置数据库、Redis、MinIO等连接信息
   vim src/main/resources/application-dev.yml
   
   # 编译启动
   mvn clean package -DskipTests
   java -jar target/cropseed-trace-api-*.jar
   ```
   
   后端默认端口：8085

4. **前端启动**
   ```bash
   # Web管理端
   cd cropseed-trace-web
   npm install
   npm run dev
   ```
   
   前端默认端口：5173（开发环境）

5. **微信小程序**
   ```bash
   cd cropseed-trace-miniapp
   npm install
   # 使用微信开发者工具导入项目目录
   ```

## 主要功能模块

### 1. 溯源管理系统
- 区块链数据存证确保记录不可篡改
- 智能溯源码生成和验证
- 完整供应链可视化展示
- 消费者端溯源信息查询

### 2. 电商平台
- 商品目录管理
- 购物车功能
- 订单处理流程
- 支付网关集成

### 3. 库存管理
- 实时库存追踪
- 自动补货提醒
- 仓库管理
- 批量处理能力

### 4. 推荐引擎
- 协同过滤推荐算法
- 用户行为分析
- 个性化商品推荐
- 销售业绩优化

## 项目结构

```
cropseed-trace/
├── cropseed-trace-api/          # 后端Spring Boot项目（DDD架构）
│   ├── src/main/java/com/linyi/cropseed/trace/
│   │   ├── CropSeedTraceApplication.java # 启动类
│   │   ├── module/            # 业务模块（DDD领域模型）
│   │   │   ├── seed/          # 种子管理模块
│   │   │   │   ├── controller/  # 种子相关API
│   │   │   │   ├── service/     # 种子业务逻辑
│   │   │   │   ├── mapper/      # 种子数据访问
│   │   │   │   └── model/       # 种子实体、DTO、VO
│   │   │   ├── order/         # 订单管理模块
│   │   │   ├── inventory/     # 库存管理模块
│   │   │   ├── trace/         # 溯源管理模块
│   │   │   ├── payment/       # 支付管理模块
│   │   │   ├── recommendation/ # 推荐系统模块
│   │   │   ├── statistics/    # 统计分析模块
│   │   │   ├── system/        # 系统管理模块（用户、角色、菜单）
│   │   │   └── wx/            # 微信管理模块
│   │   ├── common/            # 公共工具类和常量
│   │   ├── config/            # 配置类（Redis、MinIO、TBaas等）
│   │   ├── security/          # 安全认证模块（JWT）
│   │   └── infrastructure/    # 基础设施层
│   ├── src/main/resources/    # 配置文件
│   │   ├── application.yml    # 主配置文件
│   │   ├── application-dev.yml # 开发环境配置
│   │   ├── application-test.yml # 测试环境配置
│   │   ├── application-prod.yml # 生产环境配置
│   │   └── mapper/            # MyBatis XML映射文件
│   └── pom.xml                # Maven配置文件
├── cropseed-trace-web/         # Vue.js Web管理端
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   │   ├── dashboard/     # 数据统计看板
│   │   │   ├── seed/          # 种子管理
│   │   │   ├── order/         # 订单管理
│   │   │   ├── inventory/     # 库存管理
│   │   │   ├── trace/         # 溯源管理（记录、码管理、区块链、查询统计）
│   │   │   ├── recommendation/ # 推荐系统（行为分析、用户画像、结果展示）
│   │   │   ├── statistics/    # 数据统计
│   │   │   ├── system/        # 系统管理（用户、角色、菜单）
│   │   │   ├── payment/       # 支付管理
│   │   │   ├── wechat/        # 微信管理
│   │   │   ├── excel/         # Excel导入导出
│   │   │   └── error/         # 错误页面
│   │   ├── layout/            # 布局组件（侧边栏、面包屑）
│   │   ├── components/        # 公共组件（图片上传、文件上传等）
│   │   ├── api/               # API接口封装
│   │   ├── stores/            # Pinia状态管理
│   │   ├── router/            # 路由配置
│   │   ├── utils/             # 工具函数
│   │   └── styles/            # 全局样式
│   ├── package.json           # npm依赖配置
│   └── vite.config.js         # Vite构建配置
├── cropseed-trace-miniapp/     # 微信小程序（uni-app）
│   ├── pages/                 # 小程序页面
│   │   ├── index/             # 引导页
│   │   ├── home/              # 首页
│   │   ├── category/          # 分类页面
│   │   ├── product/           # 商品详情页
│   │   ├── cart/              # 购物车
│   │   ├── order/             # 订单管理（列表、详情、确认）
│   │   ├── payment/           # 支付页面（支付宝等）
│   │   ├── trace/             # 溯源查询
│   │   ├── address/           # 地址管理
│   │   ├── afterSale/         # 售后服务
│   │   ├── auth/              # 授权登录
│   │   └── user/              # 用户中心（个人信息、设置）
│   ├── api/                   # API接口封装
│   ├── stores/                # Pinia状态管理
│   ├── common/                # 公共工具和样式
│   ├── static/                # 静态资源（图片等）
│   ├── App.vue                # 应用入口
│   ├── main.js                # 主入口文件
│   ├── manifest.json          # 应用配置
│   ├── pages.json             # 页面路由配置
│   └── uni.scss               # uni-app全局样式
├── script/                    # 部署脚本
│   └── linux/                 # Linux Docker部署方案
│       ├── docker-compose.yml # Docker编排文件
│       ├── Dockerfile.backend # 后端镜像构建文件
│       ├── Dockerfile.web     # 前端镜像构建文件
│       ├── nginx.conf         # Nginx配置
│       ├── config/            # 配置文件目录
│       │   └── application.yml # 生产环境配置
│       ├── start.sh           # 启动脚本
│       ├── stop.sh            # 停止脚本
│       ├── restart.sh         # 重启脚本
│       └── README.md          # 部署文档
├── sql/                       # 数据库脚本
│   ├── cropseed_trace.sql     # 完整数据库（含数据）
│   └── cropseed_trace_nodata.sql # 仅表结构
├── doc/                       # 项目文档
│   ├── img/                   # 文档图片
│   │   ├── web/               # Web端截图
│   │   └── uniapp/            # 小程序端截图
│   ├── 溯源模块实现说明.md    # 溯源功能文档
│   ├── 推荐系统实现文档.md    # 推荐系统文档
│   ├── 对象存储模块.md        # MinIO存储文档
│   └── TBaas_Java开发指导文档.md # 区块链开发文档
├── logs/                      # 日志文件目录
├── .gitignore                 # Git忽略配置
├── LICENSE                    # 开源协议
└── README.md                  # 项目说明文档
```

## 部署说明

### 生产环境部署

推荐使用Docker方式部署，简单快捷：

1. **前置准备**
   - 准备好MySQL 8.0数据库（需自行部署）
   - 准备好Redis 6.0+服务（需自行部署）
   - 准备好MinIO对象存储（需自行部署）
   - 服务器已安装Docker和Docker Compose

2. **配置要求**
   - 修改 `script/linux/config/application.yml` 配置文件
   - 配置MySQL、Redis、MinIO连接信息
   - 配置腾讯云TBaaS区块链（可选）
   - 配置支付宝沙箱（可选）

3. **部署步骤**
   - 详见 [script/linux/README.md](script/linux/README.md)

4. **端口说明**
   - 后端服务：8085
   - 前端服务：8086
   - 数据库：需自行部署
   - Redis：需自行部署
   - MinIO：需自行部署

### 开发环境部署

适合本地开发和调试：

1. 使用IDE（IntelliJ IDEA推荐）导入项目
2. 配置 `application-dev.yml` 开发环境配置
3. 启动后端服务（Spring Boot）
4. 启动前端开发服务器（npm run dev）
5. 使用微信开发者工具打开小程序项目

## 贡献指南

欢迎为本项目贡献代码！请在提交PR前阅读[贡献指南](CONTRIBUTING.md)。

1. Fork本仓库
2. 创建功能分支
3. 提交您的修改
4. 添加相应测试
5. 提交Pull Request

## 开源协议

本项目基于MIT协议开源 - 详见[LICENSE](https://github.com/linyshdhhcb/cropseed-trace?tab=MIT-1-ov-file#)文件。

## 技术支持

如需帮助或有任何问题：

- 作者：linyi
- 联系邮箱：jingshuihuayue@qq.com
- 在GitHub创建Issue
- 查看[项目文档](docs/)



---

> **注**: 本项目为毕业设计/学习项目，集成了区块链、电商、推荐系统等多个技术栈，适合学习和研究使用。

**如果这个项目对你有帮助，请给我一个 ⭐ Star！**
