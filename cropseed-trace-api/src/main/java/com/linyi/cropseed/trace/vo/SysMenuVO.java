package com.linyi.cropseed.trace.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统菜单VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SysMenuVO {
    private Long id;
    private String menuName;
    private Long parentId;
    private String path;
    private String component;
    private Integer menuType;
    private String icon;
    private Integer sort;
    private Integer status;
    private Integer visible;
    private String perms;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SysMenuVO> children;
}
