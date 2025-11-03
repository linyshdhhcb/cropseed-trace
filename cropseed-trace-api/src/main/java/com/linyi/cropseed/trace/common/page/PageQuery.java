package com.linyi.cropseed.trace.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "分页查询参数")
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "create_time")
    private String orderBy;

    /**
     * 排序方式：asc/desc
     */
    @Schema(description = "排序方式", example = "desc")
    private String sortOrder = "desc";

    /**
     * 转换为MyBatis-Plus的Page对象
     */
    public <T> Page<T> toMpPage() {
        // 限制每页最大记录数
        if (pageSize > 200) {
            pageSize = 200;
        }
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 转换为MyBatis-Plus的Page对象（带排序）
     */
    public <T> Page<T> toMpPageWithOrder() {
        Page<T> page = toMpPage();
        if (orderBy != null && !orderBy.isEmpty()) {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                page.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.asc(orderBy));
            } else {
                page.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.desc(orderBy));
            }
        }
        return page;
    }
}
