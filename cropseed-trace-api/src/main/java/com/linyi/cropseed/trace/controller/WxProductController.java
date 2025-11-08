package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.mapper.SeedFeaturesMapper;
import com.linyi.cropseed.trace.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.service.SeedCategoryService;
import com.linyi.cropseed.trace.service.SeedInfoService;
import com.linyi.cropseed.trace.vo.SeedCategoryTreeVO;
import com.linyi.cropseed.trace.vo.SeedInfoVO;
import com.linyi.cropseed.trace.vo.WxProductDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信小程序商品接口
 */
@Tag(name = "微信商品", description = "微信小程序商品接口")
@RestController
@RequestMapping("/wx/product")
@RequiredArgsConstructor
public class WxProductController {

    private final SeedInfoMapper seedInfoMapper;
    private final SeedInfoService seedInfoService;
    private final SeedFeaturesMapper seedFeaturesMapper;
    private final SeedCategoryService seedCategoryService;

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<WxProductDetailVO> detail(@PathVariable Long id) {
        WxProductDetailVO detailVO = seedInfoMapper.selectDetailById(id);
        if (detailVO == null) {
            return Result.<WxProductDetailVO>fail(ResultCode.SEED_NOT_EXIST);
        }

        detailVO.setFeatures(seedFeaturesMapper.selectBySeedId(id));
        if (detailVO.getImageUrl() != null) {
            List<String> images = Arrays.stream(detailVO.getImageUrl().split(","))
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .collect(Collectors.toList());
            detailVO.setImageList(images);
        }
        return Result.success(detailVO);
    }

    @Operation(summary = "获取品类树形结构")
    @GetMapping("/category/tree")
    public Result<List<SeedCategoryTreeVO>> getCategoryTree() {
        List<SeedCategoryTreeVO> tree = seedCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "获取商品列表（分页）")
    @GetMapping("/page")
    public Result<PageResult<SeedInfoVO>> page(PageQuery pageQuery,
            @RequestParam(required = false) String seedName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        // 小程序只查询上架的商品
        PageResult<SeedInfoVO> result = seedInfoService.pageSeedInfos(pageQuery, seedName, null, categoryId, 1);
        return Result.success(result);
    }
}
