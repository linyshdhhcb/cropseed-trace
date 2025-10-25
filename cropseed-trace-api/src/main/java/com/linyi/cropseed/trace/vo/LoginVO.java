package com.linyi.cropseed.trace.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录VO
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "登录VO")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;
}
