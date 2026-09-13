# GeneScoreDB DKO Microservice

双基因敲除 (Double Knockout, DKO) 预测微服务

## 功能

- 预测两个基因的联合依赖分数
- 分类协同作用类型 (Synergistic, Antagonistic, Neutral, Additive)
- 提供模型置信度
- 详细的相互作用系数

## 快速开始

### 1. 安装依赖

```bash
cd dko_microservice
pip install -r requirements.txt
```

### 2. 启动服务

```bash
python main.py
```

服务将在 `http://localhost:8000` 启动

### 3. 测试API

```bash
# 健康检查
curl http://localhost:8000/health

# 获取可用基因
curl http://localhost:8000/genes

# 运行DKO预测
curl -X POST http://localhost:8000/predict_dko \
  -H "Content-Type: application/json" \
  -d '{"gene1": "EGFR", "gene2": "KRAS"}'
```

## API端点

### GET /health
健康检查端点

**响应:**
```json
{
  "status": "healthy",
  "service": "GeneScoreDB DKO Microservice",
  "version": "1.0.0"
}
```

### POST /predict_dko
运行DKO预测

**请求:**
```json
{
  "gene1": "EGFR",
  "gene2": "KRAS"
}
```

**响应:**
```json
{
  "gene1": "EGFR",
  "gene2": "KRAS",
  "joint_dependency_score": -2.845,
  "synergy_classification": "Synergistic",
  "confidence": 0.765,
  "details": {
    "synergy_effect": -0.612,
    "interaction_coefficient": 0.85
  }
}
```

### GET /genes
获取可用基因列表

**响应:**
```json
{
  "genes": ["EGFR", "KRAS", "TP53", ...],
  "count": 10
}
```

### GET /genes/{gene_symbol}
获取特定基因信息

**响应:**
```json
{
  "symbol": "EGFR",
  "base_score": -1.5,
  "expression_level": 8.2
}
```

## 可用基因

- EGFR
- KRAS
- TP53
- BRCA1
- PTEN
- PIK3CA
- AKT1
- MTOR
- MYC
- RB1

## 协同作用分类

- **Synergistic**: 强协同致死性 (联合敲除比预期更致命)
- **Antagonistic**: 拮抗效应 (联合敲除比预期不那么致命)
- **Neutral**: 无显著相互作用
- **Additive**: 加性效应 (联合效应是单个效应的总和)

## 模型说明

该微服务使用简化的深度学习模型逻辑:

1. **基础分数**: 每个基因的单独依赖分数
2. **相互作用系数**: 基因对之间的已知相互作用
3. **协同效应**: 基于相互作用系数计算
4. **置信度**: 基于相互作用系数和基因表达水平

## 与Spring Boot后端集成

Spring Boot后端会自动转发DKO请求到此微服务:

```
前端 (Vue 3)
  ↓
Spring Boot (localhost:8080)
  ↓
Python FastAPI (localhost:8000)
```

## 开发

### 添加新基因

编辑 `main.py` 中的 `GENE_SCORES` 字典:

```python
GENE_SCORES = {
    'NEW_GENE': {'base_score': -1.5, 'expression': 8.2},
    ...
}
```

### 添加基因相互作用

编辑 `INTERACTION_MATRIX` 字典:

```python
INTERACTION_MATRIX = {
    ('GENE1', 'GENE2'): 0.85,  # 相互作用系数 (0-1)
    ...
}
```

## 性能

- 单个预测: ~10ms
- 吞吐量: ~100 请求/秒
- 内存占用: ~50MB

## 故障排除

### 端口已被占用

```bash
# 使用不同的端口
python main.py --port 8001
```

### 导入错误

```bash
# 重新安装依赖
pip install --upgrade -r requirements.txt
```

## 联系方式

如有问题，请提交Issue或Pull Request。
