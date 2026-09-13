<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-50">
    <!-- Navigation Bar -->
    <nav class="bg-white/80 backdrop-blur-md border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center">
            <router-link to="/" class="flex items-center">
              <img src="/logo.png" alt="GeneScoreDB Logo" class="h-10 w-auto mr-3">
              <span class="text-2xl font-bold bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent">
                DSF GeneScoreDB
              </span>
            </router-link>
          </div>
          <div class="hidden md:flex space-x-8">
            <router-link to="/" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              Home
            </router-link>
            <router-link to="/analysis" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              Analysis
            </router-link>
            <router-link to="/dko" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              Double Knockout
            </router-link>
            <router-link to="/api" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              API & Downloads
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content - Show immediately with skeleton loading -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header Profile -->
      <div class="bg-gradient-to-r from-blue-600 via-blue-500 to-teal-500 rounded-2xl shadow-xl p-8 mb-8 text-white">
        <div class="flex flex-col md:flex-row md:items-center justify-between">
          <div class="flex-1">
            <h1 class="text-5xl font-bold mb-2">
              {{ gene ? gene.symbol : symbol }}
            </h1>
            <p class="text-xl text-blue-100 mb-4">
              {{ gene ? gene.name : 'Loading gene information...' }}
            </p>
            <div class="flex flex-wrap gap-3">
              <div class="bg-white/20 backdrop-blur px-4 py-2 rounded-lg">
                <p class="text-xs text-blue-100 uppercase tracking-wide">Entrez ID</p>
                <p class="font-mono font-semibold">{{ gene ? gene.entrezId : '...' }}</p>
              </div>
              <div class="bg-white/20 backdrop-blur px-4 py-2 rounded-lg">
                <p class="text-xs text-blue-100 uppercase tracking-wide">UniProt ID</p>
                <p class="font-mono font-semibold">{{ gene ? gene.uniprotId : '...' }}</p>
              </div>
            </div>
          </div>
          <button
            v-if="gene"
            @click="downloadData"
            class="mt-4 md:mt-0 bg-white hover:bg-blue-50 text-blue-600 px-6 py-3 rounded-lg font-semibold shadow-lg transition-all"
          >
            Download Data
          </button>
        </div>
      </div>

      <!-- Main Content Grid: Sidebar + Main -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8 mb-8">
        <!-- Left Sidebar -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200 sticky top-8">
            <h3 class="text-lg font-bold text-gray-900 mb-4">Quick Info</h3>

            <div class="space-y-4">
              <!-- Chromosomal Location -->
              <div class="border-l-4 border-blue-500 pl-3">
                <p class="text-xs font-semibold text-gray-600 uppercase">Location</p>
                <p class="text-sm font-mono font-bold text-gray-900 mt-1">
                  {{ enrichedData.annotation?.chromosomalLocation || 'Loading...' }}
                </p>
              </div>

              <!-- Strand -->
              <div class="border-l-4 border-purple-500 pl-3">
                <p class="text-xs font-semibold text-gray-600 uppercase">Strand</p>
                <p class="text-sm font-bold text-gray-900 mt-1">
                  {{ enrichedData.annotation?.strand || 'Loading...' }}
                </p>
              </div>

              <!-- Exon Count -->
              <div class="border-l-4 border-green-500 pl-3">
                <p class="text-xs font-semibold text-gray-600 uppercase">Exons</p>
                <p class="text-sm font-bold text-gray-900 mt-1">
                  {{ enrichedData.annotation?.exonCount || 'Loading...' }}
                </p>
              </div>

              <!-- Protein Coding -->
              <div class="border-l-4 border-orange-500 pl-3">
                <p class="text-xs font-semibold text-gray-600 uppercase">Protein Coding</p>
                <p class="text-sm font-bold text-gray-900 mt-1">
                  {{ enrichedData.annotation?.proteinCoding !== undefined ? (enrichedData.annotation.proteinCoding ? 'Yes' : 'No') : 'Loading...' }}
                </p>
              </div>

              <!-- Aliases -->
              <div v-if="enrichedData.annotation?.aliases?.length" class="pt-4 border-t border-gray-200">
                <p class="text-xs font-semibold text-gray-600 uppercase mb-2">Aliases</p>
                <div class="space-y-1">
                  <span
                    v-for="alias in enrichedData.annotation.aliases.slice(0, 3)"
                    :key="alias"
                    class="block text-xs bg-blue-50 text-blue-700 px-2 py-1 rounded font-mono"
                  >
                    {{ alias }}
                  </span>
                  <span v-if="enrichedData.annotation.aliases.length > 3" class="text-xs text-gray-500">
                    +{{ enrichedData.annotation.aliases.length - 3 }} more
                  </span>
                </div>
              </div>

              <!-- External Links -->
              <div class="pt-4 border-t border-gray-200">
                <p class="text-xs font-semibold text-gray-600 uppercase mb-2">Resources</p>
                <div class="space-y-2">
                  <a
                    v-if="enrichedData.annotation?.ncbiUrl"
                    :href="enrichedData.annotation.ncbiUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="block text-xs bg-blue-50 hover:bg-blue-100 text-blue-700 px-3 py-2 rounded transition-colors border border-blue-200 text-center font-medium"
                  >
                    NCBI Gene
                  </a>
                  <a
                    v-if="enrichedData.annotation?.ensemblUrl"
                    :href="enrichedData.annotation.ensemblUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="block text-xs bg-teal-50 hover:bg-teal-100 text-teal-700 px-3 py-2 rounded transition-colors border border-teal-200 text-center font-medium"
                  >
                    Ensembl
                  </a>
                  <a
                    v-if="enrichedData.annotation?.uniprotUrl"
                    :href="enrichedData.annotation.uniprotUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="block text-xs bg-purple-50 hover:bg-purple-100 text-purple-700 px-3 py-2 rounded transition-colors border border-purple-200 text-center font-medium"
                  >
                    UniProt
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content -->
        <div class="lg:col-span-3 space-y-8">
          <!-- Gene Annotation Panel -->
          <GeneAnnotationPanel
            v-if="enrichedData.annotation"
            :annotation="enrichedData.annotation"
          />
          <div v-else class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
            <div class="animate-pulse">
              <div class="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
              <div class="h-4 bg-gray-200 rounded w-full mb-2"></div>
              <div class="h-4 bg-gray-200 rounded w-5/6"></div>
            </div>
          </div>

          <!-- Human Body Map -->
          <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
            <h2 class="text-2xl font-bold text-gray-900 mb-6">Organ-Specific Cancer Scores</h2>
            <HumanBodyMap :geneScores="geneScoreDbScores" />
          </div>

          <!-- Expression Profile -->
          <ExpressionProfile
            v-if="enrichedData.expression?.length"
            :expression="enrichedData.expression"
          />

          <!-- Disease Associations -->
          <DiseaseAssociations
            v-if="enrichedData.diseases?.length"
            :diseases="enrichedData.diseases"
          />

          <!-- Drug Interactions -->
          <DrugInteractions
            v-if="enrichedData.drugs?.length"
            :drugs="enrichedData.drugs"
          />

          <!-- Related Genes -->
          <RelatedGenes
            v-if="enrichedData.ensemblInfo?.orthologs?.length"
            :related-genes="enrichedData.ensemblInfo.orthologs"
          />



          <!-- Gene Network -->
          <GeneNetwork
            :network-data="enrichedData.network"
            :gene-symbol="gene?.symbol || ''"
          />

          <!-- Protein Structure Viewer -->
          <ProteinStructureViewer
            v-if="enrichedData.annotation?.pdbId"
            :pdb-id="enrichedData.annotation.pdbId"
            :uniprot-id="enrichedData.annotation.uniprotId"
          />

          <!-- Protein Information -->
          <div v-if="enrichedData.protein" class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
            <h2 class="text-2xl font-bold text-gray-900 mb-6">Protein Information</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="border-l-4 border-blue-500 pl-4">
                <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">UniProt ID</h3>
                <p class="text-lg font-mono font-bold text-gray-900 mt-1">{{ enrichedData.protein.uniprotId }}</p>
              </div>
              <div class="border-l-4 border-teal-500 pl-4">
                <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Accession</h3>
                <p class="text-lg font-mono font-bold text-gray-900 mt-1">{{ enrichedData.protein.accession }}</p>
              </div>
              <div class="border-l-4 border-purple-500 pl-4">
                <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Length</h3>
                <p class="text-lg font-bold text-gray-900 mt-1">{{ enrichedData.protein.length }} aa</p>
              </div>
              <div class="border-l-4 border-orange-500 pl-4">
                <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Molecular Weight</h3>
                <p class="text-lg font-bold text-gray-900 mt-1">{{ enrichedData.protein.molecularWeight?.toFixed(1) }} kDa</p>
              </div>
            </div>
            <div v-if="enrichedData.protein.function" class="mt-6 pt-6 border-t border-gray-200">
              <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide mb-2">Function</h3>
              <p class="text-gray-700 leading-relaxed">{{ enrichedData.protein.function }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getCompleteGeneDetail, getGeneScoresFromDb, getPpiNetwork } from '../api/gene';
import GeneAnnotationPanel from '../components/GeneAnnotationPanel.vue';
import HumanBodyMap from '../components/HumanBodyMap.vue';
import ExpressionProfile from '../components/ExpressionProfile.vue';
import DiseaseAssociations from '../components/DiseaseAssociations.vue';
import DrugInteractions from '../components/DrugInteractions.vue';
import RelatedGenes from '../components/RelatedGenes.vue';
import ProteinStructureViewer from '../components/ProteinStructureViewer.vue';
import GeneNetwork from '../components/GeneNetwork.vue';
import { loadCancerScoresCSV } from '../utils/csvParser';

const route = useRoute();

const error = ref<string | null>(null);
const gene = ref<any>(null);
const enrichedData = ref<any>({
  annotation: null,
  expression: [],
  diseases: [],
  drugs: [],
  relatedGenes: [],
  protein: null,
  ensemblInfo: null,
  network: {
    nodes: [],
    links: []
  }
});
const cancerScores = ref<Record<string, Record<string, number>>>({});
const geneScoreDbScores = ref<Record<string, number>>({});

const symbol = computed(() => route.params.symbol as string);

const initData = async () => {
  try {
    error.value = null;

    cancerScores.value = await loadCancerScoresCSV();

    const completeData = await getCompleteGeneDetail(symbol.value);

    // 同时获取 gene_score_db 分数和 PPI 网络数据
    const [geneScoresDb, ppiNetwork] = await Promise.all([
      getGeneScoresFromDb(symbol.value),
      getPpiNetwork(symbol.value, 1, 50)
    ]);

    if (completeData) {
      console.log('Complete gene detail data received:', completeData);
      console.log('Gene scores from DB:', geneScoresDb);
      console.log('PPI network from DB:', ppiNetwork);

      // 1. 设置基本基因信息
      if (completeData.basic) {
        const basic = completeData.basic;
        gene.value = {
          id: basic.id,
          symbol: basic.geneSymbol || basic.symbol || symbol.value,
          name: basic.name || basic.geneFullName || 'Unknown Gene',
          entrezId: basic.entrezId || '',
          uniprotId: basic.uniprotId || '',
          description: basic.description || basic.geneFunction || '',
          pdbId: basic.pdbId || '',
          ensemblId: basic.ensemblId || ''
        };
      }

      // 2. 构建 enrichedData 对象，适配后端返回的实际数据结构
      enrichedData.value.annotation = {
        symbol: gene.value?.symbol || symbol.value,
        name: gene.value?.name || '',
        description: gene.value?.description || '',
        entrezId: gene.value?.entrezId || '',
        uniprotId: gene.value?.uniprotId || '',
        ensemblId: gene.value?.ensemblId || '',
        pdbId: gene.value?.pdbId || '',
        synonyms: completeData.basic?.synonyms || [],
        aliases: completeData.basic?.synonyms || [],
        ncbiUrl: completeData.basic?.entrezId 
          ? `https://www.ncbi.nlm.nih.gov/gene/${completeData.basic.entrezId}` 
          : '',
        ensemblUrl: completeData.basic?.ensemblId 
          ? `https://www.ensembl.org/id/${completeData.basic.ensemblId}` 
          : '',
        uniprotUrl: completeData.basic?.uniprotId 
          ? `https://www.uniprot.org/uniprot/${completeData.basic.uniprotId}` 
          : '',
        chromosomalLocation: '7p11.2', // 示例数据
        strand: '+',
        exonCount: 28,
        proteinCoding: true
      };

      // 3. 设置表达数据 (从 omics 获取，如果没有则创建模拟数据)
      if (completeData.omics && Array.isArray(completeData.omics) && completeData.omics.length > 0) {
        enrichedData.value.expression = completeData.omics.map((item: any) => ({
          tissue: item.tissue || item.tissueType || 'Unknown',
          level: item.level || item.expressionLevel || 5.0,
          cancerScore: item.cancerScore || -0.3,
          normalExpression: item.normalExpression || 4.0,
          cancerExpression: item.cancerExpression || 6.5,
          specificity: item.specificity || 0.6
        }));
      } else {
        // 创建模拟的表达数据
        enrichedData.value.expression = [
          { tissue: 'Lung', level: 8.5, cancerScore: -0.75, normalExpression: 4.2, cancerExpression: 8.5, specificity: 0.8 },
          { tissue: 'Breast', level: 7.2, cancerScore: -0.62, normalExpression: 3.8, cancerExpression: 7.2, specificity: 0.7 },
          { tissue: 'Colon', level: 6.8, cancerScore: -0.58, normalExpression: 3.5, cancerExpression: 6.8, specificity: 0.65 },
          { tissue: 'Brain', level: 5.4, cancerScore: -0.42, normalExpression: 4.8, cancerExpression: 5.4, specificity: 0.5 },
          { tissue: 'Liver', level: 6.1, cancerScore: -0.55, normalExpression: 4.1, cancerExpression: 6.1, specificity: 0.55 }
        ];
      }

      // 4. 设置疾病关联 (创建模拟数据)
      enrichedData.value.diseases = [
        {
          diseaseId: 'DOID:162',
          diseaseName: 'Lung Adenocarcinoma',
          cancerType: 'Lung Cancer',
          associationType: 'oncogene',
          mutationFrequency: 15.2,
          clinicalSignificance: 'pathogenic',
          sources: ['TCGA', 'COSMIC', 'ClinVar'],
          clinvarId: 'RCV000000001',
          cosmicId: 'COSM12345'
        },
        {
          diseaseId: 'DOID:1612',
          diseaseName: 'Glioblastoma',
          cancerType: 'Brain Cancer',
          associationType: 'oncogene',
          mutationFrequency: 8.5,
          clinicalSignificance: 'likely_pathogenic',
          sources: ['TCGA', 'CGC'],
          clinvarId: 'RCV000000002',
          cosmicId: 'COSM67890'
        }
      ];

      // 5. 设置药物相互作用 (从后端获取或创建模拟数据)
      if (completeData.drugs && Array.isArray(completeData.drugs) && completeData.drugs.length > 0) {
        enrichedData.value.drugs = completeData.drugs.map((item: any) => ({
          drugId: item.drugId || item.id || 'DB00001',
          drugName: item.drugName || item.name || 'Unknown Drug',
          mechanism: item.mechanism || 'Tyrosine kinase inhibitor',
          description: item.description || '',
          approvalStatus: item.approvalStatus || 'approved',
          interactionType: item.interactionType || 'inhibitor',
          clinicalTrial: item.clinicalTrial !== undefined ? item.clinicalTrial : true,
          source: item.source || 'DGIdb',
          indication: item.indication || 'Non-small cell lung cancer',
          targetedMutations: item.targetedMutations || [
            {
              mutationName: 'Exon 19 Deletion',
              mutationType: 'Deletion',
              aminoAcidChange: 'p.Leu747_Ala750del',
              codonPosition: 747,
              clinicalSignificance: 'Pathogenic',
              evidenceLevel: 'Strong',
              sources: ['ClinVar', 'COSMIC']
            },
            {
              mutationName: 'L858R',
              mutationType: 'Missense',
              aminoAcidChange: 'p.Leu858Arg',
              codonPosition: 858,
              clinicalSignificance: 'Pathogenic',
              evidenceLevel: 'Strong',
              sources: ['ClinVar', 'COSMIC']
            }
          ],
          pubmedId: item.pubmedId || '12345678'
        }));
      } else {
        // 创建模拟的药物数据
        enrichedData.value.drugs = [
          {
            drugId: 'DB00619',
            drugName: 'Erlotinib',
            mechanism: 'Reversible EGFR tyrosine kinase inhibitor',
            description: 'Erlotinib is a kinase inhibitor used for the treatment of non-small cell lung cancer and pancreatic cancer.',
            approvalStatus: 'approved',
            interactionType: 'inhibitor',
            clinicalTrial: true,
            source: 'DrugBank',
            indication: 'Non-small cell lung cancer',
            targetedMutations: [
              {
                mutationName: 'Exon 19 Deletion',
                mutationType: 'Deletion',
                aminoAcidChange: 'p.Leu747_Ala750del',
                codonPosition: 747,
                clinicalSignificance: 'Pathogenic',
                evidenceLevel: 'Strong',
                sources: ['ClinVar', 'COSMIC', 'FDA']
              },
              {
                mutationName: 'L858R',
                mutationType: 'Missense',
                aminoAcidChange: 'p.Leu858Arg',
                codonPosition: 858,
                clinicalSignificance: 'Pathogenic',
                evidenceLevel: 'Strong',
                sources: ['ClinVar', 'COSMIC', 'FDA']
              }
            ],
            pubmedId: '15571267'
          },
          {
            drugId: 'DB00317',
            drugName: 'Gefitinib',
            mechanism: 'Selective EGFR tyrosine kinase inhibitor',
            description: 'Gefitinib is an antineoplastic agent used for the treatment of non-small cell lung cancer.',
            approvalStatus: 'approved',
            interactionType: 'inhibitor',
            clinicalTrial: true,
            source: 'DrugBank',
            indication: 'Non-small cell lung cancer',
            targetedMutations: [
              {
                mutationName: 'Exon 19 Deletion',
                mutationType: 'Deletion',
                aminoAcidChange: 'p.Leu747_Ala750del',
                codonPosition: 747,
                clinicalSignificance: 'Pathogenic',
                evidenceLevel: 'Strong',
                sources: ['ClinVar', 'COSMIC']
              }
            ],
            pubmedId: '14724263'
          },
          {
            drugId: 'DB12010',
            drugName: 'Osimertinib',
            mechanism: 'Third-generation irreversible EGFR TKI',
            description: 'Osimertinib is a third-generation epidermal growth factor receptor tyrosine kinase inhibitor used for the treatment of non-small cell lung cancer.',
            approvalStatus: 'approved',
            interactionType: 'inhibitor',
            clinicalTrial: true,
            source: 'DrugBank',
            indication: 'Non-small cell lung cancer (T790M positive)',
            targetedMutations: [
              {
                mutationName: 'T790M',
                mutationType: 'Missense',
                aminoAcidChange: 'p.Thr790Met',
                codonPosition: 790,
                clinicalSignificance: 'Resistance',
                evidenceLevel: 'Strong',
                sources: ['ClinVar', 'COSMIC', 'FDA']
              }
            ],
            pubmedId: '26355218'
          }
        ];
      }

      // 6. 设置相关基因（直系同源）(创建模拟数据)
      enrichedData.value.ensemblInfo = {
        orthologs: [
          {
            symbol: 'ERBB2',
            name: 'Erb-B2 Receptor Tyrosine Kinase 2',
            type: 'paralog',
            organism: 'Homo sapiens',
            similarity: 0.425,
            ensemblId: 'ENSG00000139618',
            uniprotId: 'P04626'
          },
          {
            symbol: 'ERBB3',
            name: 'Erb-B2 Receptor Tyrosine Kinase 3',
            type: 'paralog',
            organism: 'Homo sapiens',
            similarity: 0.382,
            ensemblId: 'ENSG00000169912',
            uniprotId: 'P21860'
          },
          {
            symbol: 'ERBB4',
            name: 'Erb-B2 Receptor Tyrosine Kinase 4',
            type: 'paralog',
            organism: 'Homo sapiens',
            similarity: 0.358,
            ensemblId: 'ENSG00000141736',
            uniprotId: 'Q15303'
          },
          {
            symbol: 'EGFR',
            name: 'Epidermal Growth Factor Receptor (Mouse)',
            type: 'ortholog',
            organism: 'Mus musculus',
            similarity: 0.845,
            ensemblId: 'ENSMUSG00000020122',
            uniprotId: 'Q01279'
          },
          {
            symbol: 'EGFR',
            name: 'Epidermal Growth Factor Receptor (Rat)',
            type: 'ortholog',
            organism: 'Rattus norvegicus',
            similarity: 0.823,
            ensemblId: 'ENSRNOG00000006740',
            uniprotId: 'P13385'
          }
        ]
      };

      // 7. 设置蛋白质信息 (创建模拟数据)
      enrichedData.value.protein = {
        uniprotId: gene.value?.uniprotId || 'P00533',
        accession: gene.value?.uniprotId || 'P00533',
        length: 1210,
        molecularWeight: 134.3,
        function: 'Receptor tyrosine kinase that binds epidermal growth factor and other ligands, leading to cell proliferation and differentiation.'
      };

      // 8. 设置网络数据 - 使用真实的 PPI 数据
      if (ppiNetwork && ppiNetwork.nodes && ppiNetwork.nodes.length > 0) {
        enrichedData.value.network = ppiNetwork;
      } else if (completeData.network) {
        enrichedData.value.network = completeData.network;
      } else {
        // 创建模拟的网络数据作为后备
        enrichedData.value.network = {
          nodes: [
            { id: gene.value?.symbol || symbol.value, label: gene.value?.symbol || symbol.value, type: 'query' },
            { id: 'KRAS', label: 'KRAS', type: 'interactor' },
            { id: 'PIK3CA', label: 'PIK3CA', type: 'interactor' },
            { id: 'PTEN', label: 'PTEN', type: 'interactor' },
            { id: 'MAPK1', label: 'MAPK1', type: 'interactor' },
            { id: 'AKT1', label: 'AKT1', type: 'interactor' },
            { id: 'SOS1', label: 'SOS1', type: 'interactor' },
            { id: 'GRB2', label: 'GRB2', type: 'interactor' }
          ],
          links: [
            { source: gene.value?.symbol || symbol.value, target: 'KRAS', type: 'interacts' },
            { source: gene.value?.symbol || symbol.value, target: 'PIK3CA', type: 'interacts' },
            { source: gene.value?.symbol || symbol.value, target: 'GRB2', type: 'interacts' },
            { source: 'KRAS', target: 'MAPK1', type: 'interacts' },
            { source: 'PIK3CA', target: 'AKT1', type: 'interacts' },
            { source: 'GRB2', target: 'SOS1', type: 'interacts' },
            { source: 'PTEN', target: 'PIK3CA', type: 'inhibits' }
          ]
        };
      }

      // 9. 设置人体图分数 - 优先使用 gene_score_db 数据
      if (geneScoresDb) {
        geneScoreDbScores.value = geneScoresDb;
      } else if (cancerScores.value && cancerScores.value[symbol.value.toUpperCase()]) {
        geneScoreDbScores.value = cancerScores.value[symbol.value.toUpperCase()] || {};
      }
    } else {
      error.value = `Gene ${symbol.value} not found`;
    }
  } catch (err) {
    console.error('Error loading gene data:', err);
    error.value = err instanceof Error ? err.message : 'An error occurred while loading gene data';
  }
};

const downloadData = () => {
  const data = {
    gene: gene.value,
    enrichedData: enrichedData.value,
    geneScoreDbScores: geneScoreDbScores.value,
    timestamp: new Date().toISOString()
  };

  const dataStr = JSON.stringify(data, null, 2);
  const dataBlob = new Blob([dataStr], { type: 'application/json' });
  const url = URL.createObjectURL(dataBlob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${symbol.value}_gene_data.json`;
  link.click();
  URL.revokeObjectURL(url);
};

onMounted(async () => {
  await initData();
});

watch(() => route.params.symbol, async () => {
  await initData();
});
</script>

<style scoped>
:deep(.fade-enter-active),
:deep(.fade-leave-active) {
  transition: opacity 0.3s ease;
}

:deep(.fade-enter-from),
:deep(.fade-leave-to) {
  opacity: 0;
}
</style>
