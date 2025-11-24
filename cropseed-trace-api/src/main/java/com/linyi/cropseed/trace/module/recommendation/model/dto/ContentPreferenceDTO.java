package com.linyi.cropseed.trace.module.recommendation.model.dto;

import java.util.List;

/**
 * 内容偏好分析类
 */
public class ContentPreferenceDTO {
        private double avgPriceRange;
        private double avgQualityRange;
        private List<String> preferredBrands;

        public ContentPreferenceDTO(double avgPriceRange, double avgQualityRange, List<String> preferredBrands) {
            this.avgPriceRange = avgPriceRange;
            this.avgQualityRange = avgQualityRange;
            this.preferredBrands = preferredBrands;
        }

        public double getAvgPriceRange() { return avgPriceRange; }
        public double getAvgQualityRange() { return avgQualityRange; }
        public List<String> getPreferredBrands() { return preferredBrands; }
    }
