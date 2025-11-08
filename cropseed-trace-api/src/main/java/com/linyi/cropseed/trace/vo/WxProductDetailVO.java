package com.linyi.cropseed.trace.vo;

import com.linyi.cropseed.trace.entity.SeedFeatures;
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

    private String specifications;
}
