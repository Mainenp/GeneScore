// ==================== Gene Basic Types ====================

export interface Gene {
  id?: number;
  symbol?: string;
  name?: string;
  entrezId?: string;
  uniprotId?: string;
  description?: string;
}

export interface GeneBasic {
  geneSymbol: string;
  geneName: string;
  entrezId: string;
  ensemblId: string;
}

// ==================== Python API Analysis Types ====================

/** Expression Analysis from Python API */
export interface ExpressionAnalysis {
  log2FC: number;
  p_value: number;
  tumor_mean: number;
  normal_mean: number;
  tumor_count: number;
  normal_count: number;
  status: 'Up' | 'Down' | 'Not Sig' | string;
}

/** Prognosis Analysis from Python API */
export interface PrognosisAnalysis {
  hazard_ratio: number;
  p_value: number;
  risk: 'High Risk' | 'Protective' | string;
  sample_size: number;
}

/** Drug Sensitivity Data from Python API */
export interface DrugSensitivityData {
  drug_name: string;
  target_pathway: string;
  target_genes: string;
  mean_ic50: number;
  tested_cell_lines: number;
}

/** Complete Analysis Response from Python API */
export interface GeneAnalysisResponse {
  gene_symbol: string;
  cancer_type: string;
  expression: ExpressionAnalysis;
  prognosis: PrognosisAnalysis;
  drug_sensitivity: DrugSensitivityData[];
}

/** Frontend Analysis Data Structure */
export interface AnalysisData {
  geneSymbol: string;
  cancerType: string;
  expression: ExpressionAnalysis | null;
  prognosis: PrognosisAnalysis | null;
  enrichment: any[];
  drug: DrugSensitivityData[];
}

// ==================== Analysis Component Types ====================

export interface PrognosisData {
  cancerType: string;
  hazardRatio: number;
  pValue: number;
  confidence: string;
}

export interface ExpressionData {
  tissue: string;
  expression: number;
  log2FC: number;
  pValue: number;
}

export interface EnrichmentData {
  pathway: string;
  geneCount: number;
  enrichmentScore: number;
  pValue: number;
}

export interface DrugData {
  drugName: string;
  target: string;
  ic50: number;
  sensitivity: 'High' | 'Medium' | 'Low';
}

// ==================== API Error Response ====================

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, any>;
}

// ==================== Body Map Types ====================

export interface OrganData {
  id: string;
  name: string;
  cancerScore: number;
  expressionLevel: number;
  associatedCancers: string[];
  color: string;
}

export interface RelatedGene {
  symbol: string;
  name: string;
  type: 'ortholog' | 'paralog' | 'pathway' | 'coexpression';
  organism?: string;
  similarity: number;
  ensemblId?: string;
  uniprotId?: string;
}
