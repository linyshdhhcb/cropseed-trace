package com.linyi.cropseed.trace.dto.Recommend;

import com.linyi.cropseed.trace.vo.RecommendationVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 混合推荐项目类
 */
public class HybridRecommendationItemDTO {
    private RecommendationVO recommendationVO;
    private double finalScore;
    private List<String> algorithms;
    private String reason;

    public HybridRecommendationItemDTO(RecommendationVO vo) {
        this.recommendationVO = vo;
        this.finalScore = 0.0;
        this.algorithms = new ArrayList<>();
        this.reason = "";
    }

    public void addAlgorithmScore(String algorithm, double weight, double score) {
        finalScore += weight * score;
        algorithms.add(algorithm);
        if (reason.isEmpty()) {
            reason = algorithm;
        } else {
            reason += "+" + algorithm;
        }
    }

    public RecommendationVO getRecommendationVO() {
        return recommendationVO;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public String getReason() {
        return reason;
    }
}
