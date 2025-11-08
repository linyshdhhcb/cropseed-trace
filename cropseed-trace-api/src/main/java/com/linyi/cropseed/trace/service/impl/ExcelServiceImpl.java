package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.common.constant.CommonConstant;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.dto.InventoryImportDTO;
import com.linyi.cropseed.trace.dto.SeedImportDTO;
import com.linyi.cropseed.trace.entity.*;
import com.linyi.cropseed.trace.mapper.*;
import com.linyi.cropseed.trace.service.ExcelService;
import com.linyi.cropseed.trace.vo.ExcelImportResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel服务实现
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final SeedInfoMapper seedInfoMapper;
    private final SeedCategoryMapper seedCategoryMapper;
    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final OrderInfoMapper orderInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResultVO importSeeds(MultipartFile file) {
        List<SeedImportDTO> successList = new ArrayList<>();
        List<Map<String, Object>> errorList = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), SeedImportDTO.class, new ReadListener<SeedImportDTO>() {
                private List<SeedImportDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(100);

                @Override
                public void invoke(SeedImportDTO data, AnalysisContext context) {
                    cachedDataList.add(data);
                    if (cachedDataList.size() >= 100) {
                        processBatch(cachedDataList);
                        cachedDataList = ListUtils.newArrayListWithExpectedSize(100);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    processBatch(cachedDataList);
                }

                private void processBatch(List<SeedImportDTO> dataList) {
                    for (int i = 0; i < dataList.size(); i++) {
                        SeedImportDTO data = dataList.get(i);
                        try {
                            validateAndSaveSeed(data);
                            successList.add(data);
                        } catch (Exception e) {
                            Map<String, Object> error = Map.of(
                                    "row", i + 1,
                                    "error", e.getMessage(),
                                    "data", data);
                            errorList.add(error);
                        }
                    }
                }
            }).sheet().doRead();

        } catch (Exception e) {
            log.error("种子信息导入失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.EXCEL_IMPORT_FAIL);
        }

        return ExcelImportResultVO.builder()
                .totalCount(successList.size() + errorList.size())
                .successCount(successList.size())
                .errorCount(errorList.size())
                .errorList(errorList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResultVO importInventory(MultipartFile file) {
        List<InventoryImportDTO> successList = new ArrayList<>();
        List<Map<String, Object>> errorList = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), InventoryImportDTO.class, new ReadListener<InventoryImportDTO>() {
                private List<InventoryImportDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(100);

                @Override
                public void invoke(InventoryImportDTO data, AnalysisContext context) {
                    cachedDataList.add(data);
                    if (cachedDataList.size() >= 100) {
                        processBatch(cachedDataList);
                        cachedDataList = ListUtils.newArrayListWithExpectedSize(100);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    processBatch(cachedDataList);
                }

                private void processBatch(List<InventoryImportDTO> dataList) {
                    for (int i = 0; i < dataList.size(); i++) {
                        InventoryImportDTO data = dataList.get(i);
                        try {
                            validateAndSaveInventory(data);
                            successList.add(data);
                        } catch (Exception e) {
                            Map<String, Object> error = Map.of(
                                    "row", i + 1,
                                    "error", e.getMessage(),
                                    "data", data);
                            errorList.add(error);
                        }
                    }
                }
            }).sheet().doRead();

        } catch (Exception e) {
            log.error("库存信息导入失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.EXCEL_IMPORT_FAIL);
        }

        return ExcelImportResultVO.builder()
                .totalCount(successList.size() + errorList.size())
                .successCount(successList.size())
                .errorCount(errorList.size())
                .errorList(errorList)
                .build();
    }

    @Override
    public void exportSeedTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("种子信息导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<SeedImportDTO> templateData = createSeedTemplateData();
            EasyExcel.write(response.getOutputStream(), SeedImportDTO.class)
                    .sheet("种子信息模板")
                    .doWrite(templateData);

        } catch (IOException e) {
            log.error("导出种子模板失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.EXCEL_EXPORT_FAIL);
        }
    }

    @Override
    public void exportInventoryTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("库存信息导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<InventoryImportDTO> templateData = createInventoryTemplateData();
            EasyExcel.write(response.getOutputStream(), InventoryImportDTO.class)
                    .sheet("库存信息模板")
                    .doWrite(templateData);

        } catch (IOException e) {
            log.error("导出库存模板失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.EXCEL_EXPORT_FAIL);
        }
    }

    @Override
    public void exportSeeds(HttpServletResponse response, List<Long> seedIds) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("种子信息导出", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            List<SeedInfo> seeds;
            if (CollUtil.isEmpty(seedIds)) {
                seeds = seedInfoMapper.selectList(null);
            } else {
                seeds = seedInfoMapper.selectBatchIds(seedIds);
            }

            List<SeedImportDTO> exportData = seeds.stream()
                    .map(this::convertToSeedImportDTO)
                    .collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream(), SeedImportDTO.class)
                    .sheet("种子信息")
                    .doWrite(exportData);

        } catch (IOException e) {
            log.error("导出种子信息失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.EXCEL_EXPORT_FAIL);
        }
    }

    @Override
    public void exportInventory(HttpServletResponse response, List<Long> inventoryIds) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("库存信息导出", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            List<Inventory> inventories;
            if (CollUtil.isEmpty(inventoryIds)) {
                inventories = inventoryMapper.selectList(null);
            } else {
                inventories = inventoryMapper.selectBatchIds(inventoryIds);
            }

            List<InventoryImportDTO> exportData = inventories.stream()
                    .map(this::convertToInventoryImportDTO)
                    .collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream(), InventoryImportDTO.class)
                    .sheet("库存信息")
                    .doWrite(exportData);

        } catch (IOException e) {
            log.error("导出库存信息失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.EXCEL_EXPORT_FAIL);
        }
    }

    @Override
    public void exportOrders(HttpServletResponse response, List<Long> orderIds) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("订单信息导出", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            List<OrderInfo> orders;
            if (CollUtil.isEmpty(orderIds)) {
                orders = orderInfoMapper.selectList(null);
            } else {
                orders = orderInfoMapper.selectBatchIds(orderIds);
            }

            EasyExcel.write(response.getOutputStream(), OrderInfo.class)
                    .sheet("订单信息")
                    .doWrite(orders);

        } catch (IOException e) {
            log.error("导出订单信息失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.EXCEL_EXPORT_FAIL);
        }
    }

    /**
     * 验证并保存种子信息
     */
    private void validateAndSaveSeed(SeedImportDTO data) {
        // 验证必填字段
        if (StrUtil.isBlank(data.getSeedName())) {
            throw new BusinessException("种子名称不能为空");
        }
        if (StrUtil.isBlank(data.getSeedCode())) {
            throw new BusinessException("种子编码不能为空");
        }
        if (StrUtil.isBlank(data.getCategoryCode())) {
            throw new BusinessException("品类编码不能为空");
        }

        // 验证品类是否存在
        SeedCategory category = seedCategoryMapper.selectByCategoryCode(data.getCategoryCode());
        if (category == null) {
            throw new BusinessException("品类不存在：" + data.getCategoryCode());
        }

        // 验证种子编码是否重复
        SeedInfo existSeed = seedInfoMapper.selectBySeedCode(data.getSeedCode());
        if (existSeed != null) {
            throw new BusinessException("种子编码已存在：" + data.getSeedCode());
        }

        // 保存种子信息
        SeedInfo seedInfo = new SeedInfo();
        BeanUtil.copyProperties(data, seedInfo);
        seedInfo.setCategoryId(category.getId());
        seedInfo.setStatus(CommonConstant.STATUS_ENABLE);
        seedInfoMapper.insert(seedInfo);
    }

    /**
     * 验证并保存库存信息
     */
    private void validateAndSaveInventory(InventoryImportDTO data) {
        // 验证必填字段
        if (StrUtil.isBlank(data.getSeedCode())) {
            throw new BusinessException("种子编码不能为空");
        }
        if (StrUtil.isBlank(data.getWarehouseCode())) {
            throw new BusinessException("仓库编码不能为空");
        }

        // 验证种子是否存在
        SeedInfo seedInfo = seedInfoMapper.selectBySeedCode(data.getSeedCode());
        if (seedInfo == null) {
            throw new BusinessException("种子不存在：" + data.getSeedCode());
        }

        // 验证仓库是否存在
        Warehouse warehouse = warehouseMapper.selectByWarehouseCode(data.getWarehouseCode());
        if (warehouse == null) {
            throw new BusinessException("仓库不存在：" + data.getWarehouseCode());
        }

        // 查询或创建库存记录
        Inventory inventory = inventoryMapper.selectBySeedIdAndWarehouseId(seedInfo.getId(), warehouse.getId());
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setSeedId(seedInfo.getId());
            inventory.setWarehouseId(warehouse.getId());
            inventory.setQuantity(0);
            inventory.setLockedQuantity(0);
            inventory.setAvailableQuantity(0);
            inventoryMapper.insert(inventory);
        }

        // 更新库存数量
        inventory.setQuantity(data.getQuantity());
        inventory.setAvailableQuantity(data.getQuantity());
        inventory.setMinStock(data.getMinStock());
        inventory.setMaxStock(data.getMaxStock());
        inventoryMapper.updateById(inventory);
    }

    /**
     * 创建种子模板数据
     */
    private List<SeedImportDTO> createSeedTemplateData() {
        List<SeedImportDTO> templateData = new ArrayList<>();
        SeedImportDTO example = new SeedImportDTO();
        example.setSeedName("示例种子");
        example.setSeedCode("SEED001");
        example.setCategoryCode("CORN.HYBRID");
        example.setVariety("示例品种");
        example.setOriginPlace("示例产地");
        example.setCharacteristics("示例特性");
        example.setSpecifications("示例规格");
        example.setUnitPrice(new BigDecimal("100.00"));
        templateData.add(example);
        return templateData;
    }

    /**
     * 创建库存模板数据
     */
    private List<InventoryImportDTO> createInventoryTemplateData() {
        List<InventoryImportDTO> templateData = new ArrayList<>();
        InventoryImportDTO example = new InventoryImportDTO();
        example.setSeedCode("SEED001");
        example.setWarehouseCode("WH001");
        example.setQuantity(1000);
        example.setMinStock(100);
        example.setMaxStock(5000);
        templateData.add(example);
        return templateData;
    }

    /**
     * 转换为种子导入DTO
     */
    private SeedImportDTO convertToSeedImportDTO(SeedInfo seedInfo) {
        SeedImportDTO dto = new SeedImportDTO();
        BeanUtil.copyProperties(seedInfo, dto);

        // 查询品类编码
        SeedCategory category = seedCategoryMapper.selectById(seedInfo.getCategoryId());
        if (category != null) {
            dto.setCategoryCode(category.getCategoryCode());
        }

        return dto;
    }

    /**
     * 转换为库存导入DTO
     */
    private InventoryImportDTO convertToInventoryImportDTO(Inventory inventory) {
        InventoryImportDTO dto = new InventoryImportDTO();
        BeanUtil.copyProperties(inventory, dto);

        // 查询种子编码
        SeedInfo seedInfo = seedInfoMapper.selectById(inventory.getSeedId());
        if (seedInfo != null) {
            dto.setSeedCode(seedInfo.getSeedCode());
        }

        // 查询仓库编码
        Warehouse warehouse = warehouseMapper.selectById(inventory.getWarehouseId());
        if (warehouse != null) {
            dto.setWarehouseCode(warehouse.getWarehouseCode());
        }

        return dto;
    }
}
