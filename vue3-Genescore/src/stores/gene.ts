import { defineStore } from 'pinia';
import * as geneApi from '@/api/gene';

interface Gene {
  id: number;
  symbol: string;
  name: string;
  entrezId: string;
  uniprotId: string;
  description: string;
  biologicalProcess?: string;
  molecularFunction?: string;
  pdbId?: string;
}

interface SearchResult {
  symbol: string;
  name: string;
  id: string;
}

// 模拟基因数据（作为fallback）
const mockGenes = [
  { symbol: 'TP53', name: 'Tumor protein p53', id: '1' },
  { symbol: 'EGFR', name: 'Epidermal growth factor receptor', id: '2' },
  { symbol: 'KRAS', name: 'Kirsten rat sarcoma viral oncogene homolog', id: '3' },
  { symbol: 'BRCA1', name: 'Breast cancer 1, early onset', id: '4' },
  { symbol: 'PTEN', name: 'Phosphatase and tensin homolog', id: '5' },
  { symbol: 'PIK3CA', name: 'Phosphatidylinositol-4,5-bisphosphate 3-kinase', id: '6' },
  { symbol: 'AKT1', name: 'AKT serine/threonine kinase 1', id: '7' },
  { symbol: 'MTOR', name: 'Mechanistic target of rapamycin kinase', id: '8' },
  { symbol: 'MYC', name: 'MYC proto-oncogene', id: '9' },
  { symbol: 'RB1', name: 'Retinoblastoma 1', id: '10' }
];

export const useGeneStore = defineStore('gene', {
  state: () => ({
    selectedGene: null as Gene | null,
    searchResults: [] as SearchResult[],
    isLoading: false,
    error: null as string | null
  }),
  
  actions: {
    async searchGenes(query: string): Promise<SearchResult[]> {
      this.isLoading = true;
      this.error = null;
      
      try {
        console.log('Store: Calling API with query:', query);
        const results = await geneApi.searchGenes(query);
        console.log('Store: API returned results:', results);
        // 将后端返回的格式转换为前端期望的格式
        const formattedResults = results.map((gene: any) => ({
          id: gene.id?.toString() || '1',
          symbol: gene.geneSymbol || gene.symbol,
          name: gene.geneFullName || gene.name
        }));
        this.searchResults = formattedResults;
        return formattedResults;
      } catch (error) {
        console.error('Search error, using mock data:', error);
        // 使用模拟数据作为fallback
        const results = mockGenes.filter(gene => 
          gene.symbol.toLowerCase().includes(query.toLowerCase()) || 
          gene.name.toLowerCase().includes(query.toLowerCase())
        );
        console.log('Store: Using mock data, results:', results);
        this.searchResults = results;
        return results;
      } finally {
        this.isLoading = false;
      }
    },
    
    async getGeneDetails(symbol: string): Promise<Gene | null> {
      this.isLoading = true;
      this.error = null;
      
      try {
        const gene = await geneApi.getGeneOverview(symbol);
        this.selectedGene = gene;
        return gene;
      } catch (error) {
        console.error('Get gene details error:', error);
        this.error = 'Failed to fetch gene details';
        return null;
      } finally {
        this.isLoading = false;
      }
    },
    
    async getBodyMapData(symbol: string) {
      try {
        return await geneApi.getBodyMapData(symbol);
      } catch (error) {
        console.error('Get body map data error:', error);
        return [];
      }
    },
    
    async getSankeyData(symbol: string) {
      try {
        return await geneApi.getSankeyData(symbol);
      } catch (error) {
        console.error('Get sankey data error:', error);
        return { nodes: [], links: [] };
      }
    },
    
    async getAnalyticsData(symbol: string) {
      try {
        return await geneApi.getAnalyticsData(symbol);
      } catch (error) {
        console.error('Get analytics data error:', error);
        return { scatterData: [], boxplotData: [] };
      }
    },
    
    async getCoDependencies(symbol: string) {
      try {
        return await geneApi.getCoDependencies(symbol);
      } catch (error) {
        console.error('Get co-dependencies error:', error);
        return [];
      }
    },
    
    async simulateDKO(gene1: string, gene2: string) {
      this.isLoading = true;
      this.error = null;
      
      try {
        return await geneApi.simulateDKO(gene1, gene2);
      } catch (error) {
        console.error('DKO simulation error:', error);
        this.error = 'Failed to run DKO simulation';
        return null;
      } finally {
        this.isLoading = false;
      }
    },
    
    async analyzeDKO(genes: string[]) {
      this.isLoading = true;
      this.error = null;
      
      try {
        return await geneApi.analyzeDKO(genes);
      } catch (error) {
        console.error('DKO analyze error:', error);
        this.error = 'Failed to run DKO analyze';
        return null;
      } finally {
        this.isLoading = false;
      }
    }
  }
});