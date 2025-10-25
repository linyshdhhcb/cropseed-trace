package com.linyi.cropseed.trace.common.constant;

/**
 * 通用常量
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface CommonConstant {

    /**
     * 状态 - 启用
     */
    Integer STATUS_ENABLE = 1;

    /**
     * 状态 - 禁用
     */
    Integer STATUS_DISABLE = 0;

    /**
     * 删除标记 - 未删除
     */
    Integer DELETED_FLAG_NO = 0;

    /**
     * 删除标记 - 已删除
     */
    Integer DELETED_FLAG_YES = 1;

    /**
     * 是 - 1
     */
    Integer YES = 1;

    /**
     * 否 - 0
     */
    Integer NO = 0;

    /**
     * 默认页码
     */
    Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页记录数
     */
    Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页记录数
     */
    Integer MAX_PAGE_SIZE = 50;

    /**
     * 超级管理员角色编码
     */
    String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    /**
     * JWT Token前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT Token Header名称
     */
    String TOKEN_HEADER = "Authorization";

    /**
     * Redis Key前缀 - 验证码
     */
    String REDIS_KEY_CAPTCHA = "captcha:";

    /**
     * Redis Key前缀 - 用户Token
     */
    String REDIS_KEY_USER_TOKEN = "user:token:";

    /**
     * Redis Key前缀 - 用户权限
     */
    String REDIS_KEY_USER_PERMISSION = "user:permission:";

    /**
     * Redis Key前缀 - 系统配置
     */
    String REDIS_KEY_SYS_CONFIG = "sys:config:";

    /**
     * Redis Key前缀 - 库存锁定
     */
    String REDIS_KEY_INVENTORY_LOCK = "inventory:lock:";

    /**
     * Redis Key前缀 - 推荐结果
     */
    String REDIS_KEY_RECOMMEND = "recommend:";

    /**
     * 日期格式
     */
    String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 文件上传路径
     */
    String UPLOAD_PATH = "/upload/";

    /**
     * Excel导入最大行数
     */
    Integer EXCEL_MAX_ROWS = 10000;
}
