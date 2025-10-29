package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.SeedCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 种子品类Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface SeedCategoryMapper extends BaseMapper<SeedCategory> {

    /**
     * 查询所有品类（树形结构）
     */
    @Select("SELECT * FROM seed_category WHERE deleted_flag = 0 AND status = 1 ORDER BY sort ASC, id ASC")
    List<SeedCategory> selectAllCategories();

    /**
     * 根据父ID查询子品类
     */
    @Select("SELECT * FROM seed_category WHERE parent_id = #{parentId} AND deleted_flag = 0 AND status = 1 ORDER BY sort ASC")
    List<SeedCategory> selectCategoriesByParentId(Long parentId);

    /**
     * 根据品类编码查询品类
     */
    @Select("SELECT * FROM seed_category WHERE category_code = #{categoryCode} AND deleted_flag = 0")
    SeedCategory selectByCategoryCode(String categoryCode);
}
