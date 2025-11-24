package com.linyi.cropseed.trace.module.wx.model.vo;

import com.linyi.cropseed.trace.module.recommendation.model.entity.SeedFeatures;
import com.linyi.cropseed.trace.module.seed.model.vo.SeedInfoVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 小程序商品详情 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxProductDetailVO extends SeedInfoVO {

    private SeedFeatures features;

    private List<String> imageList;

    private Integer totalStock;

    private Integer availableStock;
}
