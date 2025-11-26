package com.linyi.cropseed.trace.common.constant;

/**
 * 缓存常量
 *
 * @author LinYi
 * @since 2025-11-26
 */
public class CacheConstants {

    /**
     * 缓存名称前缀
     */
    public static final String CACHE_PREFIX = "cropseed:";

    // ==================== 系统模块缓存 ====================
    
    /**
     * 用户信息缓存
     * key: user:{userId} 或 user:username:{username}
     * 过期时间: 30分钟
     */
    public static final String CACHE_USER = "user";
    
    /**
     * 用户角色缓存
     * key: user:roles:{userId}
     * 过期时间: 30分钟
     */
    public static final String CACHE_USER_ROLES = "user:roles";
    
    /**
     * 用户权限缓存
     * key: user:permissions:{userId}
     * 过期时间: 30分钟
     */
    public static final String CACHE_USER_PERMISSIONS = "user:permissions";
    
    /**
     * 角色信息缓存
     * key: role:{roleId}
     * 过期时间: 30分钟
     */
    public static final String CACHE_ROLE = "role";
    
    /**
     * 角色菜单缓存
     * key: role:menus:{roleId}
     * 过期时间: 30分钟
     */
    public static final String CACHE_ROLE_MENUS = "role:menus";
    
    /**
     * 所有角色列表缓存
     * key: role:all
     * 过期时间: 1小时
     */
    public static final String CACHE_ROLE_ALL = "role:all";
    
    /**
     * 菜单信息缓存
     * key: menu:{menuId}
     * 过期时间: 1小时
     */
    public static final String CACHE_MENU = "menu";
    
    /**
     * 菜单列表缓存
     * key: menu:list:{条件hash}
     * 过期时间: 30分钟
     */
    public static final String CACHE_MENU_LIST = "menu:list";
    
    /**
     * 菜单树缓存
     * key: menu:tree
     * 过期时间: 1小时
     */
    public static final String CACHE_MENU_TREE = "menu:tree";

    // ==================== 缓存过期时间（秒） ====================
    
    /**
     * 默认过期时间: 30分钟
     */
    public static final long DEFAULT_EXPIRE_TIME = 30 * 60;
    
    /**
     * 短期过期时间: 5分钟
     */
    public static final long SHORT_EXPIRE_TIME = 5 * 60;
    
    /**
     * 长期过期时间: 1小时
     */
    public static final long LONG_EXPIRE_TIME = 60 * 60;
    
    /**
     * 超长期过期时间: 24小时
     */
    public static final long SUPER_LONG_EXPIRE_TIME = 24 * 60 * 60;
}
