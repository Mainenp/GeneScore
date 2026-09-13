package com.mainenp.genedb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 肺癌特异性基因评分 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LungGeneScoreDTO {
    
    /**
     * 基因符号
     */
    private String geneSymbol;
    
    /**
     * 全局评分 (Global_Score_Lung)
     */
    private Double globalScore;
    
    /**
     * 选择性评分 (Selective_Score_Lung)
     */
    private Double selectiveScore;
    
    /**
     * 驱动因子差异 (Driver_Delta)
     */
    private Double driverDelta;
    
    /**
     * 排名
     */
    private Double rank;
    
    /**
     * 综合评分（用于排序和展示）
     */
    private Double compositeScore;
    
    /**
     * 评分等级：High, Medium, Low
     */
    private String scoreLevel;
    
    /**
     * 是否为驱动基因
     */
    private Boolean isDriver;
}
