package com.linyi.cropseed.trace.module.seed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedCategory;
import com.linyi.cropseed.trace.module.seed.model.vo.SeedCategoryTreeVO;
import com.linyi.cropseed.trace.module.seed.model.vo.SeedCategoryVO;

import java.util.List;

/**
 * 种子品类服务接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface SeedCategoryService extends IService<SeedCategory> {

    /**
     * 获取品类树形结构
     */
    List<SeedCategoryTreeVO> getCategoryTree();

    /**
     * 新增品类
     */
    void addCategory(SeedCategory category);

    /**
     * 修改品类
     */
    void updateCategory(SeedCategory category);

    /**
     * 删除品类
     */
    void deleteCategory(Long id);

    /**
     * 根据ID查询品类详情
     */
    SeedCategory getCategoryById(Long id);

    /**
     * 根据品类编码查询品类
     */
    SeedCategory getCategoryByCode(String categoryCode);

    /**
     * 根据父ID查询子品类
     */
    List<SeedCategory> getCategoriesByParentId(Long parentId);

    /**
     * 构建品类路径
     */
    String buildCategoryPath(Long categoryId);

    /**
     * 获取品类列表
     */
    List<SeedCategoryVO> getCategoryList(String categoryName, Integer status);

    /**
     * 批量删除品类
     */
    void batchDeleteCategories(List<Long> ids);
}
