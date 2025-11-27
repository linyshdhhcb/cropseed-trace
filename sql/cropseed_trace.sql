/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80035 (8.0.35)
 Source Host           : localhost:3306
 Source Schema         : cropseed_trace

 Target Server Type    : MySQL
 Target Server Version : 80035 (8.0.35)
 File Encoding         : 65001

 Date: 27/11/2025 12:06:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for after_sales
-- ----------------------------
DROP TABLE IF EXISTS `after_sales`;
CREATE TABLE `after_sales`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `after_sales_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '售后单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '售后类型：1-退货，2-换货',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '售后原因',
  `evidence_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '凭证图片',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-审核通过，2-审核拒绝，3-已完成',
  `refund_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '退款金额',
  `processor_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `process_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
  `process_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_after_sales_no`(`after_sales_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '售后申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of after_sales
-- ----------------------------

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `quantity` int NOT NULL COMMENT '数量',
  `selected` tinyint NOT NULL DEFAULT 1 COMMENT '是否选中：0-否，1-是',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_seed`(`user_id` ASC, `seed_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_seed_id`(`seed_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '购物车表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (1, 1986823631491473408, 1, 1, 1, '2025-11-08 22:48:38', 1986823631491473408, '2025-11-08 22:48:38', 1986823631491473408, 1);
INSERT INTO `cart` VALUES (2, 1986823631491473408, 2, 1, 0, '2025-11-10 20:36:46', 1986823631491473408, '2025-11-10 20:36:46', 1986823631491473408, 0);

-- ----------------------------
-- Table structure for inbound_record
-- ----------------------------
DROP TABLE IF EXISTS `inbound_record`;
CREATE TABLE `inbound_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inbound_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '入库单号',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int NOT NULL COMMENT '入库数量',
  `inbound_time` datetime NOT NULL COMMENT '入库时间',
  `operator_id` bigint NOT NULL COMMENT '操作员ID',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inbound_no`(`inbound_no` ASC) USING BTREE,
  INDEX `idx_seed_id`(`seed_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_inbound_time`(`inbound_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入库记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inbound_record
-- ----------------------------

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `locked_quantity` int NOT NULL DEFAULT 0 COMMENT '锁定数量',
  `available_quantity` int NOT NULL DEFAULT 0 COMMENT '可用数量',
  `min_stock` int NULL DEFAULT 0 COMMENT '最低库存预警',
  `max_stock` int NULL DEFAULT 0 COMMENT '最高库存预警',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_seed_batch_warehouse`(`seed_id` ASC, `batch_id` ASC, `warehouse_id` ASC) USING BTREE,
  INDEX `idx_seed_id`(`seed_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存台账表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inventory
-- ----------------------------
INSERT INTO `inventory` VALUES (1, 1, 1, 1, 462, 0, 462, 8, 12, '2025-11-02 22:32:10', NULL, '2025-11-02 22:32:10', 1986823631491473408, 0);

-- ----------------------------
-- Table structure for inventory_inbound
-- ----------------------------
DROP TABLE IF EXISTS `inventory_inbound`;
CREATE TABLE `inventory_inbound`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批次号',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '仓库ID',
  `seed_batch_id` bigint NULL DEFAULT NULL COMMENT '种子批次ID',
  `quantity` int NULL DEFAULT NULL COMMENT '入库数量',
  `supplier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商',
  `quality_grade` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '质量等级',
  `storage_condition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '存储条件',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_inbound
-- ----------------------------
INSERT INTO `inventory_inbound` VALUES (3, '1254', 1, 1, 453, '45321', '4532', '4532', 2, '453', '2025-11-02 19:43:25', NULL, '2025-11-02 19:43:25', NULL, 0);
INSERT INTO `inventory_inbound` VALUES (4, '46564', 1, 1, 10, '11', '11', '11', 2, '01', '2025-11-19 14:26:34', 1, '2025-11-19 14:26:34', 1, 0);

-- ----------------------------
-- Table structure for inventory_outbound
-- ----------------------------
DROP TABLE IF EXISTS `inventory_outbound`;
CREATE TABLE `inventory_outbound`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批次号',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '仓库ID',
  `seed_batch_id` bigint NULL DEFAULT NULL COMMENT '种子批次ID',
  `quantity` int NULL DEFAULT NULL COMMENT '出库数量',
  `recipient` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收方',
  `purpose` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用途',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '出库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_outbound
-- ----------------------------
INSERT INTO `inventory_outbound` VALUES (4, 'B20251001001', 1, 1, 463, '测试', '测试', 3, '太多了', '2025-11-06 10:34:05', NULL, '2025-11-06 10:34:05', NULL, 0);

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_type` tinyint NOT NULL DEFAULT 1 COMMENT '订单类型：1-C端订单，2-B端订单',
  `total_amount` decimal(12, 2) NOT NULL COMMENT '订单总金额',
  `discount_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `freight_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '运费',
  `payable_amount` decimal(12, 2) NOT NULL COMMENT '应付金额',
  `paid_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '实付金额',
  `payment_method` tinyint NULL DEFAULT NULL COMMENT '支付方式：1-微信支付，2-支付宝',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0-待付款，1-待审核，2-待发货，3-已发货，4-已完成，5-已取消，6-退款中',
  `consignee` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货地址',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_type`(`order_type` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_info
-- ----------------------------
INSERT INTO `order_info` VALUES (11, 'ORD20251113201931134062', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 18.50, 2, '2025-11-13 20:19:39', 1, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-13 20:19:32', 1986823631491473408, '2025-11-13 20:19:32', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (12, 'ORD20251113202521487021', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 18.50, 1, '2025-11-14 20:43:32', 1, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-13 20:25:21', 1986823631491473408, '2025-11-13 20:25:21', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (13, 'ORD20251114135453207390', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 18.50, 2, '2025-11-14 20:33:15', 1, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-14 13:54:53', 1986823631491473408, '2025-11-14 13:54:53', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (14, 'ORD20251114204345759109', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 0.00, NULL, NULL, 0, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-14 20:43:45', 1986823631491473408, '2025-11-14 20:43:45', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (15, 'ORD20251116172909414848', 1986823631491473408, 1, 37.00, 0.00, 0.00, 37.00, 37.00, 1, '2025-11-16 17:29:16', 1, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-16 17:29:10', 1986823631491473408, '2025-11-16 17:29:10', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (16, 'ORD20251116173659504327', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 18.50, 1, '2025-11-16 17:37:03', 1, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-16 17:36:59', 1986823631491473408, '2025-11-16 17:36:59', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (17, 'ORD20251116174031136503', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 18.50, 1, '2025-11-16 17:40:35', 1, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-16 17:40:32', 1986823631491473408, '2025-11-16 17:40:32', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (18, 'ORD20251117213540945305', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 0.00, NULL, NULL, 0, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-17 21:35:40', 1986823631491473408, '2025-11-17 21:35:40', 1986823631491473408, 0);
INSERT INTO `order_info` VALUES (19, 'ORD20251117213834808450', 1986823631491473408, 1, 18.50, 0.00, 0.00, 18.50, 18.50, 1, '2025-11-17 21:38:58', 4, '就好', '13112665369', '河北省石家庄市长安区1312313', '', '2025-11-17 21:38:35', 1986823631491473408, '2025-11-17 21:38:35', 1986823631491473408, 0);

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '批次ID',
  `trace_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '溯源码',
  `seed_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '种子名称',
  `seed_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '种子图片',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL COMMENT '数量',
  `total_amount` decimal(12, 2) NOT NULL COMMENT '总金额',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_seed_id`(`seed_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_trace_code`(`trace_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单商品明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (13, 11, 1, 1, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-13 20:19:32', 1986823631491473408, '2025-11-13 20:19:32', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (14, 12, 1, 1, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-13 20:25:21', 1986823631491473408, '2025-11-13 20:25:21', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (15, 13, 1, 1, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-14 13:54:54', 1986823631491473408, '2025-11-14 13:54:54', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (16, 14, 1, NULL, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-14 20:43:45', 1986823631491473408, '2025-11-14 20:43:45', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (17, 15, 1, 1, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 2, 37.00, '2025-11-16 17:29:10', 1986823631491473408, '2025-11-16 17:29:10', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (18, 16, 1, 1, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-16 17:36:59', 1986823631491473408, '2025-11-16 17:36:59', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (19, 17, 1, 1, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-16 17:40:32', 1986823631491473408, '2025-11-16 17:40:32', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (20, 18, 1, NULL, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-17 21:35:40', 1986823631491473408, '2025-11-17 21:35:40', 1986823631491473408, 0);
INSERT INTO `order_item` VALUES (21, 19, 1, 1, NULL, '红宝石番茄种子', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, 18.50, '2025-11-17 21:38:35', 1986823631491473408, '2025-11-17 21:38:35', 1986823631491473408, 0);

-- ----------------------------
-- Table structure for order_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `order_operation_log`;
CREATE TABLE `order_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `from_status` tinyint NULL DEFAULT NULL COMMENT '原状态',
  `to_status` tinyint NULL DEFAULT NULL COMMENT '新状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_operation_log
-- ----------------------------
INSERT INTO `order_operation_log` VALUES (16, 11, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户立即购买创建订单', '2025-11-13 20:19:32', 1986823631491473408, '2025-11-13 20:19:32', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (17, 11, 1986823631491473408, '1986823631491473408', '支付订单', 0, 1, '订单支付成功，库存已扣减', '2025-11-13 20:19:39', 1986823631491473408, '2025-11-13 20:19:39', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (18, 12, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户创建订单', '2025-11-13 20:25:21', 1986823631491473408, '2025-11-13 20:25:21', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (19, 13, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户创建订单', '2025-11-14 13:54:54', 1986823631491473408, '2025-11-14 13:54:54', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (20, 13, 1986823631491473408, '1986823631491473408', '支付订单', 0, 1, '订单支付成功，库存已扣减', '2025-11-14 20:33:15', 1986823631491473408, '2025-11-14 20:33:15', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (21, 12, 1986823631491473408, '1986823631491473408', '支付订单', 0, 1, '订单支付成功，库存已扣减', '2025-11-14 20:43:32', 1986823631491473408, '2025-11-14 20:43:32', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (22, 14, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户创建订单', '2025-11-14 20:43:45', 1986823631491473408, '2025-11-14 20:43:45', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (23, 15, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户创建订单', '2025-11-16 17:29:10', 1986823631491473408, '2025-11-16 17:29:10', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (24, 15, 1986823631491473408, '1986823631491473408', '支付订单', 0, 1, '订单支付成功，库存已扣减', '2025-11-16 17:29:16', 1986823631491473408, '2025-11-16 17:29:16', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (25, 16, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户创建订单', '2025-11-16 17:36:59', 1986823631491473408, '2025-11-16 17:36:59', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (26, 16, 1986823631491473408, '1986823631491473408', '支付订单', 0, 1, '订单支付成功，库存已扣减', '2025-11-16 17:37:03', 1986823631491473408, '2025-11-16 17:37:03', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (27, 17, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户立即购买创建订单', '2025-11-16 17:40:32', 1986823631491473408, '2025-11-16 17:40:32', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (28, 17, 1986823631491473408, '1986823631491473408', '支付订单', 0, 1, '订单支付成功，库存已扣减', '2025-11-16 17:40:35', 1986823631491473408, '2025-11-16 17:40:35', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (29, 18, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户创建订单', '2025-11-17 21:35:40', 1986823631491473408, '2025-11-17 21:35:40', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (30, 19, 1986823631491473408, '1986823631491473408', '创建订单', NULL, 0, '用户创建订单', '2025-11-17 21:38:35', 1986823631491473408, '2025-11-17 21:38:35', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (31, 19, 1986823631491473408, '1986823631491473408', '支付订单', 0, 1, '订单支付成功，库存已扣减', '2025-11-17 21:38:58', 1986823631491473408, '2025-11-17 21:38:58', 1986823631491473408, 0);
INSERT INTO `order_operation_log` VALUES (32, 19, 1, '1', '审核通过', 1, 2, NULL, '2025-11-18 13:04:28', 1, '2025-11-18 13:04:28', 1, 0);
INSERT INTO `order_operation_log` VALUES (33, 19, 1, '1', '订单发货', 2, 3, '备注', '2025-11-18 13:04:57', 1, '2025-11-18 13:04:57', 1, 0);
INSERT INTO `order_operation_log` VALUES (34, 19, 1, '1', '订单完成', 3, 4, '订单已完成', '2025-11-18 13:05:00', 1, '2025-11-18 13:05:00', 1, 0);

-- ----------------------------
-- Table structure for outbound_record
-- ----------------------------
DROP TABLE IF EXISTS `outbound_record`;
CREATE TABLE `outbound_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `outbound_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出库单号',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int NOT NULL COMMENT '出库数量',
  `outbound_time` datetime NOT NULL COMMENT '出库时间',
  `operator_id` bigint NOT NULL COMMENT '操作员ID',
  `purpose` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出库用途',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbound_no`(`outbound_no` ASC) USING BTREE,
  INDEX `idx_seed_id`(`seed_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_outbound_time`(`outbound_time` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '出库记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of outbound_record
-- ----------------------------
INSERT INTO `outbound_record` VALUES (1, 'OUT202511131846586275', 1, 1, 1, 1, '2025-11-13 18:46:59', 1986823631491473408, '订单出库', 4, '订单支付自动出库', '2025-11-13 18:46:59', 1986823631491473408, '2025-11-13 18:46:59', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (2, 'OUT202511132004595382', 1, 1, 1, 1, '2025-11-13 20:04:59', 1986823631491473408, '订单出库', 9, '订单支付自动出库', '2025-11-13 20:04:59', 1986823631491473408, '2025-11-13 20:04:59', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (3, 'OUT202511132015263277', 1, 1, 1, 1, '2025-11-13 20:15:26', 1986823631491473408, '订单出库', 10, '订单支付自动出库', '2025-11-13 20:15:26', 1986823631491473408, '2025-11-13 20:15:26', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (4, 'OUT202511132019383118', 1, 1, 1, 1, '2025-11-13 20:19:39', 1986823631491473408, '订单出库', 11, '订单支付自动出库', '2025-11-13 20:19:39', 1986823631491473408, '2025-11-13 20:19:39', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (5, 'OUT202511142033144749', 1, 1, 1, 1, '2025-11-14 20:33:14', 1986823631491473408, '订单出库', 13, '订单支付自动出库', '2025-11-14 20:33:14', 1986823631491473408, '2025-11-14 20:33:14', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (6, 'OUT202511142043317007', 1, 1, 1, 1, '2025-11-14 20:43:32', 1986823631491473408, '订单出库', 12, '订单支付自动出库', '2025-11-14 20:43:32', 1986823631491473408, '2025-11-14 20:43:32', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (7, 'OUT202511161729142851', 1, 1, 1, 2, '2025-11-16 17:29:15', 1986823631491473408, '订单出库', 15, '订单支付自动出库', '2025-11-16 17:29:15', 1986823631491473408, '2025-11-16 17:29:15', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (8, 'OUT202511161737025679', 1, 1, 1, 1, '2025-11-16 17:37:03', 1986823631491473408, '订单出库', 16, '订单支付自动出库', '2025-11-16 17:37:03', 1986823631491473408, '2025-11-16 17:37:03', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (9, 'OUT202511161740350389', 1, 1, 1, 1, '2025-11-16 17:40:35', 1986823631491473408, '订单出库', 17, '订单支付自动出库', '2025-11-16 17:40:35', 1986823631491473408, '2025-11-16 17:40:35', 1986823631491473408, 0);
INSERT INTO `outbound_record` VALUES (10, 'OUT202511172138573637', 1, 1, 1, 1, '2025-11-17 21:38:58', 1986823631491473408, '订单出库', 19, '订单支付自动出库', '2025-11-17 21:38:58', 1986823631491473408, '2025-11-17 21:38:58', 1986823631491473408, 0);

-- ----------------------------
-- Table structure for recommendation
-- ----------------------------
DROP TABLE IF EXISTS `recommendation`;
CREATE TABLE `recommendation`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `recommendation_type` tinyint NOT NULL COMMENT '推荐类型：1-协同过滤，2-内容推荐，3-热门推荐，4-个性化推荐',
  `target_type` tinyint NOT NULL COMMENT '目标类型：1-种子，2-品类',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `recommendation_score` decimal(5, 4) NOT NULL COMMENT '推荐分数：0-1之间的小数',
  `recommendation_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐理由',
  `recommendation_weight` decimal(3, 2) NULL DEFAULT 1.00 COMMENT '推荐权重：0-1之间的小数',
  `is_shown` tinyint NULL DEFAULT 0 COMMENT '是否已展示：0-未展示，1-已展示',
  `is_clicked` tinyint NULL DEFAULT 0 COMMENT '是否已点击：0-未点击，1-已点击',
  `is_purchased` tinyint NULL DEFAULT 0 COMMENT '是否已购买：0-未购买，1-已购买',
  `click_time` datetime NULL DEFAULT NULL COMMENT '点击时间',
  `purchase_time` datetime NULL DEFAULT NULL COMMENT '购买时间',
  `algorithm_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'v1.0' COMMENT '推荐算法版本',
  `batch_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐批次',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_recommendation_type`(`recommendation_type` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_recommendation_score`(`recommendation_score` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '推荐结果表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of recommendation
-- ----------------------------
INSERT INTO `recommendation` VALUES (1, 1, 1, 1, 2, 0.8500, '基于相似用户的购买行为推荐', 1.00, 0, 0, 0, NULL, NULL, 'v1.0', 'batch_001', '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `recommendation` VALUES (2, 1, 2, 1, 3, 0.7800, '基于商品特征相似性推荐', 1.00, 0, 0, 0, NULL, NULL, 'v1.0', 'batch_001', '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `recommendation` VALUES (3, 2, 3, 1, 1, 0.7000, '基于市场热度的热门商品推荐', 1.00, 0, 0, 0, NULL, NULL, 'v1.0', 'batch_002', '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `recommendation` VALUES (4, 3, 4, 1, 1, 0.9200, '基于质量要求的个性化推荐', 1.00, 0, 0, 0, NULL, NULL, 'v1.0', 'batch_003', '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);

-- ----------------------------
-- Table structure for seed_batch
-- ----------------------------
DROP TABLE IF EXISTS `seed_batch`;
CREATE TABLE `seed_batch`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次号',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `production_date` date NOT NULL COMMENT '生产日期',
  `expiry_date` date NULL DEFAULT NULL COMMENT '有效期至',
  `quality_report` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '质检报告URL',
  `quality_status` tinyint NULL DEFAULT 1 COMMENT '质检状态：0-不合格，1-合格',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `trace_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '溯源码',
  `producer_id` bigint NULL DEFAULT NULL COMMENT '生产商ID',
  `producer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产商名称',
  `production_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产地点',
  `harvest_date` date NULL DEFAULT NULL COMMENT '收获日期',
  `processing_date` date NULL DEFAULT NULL COMMENT '加工日期',
  `storage_condition` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '储存条件',
  `certification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '认证信息(有机/绿色/无公害等)',
  `traceability_level` tinyint NULL DEFAULT 1 COMMENT '可追溯等级：1-基础，2-详细，3-完整',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `initial_quality_grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '初始质量等级',
  `moisture_content` decimal(5, 2) NULL DEFAULT NULL COMMENT '含水率(%)',
  `germination_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '发芽率(%)',
  `purity` decimal(5, 2) NULL DEFAULT NULL COMMENT '纯度(%)',
  `packaging_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '包装类型',
  `packaging_specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '包装规格',
  `initial_operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '初始操作员姓名',
  `initial_operator_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '初始操作员电话',
  `production_equipment` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产设备',
  `processing_method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加工方式',
  `seed_source` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '种子来源',
  `parent_variety` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '亲本品种',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_batch_no`(`batch_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_trace_code`(`trace_code` ASC) USING BTREE,
  INDEX `idx_seed_id`(`seed_id` ASC) USING BTREE,
  INDEX `idx_production_date`(`production_date` ASC) USING BTREE,
  INDEX `idx_producer_id`(`producer_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '种子批次表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of seed_batch
-- ----------------------------
INSERT INTO `seed_batch` VALUES (1, 'B20251001001', 1, '2025-10-01', '2027-10-01', 'https://example.com/reports/TOM001_B20251001001.pdf', 1, '抽检合格，包装完好', 'BJ2025000006', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2025-10-27 14:30:07', 1, '2025-11-19 11:38:18', 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `seed_batch` VALUES (2, 'B20251002001', 2, '2025-10-02', '2027-10-02', 'https://example.com/reports/CUC001_B20251002001.pdf', 1, '高温烘干处理，发芽率达标', 'BJ2025000007', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2025-10-27 14:30:07', 1, '2025-11-19 11:42:49', 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `seed_batch` VALUES (3, 'B20251003001', 3, '2025-10-03', '2027-10-03', 'https://example.com/reports/RICE001_B20251003001.pdf', 1, '国家良种认证，附带证书', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2025-10-27 14:30:07', 1, '2025-10-27 14:30:07', 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `seed_batch` VALUES (4, '89764556', 3, '2025-11-20', '2026-11-30', '1232', 1, '11', 'BJ2025000008', 1, '北京优质农业基地', '北京市通州区农业示范园', '2025-11-01', '2025-11-06', '11', '11', 1, '2025-11-20 21:46:41', 1, '2025-11-21 10:06:38', 1, 0, '千克', '优等', 10.00, 10.00, 10.00, '11', '11', '11', '11', '测试', '测试', '测试', '测试');
INSERT INTO `seed_batch` VALUES (8, '649798658789', 1, '2025-11-20', '2026-11-20', 'http://117.72.101.170:9000/cropseed-trace/images/6b56e793054f48f0bc8a478f9b1ad726.jpg', 1, '465456', 'BJ2025000009', 1, '北京优质农业基地', '北京市通州区农业示范园', '2025-11-11', '2025-11-18', '123', '有机认证', 1, '2025-11-20 22:16:50', 1, '2025-11-21 10:13:18', 1, 0, '斤', '优等', 11.00, 12.00, 12.00, '1213', '123', 'linyi', '13112665356', '123', '干燥加工', '123', '123');
INSERT INTO `seed_batch` VALUES (9, '1212', 2, '2025-11-05', '2025-11-25', 'http://117.72.101.170:9000/cropseed-trace/images/b1b2b531ec3a476f8d6db1663d71dc84.jpg', 1, '测试', 'BJ2025000010', 1, '北京优质农业基地', '北京市通州区农业示范园', '2025-11-04', '2025-11-17', '测试', 'GAP认证', 1, '2025-11-21 10:21:26', 1, '2025-11-21 10:21:29', 1, 0, 'kg', '一等', 12.00, 12.00, 12.00, '测试', '测试', '测试', '13112558989', '测试', '干燥加工', '测试', '测试');

-- ----------------------------
-- Table structure for seed_category
-- ----------------------------
DROP TABLE IF EXISTS `seed_category`;
CREATE TABLE `seed_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父品类ID',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品类编码',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品类名称',
  `level` int NOT NULL COMMENT '层级',
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '层级路径',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '品类描述',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_code`(`category_code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '种子品类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of seed_category
-- ----------------------------
INSERT INTO `seed_category` VALUES (1, 0, 'VEG', '蔬菜类', 1, '/VEG', '各类蔬菜种子', 1, 1, '2025-10-27 14:29:17', 1, '2025-10-27 14:29:17', 1, 0);
INSERT INTO `seed_category` VALUES (2, 1, 'VEG_TOM', '番茄类', 2, '/VEG/VEG_TOM', '番茄品种分类', 1, 1, '2025-10-27 14:29:17', 1, '2025-10-27 14:29:17', 1, 0);
INSERT INTO `seed_category` VALUES (3, 1, 'VEG_CUC', '黄瓜类', 2, '/VEG/VEG_CUC', '黄瓜品种分类', 2, 1, '2025-10-27 14:29:17', 1, '2025-10-27 14:29:17', 1, 0);
INSERT INTO `seed_category` VALUES (4, 0, 'GRAIN', '粮食类', 1, '/GRAIN', '主要粮食作物种子', 2, 1, '2025-10-27 14:29:17', 1, '2025-10-27 14:29:17', 1, 0);
INSERT INTO `seed_category` VALUES (5, 4, 'GRAIN_RICE', '水稻类', 2, '/GRAIN/GRAIN_RICE', '水稻品种分类', 1, 1, '2025-10-27 14:29:17', 1, '2025-11-18 12:32:55', 1, 0);
INSERT INTO `seed_category` VALUES (6, 0, '1212', '测试', 1, '1212', '', 1, 1, '2025-10-29 12:16:50', NULL, '2025-10-29 12:17:10', NULL, 1);
INSERT INTO `seed_category` VALUES (7, 6, '13456', '测试', 2, '.13456', '', 1, 1, '2025-10-29 12:17:01', NULL, '2025-10-29 12:17:08', NULL, 1);

-- ----------------------------
-- Table structure for seed_features
-- ----------------------------
DROP TABLE IF EXISTS `seed_features`;
CREATE TABLE `seed_features`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `seed_id` bigint NOT NULL COMMENT '种子ID',
  `price_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '价格特征：0-1之间的小数',
  `quality_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '质量特征：0-1之间的小数',
  `brand_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '品牌特征：0-1之间的小数',
  `yield_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '产量特征：0-1之间的小数',
  `disease_resistance_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '抗病性特征：0-1之间的小数',
  `adaptability_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '适应性特征：0-1之间的小数',
  `maturity_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '成熟期特征：0-1之间的小数',
  `planting_difficulty_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '种植难度特征：0-1之间的小数',
  `market_heat` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '市场热度：0-1之间的小数',
  `user_rating` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '用户评分：0-1之间的小数',
  `sales_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '销量特征：0-1之间的小数',
  `inventory_feature` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '库存特征：0-1之间的小数',
  `recommendation_weight` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '推荐权重：0-1之间的小数',
  `feature_vector` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '特征向量（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_seed_id`(`seed_id` ASC) USING BTREE,
  INDEX `idx_price_feature`(`price_feature` ASC) USING BTREE,
  INDEX `idx_quality_feature`(`quality_feature` ASC) USING BTREE,
  INDEX `idx_market_heat`(`market_heat` ASC) USING BTREE,
  INDEX `idx_recommendation_weight`(`recommendation_weight` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品特征表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of seed_features
-- ----------------------------
INSERT INTO `seed_features` VALUES (1, 1, 0.72, 0.85, 0.68, 0.80, 0.78, 0.75, 0.70, 0.65, 0.88, 0.92, 0.85, 0.70, 0.83, '{\"tags\":[\"高抗病\",\"高产\",\"市场热\"]}', '2025-10-27 14:30:24', '2025-10-27 14:30:24', 'admin', 'admin', 0);
INSERT INTO `seed_features` VALUES (2, 2, 0.68, 0.82, 0.70, 0.85, 0.80, 0.78, 0.72, 0.68, 0.90, 0.89, 0.88, 0.75, 0.85, '{\"tags\":[\"高产\",\"易种植\",\"热销\"]}', '2025-10-27 14:30:24', '2025-10-27 14:30:24', 'admin', 'admin', 0);
INSERT INTO `seed_features` VALUES (3, 3, 0.75, 0.88, 0.80, 0.78, 0.82, 0.85, 0.80, 0.72, 0.85, 0.90, 0.80, 0.78, 0.86, '{\"tags\":[\"优质\",\"抗倒伏\",\"食味佳\"]}', '2025-10-27 14:30:24', '2025-10-27 14:30:24', 'admin', 'admin', 0);

-- ----------------------------
-- Table structure for seed_info
-- ----------------------------
DROP TABLE IF EXISTS `seed_info`;
CREATE TABLE `seed_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seed_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '种子名称',
  `seed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '种子编码',
  `category_id` bigint NOT NULL COMMENT '品类ID',
  `variety` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品种',
  `origin_place` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产地',
  `characteristics` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '特性描述',
  `specifications` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '规格参数',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片URL',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '单价',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_seed_code`(`seed_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_seed_name_category`(`seed_name` ASC, `category_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '种子信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of seed_info
-- ----------------------------
INSERT INTO `seed_info` VALUES (1, '红宝石番茄种子', 'TOM001', 2, '红宝石', '山东寿光', '测试', '测试', 'http://117.72.101.170:9000/cropseed-trace/seed-images/a4910cef9e134975a86d1638d08e367e.jpg', 18.50, 1, '2025-10-27 14:29:31', 1, '2025-11-13 17:24:52', 1, 0);
INSERT INTO `seed_info` VALUES (2, '绿冠黄瓜种子', 'CUC001', 3, '绿冠', '河北廊坊', '测试', '测试', 'http://117.72.101.170:9000/cropseed-trace/seed-images/f5d47c30af184298ae11d7ec25ce94cc.jpg', 22.00, 1, '2025-10-27 14:29:31', 1, '2025-11-13 16:58:02', 1, 0);
INSERT INTO `seed_info` VALUES (3, '南粳46水稻种子', 'RICE001', 5, '南粳46', '江苏南京', '优质粳稻，食味佳，抗倒伏', '千粒重≥2.5g，发芽率≥88%', '', 15.80, 1, '2025-10-27 14:29:31', 1, '2025-10-27 14:29:31', 1, 0);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `config_type` tinyint NULL DEFAULT 1 COMMENT '配置类型：1-系统配置，2-业务配置',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'system.batch_prefix', 'SP', '批次号前缀', 1, '种子批次号生成前缀', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (2, 'system.default_freight', '10.00', '默认运费', 1, '订单默认运费金额', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (3, 'system.min_stock_threshold', '100', '最低库存阈值', 1, '触发库存预警的最低数量', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (4, 'recommend.behavior.weight.browse', '0.1', '浏览行为权重', 2, '用户浏览商品的行为权重', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (5, 'recommend.behavior.weight.click', '0.3', '点击行为权重', 2, '用户点击商品的行为权重', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (6, 'recommend.behavior.weight.cart', '0.6', '加购行为权重', 2, '用户加入购物车的行为权重', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (7, 'recommend.behavior.weight.purchase', '1.0', '购买行为权重', 2, '用户购买商品的行为权重', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (8, 'recommend.result.count', '10', '推荐结果数量', 2, '每次给用户推荐的商品数量', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_config` VALUES (9, 'recommend.result.ttl', '86400', '推荐结果有效期', 2, '推荐结果的缓存有效期（秒）', '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 241 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', 1, NULL, '/system', NULL, 'system', 10, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (2, 1, '用户管理', 2, NULL, 'user', '@/views/system/user/index.vue', 'user', 1, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (3, 2, '用户查询', 3, 'system:user:list', NULL, NULL, NULL, 1, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (4, 2, '用户详情', 3, 'system:user:query', NULL, NULL, NULL, 2, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (5, 2, '新增用户', 3, 'system:user:add', NULL, NULL, NULL, 3, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (6, 2, '编辑用户', 3, 'system:user:edit', NULL, NULL, NULL, 4, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (7, 2, '删除用户', 3, 'system:user:remove', NULL, NULL, NULL, 5, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (8, 2, '重置密码', 3, 'system:user:resetPwd', NULL, NULL, NULL, 6, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (9, 1, '角色管理', 2, NULL, 'role', '@/views/system/role/index.vue', 'role', 2, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (10, 9, '角色查询', 3, 'system:role:list', NULL, NULL, NULL, 1, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (11, 9, '角色详情', 3, 'system:role:query', NULL, NULL, NULL, 2, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (12, 9, '新增角色', 3, 'system:role:add', NULL, NULL, NULL, 3, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (13, 9, '编辑角色', 3, 'system:role:edit', NULL, NULL, NULL, 4, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (14, 9, '删除角色', 3, 'system:role:remove', NULL, NULL, NULL, 5, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (15, 1, '菜单管理', 2, NULL, 'menu', '@/views/system/menu/index.vue', 'menu', 3, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (16, 15, '菜单查询', 3, 'system:menu:list', NULL, NULL, NULL, 1, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (17, 15, '菜单详情', 3, 'system:menu:query', NULL, NULL, NULL, 2, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (18, 15, '新增菜单', 3, 'system:menu:add', NULL, NULL, NULL, 3, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (19, 15, '编辑菜单', 3, 'system:menu:edit', NULL, NULL, NULL, 4, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (20, 15, '删除菜单', 3, 'system:menu:remove', NULL, NULL, NULL, 5, 1, '2025-10-27 10:48:38', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (21, 0, '种子管理', 1, NULL, '/seed', NULL, 'tree-table', 20, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (22, 21, '种子批次', 2, NULL, 'batch', '@/views/seed/batch/index.vue', 'document', 1, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (23, 22, '批次查询', 3, 'seed:batch:list', NULL, NULL, NULL, 1, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (24, 22, '批次详情', 3, 'seed:batch:query', NULL, NULL, NULL, 2, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (25, 22, '新增批次', 3, 'seed:batch:add', NULL, NULL, NULL, 3, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (26, 22, '编辑批次', 3, 'seed:batch:edit', NULL, NULL, NULL, 4, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (27, 22, '删除批次', 3, 'seed:batch:remove', NULL, NULL, NULL, 5, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (28, 21, '种子品类', 2, NULL, 'category', '@/views/seed/category/index.vue', 'category', 2, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (29, 28, '品类查询', 3, 'seed:category:list', NULL, NULL, NULL, 1, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (30, 28, '品类详情', 3, 'seed:category:query', NULL, NULL, NULL, 2, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (31, 28, '新增品类', 3, 'seed:category:add', NULL, NULL, NULL, 3, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (32, 28, '编辑品类', 3, 'seed:category:edit', NULL, NULL, NULL, 4, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (33, 28, '删除品类', 3, 'seed:category:remove', NULL, NULL, NULL, 5, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (34, 21, '种子信息', 2, NULL, 'info', '@/views/seed/info/index.vue', 'info-card', 3, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (35, 34, '信息查询', 3, 'seed:info:list', NULL, NULL, NULL, 1, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (36, 34, '信息详情', 3, 'seed:info:query', NULL, NULL, NULL, 2, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (37, 34, '新增信息', 3, 'seed:info:add', NULL, NULL, NULL, 3, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (38, 34, '编辑信息', 3, 'seed:info:edit', NULL, NULL, NULL, 4, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (39, 34, '删除信息', 3, 'seed:info:remove', NULL, NULL, NULL, 5, 1, '2025-10-27 14:26:25', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (40, 0, '订单管理', 1, NULL, '/order', NULL, 'icon-order', 10, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (41, 40, '订单列表', 2, NULL, 'list', '@/views/order/list/index.vue', 'icon-list', 1, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (42, 41, '订单详情', 2, NULL, 'detail', '@/views/order/detail/index.vue', 'icon-detail', 2, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (43, 41, '查看订单', 3, 'order:list', NULL, NULL, NULL, 1, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (44, 41, '创建订单', 3, 'order:create', NULL, NULL, NULL, 2, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (45, 41, '取消订单', 3, 'order:cancel', NULL, NULL, NULL, 3, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (46, 41, '查询订单', 3, 'order:query', NULL, NULL, NULL, 4, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (47, 42, '订单支付', 3, 'order:pay', NULL, NULL, NULL, 5, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (48, 42, '订单审核', 3, 'order:audit', NULL, NULL, NULL, 6, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (49, 42, '订单发货', 3, 'order:ship', NULL, NULL, NULL, 7, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (50, 42, '订单完成', 3, 'order:complete', NULL, NULL, NULL, 8, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (51, 42, '申请退款', 3, 'order:refund', NULL, NULL, NULL, 9, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (52, 42, '处理退款', 3, 'order:process-refund', NULL, NULL, NULL, 10, 1, '2025-11-06 10:44:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (53, 0, '微信管理', 1, NULL, '/wechat', NULL, 'icon-wechat', 20, 1, '2025-11-06 14:32:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (54, 53, '微信用户管理', 2, NULL, 'user', '@/views/wechat/user/index.vue', 'icon-user', 1, 1, '2025-11-06 14:32:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (55, 54, '查看用户列表', 3, 'wechat:user:list', NULL, NULL, NULL, 1, 1, '2025-11-06 14:32:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (56, 54, '查询用户', 3, 'wechat:user:query', NULL, NULL, NULL, 2, 1, '2025-11-06 14:32:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (57, 54, '编辑用户', 3, 'wechat:user:edit', NULL, NULL, NULL, 3, 1, '2025-11-06 14:32:41', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (58, 54, '删除用户', 3, 'wechat:user:remove', '', '', '', 4, 1, '2025-11-06 14:32:41', NULL, '2025-11-18 12:30:30', 1, 0);
INSERT INTO `sys_menu` VALUES (59, 0, '统计分析', 1, NULL, '/statistics', 'Layout', 'el-icon-s-data', 10, 1, '2025-11-20 19:36:20', 1, '2025-11-20 19:36:20', 1, 0);
INSERT INTO `sys_menu` VALUES (60, 59, '统计查看', 2, 'statistics:view', 'list', '@/views/statistics/index.vue', 'el-icon-s-data', 1, 1, '2025-11-20 19:36:20', 1, '2025-11-20 19:36:20', 1, 0);
INSERT INTO `sys_menu` VALUES (61, 60, '导出', 3, 'statistics:export', NULL, NULL, NULL, 2, 1, '2025-11-20 19:36:20', 1, '2025-11-20 19:36:20', 1, 0);
INSERT INTO `sys_menu` VALUES (100, 0, '库存管理', 1, NULL, '/inventory', NULL, 'ri:stack-line', 5, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (101, 100, '库存管理', 2, NULL, 'stock', '@/views/inventory/stock/index.vue', NULL, 1, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (102, 100, '入库管理', 2, NULL, 'inbound', '@/views/inventory/inbound/index.vue', NULL, 2, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (103, 100, '出库管理', 2, NULL, 'outbound', '@/views/inventory/outbound/index.vue', NULL, 3, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (104, 100, '仓库管理', 2, NULL, 'warehouse', '@/views/inventory/warehouse/index.vue', NULL, 4, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (105, 101, '查询库存', 3, 'inventory:list', NULL, NULL, NULL, 1, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (106, 101, '入库', 3, 'inventory:inbound', NULL, NULL, NULL, 2, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (107, 101, '出库', 3, 'inventory:outbound', NULL, NULL, NULL, 3, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (108, 101, '锁定库存', 3, 'inventory:lock', NULL, NULL, NULL, 4, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (109, 101, '解锁库存', 3, 'inventory:unlock', NULL, NULL, NULL, 5, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (110, 101, '扣减库存', 3, 'inventory:deduct', NULL, NULL, NULL, 6, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (111, 101, '库存查询', 3, 'inventory:query', NULL, NULL, NULL, 7, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (112, 101, '编辑库存', 3, 'inventory:edit', NULL, NULL, NULL, 8, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (113, 102, '入库列表', 3, 'inventory:inbound:list', NULL, NULL, NULL, 1, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (114, 102, '查询入库', 3, 'inventory:inbound:query', NULL, NULL, NULL, 2, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (115, 102, '新增入库', 3, 'inventory:inbound:add', NULL, NULL, NULL, 3, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (116, 103, '出库列表', 3, 'inventory:outbound:list', NULL, NULL, NULL, 1, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (117, 103, '查询出库', 3, 'inventory:outbound:query', NULL, NULL, NULL, 2, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (118, 103, '新增出库', 3, 'inventory:outbound:add', NULL, NULL, NULL, 3, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (119, 103, '编辑出库', 3, 'inventory:outbound:edit', NULL, NULL, NULL, 4, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (120, 103, '删除出库', 3, 'inventory:outbound:remove', NULL, NULL, NULL, 5, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (121, 104, '仓库列表', 3, 'warehouse:list', NULL, NULL, NULL, 1, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (122, 104, '查询仓库', 3, 'warehouse:query', NULL, NULL, NULL, 2, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (123, 104, '新增仓库', 3, 'warehouse:add', NULL, NULL, NULL, 3, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (124, 104, '编辑仓库', 3, 'warehouse:edit', NULL, NULL, NULL, 4, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (125, 104, '删除仓库', 3, 'warehouse:remove', NULL, NULL, NULL, 5, 1, '2025-10-29 14:43:04', NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (126, 102, '修改入库', 3, 'inventory:inbound:edit', NULL, NULL, NULL, 4, 1, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (127, 102, '删除入库', 3, 'inventory:inbound:remove', NULL, NULL, NULL, 5, 1, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (201, 0, '数据统计', 2, NULL, 'statistics', '@/views/statistics/index.vue', 'TrendCharts', 2, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (210, 0, '推荐系统', 1, NULL, '/recommendation', NULL, 'DataAnalysis', 30, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (211, 210, '用户画像', 2, NULL, 'profile', '@/views/recommendation/profile/index.vue', 'User', 1, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (212, 210, '行为分析', 2, NULL, 'behavior', '@/views/recommendation/behavior/index.vue', 'Monitor', 2, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (213, 210, '推荐结果', 2, NULL, 'result', '@/views/recommendation/result/index.vue', 'Trophy', 3, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (214, 210, '推荐展示', 2, NULL, 'display', '@/views/recommendation/display/index.vue', 'Present', 4, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (220, 0, '溯源管理', 1, NULL, '/trace', NULL, 'Link', 40, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (221, 220, '溯源总览', 2, NULL, 'index', '@/views/trace/index.vue', 'Menu', 1, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (222, 220, '溯源记录', 2, NULL, 'records', '@/views/trace/records.vue', 'Document', 2, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (223, 220, '溯源码管理', 2, NULL, 'codes', '@/views/trace/codes.vue', 'Postcard', 3, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (224, 220, '溯源实体', 2, NULL, 'entities', '@/views/trace/entities.vue', 'OfficeBuilding', 4, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (225, 220, '溯源查询', 2, NULL, 'query', '@/views/trace/query.vue', 'Search', 5, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (226, 220, '区块链管理', 2, NULL, 'blockchain', '@/views/trace/blockchain.vue', 'Connection', 6, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (227, 220, '数据验证', 2, NULL, 'verify', '@/views/trace/verify.vue', 'Select', 7, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (230, 0, 'Excel处理', 1, NULL, '/excel', NULL, 'Files', 50, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (231, 230, '数据导入', 2, NULL, 'import', '@/views/excel/import/index.vue', 'Upload', 1, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (232, 230, '数据导出', 2, NULL, 'export', '@/views/excel/export/index.vue', 'Download', 2, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (240, 53, '支付管理', 2, NULL, 'pay', '@/views/wechat/pay/index.vue', 'CreditCard', 2, 1, '2025-11-26 18:47:00', 1, NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人用户名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理',
  `execute_time` bigint NULL DEFAULT NULL COMMENT '执行时间(毫秒)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation`(`operation` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1, '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_role` VALUES (2, '仓库管理员', 'WAREHOUSE_MANAGER', '管理仓库和库存', 1, '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);
INSERT INTO `sys_role` VALUES (3, '订单管理员', 'ORDER_MANAGER', '处理订单相关业务', 1, '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800138000', 'admin@seed.com', NULL, 1, NULL, NULL, '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2025-10-25 15:49:57', 1, '2025-10-25 15:49:57', 1, 0);

-- ----------------------------
-- Table structure for trace_code_config
-- ----------------------------
DROP TABLE IF EXISTS `trace_code_config`;
CREATE TABLE `trace_code_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地区代码',
  `region_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地区名称',
  `code_prefix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编码前缀',
  `current_number` bigint NOT NULL DEFAULT 1 COMMENT '当前编号',
  `code_format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编码格式(如：{prefix}{year}{number:06d})',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_region_code`(`region_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '溯源码生成规则配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of trace_code_config
-- ----------------------------
INSERT INTO `trace_code_config` VALUES (1, 'BJ', '北京市', 'BJ', 10, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-21 10:21:29', NULL, 0);
INSERT INTO `trace_code_config` VALUES (2, 'SH', '上海市', 'SH', 0, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_code_config` VALUES (3, 'GZ', '广州市', 'GZ', 0, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_code_config` VALUES (4, 'SZ', '深圳市', 'SZ', 0, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_code_config` VALUES (5, 'HZ', '杭州市', 'HZ', 0, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_code_config` VALUES (6, 'CD', '成都市', 'CD', 0, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_code_config` VALUES (7, 'WH', '武汉市', 'WH', 0, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_code_config` VALUES (8, 'XA', '西安市', 'XA', 0, '{prefix}{year}{number:06d}', 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);

-- ----------------------------
-- Table structure for trace_entity
-- ----------------------------
DROP TABLE IF EXISTS `trace_entity`;
CREATE TABLE `trace_entity`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `entity_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实体编码',
  `entity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实体名称',
  `entity_type` tinyint NOT NULL COMMENT '实体类型：1-生产基地，2-加工厂，3-仓库，4-经销商，5-零售商',
  `legal_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '法人代表',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `license_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `certification_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '资质认证信息(JSON)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_entity_code`(`entity_code` ASC) USING BTREE,
  INDEX `idx_entity_type`(`entity_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '溯源实体表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of trace_entity
-- ----------------------------
INSERT INTO `trace_entity` VALUES (1, 'FARM001', '北京优质农业基地', 1, NULL, '张农民', '13800138001', '北京市通州区农业示范园', NULL, NULL, NULL, NULL, 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_entity` VALUES (2, 'PROC001', '绿色种子加工厂', 2, NULL, '李加工', '13800138002', '北京市大兴区工业园区', NULL, NULL, NULL, NULL, 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_entity` VALUES (3, 'WARE001', '现代化仓储中心', 3, NULL, '王仓管', '13800138003', '北京市丰台区物流园', NULL, NULL, NULL, NULL, 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);
INSERT INTO `trace_entity` VALUES (4, 'DEAL001', '种子批发中心', 4, NULL, '赵批发', '13800138004', '北京市朝阳区批发市场', NULL, NULL, NULL, NULL, 1, '2025-11-18 19:37:01', NULL, '2025-11-18 19:37:01', NULL, 0);

-- ----------------------------
-- Table structure for trace_query
-- ----------------------------
DROP TABLE IF EXISTS `trace_query`;
CREATE TABLE `trace_query`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '溯源码',
  `query_user_id` bigint NULL DEFAULT NULL COMMENT '查询用户ID(如果已登录)',
  `query_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信用户openid',
  `query_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '查询IP地址',
  `query_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '查询地理位置',
  `query_device` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '查询设备信息',
  `query_channel` tinyint NOT NULL DEFAULT 1 COMMENT '查询渠道：1-小程序扫码，2-小程序输入，3-网页查询，4-APP查询',
  `query_time` datetime NOT NULL COMMENT '查询时间',
  `query_result` tinyint NOT NULL COMMENT '查询结果：1-成功，2-溯源码不存在，3-溯源码已过期，4-系统错误',
  `response_time` int NULL DEFAULT NULL COMMENT '响应时间(毫秒)',
  `user_feedback` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户反馈',
  `satisfaction_score` tinyint NULL DEFAULT NULL COMMENT '满意度评分：1-5分',
  `feedback_time` datetime NULL DEFAULT NULL COMMENT '反馈时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_code`(`trace_code` ASC) USING BTREE,
  INDEX `idx_query_user_id`(`query_user_id` ASC) USING BTREE,
  INDEX `idx_query_openid`(`query_openid` ASC) USING BTREE,
  INDEX `idx_query_time`(`query_time` ASC) USING BTREE,
  INDEX `idx_query_result`(`query_result` ASC) USING BTREE,
  INDEX `idx_query_channel`(`query_channel` ASC) USING BTREE,
  INDEX `idx_query_stat`(`query_time` ASC, `query_result` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消费者溯源查询记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of trace_query
-- ----------------------------

-- ----------------------------
-- Table structure for trace_record
-- ----------------------------
DROP TABLE IF EXISTS `trace_record`;
CREATE TABLE `trace_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '溯源码',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `record_type` tinyint NOT NULL COMMENT '记录类型：1-生产记录，2-质检记录，3-流通记录，4-销售记录，5-异常记录',
  `record_stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录阶段',
  `entity_id` bigint NULL DEFAULT NULL COMMENT '相关实体ID',
  `entity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相关实体名称',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人员',
  `operator_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人电话',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '位置信息',
  `temperature` decimal(5, 2) NULL DEFAULT NULL COMMENT '温度(℃)',
  `humidity` decimal(5, 2) NULL DEFAULT NULL COMMENT '湿度(%)',
  `quantity` decimal(12, 2) NULL DEFAULT NULL COMMENT '数量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `quality_grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '质量等级',
  `test_result` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '检测结果',
  `content_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容摘要',
  `detailed_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细内容(JSON格式)',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片URLs(JSON数组)',
  `attachment_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件URLs(JSON数组)',
  `blockchain_tx_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区块链交易哈希',
  `blockchain_status` tinyint NOT NULL DEFAULT 0 COMMENT '区块链状态：0-未上链，1-上链中，2-上链成功，3-上链失败',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_code`(`trace_code` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_record_type`(`record_type` ASC) USING BTREE,
  INDEX `idx_entity_id`(`entity_id` ASC) USING BTREE,
  INDEX `idx_record_time`(`record_time` ASC) USING BTREE,
  INDEX `idx_trace_record_time`(`trace_code` ASC, `record_time` ASC) USING BTREE,
  INDEX `idx_blockchain_status`(`blockchain_status` ASC) USING BTREE,
  INDEX `idx_blockchain_tx_hash`(`blockchain_tx_hash` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '溯源记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of trace_record
-- ----------------------------
INSERT INTO `trace_record` VALUES (1, 'BJ2025000006', 1, 1, '批次创建', NULL, NULL, '系统管理员', NULL, '2025-10-01 00:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '批次 B20251001001 生成溯源码', '{\"action\":\"生成溯源码\",\"batchNo\":\"B20251001001\",\"traceCode\":\"BJ2025000006\",\"productionDate\":\"2025-10-01\"}', NULL, NULL, 'adc24b9ff8b74a698ec8365d9c433fe672baada88ab149389d831e24fde336ec', 2, NULL, '2025-11-19 11:38:18', 1, '2025-11-19 12:26:36', 1, 0);
INSERT INTO `trace_record` VALUES (2, 'BJ2025000007', 2, 1, '批次创建', NULL, NULL, '系统管理员', NULL, '2025-10-02 00:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '批次 B20251002001 生成溯源码', '{\"action\":\"生成溯源码\",\"batchNo\":\"B20251002001\",\"traceCode\":\"BJ2025000007\",\"productionDate\":\"2025-10-02\"}', NULL, NULL, '45d0fc2e8de64d3a80abd84e0d4927a3c912d8d6c0664b918a0258dc6791f022', 2, NULL, '2025-11-19 11:42:49', 1, '2025-11-19 11:42:50', 1, 0);
INSERT INTO `trace_record` VALUES (3, 'BJ2025000008', 4, 1, '批次创建-生产记录', 1, '北京优质农业基地', '11', '11', '2025-11-20 00:00:00', '北京市通州区农业示范园', NULL, NULL, NULL, '千克', '优等', NULL, '批次 89764556 生成溯源码，生产商：北京优质农业基地，生产地点：北京市通州区农业示范园，质量等级：优等', '{\"action\":\"批次创建-生成溯源码\",\"batchNo\":\"89764556\",\"traceCode\":\"BJ2025000008\",\"seedId\":3,\"producerName\":\"北京优质农业基地\",\"productionLocation\":\"北京市通州区农业示范园\",\"productionDate\":\"2025-11-20\",\"harvestDate\":\"2025-11-01\",\"processingDate\":\"2025-11-06\",\"productionEquipment\":\"测试\",\"processingMethod\":\"测试\",\"seedSource\":\"测试\",\"parentVariety\":\"测试\",\"qualityGrade\":\"优等\",\"moistureContent\":10.00,\"germinationRate\":10.00,\"purity\":10.00,\"qualityStatus\":1,\"unit\":\"千克\",\"packagingType\":\"11\",\"packagingSpecification\":\"11\",\"storageCondition\":\"11\",\"certification\":\"11\",\"traceabilityLevel\":1,\"operatorName\":\"11\",\"operatorPhone\":\"11\"}', NULL, NULL, 'f7e59fe2d8ee4a39a2625b063b9c661d453d315f4899412e95d64d0972808b70', 2, NULL, '2025-11-21 10:06:38', 1, '2025-11-21 10:06:59', 1, 0);
INSERT INTO `trace_record` VALUES (4, 'BJ2025000009', 8, 1, '批次创建-生产记录', 1, '北京优质农业基地', 'linyi', '13112665356', '2025-11-20 00:00:00', '北京市通州区农业示范园', NULL, NULL, NULL, '斤', '优等', NULL, '批次 649798658789 生成溯源码，生产商：北京优质农业基地，生产地点：北京市通州区农业示范园，质量等级：优等', '{\"action\":\"批次创建-生成溯源码\",\"batchNo\":\"649798658789\",\"traceCode\":\"BJ2025000009\",\"seedId\":1,\"producerName\":\"北京优质农业基地\",\"productionLocation\":\"北京市通州区农业示范园\",\"productionDate\":\"2025-11-20\",\"harvestDate\":\"2025-11-11\",\"processingDate\":\"2025-11-18\",\"productionEquipment\":\"123\",\"processingMethod\":\"干燥加工\",\"seedSource\":\"123\",\"parentVariety\":\"123\",\"qualityGrade\":\"优等\",\"moistureContent\":11.00,\"germinationRate\":12.00,\"purity\":12.00,\"qualityStatus\":1,\"unit\":\"斤\",\"packagingType\":\"1213\",\"packagingSpecification\":\"123\",\"storageCondition\":\"123\",\"certification\":\"有机认证\",\"traceabilityLevel\":1,\"operatorName\":\"linyi\",\"operatorPhone\":\"13112665356\"}', NULL, NULL, 'aff2c28d246241cc8cff2a01680fd832a0f4b9001b2040b99e4c81da83bf37a5', 2, NULL, '2025-11-21 10:13:18', 1, '2025-11-21 10:13:21', 1, 0);
INSERT INTO `trace_record` VALUES (5, 'BJ2025000010', 9, 1, '批次创建-生产记录', 1, '北京优质农业基地', '测试', '13112558989', '2025-11-05 00:00:00', '北京市通州区农业示范园', NULL, NULL, NULL, 'kg', '一等', '含水率:12.00% 发芽率:12.00% 纯度:12.00%', '批次1212生成溯源码。生产商：北京优质农业基地；产地：北京市通州区农业示范园；生产日期：2025-11-05；质量等级：一等；质检数据：含水率12.00% 发芽率12.00% 纯度12.00%；认证：GAP认证；包装：测试(测试)；', '{\"action\":\"批次创建-生成溯源码\",\"batchNo\":\"1212\",\"traceCode\":\"BJ2025000010\",\"seedId\":2,\"producerName\":\"北京优质农业基地\",\"producerId\":1,\"productionLocation\":\"北京市通州区农业示范园\",\"productionDate\":\"2025-11-05\",\"expiryDate\":\"2025-11-25\",\"harvestDate\":\"2025-11-04\",\"processingDate\":\"2025-11-17\",\"productionEquipment\":\"测试\",\"processingMethod\":\"干燥加工\",\"seedSource\":\"测试\",\"parentVariety\":\"测试\",\"qualityGrade\":\"一等\",\"moistureContent\":12.00,\"germinationRate\":12.00,\"purity\":12.00,\"qualityStatus\":1,\"qualityReport\":\"http://117.72.101.170:9000/cropseed-trace/images/b1b2b531ec3a476f8d6db1663d71dc84.jpg\",\"unit\":\"kg\",\"packagingType\":\"测试\",\"packagingSpecification\":\"测试\",\"storageCondition\":\"测试\",\"certification\":\"GAP认证\",\"traceabilityLevel\":1,\"operatorName\":\"测试\",\"operatorPhone\":\"13112558989\",\"remarks\":\"测试\"}', NULL, NULL, '6876a8841ad44e39b9a4687416aea26f2b0114a9dd164212aca5fb2dc1cc7fbc', 2, NULL, '2025-11-21 10:21:29', 1, '2025-11-21 10:21:31', 1, 0);

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `consignee` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_default`(`is_default` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收货地址表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_address
-- ----------------------------
INSERT INTO `user_address` VALUES (1, 1, '张三', '13800138001', '北京市', '北京市', '朝阳区', '某某街道123号', 1, '2025-10-25 17:30:03', '2025-10-25 17:30:03', NULL, NULL, 0);
INSERT INTO `user_address` VALUES (2, 1, '李四', '13800138002', '上海市', '上海市', '浦东新区', '某某路456号', 0, '2025-10-25 17:30:03', '2025-10-25 17:30:03', NULL, NULL, 0);
INSERT INTO `user_address` VALUES (3, 2, '王五', '13800138003', '广东省', '深圳市', '南山区', '某某大道789号', 1, '2025-10-25 17:30:03', '2025-10-25 17:30:03', NULL, NULL, 0);
INSERT INTO `user_address` VALUES (1987037560129908736, 1986823631491473408, '11', '13112665250', '北京市', '市辖区', '东城区', '111', 1, '2025-11-08 14:00:46', '2025-11-08 14:32:05', '1986823631491473408', '1986823631491473408', 1);
INSERT INTO `user_address` VALUES (1987046214807764992, 1986823631491473408, '133213', '13112665250', '北京市', '市辖区', '东城区', '65465465', 0, '2025-11-08 14:35:10', '2025-11-08 14:35:59', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_address` VALUES (1987046371238526976, 1986823631491473408, '就好', '13112665369', '河北省', '石家庄市', '长安区', '1312313', 1, '2025-11-08 14:35:47', '2025-11-08 14:35:55', '1986823631491473408', '1986823631491473408', 0);

-- ----------------------------
-- Table structure for user_behavior
-- ----------------------------
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `behavior_type` tinyint NOT NULL COMMENT '行为类型：1-浏览，2-搜索，3-收藏，4-加购物车，5-购买，6-评价',
  `target_type` tinyint NOT NULL COMMENT '目标类型：1-种子，2-品类，3-品牌',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `behavior_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '行为内容',
  `behavior_weight` double NULL DEFAULT 1 COMMENT '行为权重：0-1之间的小数',
  `behavior_time` datetime NOT NULL COMMENT '行为时间',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID',
  `device_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备信息',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理',
  `referrer` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源页面',
  `duration` int NULL DEFAULT NULL COMMENT '停留时长（秒）',
  `rating` tinyint NULL DEFAULT NULL COMMENT '行为评分：1-5分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_behavior_type`(`behavior_type` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_behavior_time`(`behavior_time` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户行为记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_behavior
-- ----------------------------
INSERT INTO `user_behavior` VALUES (1, 1, 1, 1, 1, '浏览玉米种子', 1, '2025-10-25 18:01:59', 'session_001', NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (2, 1, 5, 1, 1, '购买玉米种子', 1, '2025-10-25 18:01:59', 'session_001', NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (3, 1, 6, 1, 1, '评价玉米种子', 1, '2025-10-25 18:01:59', 'session_001', NULL, NULL, NULL, NULL, NULL, 5, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (4, 2, 1, 1, 2, '浏览水稻种子', 1, '2025-10-25 18:01:59', 'session_002', NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (5, 2, 4, 1, 2, '加入购物车', 0.8, '2025-10-25 18:01:59', 'session_002', NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (6, 3, 1, 1, 3, '浏览蔬菜种子', 1, '2025-10-25 18:01:59', 'session_003', NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (7, 3, 5, 1, 3, '购买蔬菜种子', 1, '2025-10-25 18:01:59', 'session_003', NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (8, 3, 6, 1, 3, '评价蔬菜种子', 1, '2025-10-25 18:01:59', 'session_003', NULL, NULL, NULL, NULL, NULL, 4, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_behavior` VALUES (1989987759529811968, 1986823631491473408, 1, 1, 1, '首页推荐', 1, '2025-11-16 17:23:49', 'd21b5a480c9d4444b49605ab74d1d173', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:23:49', '2025-11-16 17:23:49', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989987769638084608, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 2, '2025-11-16 17:23:51', 'ce99996476d3492eae49c79e0df5bd26', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:23:51', '2025-11-16 17:23:51', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989987770174955520, 1986823631491473408, 4, 1, 1, '商品详情页-加购物车', 1, '2025-11-16 17:23:51', '1737aaf740074dc8a1bba1f25baf8590', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:23:51', '2025-11-16 17:23:51', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989987785328975872, 1986823631491473408, 1, 1, 1, '商品详情页-离开', 5, '2025-11-16 17:23:55', '61708b50fe554b7f9b17576c7f207429', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:23:55', '2025-11-16 17:23:55', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989987822402428928, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:24:04', '50a0c3805144490983251c46b9f5d03b', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:24:04', '2025-11-16 17:24:04', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989987982578704384, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:24:42', 'b58318e94e5a48aaa290736649efef7f', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:24:42', '2025-11-16 17:24:42', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988057438642176, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:25:00', 'e9a27a111a624ab5b51f82e4d4cab76f', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:25:00', '2025-11-16 17:25:00', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988116968398848, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:25:14', '95c6a2512a5c4372b978ef8f38a97175', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:25:14', '2025-11-16 17:25:14', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988149201625088, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:25:22', 'c00d703a8e6b40c0995816eb9cb40778', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:25:22', '2025-11-16 17:25:22', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988198979624960, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:25:33', '3f4483ec1eb545c4aae48af195759b56', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:25:33', '2025-11-16 17:25:33', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988243959341056, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:25:44', '5f9ed53deb4d4feda8dabae095ae7b2c', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:25:44', '2025-11-16 17:25:44', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988415015641088, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-16 17:26:25', '9f9ec58f7d494ae29aa43db06281364c', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:26:25', '2025-11-16 17:26:25', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988422246621184, 1986823631491473408, 5, 1, 1, '购物车-结算', 2, '2025-11-16 17:26:27', 'b3c6c27c00b7429d979dac611ede1d64', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:26:27', '2025-11-16 17:26:27', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988452370112512, 1986823631491473408, 5, 1, 1, '购物车-结算', 2, '2025-11-16 17:26:34', '6533e7bf1c304153816fbf232c6e173c', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:26:34', '2025-11-16 17:26:34', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988914892791808, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:28:24', 'f568d22436fd481c8b4c6ef00356fd2d', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:28:24', '2025-11-16 17:28:24', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989988972929376256, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:28:38', 'b1cfc9bcff134a3a9dbbd0bcc814debe', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:28:38', '2025-11-16 17:28:38', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989989052931530752, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 2, '2025-11-16 17:28:57', '2c8231a42ebb43539160a0ca35cdd42f', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:28:57', '2025-11-16 17:28:57', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989989068580478976, 1986823631491473408, 1, 1, 1, '商品详情页-离开', 5, '2025-11-16 17:29:01', '9d0a5cb9c48b45f7936fe57ebe1346d3', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:29:01', '2025-11-16 17:29:01', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989989086766981120, 1986823631491473408, 5, 1, 1, '购物车-结算', 2, '2025-11-16 17:29:05', 'e25d9a38f00e4c568225cabf95241e00', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:29:05', '2025-11-16 17:29:05', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989989086775369728, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-16 17:29:05', '55e041a0befd4ef99272ad41a81fccd2', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:29:05', '2025-11-16 17:29:05', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989990957183340544, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:36:31', '95cab281fc7348d2bac17f647605bd1d', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:31', '2025-11-16 17:36:31', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989990975109795840, 1986823631491473408, 1, 1, 2, '首页推荐', 1, '2025-11-16 17:36:35', '0f69d4d9b3c84ac6af4f0dadd9e69b7a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:35', '2025-11-16 17:36:35', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989990985377452032, 1986823631491473408, 1, 1, 2, '商品详情页-浏览', 2, '2025-11-16 17:36:38', '398bf6c0fb864a62871efcdce31bff13', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:38', '2025-11-16 17:36:38', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989990998379794432, 1986823631491473408, 1, 1, 2, '商品详情页-离开', 5, '2025-11-16 17:36:41', '661d05ba378745c897a2c9181583d63a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:41', '2025-11-16 17:36:41', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991003404570624, 1986823631491473408, 1, 1, 1, '首页推荐', 1, '2025-11-16 17:36:42', '4de62c79db5848e1b9fd79b2bedf3e04', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:42', '2025-11-16 17:36:42', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991013324099584, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 2, '2025-11-16 17:36:44', '89e6c25c8ebf4ceb95ae57038e203403', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:44', '2025-11-16 17:36:44', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991014540447744, 1986823631491473408, 5, 1, 1, '商品详情页-立即购买', 1, '2025-11-16 17:36:45', 'efa9380a079643099adcf36d266a3c1c', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:45', '2025-11-16 17:36:45', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991054277283840, 1986823631491473408, 1, 1, 1, '商品详情页-离开', 11, '2025-11-16 17:36:54', '66998a06d70a41e399db44c74b46561b', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:54', '2025-11-16 17:36:54', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991069582299136, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-16 17:36:58', '1a4a3f3728884af99596bc01f9040fea', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:58', '2025-11-16 17:36:58', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991070412771328, 1986823631491473408, 5, 1, 1, '购物车-结算', 1, '2025-11-16 17:36:58', '2205a8b7c3bf4a7d8a161ff0f39cf054', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:36:58', '2025-11-16 17:36:58', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991182476185600, 1986823631491473408, 1, 1, 1, '首页推荐', 1, '2025-11-16 17:37:25', '78cc3aa11c224daeb47ae6268ed87242', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:37:25', '2025-11-16 17:37:25', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991192458629120, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 2, '2025-11-16 17:37:27', '80a08a3f26e547a4a4911a43d3241e07', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:37:27', '2025-11-16 17:37:27', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991309974638592, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 30, '2025-11-16 17:37:55', 'd38eb8fb935e4fa8a50d37afa427143f', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:37:55', '2025-11-16 17:37:55', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991435875061760, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 60, '2025-11-16 17:38:25', '44549372affb4d01ab0274ade88306c5', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:38:25', '2025-11-16 17:38:25', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991461716168704, 1986823631491473408, 5, 1, 1, '商品详情页-立即购买', 1, '2025-11-16 17:38:31', 'bdcbf59a87e14083b1bcda45909b72a0', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:38:31', '2025-11-16 17:38:31', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991561649655808, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 90, '2025-11-16 17:38:55', '1a7e94bf9a8743fcbf19065ea0e9bdab', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:38:55', '2025-11-16 17:38:55', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991687529107456, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 120, '2025-11-16 17:39:25', '506b2560f5f9483fa440ff1d5c24502e', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:39:25', '2025-11-16 17:39:25', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991725013602304, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:39:34', '467a5476a26740dda789a02a80c8e445', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:39:34', '2025-11-16 17:39:34', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991783595446272, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:39:48', '3a5dcd97a54c41fbb3e58368d2057d8a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:39:48', '2025-11-16 17:39:48', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991819582574592, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:39:57', '6708b8ff6d13419a89c2dc7d8bb7d4b3', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:39:57', '2025-11-16 17:39:57', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991854575652864, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:40:05', '5b4119a78934404e93eebb5cd0918656', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:05', '2025-11-16 17:40:05', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991892437635072, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:40:14', '9ebaf7fb895f4f828be482c3108ae901', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:14', '2025-11-16 17:40:14', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991944933543936, 1986823631491473408, 1, 1, 1, '首页推荐', 1, '2025-11-16 17:40:27', '6f71d98991fb4fd999f41e95122d6caa', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:27', '2025-11-16 17:40:27', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991957394821120, 1986823631491473408, 5, 1, 1, '商品详情页-立即购买', 1, '2025-11-16 17:40:30', 'e106ddba46fc4560804529f456553313', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:30', '2025-11-16 17:40:30', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989991958728609792, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 2, '2025-11-16 17:40:30', 'a471ddebe7c14898a1213d3d9da18013', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:30', '2025-11-16 17:40:30', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989992005029531648, 1986823631491473408, 1, 1, 1, '商品详情页-离开', 13, '2025-11-16 17:40:41', 'e3d0e62f6aeb4f5c9c3ec79455292384', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:41', '2025-11-16 17:40:41', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989992028756709376, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:40:47', 'be657cd3fa74438d851d4087c2c7e267', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:47', '2025-11-16 17:40:47', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989992070829772800, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:40:57', 'd3e03b96479140a9b356807f14034732', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:40:57', '2025-11-16 17:40:57', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989993132735262720, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-16 17:45:10', '92ba52ce42d043239230064c73c49fe8', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:45:10', '2025-11-16 17:45:10', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989993151743848448, 1986823631491473408, 1, 1, 2, '首页推荐', 1, '2025-11-16 17:45:14', 'bbda438d7c434f918b944b8451e50ae4', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:45:14', '2025-11-16 17:45:14', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989993162183471104, 1986823631491473408, 1, 1, 2, '商品详情页-浏览', 2, '2025-11-16 17:45:17', '16517ff5ef0b4a458c04a1af3db2a15b', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:45:17', '2025-11-16 17:45:17', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989993164544864256, 1986823631491473408, 1, 1, 2, '商品详情页-离开', 2, '2025-11-16 17:45:17', '6bd77e54bf17422797719ba89b1a789b', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:45:17', '2025-11-16 17:45:17', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989993169221513216, 1986823631491473408, 1, 1, 1, '首页推荐', 1, '2025-11-16 17:45:18', 'a6d4ff5b84c04a14bd49dd4f8e9d3a1a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:45:18', '2025-11-16 17:45:18', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989993179208151040, 1986823631491473408, 1, 1, 1, '商品详情页-浏览', 2, '2025-11-16 17:45:21', 'e642d502ea424e139b6364df3c3120b2', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:45:21', '2025-11-16 17:45:21', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1989993183012384768, 1986823631491473408, 1, 1, 1, '商品详情页-离开', 2, '2025-11-16 17:45:22', '86e5aad752be47449e5b4fc58b5398b2', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-16 17:45:22', '2025-11-16 17:45:22', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990393452846092288, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 20:15:54', '1092ddcc02fc413d8781a0ed81ef5dc4', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 20:15:54', '2025-11-17 20:15:54', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990408567083380736, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:15:57', '8796625de5784f90b688cc3a28092e58', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:15:57', '2025-11-17 21:15:57', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990408611182292992, 1986823631491473408, 1, 1, 2, '首页推荐', 1, '2025-11-17 21:16:08', '516361f5257449c5bba92202773c7c58', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:16:08', '2025-11-17 21:16:08', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990408622066511872, 1986823631491473408, 1, 1, 2, '商品详情页-浏览', 2, '2025-11-17 21:16:10', 'c3a006cc858c4c69abeb206df8a8219b', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:16:10', '2025-11-17 21:16:10', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990408631075876864, 1986823631491473408, 1, 1, 2, '商品详情页-离开', 4, '2025-11-17 21:16:12', 'bcf5258d945b436db16021d833cf1dd9', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:16:12', '2025-11-17 21:16:12', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990408685266284544, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:16:25', '6325ecb6fe3a4f8385173e185aa13f62', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:16:25', '2025-11-17 21:16:25', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409100888256512, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:18:04', 'b4027937f4b44554b722d560da2afcee', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:18:04', '2025-11-17 21:18:04', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409129564712960, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:18:11', '05447b50c1f54a43bcd6dd8b38aee98a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:18:11', '2025-11-17 21:18:11', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409201559941120, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:18:28', '04a177848fcf4efbb8c53e6386d9baeb', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:18:28', '2025-11-17 21:18:28', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409249278537728, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:18:40', '78e76d6e64254b06bb4b53b3ffbcf7b5', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:18:40', '2025-11-17 21:18:40', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409339338633216, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:19:01', 'f2e9f8446c5443ae8d2f39c41cd541a4', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:19:01', '2025-11-17 21:19:01', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409371525722112, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:19:09', 'a33780378e924f8191485afae4094543', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:19:09', '2025-11-17 21:19:09', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409417155555328, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:19:20', 'f34ed889a491409a871c9c73acb72b9e', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:19:20', '2025-11-17 21:19:20', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409593261797376, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:20:02', '31de09f1a503476ca901505240f2c2b2', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:20:02', '2025-11-17 21:20:02', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990409643559890944, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:20:14', 'b2830065cb7c4137a0602b5692419058', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:20:14', '2025-11-17 21:20:14', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990412376539017216, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-17 21:31:05', '4a428f52e90f4237834583f2773aafe2', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:31:05', '2025-11-17 21:31:05', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990412649630150656, 1986823631491473408, 5, 1, 1, '购物车-结算', 1, '2025-11-17 21:32:10', '7b62709271b24d38820d67a0e0ea1f56', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:32:10', '2025-11-17 21:32:10', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990413140149809152, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:34:07', '0afd7646b69c42028a4c19f31aaca1a2', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:34:07', '2025-11-17 21:34:07', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990413203357970432, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:34:22', '2683bc6d74fe4cdab32dd5deb259c27a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:34:22', '2025-11-17 21:34:22', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990413271284723712, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:34:39', 'd8dd4f56e6cb4500a63f81017fd55d96', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:34:39', '2025-11-17 21:34:39', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990413507931549696, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:35:35', '4d656cf8e20943b3942f04e32dec2fd2', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:35:35', '2025-11-17 21:35:35', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990413516169162752, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-17 21:35:37', '806eadca6c274225a99b5d5d5fb00039', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:35:37', '2025-11-17 21:35:37', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990413519012900864, 1986823631491473408, 5, 1, 1, '购物车-结算', 1, '2025-11-17 21:35:38', 'e5ffa5054dc34bd8a58cfb6c8ad945bf', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:35:38', '2025-11-17 21:35:38', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990413882457731072, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:37:04', '382c669cf6b549dd91c0537fd4c7434a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:37:04', '2025-11-17 21:37:04', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990414187534626816, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-17 21:38:17', '60386cd7037e47f59dcd26b56b184be0', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:38:17', '2025-11-17 21:38:17', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990414252445675520, 1986823631491473408, 5, 1, 1, '购物车-结算', 1, '2025-11-17 21:38:33', 'ad7ae6eba64b4ac1bfa49af9ab087045', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:38:33', '2025-11-17 21:38:33', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990414253552971776, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-17 21:38:33', '46af10d6cc324f02a27daf08b544bd5f', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:38:33', '2025-11-17 21:38:33', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990414680373735424, 1986823631491473408, 1, 1, 2, '首页推荐', 1, '2025-11-17 21:40:15', '3b68095630274b3596f0dd43f550e27c', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:40:15', '2025-11-17 21:40:15', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990414691337646080, 1986823631491473408, 1, 1, 2, '商品详情页-浏览', 2, '2025-11-17 21:40:17', 'eac59c1c79814e04a8697dd6c474a3e9', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:40:17', '2025-11-17 21:40:17', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1990414702024732672, 1986823631491473408, 1, 1, 2, '商品详情页-离开', 4, '2025-11-17 21:40:20', 'f3dc0792568148d09e2c10d42702f3ec', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-17 21:40:20', '2025-11-17 21:40:20', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991779203294912512, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:02:22', '283def5e01d84923b55d3d620ef2021a', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:02:22', '2025-11-21 16:02:22', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991779266725371904, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-21 16:02:37', '95a272beb1af4b29806e7bae2f71d2fb', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:02:37', '2025-11-21 16:02:37', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991783057155645440, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:17:41', 'ea81a3d3280946ddbd0572272b5aa5e6', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:17:41', '2025-11-21 16:17:41', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991787069582041088, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:33:38', 'ef46f0a6a4fa43d6ae0c84d68a2dd407', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:33:38', '2025-11-21 16:33:38', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991788045059710976, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:37:30', '9ab778836f464800a09d17ee1ea880d4', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:37:30', '2025-11-21 16:37:30', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991788767599878144, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:40:22', '134de87b85114e1f975657c40d7938a0', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:40:22', '2025-11-21 16:40:22', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991788926266204160, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:41:00', 'cd23b0b749d643f38d9bef9a806f3fff', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:41:00', '2025-11-21 16:41:00', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991790203020656640, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:46:05', 'a44aa82d86624d1fb642d22c3aacbe1f', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:46:05', '2025-11-21 16:46:05', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991790229964865536, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:46:11', '61336b123bcb46e8881f9c854478cb31', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:46:11', '2025-11-21 16:46:11', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991791914594484224, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:52:53', 'f65815beec09477f84886a751f95c23e', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:52:53', '2025-11-21 16:52:53', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991792850335318016, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:56:36', '93fab72463364d22a667b8dc4d498e04', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:56:36', '2025-11-21 16:56:36', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991793354666819584, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 16:58:36', '1930eb57304d46d8ada9db132dc66005', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 16:58:36', '2025-11-21 16:58:36', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991793894016565248, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:00:45', '73e5fb87527541759d0cf02d849c68ca', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:00:45', '2025-11-21 17:00:45', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991794101089353728, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:01:34', '59f3712779cb4069a8b3573d883503f5', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:01:34', '2025-11-21 17:01:34', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991794384813047808, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:02:42', '86c76d8d00254a8b9d1456c65ec9fec8', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:02:42', '2025-11-21 17:02:42', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991796041248555008, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:09:17', '27dbcdee4b4b4ae08cb995670b7af1d0', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:09:17', '2025-11-21 17:09:17', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991797212331786240, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:13:56', 'ca659cecb5e5492596d3191886110cb3', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:13:56', '2025-11-21 17:13:56', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991797288047362048, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:14:14', 'f7445f3f686648bfb2b03dea39028616', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:14:14', '2025-11-21 17:14:14', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991797327608037376, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:14:23', '94130db932824c2fbf4a2caaa7c0b173', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:14:23', '2025-11-21 17:14:23', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991805565825073152, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-21 17:47:08', '432ae4d16bc44bb487ae8fd55494b474', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:47:08', '2025-11-21 17:47:08', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1991805635706372096, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-21 17:47:24', '61c72c05f8dd4924be437d2bb44e1ce7', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-21 17:47:24', '2025-11-21 17:47:24', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992143004255051776, 1986823631491473408, 1, 1, 0, '首页浏览', 5, '2025-11-22 16:07:59', '7d732ad854be4b228d6a1c3fcb547d31', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:07:59', '2025-11-22 16:07:59', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992147602986426368, 1986823631491473408, 1, 1, 2, '首页推荐', 1, '2025-11-22 16:26:16', 'e319fcb788e54a939833bcf0e791cd37', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:26:16', '2025-11-22 16:26:16', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992147613883228160, 1986823631491473408, 1, 1, 2, '商品详情页-浏览', 2, '2025-11-22 16:26:18', '52035b0d552a48219854277fdbfe0d33', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:26:18', '2025-11-22 16:26:18', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992147627678294016, 1986823631491473408, 1, 1, 2, '商品详情页-离开', 5, '2025-11-22 16:26:21', 'a63d49efd3164df382780624a005d96d', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:26:21', '2025-11-22 16:26:21', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992147631172149248, 1986823631491473408, 1, 1, 3, '首页推荐', 1, '2025-11-22 16:26:22', 'e929441bd24645b3963f8a247763ce4c', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:26:22', '2025-11-22 16:26:22', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992147641968287744, 1986823631491473408, 1, 1, 3, '商品详情页-浏览', 2, '2025-11-22 16:26:25', 'caef5da49af64476916ba4fb7cddc7ee', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:26:25', '2025-11-22 16:26:25', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992147691503017984, 1986823631491473408, 1, 1, 3, '商品详情页-离开', 13, '2025-11-22 16:26:37', 'e9a181befc594fd39de2aa9695d90f59', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:26:37', '2025-11-22 16:26:37', '1986823631491473408', '1986823631491473408', 0);
INSERT INTO `user_behavior` VALUES (1992147848885886976, 1986823631491473408, 1, 1, 0, '购物车页面', 3, '2025-11-22 16:27:14', '8a5223aaca4b46d5a1ff19ac78586327', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-22 16:27:14', '2025-11-22 16:27:14', '1986823631491473408', '1986823631491473408', 0);

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_type` tinyint NULL DEFAULT 1 COMMENT '用户类型：1-个人用户，2-企业用户',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `region` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地区',
  `planting_experience` tinyint NULL DEFAULT 1 COMMENT '种植经验：1-新手，2-有经验，3-专家',
  `planting_scale` tinyint NULL DEFAULT 1 COMMENT '种植规模：1-小规模，2-中规模，3-大规模',
  `main_crops` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主要作物类型',
  `purchase_preference` tinyint NULL DEFAULT 1 COMMENT '购买偏好：1-价格敏感，2-质量优先，3-品牌优先',
  `price_sensitivity` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '价格敏感度：0-1之间的小数',
  `quality_requirement` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '质量要求：0-1之间的小数',
  `brand_loyalty` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '品牌忠诚度：0-1之间的小数',
  `activity_level` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '活跃度：0-1之间的小数',
  `purchase_frequency` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '购买频率：0-1之间的小数',
  `user_tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户标签（JSON格式）',
  `recommendation_weight` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '推荐权重：0-1之间的小数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_type` ASC) USING BTREE,
  INDEX `idx_region`(`region` ASC) USING BTREE,
  INDEX `idx_activity_level`(`activity_level` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户画像表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_profile
-- ----------------------------
INSERT INTO `user_profile` VALUES (1, 1, 1, 35, 1, '北京市', 2, 2, '玉米,小麦', 2, 0.30, 0.80, 0.60, 0.70, 0.60, NULL, 0.75, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_profile` VALUES (2, 2, 1, 28, 2, '上海市', 1, 1, '水稻', 1, 0.70, 0.60, 0.40, 0.80, 0.40, NULL, 0.65, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_profile` VALUES (3, 3, 2, 45, 1, '广东省', 3, 3, '蔬菜,水果', 3, 0.20, 0.90, 0.80, 0.60, 0.80, NULL, 0.85, '2025-10-25 18:01:59', '2025-10-25 18:01:59', NULL, NULL, 0);
INSERT INTO `user_profile` VALUES (1989990959104331776, 1986823631491473408, 1, NULL, 0, '未知', 1, 1, NULL, 1, 0.91, 0.60, 0.50, 1.00, 1.00, NULL, 0.50, '2025-11-16 17:36:32', '2025-11-16 17:36:32', NULL, NULL, 0);

-- ----------------------------
-- Table structure for warehouse
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '仓库位置',
  `manager` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `capacity` decimal(12, 2) NULL DEFAULT NULL COMMENT '仓库容量',
  `used_capacity` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '已用容量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `warehouse_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '仓库类型（如：普通仓、冷藏仓、保税仓等）',
  `temperature_range` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '温度范围（如：-18℃~-10℃、常温）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建用户ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改用户ID',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_code`(`warehouse_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '仓库信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of warehouse
-- ----------------------------
INSERT INTO `warehouse` VALUES (1, 'ce', 'ce', '23132', 'linyi', '13112665356', 10.00, 0.00, 1, '普通仓', '15', '4452', '2025-11-02 13:12:32', NULL, '2025-11-02 14:22:14', NULL, 0);

-- ----------------------------
-- Table structure for wx_user
-- ----------------------------
DROP TABLE IF EXISTS `wx_user`;
CREATE TABLE `wx_user`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信openid',
  `unionid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信unionid',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_unionid`(`unionid` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '微信小程序用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_user
-- ----------------------------
INSERT INTO `wx_user` VALUES (1, 'test_openid_001', NULL, '测试用户1', 'https://example.com/avatar1.jpg', 1, NULL, NULL, NULL, NULL, 1, '2025-10-25 17:30:03', '2025-10-25 17:30:03', '2025-10-25 17:30:03', NULL, NULL, 0);
INSERT INTO `wx_user` VALUES (2, 'test_openid_002', NULL, '测试用户2', 'https://example.com/avatar2.jpg', 2, NULL, NULL, NULL, NULL, 1, '2025-10-25 17:30:03', '2025-10-25 17:30:03', '2025-10-25 17:30:03', NULL, NULL, 0);
INSERT INTO `wx_user` VALUES (1986823631491473408, 'oWKkl7VWjko_73U90jDtwjvpYbFI', NULL, '林一', 'http://117.72.101.170:9000/cropseed-trace/7d26886dd20843f6985b87a7726ed5c8.jpg', 1, '中国', '广东省', '广州市', '13112665250', 1, '2025-11-22 16:30:03', '2025-11-07 23:50:42', '2025-11-07 23:50:42', NULL, '1986823631491473408', 0);

SET FOREIGN_KEY_CHECKS = 1;
