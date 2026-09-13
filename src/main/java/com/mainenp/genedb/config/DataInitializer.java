package com.mainenp.genedb.config;


import com.mainenp.genedb.repository.*;
import com.mainenp.genedb.service.MockDataService;
import com.mainenp.genedb.service.LungSpecificCSVImportService;
import com.mainenp.genedb.service.PDBService;
import com.mainenp.genedb.service.GeneScoreDbImportService;
import com.mainenp.genedb.util.GeneDataImportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final GeneRepository geneRepository;
    private final GeneScoreRepository geneScoreRepository;
    private final GeneDrugRepository geneDrugRepository;
    private final GenePathwayRepository genePathwayRepository;
    private final SurvivalDataRepository survivalDataRepository;
    private final CoDependencyRepository coDependencyRepository;
    private final MockDataService mockDataService;
    private final LungSpecificCSVImportService lungSpecificCSVImportService;
    private final PDBService pdbService;
    private final GeneScoreDbImportService geneScoreDbImportService;
    private final GeneBasicRepository geneBasicRepository;
    private final GeneDataImportUtil geneDataImportUtil;
    
    public DataInitializer(GeneRepository geneRepository,
                          GeneScoreRepository geneScoreRepository,
                          GeneDrugRepository geneDrugRepository,
                          GenePathwayRepository genePathwayRepository,
                          SurvivalDataRepository survivalDataRepository,
                          CoDependencyRepository coDependencyRepository,
                          MockDataService mockDataService,
                          LungSpecificCSVImportService lungSpecificCSVImportService,
                          PDBService pdbService,
                          GeneScoreDbImportService geneScoreDbImportService,
                          GeneBasicRepository geneBasicRepository,
                          GeneDataImportUtil geneDataImportUtil) {
        this.geneRepository = geneRepository;
        this.geneScoreRepository = geneScoreRepository;
        this.geneDrugRepository = geneDrugRepository;
        this.genePathwayRepository = genePathwayRepository;
        this.survivalDataRepository = survivalDataRepository;
        this.coDependencyRepository = coDependencyRepository;
        this.mockDataService = mockDataService;
        this.lungSpecificCSVImportService = lungSpecificCSVImportService;
        this.pdbService = pdbService;
        this.geneScoreDbImportService = geneScoreDbImportService;
        this.geneBasicRepository = geneBasicRepository;
        this.geneDataImportUtil = geneDataImportUtil;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有 GeneBasic 数据
        if (geneBasicRepository.count() > 0) {
            log.info("GeneBasic data already exists, skipping import");
        } else {
            log.info("Importing GeneBasic data...");
            try {
                geneDataImportUtil.importGeneData();
                log.info("GeneBasic data imported successfully!");
            } catch (Exception e) {
                log.error("Failed to import GeneBasic data", e);
            }
        }
        
        // 检查是否已有 GENE_SCORE_DB 数据
        if (!geneScoreDbImportService.hasData()) {
            log.info("Importing GENE_SCORE_DB CSV data...");
            geneScoreDbImportService.importGeneScoreDbFromResources();
            log.info("GENE_SCORE_DB import completed.");
        } else {
            log.info("GENE_SCORE_DB data already exists");
        }
        
        // 检查数据库是否已有基因数据
        if (geneRepository.count() > 0) {
            log.info("Database already initialized with {} genes", geneRepository.count());
            // 更新 PDB ID（即使数据已存在）
            log.info("Updating PDB IDs for existing genes...");
            pdbService.updateGenePdbIds();
            return;
        }
        
        log.info("Initializing database...");
        
        try {
            // 首先导入 LUNG_SPECIFIC CSV 数据（包含所有基因）
            log.info("Importing LUNG_SPECIFIC CSV data...");
            lungSpecificCSVImportService.importLungSpecificFromResources();
            
            long geneCount = geneRepository.count();
            long scoreCount = geneScoreRepository.count();
            log.info("CSV import completed: {} genes, {} scores", geneCount, scoreCount);
            
            // 初始化基因药物数据
            var geneDrugs = mockDataService.getMockGeneDrugs();
            geneDrugRepository.saveAll(geneDrugs);
            log.info("Saved {} gene-drug interactions", geneDrugs.size());
            
            // 初始化基因通路数据
            var genePathways = mockDataService.getMockGenePathways();
            genePathwayRepository.saveAll(genePathways);
            log.info("Saved {} gene-pathway associations", genePathways.size());
            
            // 初始化生存数据
            var survivalData = mockDataService.getMockSurvivalData();
            survivalDataRepository.saveAll(survivalData);
            log.info("Saved {} survival data records", survivalData.size());
            
            // 初始化共依赖数据
            var coDependencies = mockDataService.getMockCoDependencies();
            coDependencyRepository.saveAll(coDependencies);
            log.info("Saved {} co-dependency records", coDependencies.size());
            
            // 更新基因的 PDB ID
            log.info("Updating PDB IDs for genes...");
            pdbService.updateGenePdbIds();
            
            log.info("Database initialization completed successfully!");
            
        } catch (Exception e) {
            log.error("Error initializing database", e);
        }
    }
}
