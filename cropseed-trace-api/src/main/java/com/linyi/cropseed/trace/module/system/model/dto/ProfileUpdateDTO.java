package com.linyi.cropseed.trace.module.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人信息更新DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "个人信息更新DTO")
public class ProfileUpdateDTO {

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Schema(description = "真实姓名")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;
}
