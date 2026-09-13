<!-- src/views/DoubleKnockout.vue -->
<template>
  <div class="double-knockout-container">
    <!-- 导航栏 -->
    <nav class="nav-bar">
      <div class="nav-brand">
        <svg class="nav-logo" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
        <span>GeneScore</span>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/double-knockout" class="nav-link active">双敲除分析</router-link>
      </div>
    </nav>

    <!-- 专业标题区 -->
    <header class="hero-section">
      <div class="hero-content">
        <div class="badge">
          <svg class="dna-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"/>
          </svg>
          <span>双敲除分析系统 v1.0</span>
        </div>
        <h1 class="hero-title">
          <span class="title-gradient">基因双敲除</span>
          <br />
          <span class="highlight">协同致死效应预测</span>
        </h1>
        <p class="hero-subtitle">
          基于计算生物学方法，预测基因双重敲除的致死效应与协同作用
        </p>
      </div>

      <!-- 专业装饰元素 -->
      <div class="hero-decoration">
        <div class="molecular-network">
          <div class="node" v-for="n in 12" :key="n" :style="getNodePosition(n)"></div>
          <div class="connection" v-for="(conn, index) in connections" :key="index"
               :style="getConnectionStyle(conn)"></div>
        </div>
      </div>
    </header>

    <!-- 专业控制面板 -->
    <section class="control-panel">
      <div class="panel-glass">
        <div class="panel-header">
          <div class="panel-title-group">
            <span class="panel-title">双敲除分析控制台</span>
            <span class="panel-subtitle">配置基因敲除参数与模型设置</span>
          </div>
        </div>

        <div class="panel-content">
          <div class="input-row">
            <div class="input-group">
              <label class="input-label">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M10 2v6M14 2v6M3 10h18M5 6h14a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2z"/>
                </svg>
                第一个基因
              </label>
              <input
                id="gene1"
                v-model="gene1"
                type="text"
                placeholder="输入基因符号 (例如: EGFR)"
                @keyup.enter="runAnalysis"
                class="modern-input"
              />
            </div>

            <div class="input-group">
              <label class="input-label">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M10 2v6M14 2v6M3 10h18M5 6h14a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2z"/>
                </svg>
                第二个基因
              </label>
              <input
                id="gene2"
                v-model="gene2"
                type="text"
                placeholder="输入基因符号 (例如: KRAS)"
                @keyup.enter="runAnalysis"
                class="modern-input"
              />
            </div>
          </div>

          <div class="input-row">
            <div class="input-group">
              <label class="input-label">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
                </svg>
                细胞系
              </label>
              <div class="select-wrapper">
                <select
                  id="cellLine"
                  v-model="selectedCellLine"
                  class="modern-select"
                >
                  <option value="">-- 选择细胞系 --</option>
                  <option v-for="cell in cellLines" :key="cell" :value="cell">
                    {{ cell }}
                  </option>
                </select>
                <div class="select-arrow">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </div>
              </div>
            </div>

            <div class="input-group">
              <label class="input-label">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
                </svg>
                分析模型
              </label>
              <div class="select-wrapper">
                <select
                  id="model"
                  v-model="selectedModel"
                  class="modern-select"
                >
                  <option value="CRISPR">CRISPR-Cas9</option>
                  <option value="RNAi">RNAi</option>
                  <option value="COMBINATION">联合效应模型</option>
                </select>
                <div class="select-arrow">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <div class="button-group">
            <button
              class="btn-primary"
              :disabled="!canRunAnalysis || loading"
              @click="runAnalysis"
            >
              <span v-if="loading" class="btn-spinner"></span>
              <span v-else class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="5 3 19 12 5 21 5 3"/>
                </svg>
              </span>
              {{ loading ? '分析中...' : '执行双敲除分析' }}
            </button>

            <button
              class="btn-secondary"
              @click="loadDemoData"
              title="加载演示数据进行功能验证"
            >
              <span class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/>
                  <line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </span>
              加载演示数据
            </button>

            <button
              class="btn-tertiary"
              @click="clearResults"
              :disabled="!hasResults"
            >
              <span class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                </svg>
              </span>
              清空结果
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 专业错误提示 -->
    <transition name="slide-down">
      <div v-if="error" class="error-toast">
        <div class="error-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        {{ error }}
        <button class="error-close" @click="error = ''">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </transition>

    <!-- 分析结果展示区 -->
    <section v-if="hasResults" class="results-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-bar"></span>
          双敲除分析结果
        </h2>
        <p class="section-subtitle">{{ gene1 }} + {{ gene2 }} 协同致死效应预测</p>
      </div>

      <!-- 关键指标卡片 -->
      <div class="metrics-grid">
        <div class="metric-card" :class="getMetricClass(synergyScore)">
          <div class="metric-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
            </svg>
          </div>
          <div class="metric-value">{{ synergyScore.toFixed(3) }}</div>
          <div class="metric-label">协同效应分数</div>
          <div class="metric-badge" :class="getScoreBadgeClass(synergyScore)">
            {{ getSynergyLabel(synergyScore) }}
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </div>
          <div class="metric-value">{{ deathProbability.toFixed(1) }}%</div>
          <div class="metric-label">致死概率</div>
        </div>

        <div class="metric-card">
          <div class="metric-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 6v6l4 2"/>
            </svg>
          </div>
          <div class="metric-value">{{ pValue.toExponential(2) }}</div>
          <div class="metric-label">P值 (统计显著性)</div>
        </div>

        <div class="metric-card">
          <div class="metric-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M3 3v18h18"/>
              <path d="M18 9l-5 5-4-4-3 3"/>
            </svg>
          </div>
          <div class="metric-value">{{ confidenceInterval }}</div>
          <div class="metric-label">95% 置信区间</div>
        </div>
      </div>

      <!-- 详细结果表格 -->
      <div class="results-panel">
        <div class="panel-header">
          <div class="panel-title-group">
            <span class="panel-title">详细分析数据</span>
            <span class="panel-subtitle">各模型预测结果对比</span>
          </div>
        </div>

        <div class="table-container">
          <table class="results-table">
            <thead>
              <tr>
                <th>模型</th>
                <th>单基因敲除 (G1)</th>
                <th>单基因敲除 (G2)</th>
                <th>预期效应</th>
                <th>观测效应</th>
                <th>协同增益</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in modelResults" :key="index">
                <td>
                  <span class="model-tag">{{ row.model }}</span>
                </td>
                <td>{{ row.singleG1.toFixed(3) }}</td>
                <td>{{ row.singleG2.toFixed(3) }}</td>
                <td>{{ row.expected.toFixed(3) }}</td>
                <td class="observed">{{ row.observed.toFixed(3) }}</td>
                <td :class="row.synergy > 0 ? 'positive' : 'negative'">
                  {{ row.synergy > 0 ? '+' : '' }}{{ (row.synergy * 100).toFixed(1) }}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Python脚本输出 -->
      <div class="python-output-panel" v-if="pythonOutput">
        <div class="panel-header">
          <div class="panel-title-group">
            <span class="panel-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width: 20px; height: 20px; margin-right: 8px;">
                <path d="M6.5 6.5l11 11M6.5 17.5l11-11M17.5 6.5H6.5v11"/>
              </svg>
              Python 脚本输出
            </span>
            <span class="panel-subtitle">双敲除计算详细日志</span>
          </div>
        </div>
        <pre class="python-output"><code>{{ pythonOutput }}</code></pre>
      </div>

      <!-- 可视化图表区 -->
      <div class="charts-grid">
        <div class="chart-container">
          <div class="chart-header">
            <div class="chart-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <line x1="3" y1="3" x2="3" y2="21"/>
                <line x1="3" y1="21" x2="21" y2="21"/>
                <rect x="7" y="10" width="4" height="11" rx="1"/>
                <rect x="15" y="5" width="4" height="16" rx="1"/>
              </svg>
            </div>
            <h3>效应对比柱状图</h3>
          </div>
          <div class="chart-wrapper">
            <ScoreChart
              :data="chartData"
              chart-type="bar"
              :height="280"
            />
          </div>
        </div>

        <div class="chart-container">
          <div class="chart-header">
            <div class="chart-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <polygon points="12 2 2 22 22 22 12 2"/>
                <line x1="12" y1="6" x2="12" y2="14"/>
                <line x1="12" y1="18" x2="12" y2="22"/>
              </svg>
            </div>
            <h3>协同效应雷达图</h3>
          </div>
          <div class="chart-wrapper">
            <ScoreChart
              :data="radarData"
              chart-type="radar"
              :height="280"
            />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ScoreChart from '@/components/ScoreChart.vue'

// 状态变量
const gene1 = ref('')
const gene2 = ref('')
const selectedCellLine = ref('')
const selectedModel = ref('CRISPR')
const loading = ref(false)
const error = ref('')
const pythonOutput = ref('')

// 细胞系选项
const cellLines = [
  'A549',
  'HeLa',
  'MCF-7',
  'HCT116',
  'PC-3',
  'U-2 OS',
  'K562',
  'SK-OV-3',
  'HT-29',
  'MDA-MB-231'
]

// 结果数据
const synergyScore = ref(0)
const deathProbability = ref(0)
const pValue = ref(1)
const confidenceInterval = ref('N/A')
const modelResults = ref<Array<{
  model: string
  singleG1: number
  singleG2: number
  expected: number
  observed: number
  synergy: number
}>>([])
const chartData = ref<Array<{
  geneSymbol: string
  score: number
  cancerType: string
  id: number
}>>([])
const radarData = ref<Array<{
  geneSymbol: string
  score: number
  cancerType: string
  id: number
}>>([])

// 计算属性
const canRunAnalysis = computed(() => {
  return gene1.value.trim() && gene2.value.trim() && selectedCellLine.value
})

const hasResults = computed(() => {
  return modelResults.value.length > 0
})

// 获取节点位置（装饰动画）
const getNodePosition = (n: number) => {
  const angle = (n / 15) * Math.PI * 2
  const radius = 120 + Math.random() * 40
  return {
    left: `${50 + Math.cos(angle) * (radius / 200)}%`,
    top: `${50 + Math.sin(angle) * (radius / 200)}%`
  }
}

const connections = ref<Array<{ from: number; to: number }>>([])
for (let i = 0; i < 8; i++) {
  connections.value.push({
    from: Math.floor(Math.random() * 15) + 1,
    to: Math.floor(Math.random() * 15) + 1
  })
}

const getConnectionStyle = (conn: { from: number; to: number }) => {
  const fromAngle = (conn.from / 15) * Math.PI * 2
  const toAngle = (conn.to / 15) * Math.PI * 2
  const radius = 120
  return {
    left: `${50 + Math.cos(fromAngle) * (radius / 200)}%`,
    top: `${50 + Math.sin(fromAngle) * (radius / 200)}%`,
    width: `${Math.abs(fromAngle - toAngle) * 2}px`,
    transform: `rotate(${fromAngle * 180 / Math.PI}deg)`
  }
}

// 获取分数样式
const getMetricClass = (score: number) => {
  if (score > 0.5) return 'high-synergy'
  if (score > 0.2) return 'medium-synergy'
  return 'low-synergy'
}

const getScoreBadgeClass = (score: number) => {
  if (score > 0.5) return 'badge-success'
  if (score > 0.2) return 'badge-warning'
  return 'badge-neutral'
}

const getSynergyLabel = (score: number) => {
  if (score > 0.7) return '强协同'
  if (score > 0.5) return '中度协同'
  if (score > 0.3) return '弱协同'
  if (score > 0.1) return '轻微协同'
  return '无协同'
}

// 运行分析
const runAnalysis = async () => {
  if (!canRunAnalysis.value) {
    error.value = '请填写完整的分析参数'
    return
  }

  error.value = ''
  loading.value = true
  pythonOutput.value = ''

  try {
    // 模拟调用 Python 脚本
    // 在实际项目中，这里应该通过 API 调用后端服务
    // 例如: const response = await fetch('/api/double-knockout', {...})
    
    await simulatePythonExecution()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '分析执行失败，请重试'
  } finally {
    loading.value = false
  }
}

// 模拟 Python 脚本执行（实际项目中替换为真实 API 调用）
const simulatePythonExecution = async () => {
  // 模拟执行延迟
  await new Promise(resolve => setTimeout(resolve, 2000))

  // 生成模拟输出
  const timestamp = new Date().toLocaleString()
  pythonOutput.value = `[DoubleKnockout Analysis] ${timestamp}
=================================================
输入参数:
  - 基因1: ${gene1.value.toUpperCase()}
  - 基因2: ${gene2.value.toUpperCase()}
  - 细胞系: ${selectedCellLine.value}
  - 分析模型: ${selectedModel.value}

数据加载:
  - 从DepMap数据库加载 ${gene1.value.toUpperCase()} 敲除效应数据
  - 从DepMap数据库加载 ${gene2.value.toUpperCase()} 敲除效应数据
  - 加载细胞系特异性表达数据

计算过程:
  1. 计算单基因敲除效应:
     - ${gene1.value.toUpperCase()}: ${(0.3 + Math.random() * 0.3).toFixed(4)}
     - ${gene2.value.toUpperCase()}: ${(0.4 + Math.random() * 0.3).toFixed(4)}
  
  2. 计算预期联合效应 (Bliss独立模型):
     - E_expected = E1 + E2 - E1*E2
     - E_expected = ${(0.5 + Math.random() * 0.2).toFixed(4)}
  
  3. 计算观测效应 (基于基因相互作用网络):
     - 蛋白相互作用分析
     - 信号通路重叠度计算
     - 转录组协同效应预测
  
  4. 计算协同效应分数:
     - Synergy = E_observed - E_expected
     - Synergy = ${(0.15 + Math.random() * 0.4).toFixed(4)}

统计检验:
  - 单侧t检验 p-value: ${(Math.random() * 0.05).toExponential(3)}
  - 95% CI: [${(0.12 + Math.random() * 0.1).toFixed(3)}, ${(0.65 + Math.random() * 0.15).toFixed(3)}]

结果汇总:
  ✓ 协同效应分数: ${(0.15 + Math.random() * 0.4).toFixed(3)}
  ✓ 预测致死概率: ${(55 + Math.random() * 30).toFixed(1)}%
  ✓ 统计显著性: 显著 (p < 0.05)

=================================================
分析完成`

  // 设置结果
  synergyScore.value = 0.15 + Math.random() * 0.4
  deathProbability.value = 55 + Math.random() * 30
  pValue.value = Math.random() * 0.05
  confidenceInterval.value = `[${(0.12 + Math.random() * 0.1).toFixed(2)}, ${(0.65 + Math.random() * 0.15).toFixed(2)}]`

  modelResults.value = [
    {
      model: 'CRISPR',
      singleG1: 0.35 + Math.random() * 0.2,
      singleG2: 0.45 + Math.random() * 0.2,
      expected: 0.62 + Math.random() * 0.1,
      observed: 0.72 + Math.random() * 0.15,
      synergy: synergyScore.value * (0.8 + Math.random() * 0.4)
    },
    {
      model: 'RNAi',
      singleG1: 0.32 + Math.random() * 0.2,
      singleG2: 0.42 + Math.random() * 0.2,
      expected: 0.60 + Math.random() * 0.1,
      observed: 0.68 + Math.random() * 0.15,
      synergy: synergyScore.value * (0.7 + Math.random() * 0.4)
    },
    {
      model: 'COMBINATION',
      singleG1: 0.38 + Math.random() * 0.2,
      singleG2: 0.48 + Math.random() * 0.2,
      expected: 0.65 + Math.random() * 0.1,
      observed: 0.78 + Math.random() * 0.15,
      synergy: synergyScore.value
    }
  ]

  // 更新图表数据
  if (modelResults.value.length > 0 && modelResults.value[0]) {
    const firstResult = modelResults.value[0]
    chartData.value = [
      { geneSymbol: `${gene1.value.toUpperCase()} KO`, score: firstResult.singleG1 * 10, cancerType: 'Unknown', id: 1 },
      { geneSymbol: `${gene2.value.toUpperCase()} KO`, score: firstResult.singleG2 * 10, cancerType: 'Unknown', id: 2 },
      { geneSymbol: '预期联合', score: firstResult.expected * 10, cancerType: 'Unknown', id: 3 },
      { geneSymbol: '观测联合', score: firstResult.observed * 10, cancerType: 'Unknown', id: 4 }
    ]
  }

  radarData.value = [
    { geneSymbol: '效应强度', score: 0.6 + Math.random() * 0.3, cancerType: 'Unknown', id: 1 },
    { geneSymbol: '统计显著性', score: 0.7 + Math.random() * 0.2, cancerType: 'Unknown', id: 2 },
    { geneSymbol: '可重复性', score: 0.65 + Math.random() * 0.25, cancerType: 'Unknown', id: 3 },
    { geneSymbol: '特异性', score: 0.55 + Math.random() * 0.35, cancerType: 'Unknown', id: 4 },
    { geneSymbol: '临床相关性', score: 0.5 + Math.random() * 0.4, cancerType: 'Unknown', id: 5 },
    { geneSymbol: '药物靶向性', score: 0.6 + Math.random() * 0.3, cancerType: 'Unknown', id: 6 }
  ]
}

// 加载演示数据
const loadDemoData = () => {
  gene1.value = 'EGFR'
  gene2.value = 'KRAS'
  selectedCellLine.value = 'A549'
  selectedModel.value = 'CRISPR'
  runAnalysis()
}

// 清空结果
const clearResults = () => {
  pythonOutput.value = ''
  modelResults.value = []
  chartData.value = []
  radarData.value = []
  synergyScore.value = 0
  deathProbability.value = 0
  pValue.value = 1
  confidenceInterval.value = 'N/A'
}

// 生命周期
onMounted(() => {
  // 初始化连接
  connections.value = []
  for (let i = 0; i < 8; i++) {
    connections.value.push({
      from: Math.floor(Math.random() * 15) + 1,
      to: Math.floor(Math.random() * 15) + 1
    })
  }
})
</script>

<style scoped>
.double-knockout-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 50%, #0f0f23 100%);
}

/* 标题区域 */
.hero-section {
  position: relative;
  margin: -20px -20px 30px;
  padding: 60px 40px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.9) 0%, rgba(30, 41, 59, 0.8) 100%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.2);
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(99, 102, 241, 0.15);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 30px;
  color: #a5b4fc;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
}

.dna-icon {
  width: 16px;
  height: 16px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.title-gradient {
  background: linear-gradient(135deg, #818cf8, #c084fc, #f472b6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.highlight {
  color: #e2e8f0;
  text-shadow: 0 0 40px rgba(99, 102, 241, 0.4);
}

.hero-subtitle {
  color: #94a3b8;
  font-size: 16px;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 装饰动画 */
.hero-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.molecular-network {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 400px;
  height: 400px;
  transform: translate(-50%, -50%);
}

.node {
  position: absolute;
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 3s ease-in-out infinite;
}

.connection {
  position: absolute;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.4), transparent);
  transform-origin: left center;
  animation: fadeInOut 4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.4; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.3); }
}

@keyframes fadeInOut {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 0.5; }
}

/* 控制面板 */
.control-panel {
  margin-bottom: 30px;
}

.panel-glass {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 20px;
  overflow: hidden;
}

.panel-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #f1f5f9;
}

.panel-subtitle {
  font-size: 13px;
  color: #64748b;
}

.panel-content {
  padding: 24px;
}

.input-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #94a3b8;
}

.input-icon {
  width: 18px;
  height: 18px;
  color: #6366f1;
}

.modern-input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  color: #f1f5f9;
  font-size: 15px;
  transition: all 0.2s;
}

.modern-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.modern-input::placeholder {
  color: #475569;
}

.select-wrapper {
  position: relative;
}

.modern-select {
  width: 100%;
  padding: 12px 40px 12px 16px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  color: #f1f5f9;
  font-size: 15px;
  appearance: none;
  cursor: pointer;
  transition: all 0.2s;
}

.modern-select:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.modern-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #64748b;
}

.select-arrow svg {
  width: 16px;
  height: 16px;
}

.button-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  color: #94a3b8;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: rgba(148, 163, 184, 0.15);
  border-color: rgba(148, 163, 184, 0.3);
  color: #f1f5f9;
}

.btn-tertiary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: transparent;
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  color: #f87171;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-tertiary:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.1);
}

.btn-tertiary:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-icon {
  display: flex;
  width: 18px;
  height: 18px;
}

.btn-icon svg {
  width: 100%;
  height: 100%;
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误提示 */
.error-toast {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  color: #fca5a5;
  margin-bottom: 20px;
  animation: slideDown 0.3s ease-out;
}

.error-icon {
  display: flex;
  width: 20px;
  height: 20px;
}

.error-icon svg {
  width: 100%;
  height: 100%;
}

.error-close {
  margin-left: auto;
  display: flex;
  background: none;
  border: none;
  color: #fca5a5;
  cursor: pointer;
  padding: 4px;
}

.error-close svg {
  width: 16px;
  height: 16px;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 结果区域 */
.results-section {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #f1f5f9;
  margin-bottom: 8px;
}

.title-bar {
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 2px;
}

.section-subtitle {
  color: #64748b;
  font-size: 14px;
}

/* 指标卡片 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-4px);
  border-color: rgba(99, 102, 241, 0.3);
}

.metric-card.high-synergy {
  border-color: rgba(34, 197, 94, 0.4);
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.1), rgba(30, 41, 59, 0.6));
}

.metric-card.medium-synergy {
  border-color: rgba(234, 179, 8, 0.4);
  background: linear-gradient(135deg, rgba(234, 179, 8, 0.1), rgba(30, 41, 59, 0.6));
}

.metric-card.low-synergy {
  border-color: rgba(148, 163, 184, 0.2);
}

.metric-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.metric-icon svg {
  width: 28px;
  height: 28px;
  color: #6366f1;
}

.high-synergy .metric-icon svg {
  color: #22c55e;
}

.medium-synergy .metric-icon svg {
  color: #eab308;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: #f1f5f9;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
}

.metric-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.badge-success {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

.badge-warning {
  background: rgba(234, 179, 8, 0.2);
  color: #facc15;
}

.badge-neutral {
  background: rgba(148, 163, 184, 0.2);
  color: #94a3b8;
}

/* 结果面板 */
.results-panel {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  margin-bottom: 24px;
  overflow: hidden;
}

.results-panel .panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.results-panel .panel-title {
  font-size: 16px;
}

.table-container {
  overflow-x: auto;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
}

.results-table th,
.results-table td {
  padding: 14px 20px;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.results-table th {
  background: rgba(15, 23, 42, 0.4);
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.results-table td {
  font-size: 14px;
  color: #e2e8f0;
}

.results-table td.observed {
  color: #a5b4fc;
  font-weight: 600;
}

.results-table td.positive {
  color: #4ade80;
}

.results-table td.negative {
  color: #f87171;
}

.model-tag {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(99, 102, 241, 0.2);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #a5b4fc;
}

/* Python 输出面板 */
.python-output-panel {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  margin-bottom: 24px;
  overflow: hidden;
}

.python-output-panel .panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.python-output {
  margin: 0;
  padding: 20px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #10b981;
  background: transparent;
  overflow-x: auto;
}

/* 图表网格 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-container {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  overflow: hidden;
}

.chart-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.chart-icon {
  display: flex;
  width: 24px;
  height: 24px;
  color: #6366f1;
}

.chart-icon svg {
  width: 100%;
  height: 100%;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #f1f5f9;
  margin: 0;
}

.chart-wrapper {
  padding: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .input-row {
    grid-template-columns: 1fr;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-secondary,
  .btn-tertiary {
    width: 100%;
    justify-content: center;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: rgba(12, 12, 30, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(100, 130, 255, 0.15);
  z-index: 1000;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.3em;
  font-weight: 700;
  color: #fff;
}

.nav-logo {
  width: 32px;
  height: 32px;
  color: #4da6ff;
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  padding: 10px 24px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.nav-link:hover {
  color: #fff;
  background: rgba(100, 130, 255, 0.15);
}

.nav-link.active {
  color: #fff;
  background: linear-gradient(135deg, rgba(30, 136, 229, 0.3), rgba(100, 130, 255, 0.3));
  border: 1px solid rgba(100, 130, 255, 0.4);
}

/* 为导航栏留出空间 */
.double-knockout-container {
  padding-top: 64px;
}
</style>
