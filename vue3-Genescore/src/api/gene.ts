// src/api/gene.ts

import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 300000, // 增加到5分钟
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 搜索基因
 */
export const searchGenes = async (query: string) => {
  const response = await apiClient.get('/gene/search', {
    params: { keyword: query }
  });
  // 处理后端返回的 {code, msg, data} 格式
  if (response.data && response.data.code === 200 && response.data.data) {
    return response.data.data;
  }
  return [];
};

/**
 * 获取基因概览信息
 */
export const getGeneOverview = async (symbol: string) => {
  try {
    const response = await apiClient.get(`/gene/detail/${symbol}`);
    console.log('API Response for gene overview:', response.data);
    
    // 检查并正确处理响应数据结构
    let geneData = null;
    
    if (response.data && response.data.data) {
      // 响应格式是 { code, msg, data }
      const apiData = response.data.data;
      
      if (apiData.basic) {
        // 从 basic 字段提取基因信息
        const basicInfo = apiData.basic;
        geneData = {
          id: basicInfo.id || 1,
          symbol: basicInfo.geneSymbol || basicInfo.symbol || symbol,
          name: basicInfo.name || basicInfo.geneFullName || 'Unknown Gene',
          entrezId: basicInfo.entrezId || '12345',
          uniprotId: basicInfo.uniprotId || 'P00000',
          description: basicInfo.description || basicInfo.geneFunction || 'No description available',
          pdbId: basicInfo.pdbId || '4LQM',
          ensemblId: basicInfo.ensemblId || ''
        };
      } else {
        // 如果没有 basic 字段，尝试直接使用 data
        geneData = {
          id: apiData.id || 1,
          symbol: apiData.symbol || apiData.geneSymbol || symbol,
          name: apiData.name || apiData.geneFullName || 'Unknown Gene',
          entrezId: apiData.entrezId || '12345',
          uniprotId: apiData.uniprotId || 'P00000',
          description: apiData.description || 'No description available',
          pdbId: apiData.pdbId || '4LQM',
          ensemblId: apiData.ensemblId || ''
        };
      }
    } else if (response.data) {
      // 响应直接是基因数据
      geneData = {
        id: response.data.id || 1,
        symbol: response.data.symbol || response.data.geneSymbol || symbol,
        name: response.data.name || response.data.geneFullName || 'Unknown Gene',
        entrezId: response.data.entrezId || '12345',
        uniprotId: response.data.uniprotId || 'P00000',
        description: response.data.description || 'No description available',
        pdbId: response.data.pdbId || '4LQM',
        ensemblId: response.data.ensemblId || ''
      };
    }
    
    if (geneData) {
      console.log('Processed gene data:', geneData);
      return geneData;
    }
    
    // 如果所有解析都失败，使用模拟数据
    console.warn('Could not parse gene data, using mock data');
    const mockData = {
      id: 1,
      symbol: symbol,
      name: 'Mock Gene Name',
      entrezId: '12345',
      uniprotId: 'P00000',
      description: 'Mock gene description',
      pdbId: '4LQM'
    };
    return mockData;
  } catch (error) {
    console.error('Error fetching gene overview, using mock data:', error);
    // 使用模拟数据作为fallback
    const mockData = {
      id: 1,
      symbol: symbol,
      name: 'Mock Gene Name',
      entrezId: '12345',
      uniprotId: 'P00000',
      description: 'Mock gene description',
      pdbId: '4LQM'
    };
    return mockData;
  }
};

/**
 * 获取人体图数据
 */
export const getBodyMapData = async (symbol: string) => {
  const response = await apiClient.get(`/genes/${symbol}/bodymap`);
  return response.data;
};

/**
 * 获取Sankey图数据
 */
export const getSankeyData = async (symbol: string) => {
  const response = await apiClient.get(`/genes/${symbol}/sankey`);
  return response.data;
};

/**
 * 获取分析数据
 */
export const getAnalyticsData = async (symbol: string) => {
  const response = await apiClient.get(`/genes/${symbol}/analytics`);
  return response.data;
};

/**
 * 获取共依赖网络数据
 */
export const getCoDependencies = async (symbol: string) => {
  const response = await apiClient.get(`/genes/${symbol}/co-dependencies`);
  return response.data;
};

/**
 * 运行双基因敲除预测
 */
export const simulateDKO = async (gene1: string, gene2: string) => {
  const response = await apiClient.post('/dko/simulate', {
    gene1,
    gene2
  });
  return response.data;
};

/**
 * 分析多基因敲除
 */
export const analyzeDKO = async (genes: string[]) => {
  const response = await apiClient.post('/dko/analyze', {
    genes
  });
  return response.data;
};

/**
 * 检查Python微服务健康状态
 */
export const checkDKOHealth = async () => {
  const response = await apiClient.get('/dko/health');
  return response.data;
};

/**
 * 获取癌症类型列表
 */
export const getCancerTypes = async () => {
  try {
    const response = await apiClient.get('/genes/cancer-types');
    return response.data;
  } catch (error) {
    // 返回默认癌症类型
    return ['Lung Cancer', 'Breast Cancer', 'Colon Cancer', 'Prostate Cancer', 'Liver Cancer', 'Stomach Cancer'];
  }
};

/**
 * 获取特定癌症类型的基因评分
 */
export const getGeneScores = async (cancerType: string) => {
  try {
    const response = await apiClient.get('/genes/scores', {
      params: { cancerType }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching gene scores:', error);
    return [];
  }
};

/**
 * 获取特定基因的评分
 */
export const getGeneScoresByGenes = async (cancerType: string, symbols: string[]) => {
  try {
    const response = await apiClient.get('/genes/scores-by-symbols', {
      params: { 
        cancerType,
        symbols: symbols.join(',')
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching gene scores by symbols:', error);
    return [];
  }
};

/**
 * 获取基因分析数据
 * @param geneSymbol 基因符号
 * @param cancerType 癌症类型 (默认: LUAD)
 */
export const getGeneAnalysis = async (geneSymbol: string, cancerType: string = 'LUAD') => {
  try {
    const response = await apiClient.get(`/gene/analysis/${geneSymbol}`, {
      params: { cancer_type: cancerType }
    });
    console.log('Gene analysis API response:', response.data);
    
    if (response.data && response.data.code === 200 && response.data.data) {
      return response.data.data;
    }
    return null;
  } catch (error) {
    console.error('Error fetching gene analysis data:', error);
    return null;
  }
};

/**
 * 获取 GENE_SCORE_DB 中的组织特异性基因评分
 */
export const getGeneScoreDbScores = async (geneSymbol: string) => {
  try {
    const response = await apiClient.get(`/genes/${geneSymbol}/gene-score-db`);
    return response.data;
  } catch (error) {
    console.error('Error fetching gene score db scores:', error);
    return {};
  }
};

/**
 * 获取基因的 PPI 网络数据
 */
export const getGeneNetwork = async (geneSymbol: string) => {
  try {
    const response = await apiClient.get(`/genes/${geneSymbol}/network`);
    return response.data;
  } catch (error) {
    console.error('Error fetching gene network:', error);
    return { nodes: [], links: [] };
  }
};

/**
 * 获取完整的基因详情（包含所有信息）
 * 这个端点是后端提供的统一 API，包含基本信息、3D结构、组学、网络、药物、通路等
 */
export const getCompleteGeneDetail = async (geneSymbol: string) => {
  try {
    const response = await apiClient.get(`/gene/detail/${geneSymbol}`);
    console.log('Complete gene detail API response:', response.data);
    
    if (response.data && response.data.code === 200 && response.data.data) {
      return response.data.data;
    }
    return null;
  } catch (error) {
    console.error('Error fetching complete gene detail:', error);
    return null;
  }
};

/**
 * 获取 gene_score_db 表中的组织特异性基因评分
 */
export const getGeneScoresFromDb = async (geneSymbol: string) => {
  try {
    const response = await apiClient.get(`/gene/scores/${geneSymbol}`);
    console.log('Gene scores DB API response:', response.data);
    
    if (response.data && response.data.code === 200 && response.data.data) {
      return response.data.data;
    }
    return null;
  } catch (error) {
    console.error('Error fetching gene scores from DB:', error);
    return null;
  }
};

/**
 * 获取基因的 PPI 网络数据（从 ppi.csv）
 */
export const getPpiNetwork = async (geneSymbol: string, depth: number = 1, maxNodes: number = 50) => {
  try {
    const response = await apiClient.get(`/gene/ppi/${geneSymbol}`, {
      params: { depth, maxNodes }
    });
    console.log('PPI network API response:', response.data);
    
    if (response.data && response.data.code === 200 && response.data.data) {
      return response.data.data;
    }
    return { nodes: [], links: [] };
  } catch (error) {
    console.error('Error fetching PPI network:', error);
    return { nodes: [], links: [] };
  }
};
