package com.linyi.cropseed.trace.module.seed.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.constant.SeedConstant;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedCategory;
import com.linyi.cropseed.trace.module.seed.mapper.SeedCategoryMapper;
import com.linyi.cropseed.trace.module.seed.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.module.seed.service.SeedCategoryService;
import com.linyi.cropseed.trace.module.seed.model.vo.SeedCategoryTreeVO;
import com.linyi.cropseed.trace.module.seed.model.vo.SeedCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * 种子品类服务实现
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
public class SeedCategoryServiceImpl extends ServiceImpl<SeedCategoryMapper, SeedCategory>
        implements SeedCategoryService {

    private final SeedInfoMapper seedInfoMapper;

    @Override
    public List<SeedCategoryTreeVO> getCategoryTree() {
        // 查询所有品类
        List<SeedCategory> allCategories = baseMapper.selectAllCategories();

        // 转换为Map，便于查找
        Map<Long, SeedCategory> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(SeedCategory::getId, category -> category));

        // 构建树形结构
        List<SeedCategoryTreeVO> treeList = new ArrayList<>();
        for (SeedCategory category : allCategories) {
            if (SeedConstant.CATEGORY_ROOT_ID.equals(category.getParentId())) {
                SeedCategoryTreeVO treeVO = convertToTreeVO(category);
                buildChildren(treeVO, allCategories, categoryMap);
                treeList.add(treeVO);
            }
        }

        return treeList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(SeedCategory category) {
        // 检查品类编码是否重复
        if (getCategoryByCode(category.getCategoryCode()) != null) {
            throw new BusinessException(ResultCode.SEED_CATEGORY_NOT_EXIST);
        }

        // 设置层级和路径
        if (category.getParentId() == null || SeedConstant.CATEGORY_ROOT_ID.equals(category.getParentId())) {
            category.setParentId(SeedConstant.CATEGORY_ROOT_ID);
            category.setLevel(SeedConstant.CATEGORY_LEVEL_1);
            category.setPath(category.getCategoryCode());
        } else {
            SeedCategory parentCategory = getById(category.getParentId());
            if (parentCategory == null) {
                throw new BusinessException(ResultCode.SEED_CATEGORY_NOT_EXIST);
            }
            category.setLevel(parentCategory.getLevel() + 1);
            category.setPath(buildCategoryPath(category.getParentId()) + "." + category.getCategoryCode());
        }

        this.save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(SeedCategory category) {
        SeedCategory existCategory = getById(category.getId());
        if (existCategory == null) {
            throw new BusinessException(ResultCode.SEED_CATEGORY_NOT_EXIST);
        }

        // 检查品类编码是否被其他品类使用
        SeedCategory categoryByCode = getCategoryByCode(category.getCategoryCode());
        if (categoryByCode != null && !categoryByCode.getId().equals(category.getId())) {
            throw new BusinessException(ResultCode.SEED_CATEGORY_NOT_EXIST);
        }

        this.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        SeedCategory category = getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.SEED_CATEGORY_NOT_EXIST);
        }

        // 检查是否有子品类
        List<SeedCategory> children = getCategoriesByParentId(id);
        if (CollUtil.isNotEmpty(children)) {
            throw new BusinessException("该品类下还有子品类，无法删除");
        }

        this.removeById(id);
    }

    @Override
    public SeedCategory getCategoryById(Long id) {
        return this.getById(id);
    }

    @Override
    public SeedCategory getCategoryByCode(String categoryCode) {
        return baseMapper.selectByCategoryCode(categoryCode);
    }

    @Override
    public List<SeedCategory> getCategoriesByParentId(Long parentId) {
        return baseMapper.selectCategoriesByParentId(parentId);
    }

    @Override
    public String buildCategoryPath(Long categoryId) {
        List<String> pathList = new ArrayList<>();
        SeedCategory category = getById(categoryId);

        while (category != null && !SeedConstant.CATEGORY_ROOT_ID.equals(category.getParentId())) {
            pathList.add(0, category.getCategoryCode());
            category = getById(category.getParentId());
        }

        return String.join(".", pathList);
    }

    /**
     * 构建子节点
     */
    private void buildChildren(SeedCategoryTreeVO parent, List<SeedCategory> allCategories,
            Map<Long, SeedCategory> categoryMap) {
        List<SeedCategoryTreeVO> children = new ArrayList<>();

        for (SeedCategory category : allCategories) {
            if (parent.getId().equals(category.getParentId())) {
                SeedCategoryTreeVO childVO = convertToTreeVO(category);
                buildChildren(childVO, allCategories, categoryMap);
                children.add(childVO);
            }
        }

        parent.setChildren(children);
    }

    /**
     * 转换为树形VO
     */
    private SeedCategoryTreeVO convertToTreeVO(SeedCategory category) {
        SeedCategoryTreeVO treeVO = new SeedCategoryTreeVO();
        treeVO.setId(category.getId());
        treeVO.setParentId(category.getParentId());
        treeVO.setCategoryCode(category.getCategoryCode());
        treeVO.setCategoryName(category.getCategoryName());
        treeVO.setLevel(category.getLevel());
        treeVO.setPath(category.getPath());
        treeVO.setDescription(category.getDescription());
        treeVO.setSort(category.getSort());
        treeVO.setStatus(category.getStatus());

        // 统计该分类下的商品数量（只统计上架的商品）
        int productCount = seedInfoMapper.selectByCategoryId(category.getId())
                .stream()
                .mapToInt(seedInfo -> seedInfo.getStatus() == 1 ? 1 : 0)
                .sum();
        treeVO.setProductCount(productCount);

        return treeVO;
    }

    @Override
    public List<SeedCategoryVO> getCategoryList(String categoryName, Integer status) {
        LambdaQueryWrapper<SeedCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(categoryName), SeedCategory::getCategoryName, categoryName)
                .eq(status != null, SeedCategory::getStatus, status)
                .orderByAsc(SeedCategory::getSort);

        List<SeedCategory> categories = baseMapper.selectList(queryWrapper);
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void batchDeleteCategories(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        baseMapper.deleteBatchIds(ids);
    }

    private SeedCategoryVO convertToVO(SeedCategory category) {
        SeedCategoryVO vo = new SeedCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
