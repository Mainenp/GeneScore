-- GeneScoreDB: 专门用于存储 gene_score_db.csv 数据的表
USE genescore_db;

-- 检查表是否存在并创建（安全方式）
CREATE TABLE IF NOT EXISTS gene_score_db (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_symbol VARCHAR(50) NOT NULL,
    bowel DOUBLE,
    breast DOUBLE,
    brain DOUBLE,
    stomach DOUBLE,
    head_and_neck DOUBLE,
    kidney DOUBLE,
    liver DOUBLE,
    lung DOUBLE,
    ovary DOUBLE,
    pancreas DOUBLE,
    prostate DOUBLE,
    skin DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_gene_symbol (gene_symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 验证表结构
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'genescore_db'
    AND TABLE_NAME = 'gene_score_db'
ORDER BY ORDINAL_POSITION;

-- 提示信息
SELECT 'Table gene_score_db is ready!' AS message;

-- 查询组织类型列表
SELECT 
    'Brain' AS tissue, 'brain' AS column_name UNION ALL
    SELECT 'Lung', 'lung' UNION ALL
    SELECT 'Breast', 'breast' UNION ALL
    SELECT 'Liver', 'liver' UNION ALL
    SELECT 'Stomach', 'stomach' UNION ALL
    SELECT 'Pancreas', 'pancreas' UNION ALL
    SELECT 'Kidney', 'kidney' UNION ALL
    SELECT 'Ovary', 'ovary' UNION ALL
    SELECT 'Prostate', 'prostate' UNION ALL
    SELECT 'Skin', 'skin' UNION ALL
    SELECT 'Bowel', 'bowel' UNION ALL
    SELECT 'Head and Neck', 'head_and_neck';
