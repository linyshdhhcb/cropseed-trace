package com.linyi.cropseed.trace.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Date;

/**
 * ID生成器
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public class IdGenerator {

    /**
     * 生成订单号
     * 格式：ORD + yyyyMMddHHmmss + 6位随机数
     */
    public static String generateOrderNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(6);
        return "ORD" + dateStr + random;
    }

    /**
     * 生成批次号
     * 格式：前缀 + yyyyMMdd + 3位随机数
     */
    public static String generateBatchNo(String prefix) {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String random = RandomUtil.randomNumbers(3);
        return prefix + dateStr + random;
    }

    /**
     * 生成入库单号
     * 格式：IN + yyyyMMddHHmmss + 4位随机数
     */
    public static String generateInboundNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(4);
        return "IN" + dateStr + random;
    }

    /**
     * 生成出库单号
     * 格式：OUT + yyyyMMddHHmmss + 4位随机数
     */
    public static String generateOutboundNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(4);
        return "OUT" + dateStr + random;
    }

    /**
     * 生成售后单号
     * 格式：AS + yyyyMMddHHmmss + 4位随机数
     */
    public static String generateAfterSalesNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(4);
        return "AS" + dateStr + random;
    }
}
