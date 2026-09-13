-- GeneScoreDB MySQL Schema
-- 使用数据库
USE genescore_db;

-- 基因表（扩展版本）
CREATE TABLE IF NOT EXISTS genes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    symbol VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    entrez_id VARCHAR(50),
    uniprot_id VARCHAR(50),
    description TEXT,
    chromosome VARCHAR(5),
    start_position BIGINT,
    end_position BIGINT,
    strand CHAR(1),
    biotype VARCHAR(50),
    go_biological_process TEXT,
    go_molecular_function TEXT,
    go_cellular_component TEXT,
    protein_length INT,
    pdb_id VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_symbol (symbol),
    INDEX idx_uniprot (uniprot_id),
    INDEX idx_chromosome (chromosome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 基因依赖分数表（扩展版本）
CREATE TABLE IF NOT EXISTS gene_scores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_id BIGINT NOT NULL,
    lineage VARCHAR(100),
    disease_subtype VARCHAR(100),
    dependency_score DOUBLE,
    expression_level DOUBLE,
    copy_number DOUBLE,
    dataset VARCHAR(50),
    replicate_count INT,
    model_prediction_score DOUBLE,
    cancer_type VARCHAR(100),
    tissue_type VARCHAR(100),
    confidence_score DOUBLE,
    prediction_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gene_id) REFERENCES genes(id) ON DELETE CASCADE,
    INDEX idx_gene_lineage (gene_id, lineage),
    INDEX idx_lineage (lineage),
    INDEX idx_cancer_type (cancer_type),
    INDEX idx_tissue_type (tissue_type),
    INDEX idx_dataset (dataset)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 基因表达数据表
CREATE TABLE IF NOT EXISTS gene_expression (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_id BIGINT NOT NULL,
    tissue_type VARCHAR(100),
    expression_value DOUBLE,
    expression_level VARCHAR(20),
    source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gene_id) REFERENCES genes(id) ON DELETE CASCADE,
    INDEX idx_gene_tissue (gene_id, tissue_type),
    INDEX idx_tissue_type (tissue_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 基因位置信息表
CREATE TABLE IF NOT EXISTS gene_locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_id BIGINT NOT NULL UNIQUE,
    chromosome VARCHAR(5),
    start_position BIGINT,
    end_position BIGINT,
    strand CHAR(1),
    biotype VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gene_id) REFERENCES genes(id) ON DELETE CASCADE,
    INDEX idx_chromosome (chromosome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 共依赖表
CREATE TABLE IF NOT EXISTS co_dependencies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_id_a BIGINT NOT NULL,
    gene_id_b BIGINT NOT NULL,
    pearson_correlation DOUBLE,
    p_value DOUBLE,
    sample_count INT,
    dataset VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gene_id_a) REFERENCES genes(id) ON DELETE CASCADE,
    FOREIGN KEY (gene_id_b) REFERENCES genes(id) ON DELETE CASCADE,
    INDEX idx_gene_pair (gene_id_a, gene_id_b)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 基因药物表
CREATE TABLE IF NOT EXISTS gene_drugs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_id BIGINT NOT NULL,
    drug_name VARCHAR(255) NOT NULL,
    drug_id VARCHAR(100),
    interaction_type VARCHAR(100),
    evidence_type VARCHAR(100),
    evidence_count INT,
    source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gene_id) REFERENCES genes(id) ON DELETE CASCADE,
    INDEX idx_gene_drug (gene_id, drug_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 基因通路表
CREATE TABLE IF NOT EXISTS gene_pathways (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_id BIGINT NOT NULL,
    pathway_name VARCHAR(255) NOT NULL,
    pathway_id VARCHAR(100),
    source VARCHAR(100),
    p_value DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gene_id) REFERENCES genes(id) ON DELETE CASCADE,
    INDEX idx_gene_pathway (gene_id, pathway_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 生存数据表
CREATE TABLE IF NOT EXISTS survival_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gene_id BIGINT NOT NULL,
    lineage VARCHAR(100),
    hazard_ratio DOUBLE,
    p_value DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gene_id) REFERENCES genes(id) ON DELETE CASCADE,
    INDEX idx_gene_lineage_survival (gene_id, lineage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 示例数据插入
INSERT INTO genes (symbol, name, entrez_id, uniprot_id, description) VALUES
('EGFR', 'Epidermal Growth Factor Receptor', '1956', 'P00533', 'Receptor tyrosine kinase involved in cell proliferation'),
('KRAS', 'KRAS Proto-Oncogene', '3845', 'P01116', 'GTPase involved in cell signaling'),
('TP53', 'Tumor Protein P53', '7157', 'P04637', 'Tumor suppressor protein'),
('BRCA1', 'BRCA1 DNA Repair Associated', '672', 'P38398', 'DNA repair and transcription regulation'),
('MYC', 'MYC Proto-Oncogene', '4609', 'P01106', 'Transcription factor involved in cell proliferation');

-- 示例基因分数数据
INSERT INTO gene_scores (gene_id, lineage, disease_subtype, dependency_score, expression_level, dataset) VALUES
(1, 'Lung', 'Adenocarcinoma', -1.5, 8.2, 'DepMap'),
(1, 'Breast', 'Luminal A', -2.1, 7.8, 'DepMap'),
(2, 'Pancreas', 'Ductal', -1.8, 9.1, 'DepMap'),
(3, 'Colon', 'Colorectal', -2.5, 6.5, 'DepMap'),
(4, 'Breast', 'Triple Negative', -1.2, 7.3, 'DepMap');
