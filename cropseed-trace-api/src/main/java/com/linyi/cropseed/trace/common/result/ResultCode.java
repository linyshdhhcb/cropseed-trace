package com.linyi.cropseed.trace.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果码枚举
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 通用结果码
    SUCCESS(200, "操作成功"),
    FAIL(400, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    // 参数验证相关
    PARAM_VALID_ERROR(1001, "参数校验失败"),
    PARAM_IS_BLANK(1002, "参数为空"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),
    PARAM_NOT_COMPLETE(1004, "参数缺失"),

    // 用户相关
    USER_NOT_EXIST(2001, "用户不存在"),
    USER_ALREADY_EXIST(2002, "用户已存在"),
    USER_LOCKED(2003, "用户已被锁定"),
    USER_PASSWORD_ERROR(2004, "密码错误"),
    USER_NOT_LOGIN(2005, "用户未登录"),
    USER_LOGIN_EXPIRED(2006, "登录已过期"),
    USER_NO_PERMISSION(2007, "用户无权限"),
    USER_ACCOUNT_FORBIDDEN(2008, "账号已被禁用"),

    // 角色相关
    ROLE_NOT_EXIST(2010, "角色不存在"),
    ROLE_ALREADY_EXIST(2011, "角色已存在"),
    ROLE_CANNOT_DELETE(2012, "角色不允许删除"),

    // 业务相关 - 种子
    SEED_NOT_EXIST(3001, "种子信息不存在"),
    SEED_CODE_DUPLICATE(3002, "种子编码重复"),
    SEED_NAME_DUPLICATE(3003, "种子名称重复"),
    SEED_CATEGORY_NOT_EXIST(3004, "种子品类不存在"),
    SEED_BATCH_NOT_EXIST(3005, "种子批次不存在"),
    SEED_BATCH_NO_DUPLICATE(3006, "批次号重复"),

    // 业务相关 - 库存
    WAREHOUSE_NOT_EXIST(4001, "仓库不存在"),
    WAREHOUSE_CODE_DUPLICATE(4002, "仓库编码重复"),
    WAREHOUSE_HAS_INVENTORY(4007, "仓库存在库存记录，无法删除"),
    INVENTORY_NOT_EXIST(4003, "库存不存在"),
    INVENTORY_NOT_ENOUGH(4004, "库存不足"),
    INVENTORY_LOCKED(4005, "库存已锁定"),
    INVENTORY_OPERATION_FAIL(4006, "库存操作失败"),

    // 业务相关 - 订单
    ORDER_NOT_EXIST(5001, "订单不存在"),
    ORDER_STATUS_ERROR(5002, "订单状态错误"),
    ORDER_ALREADY_PAID(5003, "订单已支付"),
    ORDER_ALREADY_CANCEL(5004, "订单已取消"),
    ORDER_CREATE_FAIL(5005, "订单创建失败"),
    ORDER_PAY_FAIL(5006, "订单支付失败"),

    // 业务相关 - 文件
    FILE_UPLOAD_FAIL(6001, "文件上传失败"),
    FILE_DOWNLOAD_FAIL(6002, "文件下载失败"),
    FILE_DELETE_FAIL(6003, "文件删除失败"),
    FILE_TYPE_ERROR(6004, "文件类型错误"),
    FILE_SIZE_EXCEED(6005, "文件大小超限"),

    // 业务相关 - Excel
    EXCEL_IMPORT_FAIL(7001, "Excel导入失败"),
    EXCEL_EXPORT_FAIL(7002, "Excel导出失败"),
    EXCEL_TEMPLATE_ERROR(7003, "Excel模板错误"),
    EXCEL_DATA_ERROR(7004, "Excel数据错误"),

    // 业务相关 - 微信
    WECHAT_AUTH_FAIL(8001, "微信授权失败"),
    WECHAT_PAY_FAIL(8002, "微信支付失败"),
    WECHAT_REFUND_FAIL(8003, "微信退款失败"),
    OPENID_NOT_EXIST(8004, "OpenID不存在"),
    WX_LOGIN_FAILED(8005, "微信登录失败"),
    WX_USER_NOT_EXIST(8006, "微信用户不存在"),
    WX_PAY_NOTIFY_FAILED(8007, "微信支付回调失败"),

    // 业务相关 - 用户地址
    ADDRESS_NOT_EXIST(8501, "地址不存在"),
    ADDRESS_ALREADY_DEFAULT(8502, "地址已是默认地址"),

    // 业务相关 - 支付宝支付
    ALIPAY_FAILED(8601, "支付宝支付失败"),
    ALIPAY_REFUND_FAILED(8602, "支付宝退款失败"),
    ALIPAY_QUERY_FAILED(8603, "支付宝查询失败"),

    // 业务相关 - 推荐系统
    RECOMMENDATION_FAILED(8701, "推荐系统失败"),
    USER_PROFILE_NOT_EXIST(8702, "用户画像不存在"),
    RECOMMENDATION_NOT_EXIST(8703, "推荐结果不存在"),

    // 业务相关 - 数据
    DATA_ALREADY_EXIST(9001, "数据已存在"),
    DATA_NOT_EXIST(9002, "数据不存在"),
    DATA_IS_USING(9003, "数据使用中，无法删除"),
    DATA_OPERATION_FAIL(9004, "数据操作失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;
}
