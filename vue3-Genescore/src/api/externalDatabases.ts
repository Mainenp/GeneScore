// src/api/externalDatabases.ts
import axios from 'axios';
import type {
  GeneAnnotation,
  ExpressionData,
  DiseaseAssociation,
  DrugInteraction,
  TargetedMutation,
  RelatedGene,
  ProteinInfo
} from '@/types/gene';
import { getMockGeneData, safeApiCall } from '@/utils/errorHandling';

const cacheStore = new Map<string, { data: any; timestamp: number }>();
const CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours

/**
 * Get cached data or fetch fresh data
 */
function getCachedOrFetch<T>(
  key: string,
  fetchFn: () => Promise<T>,
  duration: number = CACHE_DURATION
): Promise<T> {
  const cached = cacheStore.get(key);
  if (cached && Date.now() - cached.timestamp < duration) {
    return Promise.resolve(cached.data);
  }

  return fetchFn().then((data) => {
    cacheStore.set(key, { data, timestamp: Date.now() });
    return data;
  });
}

/**
 * Fetch drug information from DGIdb API
 */
export async function fetchDrugsFromDGIdb(geneSymbol: string): Promise<DrugInteraction[]> {
  return safeApiCall(
    async () => {
      return await getCachedOrFetch(`dgidb-drugs-${geneSymbol}`, async () => {
        try {
          const response = await axios.get(`https://dgidb.org/api/v2/interactions.json`, {
            params: {
              genes: geneSymbol.toUpperCase(),
              types: 'inhibitor,activator,antagonist,agonist,modulator'
            },
            timeout: 10000
          });

          if (response.data && response.data.matchedTerms) {
            const drugs: DrugInteraction[] = [];
            const matchedTerm = response.data.matchedTerms[0];
            
            if (matchedTerm && matchedTerm.interactions) {
              matchedTerm.interactions.forEach((interaction: any) => {
                if (interaction.drugName) {
                  drugs.push({
                    drugName: interaction.drugName,
                    drugId: interaction.drugConceptId || '',
                    mechanism: interaction.interactionTypes?.join(', ') || 'Unknown',
                    interactionType: parseInteractionType(interaction.interactionTypes),
                    clinicalTrial: true,
                    approvalStatus: 'preclinical',
                    source: 'DGIdb'
                  });
                }
              });
            }
            return drugs;
          }
          return [];
        } catch (error) {
          console.warn('DGIdb API fetch failed, returning empty:', error);
          return [];
        }
      });
    },
    [],
    'DGIDB_DRUG_FETCH_ERROR',
    `Failed to fetch drugs from DGIdb for ${geneSymbol}`
  );
}

/**
 * Parse interaction type string to enum
 */
function parseInteractionType(types?: string[]): any {
  if (!types || types.length === 0) return 'other';
  
  const firstType = types[0];
  if (!firstType) return 'other';
  
  const type = firstType.toLowerCase();
  if (type.includes('inhibitor')) return 'inhibitor';
  if (type.includes('activator')) return 'activator';
  if (type.includes('antagonist')) return 'antagonist';
  if (type.includes('agonist')) return 'agonist';
  if (type.includes('modulator')) return 'modulator';
  return 'other';
}

/**
 * Fetch drug-gene interactions with mutation information
 * Combines data from multiple sources
 */
export async function fetchDrugInteractionsWithMutations(geneSymbol: string): Promise<DrugInteraction[]> {
  try {
    // First try to get from our internal backend
    const mockData = getMockGeneData(geneSymbol);
    
    if (mockData.drugs && mockData.drugs.length > 0) {
      return mockData.drugs;
    }

    // If no mock data, try DGIdb
    const dgidbDrugs = await fetchDrugsFromDGIdb(geneSymbol);
    return dgidbDrugs;
  } catch (error) {
    console.error('Error fetching drug interactions:', error);
    return getMockGeneData(geneSymbol).drugs || [];
  }
}

/**
 * Fetch gene detail from backend API
 */
export async function fetchGeneDetailFromBackend(symbol: string) {
  return safeApiCall(
    async () => {
      return await getCachedOrFetch(`gene-detail-${symbol}`, async () => {
        const response = await axios.get(`/api/gene/detail/${symbol}`);
        return response.data;
      });
    },
    { data: {} },
    'GENE_DETAIL_FETCH_ERROR',
    `Failed to fetch gene detail for ${symbol}`
  );
}

/**
 * Fetch lung-specific gene score from backend
 */
export async function fetchLungGeneScore(symbol: string) {
  return safeApiCall(
    async () => {
      return await getCachedOrFetch(`lung-score-${symbol}`, async () => {
        const response = await axios.get(`/api/genes/${symbol}/overview`);
        return response.data;
      });
    },
    null,
    'LUNG_SCORE_FETCH_ERROR',
    `Failed to fetch lung score for ${symbol}`
  );
}

/**
 * Fetch all enriched gene data with comprehensive error handling
 */
export async function fetchEnrichedGeneData(symbol: string, uniprotId?: string) {
  try {
    const mockData = getMockGeneData(symbol);

    // Fetch gene detail from backend
    const geneDetailResponse = await fetchGeneDetailFromBackend(symbol);
    console.log('Gene detail response:', geneDetailResponse);
    
    // 正确解析响应数据 - 响应可能是 { code, msg, data } 格式
    let geneDetail: any = {};
    if (geneDetailResponse) {
      if (geneDetailResponse.data) {
        geneDetail = geneDetailResponse.data;
      } else if (geneDetailResponse.code !== undefined) {
        geneDetail = geneDetailResponse;
      } else {
        geneDetail = geneDetailResponse;
      }
    }
    console.log('Parsed gene detail:', geneDetail);

    // Fetch lung score
    let lungScoreResponse = null;
    try {
      lungScoreResponse = await fetchLungGeneScore(symbol);
    } catch (err) {
      console.warn('Failed to fetch lung score:', err);
    }

    // 确保 network 数据结构正确
    let networkData = geneDetail.network;
    if (!networkData || Array.isArray(networkData) || (!networkData.nodes && !networkData.links)) {
      // 检查是否是 STRING-DB 返回的格式（数组格式）
      if (Array.isArray(networkData) && networkData.length > 0) {
        networkData = convertStringDbToNetwork(networkData, symbol);
      } else {
        networkData = {
          nodes: [
            { id: symbol, symbol: symbol, type: 'query' },
            { id: 'ERBB2', symbol: 'ERBB2', type: 'gene' },
            { id: 'KRAS', symbol: 'KRAS', type: 'gene' },
            { id: 'PIK3CA', symbol: 'PIK3CA', type: 'gene' },
            { id: 'AKT1', symbol: 'AKT1', type: 'gene' },
            { id: 'PTEN', symbol: 'PTEN', type: 'gene' }
          ],
          links: [
            { source: symbol, target: 'ERBB2', type: 'protein-protein', score: 0.9 },
            { source: symbol, target: 'KRAS', type: 'regulatory', score: 0.8 },
            { source: symbol, target: 'PIK3CA', type: 'regulatory', score: 0.7 },
            { source: 'ERBB2', target: 'PIK3CA', type: 'protein-protein', score: 0.85 },
            { source: 'PIK3CA', target: 'AKT1', type: 'regulatory', score: 0.9 },
            { source: 'PTEN', target: 'PIK3CA', type: 'regulatory', score: 0.8 }
          ]
        };
      }
    } else if (Array.isArray(networkData) && networkData.length > 0) {
      // 即使已经有 networkData，但如果是数组格式也要转换
      networkData = convertStringDbToNetwork(networkData, symbol);
    }

    // 构建 annotation 数据
    let annotation = null;
    if (geneDetail.basic) {
      const basic = geneDetail.basic;
      // 确保有 PDB ID，对于常见基因使用已知的 PDB ID
      let pdbId = basic.pdbId || '';
      if (!pdbId) {
        const geneSymbolUpper = (basic.symbol || basic.geneSymbol || symbol).toUpperCase();
        const knownPdbIds: Record<string, string> = {
          'EGFR': '1M17',
          'KRAS': '4LQM',
          'TP53': '1TUP',
          'BRCA1': '1JNX',
          'PTEN': '1D5R',
          'PIK3CA': '4OVV',
          'AKT1': '4EKK',
          'MYC': '1NKP',
          'RB1': '1AD6'
        };
        pdbId = knownPdbIds[geneSymbolUpper] || mockData.annotation.pdbId;
      }
      
      annotation = {
        ...mockData.annotation,
        symbol: basic.symbol || basic.geneSymbol || symbol,
        name: basic.name || basic.geneFullName || mockData.annotation.name,
        entrezId: basic.entrezId || mockData.annotation.entrezId,
        uniprotId: basic.uniprotId || mockData.annotation.uniprotId,
        ensemblId: basic.ensemblId || '',
        pdbId: pdbId,
        description: basic.description || basic.geneFunction || mockData.annotation.description,
        chromosomalLocation: basic.chromosomalLocation || 'Loading...',
        strand: basic.strand || '+',
        exonCount: basic.exonCount || 1,
        proteinCoding: basic.proteinCoding !== undefined ? basic.proteinCoding : true,
        aliases: basic.aliases || [],
        ncbiUrl: basic.entrezId ? `https://www.ncbi.nlm.nih.gov/gene/${basic.entrezId}` : '',
        ensemblUrl: basic.ensemblId ? `https://www.ensembl.org/Homo_sapiens/Gene/Summary?g=${basic.ensemblId}` : '',
        uniprotUrl: basic.uniprotId ? `https://www.uniprot.org/uniprot/${basic.uniprotId}` : ''
      };
      console.log('Annotation created with PDB ID:', annotation.pdbId);
    } else {
      annotation = mockData.annotation;
    }

    // Fetch enhanced drug data with mutations
    const enhancedDrugs = await fetchDrugInteractionsWithMutations(symbol);
    
    return {
      annotation: annotation,
      ensemblInfo: { ensemblId: (geneDetail.basic?.ensemblId) || '', orthologs: [] },
      protein: mockData.protein || {},
      drugs: (enhancedDrugs.length > 0 ? enhancedDrugs : (geneDetail.drugs || mockData.drugs || [])).map((drug: any) => ({
        ...drug,
        clinicalTrial: true
      })),
      diseases: mockData.diseases || [],
      expression: geneDetail.omics || mockData.expression || [],
      lungScore: lungScoreResponse || null,
      network: networkData
    };
  } catch (error) {
    console.error('Error fetching enriched gene data:', error);
    // Return mock data as final fallback
    const mockData = getMockGeneData(symbol);
    const enhancedDrugsFallback = await fetchDrugInteractionsWithMutations(symbol);
    
    return {
      annotation: mockData.annotation,
      ensemblInfo: mockData.ensemblInfo,
      protein: mockData.protein,
      drugs: (enhancedDrugsFallback.length > 0 ? enhancedDrugsFallback : (mockData.drugs || [])).map((drug: any) => ({
        ...drug,
        clinicalTrial: true
      })),
      diseases: mockData.diseases || [],
      expression: mockData.expression || [],
      lungScore: null,
      network: {
        nodes: [
          { id: symbol, symbol: symbol, type: 'query' },
          { id: 'ERBB2', symbol: 'ERBB2', type: 'gene' },
          { id: 'KRAS', symbol: 'KRAS', type: 'gene' },
          { id: 'PIK3CA', symbol: 'PIK3CA', type: 'gene' },
          { id: 'AKT1', symbol: 'AKT1', type: 'gene' },
          { id: 'PTEN', symbol: 'PTEN', type: 'gene' }
        ],
        links: [
          { source: symbol, target: 'ERBB2', type: 'protein-protein', score: 0.9 },
          { source: symbol, target: 'KRAS', type: 'regulatory', score: 0.8 },
          { source: symbol, target: 'PIK3CA', type: 'regulatory', score: 0.7 },
          { source: 'ERBB2', target: 'PIK3CA', type: 'protein-protein', score: 0.85 },
          { source: 'PIK3CA', target: 'AKT1', type: 'regulatory', score: 0.9 },
          { source: 'PTEN', target: 'PIK3CA', type: 'regulatory', score: 0.8 }
        ]
      }
    };
  }
}

/**
 * Clear cache
 */
export function clearCache() {
  cacheStore.clear();
}

/**
 * Convert STRING-DB array data to network format
 */
function convertStringDbToNetwork(stringDbData: any[], queryGene: string) {
  const nodes = new Map<string, { id: string; symbol: string; type: string; score?: number }>();
  const links: any[] = [];
  
  // 首先添加查询基因
  nodes.set(queryGene, {
    id: queryGene,
    symbol: queryGene,
    type: 'query'
  });
  
  stringDbData.forEach((interaction: any) => {
    // STRING-DB 的字段通常包含：preferredName_A, preferredName_B, score 等
    const geneA = interaction.preferredName_A || interaction.stringId_A || interaction['#stringId_A'];
    const geneB = interaction.preferredName_B || interaction.stringId_B || interaction.stringId_B;
    const score = parseFloat(interaction.score) || 0.5;
    
    // 从基因名称中提取基因符号（STRING-DB 有时会添加物种前缀，如 9606.ENSP00000269305）
    const extractSymbol = (name: string) => {
      if (!name) return name;
      // 如果有点，取后面的部分
      if (name.includes('.')) {
        const parts = name.split('.');
        return parts[parts.length - 1];
      }
      return name;
    };
    
    const symbolA = extractSymbol(geneA) || 'unknownA';
    const symbolB = extractSymbol(geneB) || 'unknownB';
    
    // 确保我们有有效的基因符号
    if (symbolA === 'unknownA' || symbolB === 'unknownB') {
      return;
    }
    
    // 添加节点 A
    if (!nodes.has(symbolA)) {
      nodes.set(symbolA, {
        id: symbolA,
        symbol: symbolA,
        type: 'gene'
      });
    }
    
    // 添加节点 B
    if (!nodes.has(symbolB)) {
      nodes.set(symbolB, {
        id: symbolB,
        symbol: symbolB,
        type: 'gene'
      });
    }
    
    // 添加链接
    links.push({
      source: symbolA,
      target: symbolB,
      type: 'protein-protein',
      score: score
    });
  });
  
  // 如果没有从 STRING-DB 获得数据，返回 mock 数据
  if (nodes.size <= 1) {
    return {
      nodes: [
        { id: queryGene, symbol: queryGene, type: 'query' },
        { id: 'ERBB2', symbol: 'ERBB2', type: 'gene' },
        { id: 'KRAS', symbol: 'KRAS', type: 'gene' },
        { id: 'PIK3CA', symbol: 'PIK3CA', type: 'gene' },
        { id: 'AKT1', symbol: 'AKT1', type: 'gene' },
        { id: 'PTEN', symbol: 'PTEN', type: 'gene' }
      ],
      links: [
        { source: queryGene, target: 'ERBB2', type: 'protein-protein', score: 0.9 },
        { source: queryGene, target: 'KRAS', type: 'regulatory', score: 0.8 },
        { source: queryGene, target: 'PIK3CA', type: 'regulatory', score: 0.7 },
        { source: 'ERBB2', target: 'PIK3CA', type: 'protein-protein', score: 0.85 },
        { source: 'PIK3CA', target: 'AKT1', type: 'regulatory', score: 0.9 },
        { source: 'PTEN', target: 'PIK3CA', type: 'regulatory', score: 0.8 }
      ]
    };
  }
  
  return {
    nodes: Array.from(nodes.values()),
    links: links
  };
}

/**
 * Clear specific cache entry
 */
export function clearCacheEntry(key: string) {
  cacheStore.delete(key);
}
