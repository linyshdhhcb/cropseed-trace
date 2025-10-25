# 农作物种质资源数字化溯源系统 - 项目文档

## 1. 项目概述

本项目旨在构建一个集种子档案管理、库存仓储、订单交易与微信小程序商城于一体的数字化溯源系统。系统采用前后端分离架构，包含一个功能完备的 **Web 管理端** 和一个面向终端用户的 **微信小程序端**，实现种质资源从入库、在库到销售的全生命周期管理。

## 2. 技术栈与规范要求

### 2.1 整体技术栈

| 端/层          | 技术选型                                                     |
| :------------- | :----------------------------------------------------------- |
| **后端**       | Spring Boot 3.5 + JDK 21 + MySQL 8.0 + MyBatis-Plus 3.5+ + Redis 7+ + MinIO |
| **Web 管理端** | Vue3 + JavaScript + Vite + Element Plus                      |
| **小程序端**   | Uniapp (Vue3 语法) + 微信小程序原生能力集成                  |
| **部署**       | Docker 容器化部署，提供 `docker-compose.yml` 文件            |

### 2.2 Excel 导入导出规范

- **格式**：统一使用 `.xlsx` 格式。
- **模板**：提供“下载模板”功能，模板中包含清晰的字段说明与填写示例。
- **数据校验**：
    - 包括但不限于：非空、格式、唯一性、外键关联校验。
    - 核心字段校验规则示例：
        - `种子名称`：长度 1-50 字符，同一品类下不可重复。
        - `批次号`：全局唯一。
        - `库存数量`：≥ 0 的整数。
        - `单价`：> 0 的数值，保留两位小数。
        - `品类编码`：字母和数字组成，层级用英文句点分隔（如 `CORN.HYBRID.2025`）。
- **容错处理**：
    - 导入过程中跳过错误行，继续处理后续数据。
    - 生成包含错误行号及具体原因的 Excel 格式错误报告，供用户下载。
- **性能**：单次导入支持 10,000 行数据，系统需进行优化，避免内存溢出。

### 2.3 数据一致性要求

- **库存操作**：高并发场景下，使用 Redis Lua 脚本或数据库行级锁保证库存扣减的原子性。
- **状态变更**：订单等关键状态变更需记录完整操作日志，支持历史追溯。
- **批量操作**：Excel 批量导入需具备事务性，部分失败时必须明确标识失败记录，确保数据不出现“半成功”状态。

### 2.4 微信小程序对接要求

- **用户身份**：基于微信 `openid` 自动注册与登录。
- **用户信息**：首次登录不强制绑定手机号，下单时再填写收货地址和联系方式。
- **支付**：必须集成支付宝沙箱环境进行支付功能测试。
- **API 交互**：所有与后端的 RESTful API 交互均需进行身份鉴权。

### 2.5 非功能性要求

- **性能**：Web 管理端在千级数据量下页面加载时间 ≤ 1.5 秒。
- **分页**：种子列表等大量数据查询必须支持分页，每页 ≤ 50 条。
- **审计**：系统需记录管理员对关键数据（删除、价格修改、权限变更）的操作日志。

---

## 3. Web 管理端功能模块

| 模块分类           | 子模块     | 功能名称                 | 功能描述                                                     | 涉及技术/说明                          | 优先级 |
| :----------------- | :--------- | :----------------------- | :----------------------------------------------------------- | :------------------------------------- | :----- |
| **用户与权限**     | 管理员管理 | 管理员账号管理           | 增删改查管理员账号，分配角色与权限                           | Spring Security + RBAC 模型            | 高     |
|                    | 角色权限   | 角色与权限配置           | 自定义角色，精细化配置菜单与操作权限                         | MyBatis-Plus 动态权限控制              | 高     |
| **种子管理**       | 种子品类   | 品类管理                 | 管理种子大类（如玉米、水稻）、子类、品种，支持多级树形结构   | 递归查询 / 路径存储                    | 高     |
|                    | 种子档案   | 种子信息录入             | 录入种子名称、品种、产地、特性、图片、库存、单价、状态等     | MinIO 存储图片，MySQL 存结构化数据     | 高     |
|                    | 批次管理   | 种子批次生成             | 为每批种子生成唯一批次号，关联生产日期、质检报告、仓库等信息 | 批次号规则可配置（如 `SP20251025001`） | 高     |
| **库存与仓储**     | 仓库管理   | 仓库信息维护             | 维护多个仓库（如A仓、B仓），记录位置、负责人、容量等         | -                                      | 中     |
|                    | 库存台账   | 实时库存查看             | 按种子、批次、仓库等多个维度查看当前库存数量                 | Redis 缓存热点库存，定时同步至 DB      | 高     |
|                    | 入库操作   | 手动/扫码入库            | 通过批次号录入入库数量，更新库存                             | 支持扫码枪或手动输入                   | 高     |
|                    | 出库操作   | 手动/扫码出库            | 按订单或调拨单出库，扣减库存                                 | 事务保障数据一致性                     | 高     |
| **Excel 批量操作** | 种子导入   | Excel 批量导入种子信息   | 下载模板，填写后上传，系统校验并批量入库                     | Apache POI + 异步任务 + 错误行反馈     | 高     |
|                    | 库存导入   | Excel 批量更新库存       | 按批次更新各仓库库存数量（覆盖或增量）                       | 支持部分失败回滚或跳过                 | 中     |
|                    | 订单导出   | 订单数据导出Excel        | 按时间、状态、用户等条件导出订单明细                         | EasyExcel，支持万级数据分页导出        | 中     |
| **订单管理**       | B端订单    | 经销商/批发订单处理      | 查看、审核、发货、标记完成、处理退款                         | 订单状态机（待审核→已发货→已完成）     | 高     |
|                    | C端订单    | 小程序商城订单同步与处理 | 同步小程序用户订单，支持后台发货、物流填写                   | 与小程序端 API 实时对接                | 高     |
| **数据统计**       | 销售报表   | 销量与销售额统计         | 按日/周/月、品类、地区等维度统计销售数据                     | MySQL 聚合查询 + Redis 缓存报表结果    | 中     |
|                    | 库存预警   | 库存不足提醒             | 库存低于设定阈值时标红或通过邮件/站内信通知管理员            | XXL-JOB 定时扫描                       | 低     |
| **系统设置**       | 参数配置   | 系统参数管理             | 配置商城开关、默认运费、批次前缀、文件存储策略等             | 配置表 + 缓存刷新机制                  | 中     |
|                    | 操作日志   | 关键操作审计日志         | 记录管理员删除、价格修改、权限变更等敏感操作                 | AOP 切面 + 日志表，包含 IP 与时间戳    | 中     |

---

## 4. 微信小程序端功能模块

| 模块分类       | 子模块     | 功能名称           | 功能描述                                           | 涉及技术/说明              | 优先级 |
| :------------- | :--------- | :----------------- | :------------------------------------------------- | :------------------------- | :----- |
| **用户中心**   | 微信登录   | 微信授权一键登录   | 获取用户 `openid`，首次登录自动注册                | 微信登录 API（静默授权）   | 高     |
|                | 收货地址   | 地址管理           | 增删改查收货地址，可设置默认地址                   | 地址组件 + 表单校验        | 高     |
| **商品展示**   | 商品列表   | 种子商品浏览       | 支持按品类筛选、关键词搜索、价格排序、分页加载     | Vue3 + Uniapp 列表组件     | 高     |
|                | 商品详情   | 种子详情页展示     | 展示图文介绍、规格参数、库存状态、用户评价等       | MinIO 图片加载，富文本渲染 | 高     |
| **购物与订单** | 购物车     | 加入与管理购物车   | 支持增减数量、删除、选中结算                       | 本地缓存 + 后端同步        | 高     |
|                | 下单支付   | 提交订单并微信支付 | 生成订单，调用支付宝沙箱接口完成支付               | 微信支付 + 支付结果回调    | 高     |
|                | 订单列表   | 查看个人订单       | 分页展示待付款、待发货、待收货、已完成等状态的订单 | 订单状态实时同步自后端     | 高     |
| **售后服务**   | 退换货申请 | 提交售后请求       | 选择订单、填写原因，上传凭证图片                   | MinIO 上传凭证，状态流转   | 中     |
| **营销功能**   | 商品分享   | 分享种子商品       | 生成小程序码或分享链接，支持社交传播               | 微信分享 API + 海报生成    | 低     |

---

## 5.数据库设计

```yaml
# 基本配置供使用 数据库已经建好了
minio:
  endpoint: http://117.72.101.170:9000
  accessKey: linyi
  secretKey: lpy.3826
  bucketName: cropseed-trace
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/seed_trace_system
    username: root
    password: abc123
    driver-class-name: com.mysql.cj.jdbc.Driver
 data:
    redis:
      database: 1
      host: 127.0.0.1
      port: 6379
      password: lpy.3826
  mail:
    host: smtphz.qiye.163.com
    port: 465
    username: ies_training@zhku.edu.cn
    password: PfnMSsdKE8txg3bA
    protocol: smtp
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
            trust: smtphz.qiye.163.com
    default-encoding: utf-8
 jackson:
    serialization:
      write-enums-using-to-string: true
      write-dates-as-timestamps: false
    deserialization:
      read-enums-using-to-string: true
      fail-on-unknown-properties: false
    default-property-inclusion: always
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 100MB  # 单个文件最大大小
      max-request-size: 200MB  # 单次请求最大大小
      enabled: true
# open api配置
springdoc:
  swagger-ui:
    enabled: true # 开关
    doc-expansion: none  #关闭展开
    tags-sorter: alpha
    server-base-url:
  api-docs:
    enabled: true # 开关
knife4j:
  enable: true
  basic:
    enable: false
    username: linyi # Basic认证用户名
    password: lpy.3826 # Basic认证密码
```



```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `seed_trace_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `seed_trace_system`;

-- 1. 系统用户表（管理员）
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 2. 角色表
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 3. 用户角色关联表
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 4. 菜单权限表
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- 5. 角色菜单关联表
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 6. 操作日志表
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID',
  `username` varchar(50) NOT NULL COMMENT '操作人用户名',
  `operation` varchar(100) NOT NULL COMMENT '操作描述',
  `method` varchar(200) NOT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时间(毫秒)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '操作状态：0-失败，1-成功',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 7. 系统参数配置表
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_type` tinyint DEFAULT '1' COMMENT '配置类型：1-系统配置，2-业务配置',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统参数配置表';

-- 8. 种子品类表
CREATE TABLE `seed_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父品类ID',
  `category_code` varchar(50) NOT NULL COMMENT '品类编码',
  `category_name` varchar(100) NOT NULL COMMENT '品类名称',
  `level` int NOT NULL COMMENT '层级',
  `path` varchar(500) DEFAULT NULL COMMENT '层级路径',
  `description` text COMMENT '品类描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='种子品类表';

-- 9. 种子信息表
CREATE TABLE `seed_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seed_name` varchar(50) NOT NULL COMMENT '种子名称',
  `seed_code` varchar(50) NOT NULL COMMENT '种子编码',
  `category_id` bigint NOT NULL COMMENT '品类ID',
  `variety` varchar(100) DEFAULT NULL COMMENT '品种',
  `origin_place` varchar(200) DEFAULT NULL COMMENT '产地',
  `characteristics` text COMMENT '特性描述',
  `specifications` text COMMENT '规格参数',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-下架，1-上架',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seed_code` (`seed_code`),
  UNIQUE KEY `uk_seed_name_category` (`seed_name`,`category_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='种子信息表';

-- 10. 种子批次表
CREATE TABLE `seed_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(50) NOT NULL COMMENT '批次号',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `production_date` date NOT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期至',
  `quality_report` varchar(500) DEFAULT NULL COMMENT '质检报告URL',
  `quality_status` tinyint DEFAULT '1' COMMENT '质检状态：0-不合格，1-合格',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_seed_id` (`seed_id`),
  KEY `idx_production_date` (`production_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='种子批次表';

-- 11. 仓库信息表
CREATE TABLE `warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_code` varchar(50) NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) NOT NULL COMMENT '仓库名称',
  `location` varchar(200) DEFAULT NULL COMMENT '仓库位置',
  `manager` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `capacity` decimal(12,2) DEFAULT NULL COMMENT '仓库容量',
  `used_capacity` decimal(12,2) DEFAULT '0.00' COMMENT '已用容量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用，1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库信息表';

-- 12. 库存台账表
CREATE TABLE `inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `locked_quantity` int NOT NULL DEFAULT '0' COMMENT '锁定数量',
  `available_quantity` int NOT NULL DEFAULT '0' COMMENT '可用数量',
  `min_stock` int DEFAULT '0' COMMENT '最低库存预警',
  `max_stock` int DEFAULT '0' COMMENT '最高库存预警',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seed_batch_warehouse` (`seed_id`,`batch_id`,`warehouse_id`),
  KEY `idx_seed_id` (`seed_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存台账表';

-- 13. 入库记录表
CREATE TABLE `inbound_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inbound_no` varchar(50) NOT NULL COMMENT '入库单号',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int NOT NULL COMMENT '入库数量',
  `inbound_time` datetime NOT NULL COMMENT '入库时间',
  `operator_id` bigint NOT NULL COMMENT '操作员ID',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_seed_id` (`seed_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_inbound_time` (`inbound_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库记录表';

-- 14. 出库记录表
CREATE TABLE `outbound_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `outbound_no` varchar(50) NOT NULL COMMENT '出库单号',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int NOT NULL COMMENT '出库数量',
  `outbound_time` datetime NOT NULL COMMENT '出库时间',
  `operator_id` bigint NOT NULL COMMENT '操作员ID',
  `purpose` varchar(200) DEFAULT NULL COMMENT '出库用途',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`),
  KEY `idx_seed_id` (`seed_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_outbound_time` (`outbound_time`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库记录表';

-- 15. 小程序用户表
CREATE TABLE `wx_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(100) NOT NULL COMMENT '微信openid',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信unionid',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像',
  `gender` tinyint DEFAULT NULL COMMENT '性别：0-未知，1-男，2-女',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序用户表';

-- 16. 收货地址表
CREATE TABLE `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `consignee` varchar(50) NOT NULL COMMENT '收货人',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail_address` varchar(200) NOT NULL COMMENT '详细地址',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- 17. 订单表
CREATE TABLE `order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_type` tinyint NOT NULL DEFAULT '1' COMMENT '订单类型：1-C端订单，2-B端订单',
  `total_amount` decimal(12,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` decimal(12,2) DEFAULT '0.00' COMMENT '优惠金额',
  `freight_amount` decimal(10,2) DEFAULT '0.00' COMMENT '运费',
  `payable_amount` decimal(12,2) NOT NULL COMMENT '应付金额',
  `paid_amount` decimal(12,2) DEFAULT '0.00' COMMENT '实付金额',
  `payment_method` tinyint DEFAULT NULL COMMENT '支付方式：1-微信支付，2-支付宝',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态：0-待付款，1-待审核，2-待发货，3-已发货，4-已完成，5-已取消，6-退款中',
  `consignee` varchar(50) NOT NULL COMMENT '收货人',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `address` varchar(500) NOT NULL COMMENT '收货地址',
  `remarks` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_type` (`order_type`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 18. 订单商品明细表
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint DEFAULT NULL COMMENT '批次ID',
  `seed_name` varchar(100) NOT NULL COMMENT '种子名称',
  `seed_image` varchar(500) DEFAULT NULL COMMENT '种子图片',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL COMMENT '数量',
  `total_amount` decimal(12,2) NOT NULL COMMENT '总金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_seed_id` (`seed_id`),
  KEY `idx_batch_id` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';

-- 19. 订单操作日志表
CREATE TABLE `order_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名',
  `operation` varchar(100) NOT NULL COMMENT '操作类型',
  `from_status` tinyint DEFAULT NULL COMMENT '原状态',
  `to_status` tinyint DEFAULT NULL COMMENT '新状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '操作备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';

-- 20. 购物车表
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `quantity` int NOT NULL COMMENT '数量',
  `selected` tinyint NOT NULL DEFAULT '1' COMMENT '是否选中：0-否，1-是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_seed` (`user_id`,`seed_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_seed_id` (`seed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 21. 售后申请表
CREATE TABLE `after_sales` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `after_sales_no` varchar(50) NOT NULL COMMENT '售后单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '售后类型：1-退货，2-换货',
  `reason` varchar(500) NOT NULL COMMENT '售后原因',
  `evidence_images` text COMMENT '凭证图片',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待处理，1-审核通过，2-审核拒绝，3-已完成',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `processor_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `process_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_after_sales_no` (`after_sales_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后申请表';

--22. 用户画像表 - 整合用户基本属性和行为偏好
CREATE TABLE `user_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '用户类型：1-小程序用户，2-管理员用户',
  `age_group` tinyint DEFAULT NULL COMMENT '年龄段：1-18岁以下，2-18-25，3-26-35，4-36-45，5-46-55，6-56以上',
  `region` varchar(100) DEFAULT NULL COMMENT '所在地区',
  `planting_scale` tinyint DEFAULT NULL COMMENT '种植规模：1-小型(<10亩)，2-中型(10-50亩)，3-大型(>50亩)',
  `planting_type` varchar(200) DEFAULT NULL COMMENT '种植类型偏好（存储品类ID，逗号分隔）',
  `price_preference` tinyint DEFAULT NULL COMMENT '价格偏好：1-经济型，2-性价比，3-高端型',
  `buy_frequency` tinyint DEFAULT NULL COMMENT '购买频率：1-低频(<1次/月)，2-中频(1-4次/月)，3-高频(>4次/月)',
  `avg_order_amount` decimal(10,2) DEFAULT '0.00' COMMENT '平均订单金额',
  `total_order_count` int DEFAULT '0' COMMENT '总订单数',
  `total_spent_amount` decimal(12,2) DEFAULT '0.00' COMMENT '总消费金额',
  `last_purchase_time` datetime DEFAULT NULL COMMENT '最后购买时间',
  `preferred_seasons` varchar(50) DEFAULT NULL COMMENT '偏好购买季节（春,夏,秋,冬）',
  `tags` varchar(1000) DEFAULT NULL COMMENT '用户标签（JSON格式）',
  `profile_score` decimal(5,4) DEFAULT '0.0000' COMMENT '画像完整度评分',
  `last_update_time` datetime DEFAULT NULL COMMENT '最后画像更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_region` (`region`),
  KEY `idx_planting_scale` (`planting_scale`),
  KEY `idx_last_purchase_time` (`last_purchase_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

--23. 用户行为记录表 - 核心行为数据收集
CREATE TABLE `user_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `behavior_type` tinyint NOT NULL COMMENT '行为类型：1-浏览，2-点击，3-加入购物车，4-购买，5-收藏',
  `behavior_weight` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '行为权重',
  `duration` int DEFAULT '0' COMMENT '停留时长（秒）',
  `source` varchar(50) DEFAULT NULL COMMENT '行为来源：home-首页，search-搜索，recommend-推荐',
  `behavior_time` datetime NOT NULL COMMENT '行为发生时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_behavior` (`user_id`,`behavior_type`,`behavior_time`),
  KEY `idx_seed_behavior` (`seed_id`,`behavior_type`,`behavior_time`),
  KEY `idx_behavior_time` (`behavior_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为记录表';

--24. 商品特征表 - 商品属性特征化
CREATE TABLE `seed_features` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `category_id` bigint NOT NULL COMMENT '品类ID',
  `price_level` tinyint NOT NULL COMMENT '价格等级：1-低价，2-中价，3-高价',
  `seasonal_type` tinyint DEFAULT NULL COMMENT '季节类型：1-春季，2-夏季，3-秋季，4-冬季，5-全年',
  `maturity_days` int DEFAULT NULL COMMENT '成熟天数',
  `yield_level` tinyint DEFAULT NULL COMMENT '产量等级：1-低产，2-中产，3-高产',
  `disease_resistance` tinyint DEFAULT NULL COMMENT '抗病性：1-弱，2-中，3-强',
  `climate_adaptability` varchar(200) DEFAULT NULL COMMENT '气候适应性',
  `popularity_score` decimal(5,4) DEFAULT '0.0000' COMMENT '热度评分',
  `quality_score` decimal(5,4) DEFAULT '0.0000' COMMENT '质量评分',
  `feature_vector` text COMMENT '特征向量（JSON格式）',
  `last_update_time` datetime DEFAULT NULL COMMENT '最后特征更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seed_id` (`seed_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_price_level` (`price_level`),
  KEY `idx_popularity_score` (`popularity_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品特征表';

--25. 推荐结果表 - 存储实时推荐结果
CREATE TABLE `recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `recommend_type` varchar(20) NOT NULL COMMENT '推荐类型：cf_user-用户协同过滤，cf_item-物品协同过滤，content-内容推荐，hot-热门推荐',
  `recommend_score` decimal(8,6) NOT NULL COMMENT '推荐分数',
  `recommend_reason` varchar(200) DEFAULT NULL COMMENT '推荐理由',
  `ranking` int NOT NULL COMMENT '推荐排名',
  `strategy_version` varchar(50) NOT NULL COMMENT '策略版本',
  `expire_time` datetime DEFAULT NULL COMMENT '推荐过期时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_seed_type` (`user_id`,`seed_id`,`recommend_type`),
  KEY `idx_user_recommend` (`user_id`,`ranking`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐结果表';


-- 初始化数据
-- 插入默认管理员用户（密码：admin123）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `deleted_flag`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800138000', 'admin@seed.com', 1, NOW(), 1, NOW(), 1, 0);

-- 插入默认角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `deleted_flag`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1, NOW(), 1, NOW(), 1, 0),
(2, '仓库管理员', 'WAREHOUSE_MANAGER', '管理仓库和库存', 1, NOW(), 1, NOW(), 1, 0),
(3, '订单管理员', 'ORDER_MANAGER', '处理订单相关业务', 1, NOW(), 1, NOW(), 1, 0);

-- 关联管理员和角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `create_time`, `create_by`, `update_time`, `update_by`, `deleted_flag`) VALUES
(1, 1, NOW(), 1, NOW(), 1, 0);

-- 插入系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`, `create_time`, `create_by`, `update_time`, `update_by`, `deleted_flag`) VALUES
('system.batch_prefix', 'SP', '批次号前缀', 1, '种子批次号生成前缀', NOW(), 1, NOW(), 1, 0),
('system.default_freight', '10.00', '默认运费', 1, '订单默认运费金额', NOW(), 1, NOW(), 1, 0),
('system.min_stock_threshold', '100', '最低库存阈值', 1, '触发库存预警的最低数量', NOW(), 1, NOW(), 1, 0);


-- 初始化行为权重配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`, `create_time`, `create_by`, `update_time`, `update_by`, `deleted_flag`) VALUES
('recommend.behavior.weight.browse', '0.1', '浏览行为权重', 2, '用户浏览商品的行为权重', NOW(), 1, NOW(), 1, 0),
('recommend.behavior.weight.click', '0.3', '点击行为权重', 2, '用户点击商品的行为权重', NOW(), 1, NOW(), 1, 0),
('recommend.behavior.weight.cart', '0.6', '加购行为权重', 2, '用户加入购物车的行为权重', NOW(), 1, NOW(), 1, 0),
('recommend.behavior.weight.purchase', '1.0', '购买行为权重', 2, '用户购买商品的行为权重', NOW(), 1, NOW(), 1, 0),
('recommend.result.count', '10', '推荐结果数量', 2, '每次给用户推荐的商品数量', NOW(), 1, NOW(), 1, 0),
('recommend.result.ttl', '86400', '推荐结果有效期', 2, '推荐结果的缓存有效期（秒）', NOW(), 1, NOW(), 1, 0);
```



## 6. 部署说明

项目支持使用 Docker 和 Docker Compose 进行一键式容器化部署。请参考项目根目录下的 `docker-compose.yml` 文件进行环境配置与启动。该文件已配置好后端服务、MySQL、Redis、MinIO 等所有依赖组件。
