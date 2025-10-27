package com.linyi.cropseed.trace.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统角色VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SysRoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
