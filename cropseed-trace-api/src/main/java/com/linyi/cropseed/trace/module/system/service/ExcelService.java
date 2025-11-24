package com.linyi.cropseed.trace.module.system.service;

import com.linyi.cropseed.trace.module.system.model.vo.ExcelImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Excel服务接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface ExcelService {

    /**
     * 导入种子信息
     */
    ExcelImportResultVO importSeeds(MultipartFile file);

    /**
     * 导入库存信息
     */
    ExcelImportResultVO importInventory(MultipartFile file);

    /**
     * 导出种子信息模板
     */
    void exportSeedTemplate(HttpServletResponse response);

    /**
     * 导出库存信息模板
     */
    void exportInventoryTemplate(HttpServletResponse response);

    /**
     * 导出种子信息
     */
    void exportSeeds(HttpServletResponse response, List<Long> seedIds);

    /**
     * 导出库存信息
     */
    void exportInventory(HttpServletResponse response, List<Long> inventoryIds);

    /**
     * 导出订单信息
     */
    void exportOrders(HttpServletResponse response, List<Long> orderIds);
}
