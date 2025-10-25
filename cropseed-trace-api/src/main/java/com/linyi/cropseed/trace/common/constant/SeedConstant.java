package com.linyi.cropseed.trace.common.constant;

/**
 * 种子相关常量
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface SeedConstant {

    /**
     * 种子状态 - 下架
     */
    Integer SEED_STATUS_OFF = 0;

    /**
     * 种子状态 - 上架
     */
    Integer SEED_STATUS_ON = 1;

    /**
     * 批次号前缀（可配置）
     */
    String BATCH_NO_PREFIX = "SP";

    /**
     * 质检状态 - 不合格
     */
    Integer QUALITY_STATUS_FAIL = 0;

    /**
     * 质检状态 - 合格
     */
    Integer QUALITY_STATUS_PASS = 1;

    /**
     * 品类层级 - 一级
     */
    Integer CATEGORY_LEVEL_1 = 1;

    /**
     * 品类层级 - 二级
     */
    Integer CATEGORY_LEVEL_2 = 2;

    /**
     * 品类层级 - 三级
     */
    Integer CATEGORY_LEVEL_3 = 3;

    /**
     * 品类根节点ID
     */
    Long CATEGORY_ROOT_ID = 0L;
}
