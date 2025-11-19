package com.linyi.cropseed.trace.service;

/**
 * 溯源码生成服务接口
 * 
 * @author linyi
 * @since 2024-01-01
 */
public interface TraceCodeService {
    
    /**
     * 生成溯源码
     * @param regionCode 地区代码
     * @return 溯源码
     */
    String generateTraceCode(String regionCode);
    
    /**
     * 验证溯源码格式
     * @param traceCode 溯源码
     * @return 是否有效
     */
    boolean validateTraceCode(String traceCode);
    
    /**
     * 解析溯源码信息
     * @param traceCode 溯源码
     * @return 溯源码信息
     */
    TraceCodeInfo parseTraceCode(String traceCode);
    
    /**
     * 溯源码信息类
     */
    class TraceCodeInfo {
        private String regionCode;
        private String regionName;
        private Integer year;
        private Long number;
        
        public TraceCodeInfo() {}
        
        public TraceCodeInfo(String regionCode, String regionName, Integer year, Long number) {
            this.regionCode = regionCode;
            this.regionName = regionName;
            this.year = year;
            this.number = number;
        }
        
        public String getRegionCode() { return regionCode; }
        public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public Long getNumber() { return number; }
        public void setNumber(Long number) { this.number = number; }
    }
}
