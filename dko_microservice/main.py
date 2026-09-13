"""
GeneScoreDB - Double Knockout (DKO) Prediction Microservice
FastAPI服务用于运行深度学习模型进行双基因敲除预测
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import numpy as np
from typing import Optional, List, Dict
import logging
import base64
from io import BytesIO
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import seaborn as sns

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 创建FastAPI应用
app = FastAPI(
    title="GeneScoreDB DKO Microservice",
    description="Double Knockout prediction service",
    version="1.0.0"
)

# 添加CORS中间件
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 请求模型
class DKORequest(BaseModel):
    gene1: str
    gene2: str

class MultiGeneRequest(BaseModel):
    genes: List[str]

# 响应模型
class DKOResponse(BaseModel):
    gene1: str
    gene2: str
    joint_dependency_score: float
    synergy_classification: str
    confidence: float
    details: Optional[dict] = None

# 模拟的基因依赖分数数据库
GENE_SCORES = {
    'EGFR': {'base_score': -1.5, 'expression': 8.2},
    'KRAS': {'base_score': -1.8, 'expression': 7.5},
    'TP53': {'base_score': -2.5, 'expression': 6.5},
    'BRCA1': {'base_score': -1.2, 'expression': 7.3},
    'PTEN': {'base_score': -1.0, 'expression': 6.8},
    'PIK3CA': {'base_score': -1.3, 'expression': 7.1},
    'AKT1': {'base_score': -0.8, 'expression': 7.9},
    'MTOR': {'base_score': -1.1, 'expression': 7.4},
    'MYC': {'base_score': -1.9, 'expression': 8.5},
    'RB1': {'base_score': -1.4, 'expression': 6.9},
    'PARP1': {'base_score': -0.9, 'expression': 7.7},
    'MDM2': {'base_score': -0.7, 'expression': 7.2},
}

# 基因相互作用矩阵 (协同作用系数)
INTERACTION_MATRIX = {
    ('EGFR', 'KRAS'): 0.85,
    ('KRAS', 'PIK3CA'): 0.90,
    ('PIK3CA', 'AKT1'): 0.88,
    ('AKT1', 'MTOR'): 0.82,
    ('TP53', 'MYC'): 0.75,
    ('TP53', 'RB1'): 0.78,
    ('EGFR', 'TP53'): 0.65,
    ('KRAS', 'TP53'): 0.70,
    ('BRCA1', 'PARP1'): 0.95,
    ('TP53', 'MDM2'): 0.88,
}

def calculate_pair_score(gene1: str, gene2: str) -> float:
    """
    计算两个基因之间的相互作用分数
    """
    if gene1 not in GENE_SCORES or gene2 not in GENE_SCORES:
        return 0.0
    
    gene1_data = GENE_SCORES[gene1]
    gene2_data = GENE_SCORES[gene2]
    
    base_score1 = gene1_data['base_score']
    base_score2 = gene2_data['base_score']
    
    key1 = (gene1, gene2)
    key2 = (gene2, gene1)
    interaction_coeff = INTERACTION_MATRIX.get(key1) or INTERACTION_MATRIX.get(key2) or 0.5
    
    additive_score = base_score1 + base_score2
    synergy_effect = (base_score1 * base_score2) * interaction_coeff
    joint_score = additive_score + synergy_effect * 0.3
    
    return round(joint_score, 2)

def generate_heatmap(genes: List[str], scores: Dict[str, Dict[str, float]]) -> str:
    """
    生成热图并返回base64编码的图片
    """
    n = len(genes)
    matrix = np.zeros((n, n))
    
    for i, gene1 in enumerate(genes):
        for j, gene2 in enumerate(genes):
            matrix[i, j] = scores.get(gene1, {}).get(gene2, 0)
    
    plt.figure(figsize=(10, 8))
    sns.set_style("white")
    
    # 创建热图
    ax = sns.heatmap(
        matrix,
        annot=True,
        fmt='.2f',
        cmap='RdBu_r',
        center=0,
        xticklabels=genes,
        yticklabels=genes,
        cbar_kws={'label': 'Interaction Score'}
    )
    
    plt.title('Gene Interaction Heatmap', fontsize=16, pad=20)
    plt.xlabel('Target Gene', fontsize=12)
    plt.ylabel('Source Gene', fontsize=12)
    plt.tight_layout()
    
    # 转换为base64
    buffer = BytesIO()
    plt.savefig(buffer, format='png', dpi=150, bbox_inches='tight')
    buffer.seek(0)
    image_base64 = base64.b64encode(buffer.getvalue()).decode('utf-8')
    plt.close()
    
    return image_base64

def calculate_dko_score(gene1: str, gene2: str) -> dict:
    """
    计算双基因敲除分数
    使用简化的深度学习模型逻辑
    """
    
    # 检查基因是否存在
    if gene1 not in GENE_SCORES or gene2 not in GENE_SCORES:
        raise ValueError(f"Gene not found in database")
    
    # 获取基因信息
    gene1_data = GENE_SCORES[gene1]
    gene2_data = GENE_SCORES[gene2]
    
    # 计算基础联合依赖分数
    base_score1 = gene1_data['base_score']
    base_score2 = gene2_data['base_score']
    
    # 简单的加法模型作为基础
    additive_score = base_score1 + base_score2
    
    # 获取相互作用系数
    key1 = (gene1, gene2)
    key2 = (gene2, gene1)
    interaction_coeff = INTERACTION_MATRIX.get(key1) or INTERACTION_MATRIX.get(key2) or 0.5
    
    # 计算协同效应
    synergy_effect = (base_score1 * base_score2) * interaction_coeff
    
    # 最终联合依赖分数
    joint_score = additive_score + synergy_effect * 0.3
    
    # 计算置信度 (基于相互作用系数和基因表达水平)
    expr_factor = (gene1_data['expression'] + gene2_data['expression']) / 20
    confidence = min(0.95, interaction_coeff * expr_factor)
    
    # 分类协同作用
    if synergy_effect < -0.5:
        classification = "Synergistic"
    elif synergy_effect > 0.2:
        classification = "Antagonistic"
    elif abs(synergy_effect) < 0.1:
        classification = "Neutral"
    else:
        classification = "Additive"
    
    return {
        'joint_score': round(joint_score, 3),
        'classification': classification,
        'confidence': round(confidence, 3),
        'synergy_effect': round(synergy_effect, 3),
        'interaction_coeff': round(interaction_coeff, 3)
    }

@app.get("/health")
async def health_check():
    """健康检查端点"""
    return {
        "status": "healthy",
        "service": "GeneScoreDB DKO Microservice",
        "version": "1.0.0"
    }

@app.post("/predict_dko", response_model=DKOResponse)
async def predict_dko(request: DKORequest):
    """
    运行双基因敲除预测
    
    Args:
        request: 包含两个基因符号的请求
    
    Returns:
        DKOResponse: 预测结果
    """
    
    try:
        logger.info(f"Processing DKO prediction for {request.gene1} and {request.gene2}")
        
        # 验证输入
        if not request.gene1 or not request.gene2:
            raise HTTPException(status_code=400, detail="Gene symbols are required")
        
        if request.gene1 == request.gene2:
            raise HTTPException(status_code=400, detail="Gene1 and Gene2 must be different")
        
        # 计算DKO分数
        result = calculate_dko_score(request.gene1, request.gene2)
        
        # 构建响应
        response = DKOResponse(
            gene1=request.gene1,
            gene2=request.gene2,
            joint_dependency_score=result['joint_score'],
            synergy_classification=result['classification'],
            confidence=result['confidence'],
            details={
                'synergy_effect': result['synergy_effect'],
                'interaction_coefficient': result['interaction_coeff']
            }
        )
        
        logger.info(f"DKO prediction completed: {response}")
        return response
        
    except ValueError as e:
        logger.error(f"Validation error: {str(e)}")
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.post("/api/analyze_dko")
async def analyze_dko(request: MultiGeneRequest):
    """
    分析多基因敲除，返回分数矩阵和热图
    
    Args:
        request: 包含基因列表的请求
    
    Returns:
        包含分数矩阵和base64编码图片的响应
    """
    try:
        logger.info(f"Processing multi-gene DKO analysis for: {request.genes}")
        
        # 验证输入
        if not request.genes or len(request.genes) < 2:
            raise HTTPException(status_code=400, detail="At least 2 genes are required")
        
        # 过滤有效的基因
        valid_genes = [g for g in request.genes if g in GENE_SCORES]
        
        if len(valid_genes) < 2:
            raise HTTPException(
                status_code=400, 
                detail="Not enough valid genes. Available genes: " + ", ".join(GENE_SCORES.keys())
            )
        
        logger.info(f"Valid genes found: {valid_genes}")
        
        # 计算分数矩阵
        logger.info("Calculating score matrix...")
        scores = {}
        for gene1 in valid_genes:
            scores[gene1] = {}
            for gene2 in valid_genes:
                if gene1 == gene2:
                    scores[gene1][gene2] = 0.0
                else:
                    scores[gene1][gene2] = calculate_pair_score(gene1, gene2)
        
        logger.info("Score matrix calculated, generating heatmap...")
        
        # 生成热图
        image_base64 = generate_heatmap(valid_genes, scores)
        
        logger.info("Heatmap generated, building response...")
        
        # 构建响应
        response = {
            "status": "success",
            "valid_genes": valid_genes,
            "scores": scores,
            "image_base64": image_base64
        }
        
        logger.info(f"Multi-gene DKO analysis completed for {len(valid_genes)} genes")
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Unexpected error in multi-gene analysis: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/genes")
async def get_available_genes():
    """获取可用的基因列表"""
    return {
        "genes": list(GENE_SCORES.keys()),
        "count": len(GENE_SCORES)
    }

@app.get("/genes/{gene_symbol}")
async def get_gene_info(gene_symbol: str):
    """获取特定基因的信息"""
    if gene_symbol not in GENE_SCORES:
        raise HTTPException(status_code=404, detail=f"Gene {gene_symbol} not found")
    
    return {
        "symbol": gene_symbol,
        "base_score": GENE_SCORES[gene_symbol]['base_score'],
        "expression_level": GENE_SCORES[gene_symbol]['expression']
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
