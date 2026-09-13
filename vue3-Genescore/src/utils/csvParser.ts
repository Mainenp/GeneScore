// src/utils/csvParser.ts

/**
 * Parse CSV file content
 * @param csvContent CSV file content as string
 * @returns Parsed data as array of objects
 */
export function parseCSV(csvContent: string): Record<string, Record<string, number>> {
  console.log('Parsing CSV content...');
  console.log('CSV content length:', csvContent.length);
  
  const lines = csvContent.split('\n');
  console.log('Total lines in CSV:', lines.length);
  
  const headers = lines[0].split(',').map(header => header.trim());
  console.log('Headers:', headers);
  
  const result: Record<string, Record<string, number>> = {};

  for (let i = 1; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) continue;

    const values = line.split(',');
    // 提取基因符号，去除括号中的基因ID
    const geneSymbol = values[0].trim().replace(/\s*\(.*\)$/, '').toUpperCase();
    const scores: Record<string, number> = {};

    for (let j = 1; j < values.length; j++) {
      const cancerType = headers[j].trim();
      const score = parseFloat(values[j].trim());
      if (!isNaN(score)) {
        scores[cancerType] = score;
      }
    }

    result[geneSymbol] = scores;
  }

  console.log(`Parsed ${Object.keys(result).length} genes from CSV`);
  console.log('First 10 genes:', Object.keys(result).slice(0, 10));
  
  // Test with a specific gene
  const testGene = 'KRAS';
  if (result[testGene]) {
    console.log(`Found gene ${testGene} in CSV data`);
    console.log(`${testGene} scores:`, result[testGene]);
  } else {
    console.log(`Gene ${testGene} not found in CSV data`);
  }
  
  return result;
}

/**
 * Load CSV file from resources
 * @returns Promise with parsed CSV data
 */
export async function loadCancerScoresCSV(): Promise<Record<string, Record<string, number>>> {
  try {
    console.log('Loading cancer scores CSV...');
    
    // 直接使用public文件夹中的CSV文件
    const csvPath = '/pan_cancer_driver_matrix_100pt.csv';
    console.log(`Loading CSV from: ${csvPath}`);
    
    const response = await fetch(csvPath);
    console.log(`Response status: ${response.status}`);
    console.log(`Response ok: ${response.ok}`);
    
    if (!response.ok) {
      throw new Error(`Failed to load CSV file: ${response.status}`);
    }
    
    const csvContent = await response.text();
    console.log('CSV file loaded successfully, parsing data...');
    console.log(`CSV content length: ${csvContent.length}`);
    console.log(`First 100 characters: ${csvContent.substring(0, 100)}`);
    
    const parsedData = parseCSV(csvContent);
    console.log(`Parsed ${Object.keys(parsedData).length} genes from CSV`);
    console.log(`First 10 genes: ${Object.keys(parsedData).slice(0, 10)}`);
    
    return parsedData;
  } catch (error) {
    console.error('Error loading cancer scores CSV:', error);
    return {};
  }
}

/**
 * Get cancer score for a specific gene and cancer type
 * @param geneSymbol Gene symbol
 * @param cancerType Cancer type
 * @param scores Parsed CSV data
 * @returns Cancer score or undefined
 */
export function getCancerScore(geneSymbol: string, cancerType: string, scores: Record<string, Record<string, number>>): number | undefined {
  if (!geneSymbol) {
    console.log('Gene symbol is undefined or null');
    return undefined;
  }
  
  const upperGeneSymbol = geneSymbol.toUpperCase();
  console.log(`Looking for gene: ${upperGeneSymbol} in CSV data`);
  const geneScores = scores[upperGeneSymbol];
  if (!geneScores) {
    console.log(`Gene ${upperGeneSymbol} not found in CSV data`);
    return undefined;
  }
  console.log(`Found gene ${upperGeneSymbol} in CSV data, available cancer types:`, Object.keys(geneScores).slice(0, 10));
  const score = geneScores[cancerType];
  if (score === undefined) {
    console.log(`Cancer type ${cancerType} not found for gene ${upperGeneSymbol}`);
  }
  return score;
}
