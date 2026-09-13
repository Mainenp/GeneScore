package com.mainenp.genedb.service;

import com.mainenp.genedb.entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockDataService {
    
    public List<Gene> getMockGenes() {
        List<Gene> genes = new ArrayList<>();
        
        Gene gene1 = new Gene();
        gene1.setId(1L);
        gene1.setSymbol("TP53");
        gene1.setName("Tumor protein p53");
        gene1.setEntrezId("7157");
        gene1.setUniprotId("P04637");
        gene1.setDescription("This gene encodes a tumor suppressor protein that regulates cell cycle progression and apoptosis.");
        genes.add(gene1);
        
        Gene gene2 = new Gene();
        gene2.setId(2L);
        gene2.setSymbol("KRAS");
        gene2.setName("Kirsten rat sarcoma viral oncogene homolog");
        gene2.setEntrezId("3845");
        gene2.setUniprotId("P01116");
        gene2.setDescription("This gene encodes a GTPase that functions as an oncogene in many cancers.");
        genes.add(gene2);
        
        Gene gene3 = new Gene();
        gene3.setId(3L);
        gene3.setSymbol("EGFR");
        gene3.setName("Epidermal growth factor receptor");
        gene3.setEntrezId("1956");
        gene3.setUniprotId("P00533");
        gene3.setDescription("This gene encodes a receptor tyrosine kinase that regulates cell growth and differentiation.");
        genes.add(gene3);
        
        Gene gene4 = new Gene();
        gene4.setId(4L);
        gene4.setSymbol("BRCA1");
        gene4.setName("Breast cancer 1, early onset");
        gene4.setEntrezId("672");
        gene4.setUniprotId("P38398");
        gene4.setDescription("This gene encodes a tumor suppressor protein that plays a role in DNA repair.");
        genes.add(gene4);
        
        Gene gene5 = new Gene();
        gene5.setId(5L);
        gene5.setSymbol("PTEN");
        gene5.setName("Phosphatase and tensin homolog");
        gene5.setEntrezId("5728");
        gene5.setUniprotId("P60484");
        gene5.setDescription("This gene encodes a tumor suppressor protein that regulates cell growth and survival.");
        genes.add(gene5);
        
        return genes;
    }
    
    public List<GeneScore> getMockGeneScores() {
        List<GeneScore> scores = new ArrayList<>();
        List<Gene> genes = getMockGenes();
        
        String[] lineages = {"Breast", "Lung", "Colorectal", "Prostate", "Pancreas"};
        String[] diseaseSubtypes = {"ER+/PR+", "ER-/PR-", "EGFR mutant", "KRAS mutant", "BRAF mutant"};
        
        for (Gene gene : genes) {
            for (String lineage : lineages) {
                for (String diseaseSubtype : diseaseSubtypes) {
                    GeneScore score = new GeneScore();
                    score.setId((long) (scores.size() + 1));
                    score.setGene(gene);
                    score.setLineage(lineage);
                    score.setDiseaseSubtype(diseaseSubtype);
                    score.setDependencyScore(Math.random() * 2 - 1); // -1 to 1
                    score.setExpressionLevel(Math.random() * 10); // 0 to 10
                    score.setCopyNumber(Math.random() * 4 - 2); // -2 to 2
                    score.setDataset("DepMap 23Q4");
                    score.setReplicateCount(3);
                    scores.add(score);
                }
            }
        }
        
        return scores;
    }
    
    public List<CoDependency> getMockCoDependencies() {
        List<CoDependency> dependencies = new ArrayList<>();
        List<Gene> genes = getMockGenes();
        
        for (int i = 0; i < genes.size(); i++) {
            for (int j = i + 1; j < genes.size(); j++) {
                CoDependency dependency = new CoDependency();
                dependency.setId((long) (dependencies.size() + 1));
                dependency.setGeneA(genes.get(i));
                dependency.setGeneB(genes.get(j));
                dependency.setPearsonCorrelation(Math.random() * 2 - 1); // -1 to 1
                dependency.setPValue(Math.random() * 0.05); // 0 to 0.05
                dependency.setSampleCount(500);
                dependency.setDataset("DepMap 23Q4");
                dependencies.add(dependency);
            }
        }
        
        return dependencies;
    }
    
    public List<GeneDrug> getMockGeneDrugs() {
        List<GeneDrug> drugs = new ArrayList<>();
        List<Gene> genes = getMockGenes();
        
        String[] drugNames = {"Trametinib", "Vemurafenib", "Cetuximab", "Pembrolizumab", "Olaparib"};
        String[] interactionTypes = {"Inhibitor", "Activator", "Antibody", "Immunotherapy", "PARP inhibitor"};
        
        for (Gene gene : genes) {
            for (int i = 0; i < drugNames.length; i++) {
                GeneDrug drug = new GeneDrug();
                drug.setId((long) (drugs.size() + 1));
                drug.setGene(gene);
                drug.setDrugName(drugNames[i]);
                drug.setDrugId("DRUG" + (drugs.size() + 1));
                drug.setInteractionType(interactionTypes[i]);
                drug.setEvidenceType("Clinical trial");
                drug.setEvidenceCount(i + 1);
                drug.setSource("DGIdb");
                drugs.add(drug);
            }
        }
        
        return drugs;
    }
    
    public List<SurvivalData> getMockSurvivalData() {
        List<SurvivalData> survivalDataList = new ArrayList<>();
        List<Gene> genes = getMockGenes();
        
        String[] lineages = {"Breast", "Lung", "Colorectal", "Prostate", "Pancreas"};
        
        for (Gene gene : genes) {
            for (String lineage : lineages) {
                SurvivalData survivalData = new SurvivalData();
                survivalData.setId((long) (survivalDataList.size() + 1));
                survivalData.setGene(gene);
                survivalData.setLineage(lineage);
                survivalData.setHazardRatio(Math.random() * 2 + 0.5); // 0.5 to 2.5
                survivalData.setPValue(Math.random() * 0.05); // 0 to 0.05
                survivalDataList.add(survivalData);
            }
        }
        
        return survivalDataList;
    }
    
    public List<GenePathway> getMockGenePathways() {
        List<GenePathway> pathways = new ArrayList<>();
        List<Gene> genes = getMockGenes();
        
        String[] pathwayNames = {"MAPK signaling", "PI3K-AKT signaling", "Cell cycle", "Apoptosis", "DNA repair"};
        
        for (Gene gene : genes) {
            for (int i = 0; i < pathwayNames.length; i++) {
                GenePathway pathway = new GenePathway();
                pathway.setId((long) (pathways.size() + 1));
                pathway.setGene(gene);
                pathway.setPathwayName(pathwayNames[i]);
                pathway.setPathwayId("PATHWAY" + (pathways.size() + 1));
                pathway.setSource("KEGG");
                pathway.setPValue(Math.random() * 0.05); // 0 to 0.05
                pathways.add(pathway);
            }
        }
        
        return pathways;
    }
}