package com.linyi.cropseed.trace.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果对象
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> list;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Long total;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Long pageNum;

    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数")
    private Long pageSize;

    /**
     * 总页数
     */
    @Schema(description = "总页数")
    private Long totalPages;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Long pageNum, Long pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }

    /**
     * 从MyBatis-Plus的Page对象转换
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize());
    }

    /**
     * 从MyBatis-Plus的Page对象转换（自定义数据列表）
     */
    public static <T> PageResult<T> of(Page<?> page, List<T> list) {
        return new PageResult<>(
                list,
                page.getTotal(),
                page.getCurrent(),
                page.getSize());
    }
}
