# DSFDS GeneScoreDB

[![Website](https://img.shields.io/badge/Website-Online-0f766e)](https://www.tmliang.cn/DSFDS/)

DSFDS GeneScoreDB is a database website for exploring gene dependency predictions produced by the **DSFDS model**. It organizes model results and related biological annotations into a searchable, visual interface to support cancer-gene dependency research and therapeutic-target exploration.

**Live website:** [https://www.tmliang.cn/DSFDS/](https://www.tmliang.cn/DSFDS/)

> DSFDS GeneScoreDB 是一个基于 DSFDS 模型预测结果构建的数据库网站，用于检索、浏览和分析癌症相关的基因依赖信息。

## Main features

- Search genes by symbol or name.
- Browse DSFDS gene dependency scores across cancer types and tissues.
- View gene annotations, expression profiles and protein structures.
- Explore protein–protein interactions, disease associations and drug interactions.
- Run analysis modules such as differential expression, prognosis and gene-set enrichment.
- Access double-knockout analysis and downloadable datasets.
- Query data through REST API endpoints.

## Repository structure

```text
GeneScore/
├── src/                    # Spring Boot backend
│   ├── main/java/          # Controllers, services, repositories and entities
│   └── main/resources/     # Database schemas and required datasets
├── vue3-Genescore/         # Vue 3 frontend
├── dko_microservice/       # Optional FastAPI DKO demonstration service
├── pom.xml                 # Maven configuration
└── GeneScore_API_Collection.postman_collection.json
```

The repository contains the database website and service code. The DSFDS model training pipeline is not part of this repository.

## Technology stack

- Backend: Java 17, Spring Boot, Spring Data JPA, MySQL
- Frontend: Vue 3, TypeScript, Vite, Pinia, ECharts, Tailwind CSS
- Auxiliary service: Python, FastAPI

## Local development

### Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Node.js 20.19+ (or 22.12+)
- Python 3.9+ only if the DKO demonstration service is needed

### 1. Prepare the database

Create a MySQL database named `genescore_db`, then import the schemas in `src/main/resources/` as needed.

Do not commit database passwords or API keys. Supply local settings through environment variables:

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/genescore_db'
export SPRING_DATASOURCE_USERNAME='your_username'
export SPRING_DATASOURCE_PASSWORD='your_password'
export SERVER_PORT='9092'
```

### 2. Start the backend

```bash
mvn spring-boot:run
```

The frontend development proxy expects the backend at `http://localhost:9092`.

### 3. Start the frontend

```bash
cd vue3-Genescore
npm ci
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

### 4. Optional: start the DKO demonstration service

```bash
cd dko_microservice
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 127.0.0.1 --port 8000
```

To connect a separate DSFDS synergy service, set `PYTHON_MICROSERVICE_URL` and `PYTHON_SYNERGY_SERVICE_URL` for the backend.

## Build and test

```bash
# Backend tests
mvn test

# Frontend checks and production build
cd vue3-Genescore
npm ci
npm run test:unit -- --run
npm run build
```

## API

Common endpoints include:

- `GET /api/genes/search?query=EGFR&limit=10`
- `GET /api/genes/{symbol}`
- `GET /api/genes/{symbol}/cancer-scores`
- `POST /api/enrichment`

A Postman collection is available in [`GeneScore_API_Collection.postman_collection.json`](GeneScore_API_Collection.postman_collection.json).

## Data and responsible use

The predictions and annotations provided by this project are intended for research and exploratory use. They should not be treated as clinical recommendations without independent experimental and clinical validation.

## Citation and contact

If you use DSFDS GeneScoreDB in your work, please cite the corresponding DSFDS publication or add the citation details here when they become available. For questions or bug reports, open a GitHub issue.
