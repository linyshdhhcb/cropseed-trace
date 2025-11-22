# CropSeed Trace - Linux服务器Docker部署指南

> **重要提示**：
> 1. 本部署方案需要将构建好的 **jar包** 和 **dist目录** 复制到 `script/linux/` 目录下
> 2. 将整个 `script/linux/` 目录上传到服务器后直接运行脚本
> 3. 所有路径都是相对路径（`./`开头），无需修改项目源码路径
> 4. **MySQL、Redis、MinIO 需要您自行部署**，在 `config/application.yml` 中配置连接信息

## � 快速部署（三步走）

```bash
# 第一步：本地构建（开发环境）
mvn clean package -DskipTests                    # 构建后端
npm run build                                     # 构建前端
cp target/*.jar script/linux/jar/                # 复制jar
cp -r dist script/linux/                         # 复制dist

# 第二步：修改配置（script/linux/目录）
vim config/application.yml                        # 修改MySQL数据库、Redis、Minio等配置
```

访问地址：
- 前端：http://your-server-ip:8086
- 后端：http://your-server-ip:8085

## �️ 技术栈

### 后端
- **Spring Boot 3.x** - 应用框架
- **Java 21** - 运行环境
- **MyBatis-Plus** - ORM框架
- **Druid** - 数据库连接池
- **Redis** - 缓存
- **JWT** - 认证授权
- **MinIO** - 对象存储
- **腾讯云TBaaS** - 区块链服务
- **支付宝沙箱** - 支付服务

### 前端
- **Vue.js 3** - 前端框架
- **Element Plus** - UI组件库
- **Nginx** - Web服务器

### 部署
- **Docker** - 容器化
- **Docker Compose** - 服务编排

## �📋 目录结构

```
script/linux/
├── docker-compose.yml        # Docker Compose编排文件
├── Dockerfile.backend        # 后端Dockerfile
├── Dockerfile.web           # 前端Dockerfile
├── nginx.conf               # Nginx配置文件
├── config/
│   └── application.yml      # 后端配置（需要修改）
├── jar/                     # 后端jar包目录（需要手动复制）
│   └── *.jar                # Spring Boot应用jar包
├── dist/                    # 前端构建文件目录（需要手动复制）
│   ├── index.html
│   ├── assets/
│   └── ...
├── start.sh                 # 启动脚本
├── stop.sh                  # 停止脚本
├── restart.sh               # 重启脚本
└── README.md               # 本文档
```

## 🚀 快速开始

### 1. 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- **MySQL 8.0+**（需自行部署）
- **Redis 6.0+**（需自行部署）
- **MinIO**（需自行部署，用于文件存储）
- 已构建的jar包和前端dist目录

### 2. 环境准备

#### 2.1 修改配置文件

```bash
# 进入部署目录
# 修改应用配置（必须）
vim config/application.yml
```

**config/application.yml 必须修改的配置项：**

1. **MySQL数据库**
   
   ```yaml
   url: jdbc:mysql://your-server-ip:3306/cropseed_trace?...
   username: root
   password: your_mysql_password
   ```
   
2. **Redis缓存**
   
   ```yaml
   database: 7
   host: your-server-ip
   port: 6379
   password: your_redis_password
   ```
   
3. **MinIO对象存储**
   
   ```yaml
   endpoint: http://your-server-ip:9000
   accessKey: admin
   secretKey: your_minio_password
   ```
   
4. **腾讯云TBaaS区块链**
   
   ```yaml
   secret-id: your_tbaas_secret_id
   secret-key: your_tbaas_secret_key
   cluster-id: your_cluster_id
   chain-id: your_chain_id
   ```
   
5. **支付宝支付**
   
   ```yaml
   app-id: 9021000141610286
   private-key: your_alipay_private_key
   alipay-public-key: your_alipay_public_key
   return-url: http://your-domain.com/api/payment/alipay/return
   notify-url: http://your-domain.com/api/payment/alipay/notify
   ```
   
6. **JWT密钥**
   
   ```yaml
   secret: cropseed-trace-system-jwt-secret-key-2025-for-production
   ```

7. **微信小程序**
   
   ```yaml
   app-id: your_wechat_appid
   app-secret: your_wechat_secret
   ```

**端口说明：**

- 后端端口：`8085`
- 前端端口：`8086`
- 如需修改端口，请编辑 `docker-compose.yml` 文件的 `ports` 配置

#### 2.2 准备部署文件

**部署流程：**
1. 在开发环境构建项目
2. 将jar和dist复制到 `script/linux/` 目录
3. 将整个 `script/linux/` 目录上传到服务器
4. 在服务器上直接运行脚本

```bash
# 1. 构建后端项目
cd cropseed-trace-api
mvn clean package -DskipTests

# 2. 复制jar包到部署目录
cd ../script/linux
mkdir -p jar
cp ../../cropseed-trace-api/target/*.jar jar/

# 3. 构建前端项目
cd ../../cropseed-trace-web
npm install
npm run build

# 4. 复制dist目录到部署目录
cd ../script/linux
cp -r ../../cropseed-trace-web/dist .

# 5. 现在script/linux/目录结构：
script/linux/
├── jar/          # 后端jar包
├── dist/         # 前端构建文件
├── config/       # 配置文件（已修改application.yml）
├── *.sh          # 脚本文件
└── ...
```

### 3. 在服务器上启动

```bash
# 进入部署目录
# 给脚本添加执行权限
chmod +x *.sh

# 启动服务（智能检测容器状态）
./start.sh
```

启动脚本会自动：
1. ✅ 检查Docker环境
2. ✅ 检查必要文件（jar、dist、配置）
3. ✅ 检测并清理旧容器
4. ✅ 启动后端和前端服务
5. ✅ 显示访问地址

### 4. 访问服务

启动成功后，可以通过以下地址访问：

- **前端页面**: http://localhost:8086
- **后端API**: http://localhost:8085

默认管理员账号：
- 用户名: admin
- 密码: 123456

## 🛠️ 常用命令

> 以下命令均在部署目录下执行

### 服务管理

```bash
# 启动服务
./start.sh

# 停止服务
./stop.sh

# 重启服务
./restart.sh
```

### Docker Compose命令

```bash
# 查看服务状态
docker-compose ps

# 查看所有日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 重启特定服务
docker-compose restart backend

# 停止所有服务
docker-compose stop

# 删除所有容器
docker-compose down

# 删除所有容器和数据卷（危险操作！）
docker-compose down -v
```

### 容器内操作

```bash
# 进入后端容器
docker exec -it cropseed-backend bash

# 进入前端容器
docker exec -it cropseed-frontend sh

# 查看后端日志文件
docker exec cropseed-backend tail -f /app/logs/cropseed-trace.log

# 查看后端实时日志
docker exec cropseed-backend tail -f /app/logs/cropseed-trace.log -n 100
```

## 🔧 配置说明

### 核心配置文件

**config/application.yml** - 所有应用配置集中在此文件

主要配置项：
- **数据源配置**：MySQL连接信息（Druid连接池）
- **Redis配置**：Redis连接信息和连接池配置
- **MinIO配置**：对象存储服务配置
- **邮件配置**：SMTP邮件服务配置（可选）
- **微信配置**：微信小程序配置（可选）
- **区块链配置**：腾讯云TBaaS配置
- **支付配置**：支付宝沙箱配置
- **JWT配置**：Token认证配置
- **推荐系统配置**：推荐算法参数配置
- **日志配置**：日志级别和存储配置

### 修改配置后重启

```bash
# 修改配置文件后
vim config/application.yml

# 重启服务使配置生效
./restart.sh
# 或
docker-compose restart backend
```

## 🔍 故障排查

### 1. 容器启动失败

```bash
# 查看容器状态
docker-compose ps

# 查看失败原因
docker-compose logs backend
docker-compose logs frontend
```

### 2. 数据库连接失败

```bash
# 检查后端日志
docker-compose logs backend | grep -i "mysql\|database"

# 测试MySQL连接（从您自己的MySQL服务器）
# 确保config/application.yml中的数据库配置正确
mysql -h your-server-ip -uroot -p

# 检查网络连通性
ping your-server-ip
telnet your-server-ip 3306
```

### 3. Redis连接失败

```bash
# 检查后端日志
docker-compose logs backend | grep -i "redis"

# 测试Redis连接（从您自己的Redis服务器）
redis-cli -h your-server-ip -p 6379 -a your_password ping
```

### 4. 端口被占用

```bash
# 查看端口占用（Windows）
netstat -ano | findstr "8085"
netstat -ano | findstr "8086"

# 查看端口占用（Linux）
netstat -tulpn | grep 8085
netstat -tulpn | grep 8086

# 修改docker-compose.yml中的端口配置
vim docker-compose.yml
# 修改 ports 部分：
#   backend: - "8085:8085"
#   frontend: - "8086:80"
```

### 5. 后端日志查看

```bash
# 查看实时日志
docker-compose logs -f backend

# 查看最近100行日志
docker-compose logs --tail=100 backend

# 进入容器查看日志文件
docker exec cropseed-backend tail -f /app/logs/cropseed-trace.log
```

## 部署检查清单

在部署前，请确认以下事项：

### 必备服务
- [ ] MySQL 8.0+ 已部署并可访问
- [ ] Redis 6.0+ 已部署并可访问
- [ ] MinIO 已部署并可访问
- [ ] 已创建数据库 `cropseed_trace`
- [ ] 已执行数据库初始化SQL脚本

### 部署文件
- [ ] jar包已复制到 `jar/` 目录
- [ ] dist目录已复制到当前目录
- [ ] 脚本文件已添加执行权限 (`chmod +x *.sh`)

### 网络配置
- [ ] 确认8085端口未被占用（后端）
- [ ] 确认8086端口未被占用（前端）
- [ ] Docker服务已启动
- [ ] Docker Compose已安装

### 可选配置
- [ ] 微信小程序配置（如需使用）
- [ ] 邮件服务配置（如需使用）
- [ ] 腾讯云区块链配置（如需使用）

## 📌 注意事项

1. **数据库初始化**
   - 首次部署前，请确保已在MySQL中创建数据库并执行初始化脚本
   - 数据库名称为：`cropseed_trace`

2. **配置文件安全**
   - 生产环境请修改所有默认密码
   - JWT密钥必须使用复杂的随机字符串
   - 不要将包含真实密码的配置文件提交到Git仓库

3. **端口冲突**
   - 默认使用8085（后端）和8086（前端）端口
   - 如有冲突，请修改 `docker-compose.yml` 中的端口映射

4. **日志文件**
   - 日志文件位于 `./logs/` 目录
   - 建议定期清理或配置日志轮转

5. **MinIO存储桶**
   - 首次使用需要在MinIO中手动创建存储桶 `cropseed-trace`
   - 或在MinIO配置中开启自动创建存储桶功能

6. **支付宝沙箱**
   - 默认使用支付宝沙箱环境
   - 生产环境需要替换为正式环境配置

7. **网络访问**
   - 确保容器可以访问外部的MySQL、Redis、MinIO服务
   - 如使用云服务器，注意安全组配置

## 联系支持

- 作者：linyi
- 邮箱：jingshuihuayue@qq.com
- GitHub：https://github.com/linyshdhhcb/cropseed-trace

## License

本项目基于 MIT 协议开源。
