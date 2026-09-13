-- Gene Basic Information Table
CREATE TABLE IF NOT EXISTS gene_basic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gene_symbol VARCHAR(50) NOT NULL UNIQUE,
    hgnc_id VARCHAR(50),
    entrez_id VARCHAR(50),
    ensembl_id VARCHAR(50),
    gene_full_name VARCHAR(500),
    gene_type VARCHAR(100),
    gene_function TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_gene_symbol (gene_symbol),
    INDEX idx_hgnc_id (hgnc_id),
    INDEX idx_entrez_id (entrez_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Gene Cache Table (stores API results)
CREATE TABLE IF NOT EXISTS gene_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gene_symbol VARCHAR(50) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    data_content LONGTEXT NOT NULL,
    cache_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_time TIMESTAMP,
    UNIQUE KEY uk_gene_datatype (gene_symbol, data_type),
    FOREIGN KEY (gene_symbol) REFERENCES gene_basic(gene_symbol) ON DELETE CASCADE,
    INDEX idx_gene_symbol (gene_symbol),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better query performance
CREATE INDEX idx_gene_basic_symbol_type ON gene_basic(gene_symbol, gene_type);
CREATE INDEX idx_gene_cache_expire ON gene_cache(expire_time);
