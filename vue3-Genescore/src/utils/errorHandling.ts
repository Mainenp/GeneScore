// src/utils/errorHandling.ts
import type {
  GeneAnnotation,
  ExpressionData,
  DiseaseAssociation,
  DrugInteraction,
  RelatedGene,
  ProteinInfo
} from '@/types/gene';

/**
 * Mock data for fallback when APIs are unavailable
 */
export const mockGeneData: Record<string, any> = {
  TP53: {
    annotation: {
      symbol: 'TP53',
      name: 'Tumor protein p53',
      aliases: ['P53', 'LFS1', 'TRP53'],
      description:
        'This gene encodes a tumor suppressor protein that regulates cell cycle progression and apoptosis. It is frequently mutated in human cancers.',
      chromosomalLocation: '17p13.1',
      strand: '+',
      exonCount: 11,
      proteinCoding: true,
      entrezId: '7157',
      ensemblId: 'ENSG00000141510',
      uniprotId: 'P04637',
      pdbId: '1TUP',
      ncbiUrl: 'https://www.ncbi.nlm.nih.gov/gene/7157',
      ensemblUrl: 'https://www.ensembl.org/Homo_sapiens/Gene/Summary?g=ENSG00000141510',
      uniprotUrl: 'https://www.uniprot.org/uniprotkb/P04637'
    },
    expression: [
      {
        tissue: 'Breast',
        organ: 'Breast',
        level: 7.5,
        cancerScore: -0.8,
        normalExpression: 5.2,
        cancerExpression: 8.9,
        specificity: 0.6
      },
      {
        tissue: 'Lung',
        organ: 'Lung',
        level: 6.8,
        cancerScore: -0.6,
        normalExpression: 4.5,
        cancerExpression: 7.2,
        specificity: 0.5
      },
      {
        tissue: 'Colon',
        organ: 'Colon',
        level: 5.2,
        cancerScore: -0.4,
        normalExpression: 3.8,
        cancerExpression: 6.1,
        specificity: 0.4
      }
    ],
    diseases: [
      {
        diseaseId: 'C0006826',
        diseaseName: 'Breast Cancer',
        cancerType: 'Breast',
        mutationFrequency: 80,
        clinicalSignificance: 'pathogenic',
        associationType: 'tumor_suppressor',
        sources: ['ClinVar', 'COSMIC'],
        clinvarId: 'RCV000000001',
        cosmicId: 'COSM123456'
      },
      {
        diseaseId: 'C0007131',
        diseaseName: 'Carcinoma, Non-Small-Cell Lung',
        cancerType: 'Lung',
        mutationFrequency: 50,
        clinicalSignificance: 'pathogenic',
        associationType: 'tumor_suppressor',
        sources: ['ClinVar', 'COSMIC']
      }
    ],
    drugs: [
      {
        drugName: 'Nutlin-3',
        drugId: 'DB02341',
        mechanism: 'MDM2 Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: true,
        approvalStatus: 'clinical_trial',
        indication: 'Various cancers with wild-type TP53',
        description: 'Small molecule that disrupts the interaction between MDM2 and TP53, allowing TP53 to accumulate and induce apoptosis in tumor cells.',
        source: 'DrugBank',
        pubmedId: '12345678',
        targetedMutations: [
          {
            mutationName: 'Wild-type TP53',
            mutationType: 'Wild-type',
            clinicalSignificance: 'Requires functional p53 pathway',
            evidenceLevel: 'Strong',
            sources: ['DrugBank', 'ClinicalTrials.gov']
          }
        ]
      },
      {
        drugName: 'APR-246',
        drugId: 'DB11695',
        mechanism: 'Mutant p53 Reactivator',
        interactionType: 'modulator',
        clinicalTrial: true,
        approvalStatus: 'clinical_trial',
        indication: 'Ovarian cancer, Acute myeloid leukemia',
        description: 'Reactivates mutant p53 by restoring its wild-type conformation and function.',
        source: 'ClinicalTrials.gov',
        targetedMutations: [
          {
            mutationName: 'R175H',
            mutationType: 'Missense',
            aminoAcidChange: 'Arg175His',
            codonPosition: 175,
            clinicalSignificance: 'Gain-of-function mutation',
            evidenceLevel: 'Strong',
            sources: ['PubMed', 'ClinicalTrials.gov']
          },
          {
            mutationName: 'G245S',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly245Ser',
            codonPosition: 245,
            clinicalSignificance: 'Gain-of-function mutation',
            evidenceLevel: 'Moderate',
            sources: ['PubMed']
          },
          {
            mutationName: 'R273H',
            mutationType: 'Missense',
            aminoAcidChange: 'Arg273His',
            codonPosition: 273,
            clinicalSignificance: 'Gain-of-function mutation',
            evidenceLevel: 'Strong',
            sources: ['PubMed', 'ClinicalTrials.gov']
          }
        ]
      }
    ],
    protein: {
      uniprotId: 'P04637',
      accession: 'P04637',
      length: 393,
      molecularWeight: 43.7,
      domains: [
        {
          name: 'P53 DNA-binding domain',
          start: 102,
          end: 292,
          description: 'DNA-binding domain'
        }
      ],
      modifications: [
        {
          type: 'Phosphorylation',
          position: 15,
          description: 'Phosphorylated by ATM/ATR'
        }
      ],
      function: 'Acts as a tumor suppressor. Regulates cell cycle progression and apoptosis.',
      subcellularLocalization: ['Nucleus', 'Cytoplasm']
    },
    ensemblInfo: {
      ensemblId: 'ENSG00000141510',
      orthologs: [
        {
          symbol: 'Tp53',
          name: 'Transformation related protein 53',
          type: 'ortholog',
          organism: 'Mus musculus',
          similarity: 0.98,
          ensemblId: 'ENSMUSG00000059552'
        }
      ]
    }
  },
  EGFR: {
    annotation: {
      symbol: 'EGFR',
      name: 'Epidermal growth factor receptor',
      aliases: ['ERBB1', 'HER1'],
      description:
        'This gene encodes a receptor tyrosine kinase that regulates cell growth and differentiation. It is frequently overexpressed in various cancers.',
      chromosomalLocation: '7p11.2',
      strand: '+',
      exonCount: 28,
      proteinCoding: true,
      entrezId: '1956',
      ensemblId: 'ENSG00000005249',
      uniprotId: 'P00533',
      pdbId: '1M17',
      ncbiUrl: 'https://www.ncbi.nlm.nih.gov/gene/1956',
      ensemblUrl: 'https://www.ensembl.org/Homo_sapiens/Gene/Summary?g=ENSG00000005249',
      uniprotUrl: 'https://www.uniprot.org/uniprotkb/P00533'
    },
    expression: [
      {
        tissue: 'Lung',
        organ: 'Lung',
        level: 8.2,
        cancerScore: -0.7,
        normalExpression: 6.5,
        cancerExpression: 9.1,
        specificity: 0.7
      },
      {
        tissue: 'Breast',
        organ: 'Breast',
        level: 6.5,
        cancerScore: -0.5,
        normalExpression: 4.2,
        cancerExpression: 7.8,
        specificity: 0.5
      }
    ],
    diseases: [
      {
        diseaseId: 'C0007131',
        diseaseName: 'Carcinoma, Non-Small-Cell Lung',
        cancerType: 'Lung',
        mutationFrequency: 15,
        clinicalSignificance: 'pathogenic',
        associationType: 'oncogene',
        sources: ['ClinVar', 'COSMIC']
      }
    ],
    drugs: [
      {
        drugName: 'Erlotinib',
        drugId: 'DB00530',
        mechanism: 'EGFR Tyrosine Kinase Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: false,
        approvalStatus: 'approved',
        indication: 'Non-small cell lung cancer (NSCLC)',
        description: 'A reversible tyrosine kinase inhibitor targeting EGFR, used for treatment of NSCLC with EGFR-activating mutations.',
        source: 'DrugBank',
        pubmedId: '87654321',
        targetedMutations: [
          {
            mutationName: 'Exon 19 Deletion',
            mutationType: 'Deletion',
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank', 'ClinicalTrials.gov']
          },
          {
            mutationName: 'L858R',
            mutationType: 'Missense',
            aminoAcidChange: 'Leu858Arg',
            codonPosition: 858,
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank', 'ClinicalTrials.gov']
          }
        ]
      },
      {
        drugName: 'Gefitinib',
        drugId: 'DB00619',
        mechanism: 'EGFR Tyrosine Kinase Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: false,
        approvalStatus: 'approved',
        indication: 'Non-small cell lung cancer (NSCLC)',
        description: 'First-generation EGFR tyrosine kinase inhibitor for the treatment of NSCLC with EGFR mutations.',
        source: 'DrugBank',
        targetedMutations: [
          {
            mutationName: 'Exon 19 Deletion',
            mutationType: 'Deletion',
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank']
          },
          {
            mutationName: 'L858R',
            mutationType: 'Missense',
            aminoAcidChange: 'Leu858Arg',
            codonPosition: 858,
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank']
          }
        ]
      },
      {
        drugName: 'Osimertinib',
        drugId: 'DB09330',
        mechanism: 'Third-generation EGFR TKI',
        interactionType: 'inhibitor',
        clinicalTrial: false,
        approvalStatus: 'approved',
        indication: 'EGFR T790M-positive NSCLC',
        description: 'Third-generation irreversible EGFR tyrosine kinase inhibitor that targets both activating mutations and T790M resistance mutation.',
        source: 'DrugBank',
        targetedMutations: [
          {
            mutationName: 'Exon 19 Deletion',
            mutationType: 'Deletion',
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank']
          },
          {
            mutationName: 'L858R',
            mutationType: 'Missense',
            aminoAcidChange: 'Leu858Arg',
            codonPosition: 858,
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank']
          },
          {
            mutationName: 'T790M',
            mutationType: 'Missense',
            aminoAcidChange: 'Thr790Met',
            codonPosition: 790,
            clinicalSignificance: 'Resistance mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank', 'PubMed']
          }
        ]
      },
      {
        drugName: 'Afatinib',
        drugId: 'DB08916',
        mechanism: 'Second-generation EGFR TKI',
        interactionType: 'inhibitor',
        clinicalTrial: false,
        approvalStatus: 'approved',
        indication: 'Metastatic NSCLC',
        description: 'Second-generation irreversible EGFR tyrosine kinase inhibitor that targets EGFR, HER2, and HER4.',
        source: 'DrugBank',
        targetedMutations: [
          {
            mutationName: 'Exon 19 Deletion',
            mutationType: 'Deletion',
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank']
          },
          {
            mutationName: 'L858R',
            mutationType: 'Missense',
            aminoAcidChange: 'Leu858Arg',
            codonPosition: 858,
            clinicalSignificance: 'Activating mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank']
          },
          {
            mutationName: 'G719X',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly719X',
            codonPosition: 719,
            clinicalSignificance: 'Uncommon activating mutation',
            evidenceLevel: 'Moderate',
            sources: ['FDA', 'PubMed']
          },
          {
            mutationName: 'S768I',
            mutationType: 'Missense',
            aminoAcidChange: 'Ser768Ile',
            codonPosition: 768,
            clinicalSignificance: 'Uncommon activating mutation',
            evidenceLevel: 'Moderate',
            sources: ['FDA', 'PubMed']
          },
          {
            mutationName: 'L861Q',
            mutationType: 'Missense',
            aminoAcidChange: 'Leu861Gln',
            codonPosition: 861,
            clinicalSignificance: 'Uncommon activating mutation',
            evidenceLevel: 'Moderate',
            sources: ['FDA', 'PubMed']
          }
        ]
      }
    ],
    protein: {
      uniprotId: 'P00533',
      accession: 'P00533',
      length: 1210,
      molecularWeight: 134.2,
      domains: [
        {
          name: 'Extracellular domain',
          start: 1,
          end: 645,
          description: 'Ligand-binding domain'
        },
        {
          name: 'Tyrosine kinase domain',
          start: 712,
          end: 979,
          description: 'Catalytic domain'
        }
      ],
      modifications: [
        {
          type: 'Phosphorylation',
          position: 1068,
          description: 'Autophosphorylation site'
        }
      ],
      function: 'Receptor tyrosine kinase that regulates cell growth and differentiation.',
      subcellularLocalization: ['Cell membrane']
    },
    ensemblInfo: {
      ensemblId: 'ENSG00000005249',
      orthologs: []
    }
  },
  KRAS: {
    annotation: {
      symbol: 'KRAS',
      name: 'Kirsten rat sarcoma viral oncogene homolog',
      aliases: ['RASK2', 'KRAS2'],
      description:
        'This gene encodes a GTPase that functions as an oncogene in many cancers. It is one of the most frequently mutated genes in human malignancies.',
      chromosomalLocation: '12p12.1',
      strand: '+',
      exonCount: 4,
      proteinCoding: true,
      entrezId: '3845',
      ensemblId: 'ENSG00000133703',
      uniprotId: 'P01116',
      pdbId: '4LQM',
      ncbiUrl: 'https://www.ncbi.nlm.nih.gov/gene/3845',
      ensemblUrl: 'https://www.ensembl.org/Homo_sapiens/Gene/Summary?g=ENSG00000133703',
      uniprotUrl: 'https://www.uniprot.org/uniprotkb/P01116'
    },
    expression: [
      {
        tissue: 'Pancreas',
        organ: 'Pancreas',
        level: 7.8,
        cancerScore: -0.85,
        normalExpression: 5.2,
        cancerExpression: 9.2,
        specificity: 0.75
      },
      {
        tissue: 'Colon',
        organ: 'Colon',
        level: 6.5,
        cancerScore: -0.6,
        normalExpression: 4.1,
        cancerExpression: 8.0,
        specificity: 0.6
      }
    ],
    diseases: [
      {
        diseaseId: 'C0007131',
        diseaseName: 'Carcinoma, Non-Small-Cell Lung',
        cancerType: 'Lung',
        mutationFrequency: 32,
        clinicalSignificance: 'pathogenic',
        associationType: 'oncogene',
        sources: ['ClinVar', 'COSMIC']
      },
      {
        diseaseId: 'C0007102',
        diseaseName: 'Carcinoma, Pancreatic',
        cancerType: 'Pancreas',
        mutationFrequency: 90,
        clinicalSignificance: 'pathogenic',
        associationType: 'oncogene',
        sources: ['ClinVar', 'COSMIC']
      }
    ],
    drugs: [
      {
        drugName: 'Sotorasib',
        drugId: 'DB15554',
        mechanism: 'KRAS G12C Covalent Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: false,
        approvalStatus: 'approved',
        indication: 'KRAS G12C-mutated non-small cell lung cancer',
        description: 'First-in-class covalent inhibitor that specifically targets KRAS G12C mutation, locking KRAS in its inactive GDP-bound state.',
        source: 'DrugBank',
        targetedMutations: [
          {
            mutationName: 'G12C',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly12Cys',
            codonPosition: 12,
            clinicalSignificance: 'Driver mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank', 'ClinicalTrials.gov']
          }
        ]
      },
      {
        drugName: 'Adagrasib',
        drugId: 'DB16636',
        mechanism: 'KRAS G12C Covalent Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: false,
        approvalStatus: 'approved',
        indication: 'KRAS G12C-mutated non-small cell lung cancer',
        description: 'Highly selective, covalent KRAS G12C inhibitor with improved pharmacokinetic properties and longer half-life.',
        source: 'DrugBank',
        targetedMutations: [
          {
            mutationName: 'G12C',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly12Cys',
            codonPosition: 12,
            clinicalSignificance: 'Driver mutation',
            evidenceLevel: 'Strong',
            sources: ['FDA', 'DrugBank', 'ClinicalTrials.gov']
          }
        ]
      },
      {
        drugName: 'MRTX1133',
        drugId: 'DB17892',
        mechanism: 'KRAS G12D Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: true,
        approvalStatus: 'clinical_trial',
        indication: 'KRAS G12D-mutated solid tumors',
        description: 'Selective covalent inhibitor targeting KRAS G12D mutation, currently in clinical development.',
        source: 'ClinicalTrials.gov',
        targetedMutations: [
          {
            mutationName: 'G12D',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly12Asp',
            codonPosition: 12,
            clinicalSignificance: 'Driver mutation',
            evidenceLevel: 'Moderate',
            sources: ['ClinicalTrials.gov', 'PubMed']
          }
        ]
      },
      {
        drugName: 'Divarasib',
        drugId: 'DB18022',
        mechanism: 'KRAS G12C Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: true,
        approvalStatus: 'clinical_trial',
        indication: 'KRAS G12C-mutated solid tumors',
        description: 'Next-generation KRAS G12C inhibitor with potential for improved efficacy and resistance profile.',
        source: 'ClinicalTrials.gov',
        targetedMutations: [
          {
            mutationName: 'G12C',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly12Cys',
            codonPosition: 12,
            clinicalSignificance: 'Driver mutation',
            evidenceLevel: 'Moderate',
            sources: ['ClinicalTrials.gov', 'PubMed']
          }
        ]
      },
      {
        drugName: 'RMC-6291',
        drugId: 'DB18345',
        mechanism: 'KRAS G12X Pan-Inhibitor',
        interactionType: 'inhibitor',
        clinicalTrial: true,
        approvalStatus: 'clinical_trial',
        indication: 'KRAS-mutated solid tumors',
        description: 'Pan-KRAS inhibitor targeting multiple KRAS G12 mutations, currently in clinical development.',
        source: 'ClinicalTrials.gov',
        targetedMutations: [
          {
            mutationName: 'G12C',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly12Cys',
            codonPosition: 12,
            clinicalSignificance: 'Driver mutation',
            evidenceLevel: 'Moderate',
            sources: ['ClinicalTrials.gov']
          },
          {
            mutationName: 'G12D',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly12Asp',
            codonPosition: 12,
            clinicalSignificance: 'Driver mutation',
            evidenceLevel: 'Moderate',
            sources: ['ClinicalTrials.gov']
          },
          {
            mutationName: 'G12V',
            mutationType: 'Missense',
            aminoAcidChange: 'Gly12Val',
            codonPosition: 12,
            clinicalSignificance: 'Driver mutation',
            evidenceLevel: 'Moderate',
            sources: ['ClinicalTrials.gov']
          }
        ]
      }
    ],
    protein: {
      uniprotId: 'P01116',
      accession: 'P01116',
      length: 188,
      molecularWeight: 21.6,
      domains: [
        {
          name: 'GTPase domain',
          start: 1,
          end: 166,
          description: 'Ras GTPase domain'
        }
      ],
      modifications: [
        {
          type: 'Farnesylation',
          position: 186,
          description: 'C-terminal farnesylation'
        }
      ],
      function: 'GTPase that regulates cell proliferation and differentiation.',
      subcellularLocalization: ['Cell membrane', 'Cytoplasm']
    },
    ensemblInfo: {
      ensemblId: 'ENSG00000133703',
      orthologs: []
    }
  }
};

/**
 * Get mock data for a gene
 */
export function getMockGeneData(symbol: string) {
  return mockGeneData[symbol] || mockGeneData['TP53'];
}

/**
 * Error handler with logging
 */
export class GeneDataError extends Error {
  constructor(
    public code: string,
    message: string,
    public details?: any
  ) {
    super(message);
    this.name = 'GeneDataError';
    console.error(`[${code}] ${message}`, details);
  }
}

/**
 * Safe API call wrapper with error handling
 */
export async function safeApiCall<T>(
  fn: () => Promise<T>,
  fallback: T,
  errorCode: string,
  errorMessage: string
): Promise<T> {
  try {
    return await fn();
  } catch (error) {
    console.warn(`${errorCode}: ${errorMessage}`, error);
    return fallback;
  }
}

/**
 * Validate gene annotation data
 */
export function validateGeneAnnotation(data: any): data is GeneAnnotation {
  return (
    data &&
    typeof data.symbol === 'string' &&
    typeof data.name === 'string' &&
    Array.isArray(data.aliases) &&
    typeof data.description === 'string' &&
    typeof data.chromosomalLocation === 'string'
  );
}

/**
 * Validate expression data
 */
export function validateExpressionData(data: any): data is ExpressionData {
  return (
    data &&
    typeof data.tissue === 'string' &&
    typeof data.level === 'number' &&
    typeof data.cancerScore === 'number' &&
    data.level >= 0 &&
    data.level <= 10 &&
    data.cancerScore >= -1 &&
    data.cancerScore <= 0
  );
}

/**
 * Sanitize and validate API responses
 */
export function sanitizeResponse<T>(data: any, validator: (d: any) => d is T): T | null {
  try {
    if (validator(data)) {
      return data;
    }
    console.warn('Data validation failed:', data);
    return null;
  } catch (error) {
    console.error('Error sanitizing response:', error);
    return null;
  }
}

/**
 * Retry logic for failed API calls
 */
export async function retryApiCall<T>(
  fn: () => Promise<T>,
  maxRetries: number = 3,
  delayMs: number = 1000
): Promise<T> {
  let lastError: Error | null = null;

  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error as Error;
      if (i < maxRetries - 1) {
        await new Promise((resolve) => setTimeout(resolve, delayMs * Math.pow(2, i)));
      }
    }
  }

  throw lastError || new Error('Max retries exceeded');
}

/**
 * Format error message for display
 */
export function formatErrorMessage(error: unknown): string {
  if (error instanceof GeneDataError) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'An unexpected error occurred. Please try again.';
}
