<template>
  <div class="gene-query-container">
    <div class="background-noise"></div>
    <div class="background-glow"></div>

    <nav class="nav-bar">
      <div class="nav-brand">
        <div class="brand-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <span class="brand-text">GeneScore <span class="brand-version">PRO</span></span>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-link active">Analysis</router-link>
        <router-link to="/double-knockout" class="nav-link">Double Knockout</router-link>
      </div>
    </nav>

    <header class="hero-section">
      <div class="hero-content">
        <div class="status-badge">
          <span class="status-dot"></span>
          System Online: v2.4.0
        </div>
        <h1 class="hero-title">
          Genomic Score
          <span class="gradient-text">Visualization</span>
        </h1>
        <p class="hero-subtitle">
          Advanced 3D molecular modeling and high-precision gene scoring algorithms.
        </p>
      </div>
    </header>

    <main class="main-interface">
      <div class="glass-panel control-bar">
        <div class="control-group">
          <label class="control-label">Target Cancer Type</label>
          <div class="select-container">
            <select v-model="selectedCancerType" class="cyber-select">
              <option value="" disabled selected>Select Pathology...</option>
              <option v-for="type in cancerTypes" :key="type" :value="type">{{ type }}</option>
            </select>
            <svg class="select-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
        </div>

        <div class="control-group search-group">
          <label class="control-label">Gene Filters (Optional)</label>
          <div class="input-container">
            <input
              v-model="geneFilterInput"
              type="text"
              placeholder="e.g. EGFR, TP53, KRAS"
              @keyup.enter="handleSearch"
              class="cyber-input"
            />
          </div>
        </div>

        <div class="action-group">
          <button
            class="cyber-button primary"
            :disabled="!selectedCancerType || loading.scores"
            @click="handleSearch"
          >
            <span v-if="loading.scores" class="loader"></span>
            <span v-else>INITIATE SCAN</span>
          </button>

          <button class="cyber-button secondary" @click="loadMockData">
            DEMO DATA
          </button>
        </div>
      </div>

      <transition name="fade">
        <div v-if="error" class="error-banner">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ error }}
        </div>
      </transition>

      <transition name="slide-up">
        <section v-if="geneScores.length > 0" class="visualization-dashboard">

          <div class="viz-column threed-showcase">
            <div class="section-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
              Top Candidate Structures
            </div>

            <div class="cells-grid">
              <div v-for="(gene, index) in topGenes" :key="gene.geneSymbol" class="cell-display-unit">
                <div class="hologram-container">
                  <Cell3D
                    :x="100"
                    :y="100"
                    :radius="60"
                    :color="cellColors[index]"
                    :cell-name="gene.geneSymbol"
                    :score="gene.score"
                    class="cell-model"
                  />
                  <div class="hologram-base"></div>
                </div>
                <div class="gene-meta">
                  <span class="meta-symbol">{{ gene.geneSymbol }}</span>
                  <span class="meta-score">{{ gene.score.toFixed(1) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="viz-column chart-display">
            <div class="section-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
              Gene Score Spectrum
            </div>

            <div class="spectrum-container">
              <div class="column-chart-wrapper">
                <div
                  v-for="(gene, index) in sortedGeneScores"
                  :key="gene.id"
                  class="chart-column-group"
                  :style="{ animationDelay: `${index * 0.05}s` }"
                >
                  <div class="column-value">{{ gene.score.toFixed(0) }}</div>
                  <div
                    class="visual-column"
                    :style="{
                      height: `${(gene.score / maxScore) * 100}%`,
                      backgroundColor: getScoreColor(gene.score)
                    }"
                  >
                    <div class="column-shine"></div>
                  </div>
                  <div class="column-label">{{ gene.geneSymbol }}</div>

                  <div class="column-tooltip">
                    <strong>{{ gene.geneSymbol }}</strong>
                    <div class="tooltip-row">
                      <span>Score:</span>
                      <span>{{ gene.score.toFixed(2) }}</span>
                    </div>
                    <div class="tooltip-row">
                      <span>Rank:</span>
                      <span>#{{ index + 1 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="stats-footer">
              <div class="stat-pill">
                <span class="label">Total Genes</span>
                <span class="value">{{ geneScores.length }}</span>
              </div>
              <div class="stat-pill">
                <span class="label">Avg Score</span>
                <span class="value">{{ averageScore.toFixed(1) }}</span>
              </div>
              <div class="stat-pill">
                <span class="label">Peak</span>
                <span class="value">{{ maxScore.toFixed(1) }}</span>
              </div>
            </div>
          </div>

        </section>
      </transition>

      <section v-if="geneScores.length === 0 && !loading.scores" class="empty-dashboard">
        <div class="empty-visual">
          <div class="grid-line horizontal"></div>
          <div class="grid-line vertical"></div>
          <div class="scan-line"></div>
        </div>
        <h3>Awaiting Analysis Parameters</h3>
        <p>Select a cancer pathology to render 3D models and generate score spectrum.</p>
      </section>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getCancerTypes, getGeneScores, getGeneScoresByGenes } from '@/api/gene';
import type { GeneScore } from '@/types/gene';
import Cell3D from '@/components/Cell3D.vue';

// ============ State ============
const defaultCancerTypes = ['Lung Cancer', 'Breast Cancer', 'Colon Cancer', 'Prostate Cancer', 'Liver Cancer', 'Stomach Cancer'];
const cancerTypes = ref<string[]>([...defaultCancerTypes]);
const selectedCancerType = ref<string>('');
const geneFilterInput = ref<string>('');
const geneScores = ref<GeneScore[]>([]);
const hasSearched = ref<boolean>(false);
const error = ref<string>('');
const loading = ref({ cancerTypes: false, scores: false });

// 3D Model Colors
const cellColors = [0x4CC9F0, 0x7209B7, 0xF72585];

// ============ Computed ============
const sortedGeneScores = computed(() => {
  return [...geneScores.value].sort((a, b) => b.score - a.score);
});

const topGenes = computed(() => sortedGeneScores.value.slice(0, 3));

const averageScore = computed(() => {
  if (geneScores.value.length === 0) return 0;
  return geneScores.value.reduce((acc, gene) => acc + gene.score, 0) / geneScores.value.length;
});

const maxScore = computed(() => {
  if (geneScores.value.length === 0) return 100;
  return Math.max(...geneScores.value.map(gene => gene.score)) || 100;
});

// ============ Methods ============
const getScoreColor = (score: number) => {
  const max = maxScore.value || 100;
  const ratio = score / max;
  if (ratio > 0.8) return '#F72585'; // High - Pink/Red
  if (ratio > 0.5) return '#7209B7'; // Mid - Purple
  return '#4CC9F0'; // Low - Cyan
};

const loadMockData = () => {
  selectedCancerType.value = 'Lung Cancer';
  hasSearched.value = true;
  geneScores.value = [
    { id: 1, cancerType: 'Lung Cancer', geneSymbol: 'ASCL1', score: 95.5 },
    { id: 2, cancerType: 'Lung Cancer', geneSymbol: 'IFNA1', score: 82.3 },
    { id: 3, cancerType: 'Lung Cancer', geneSymbol: 'INSM1', score: 91.2 },
    { id: 4, cancerType: 'Lung Cancer', geneSymbol: 'EGFR', score: 68.7 },
    { id: 5, cancerType: 'Lung Cancer', geneSymbol: 'ALK', score: 45.6 },
    { id: 6, cancerType: 'Lung Cancer', geneSymbol: 'KRAS', score: 78.9 },
    { id: 7, cancerType: 'Lung Cancer', geneSymbol: 'TP53', score: 62.4 },
    { id: 8, cancerType: 'Lung Cancer', geneSymbol: 'BRAF', score: 55.3 },
    { id: 9, cancerType: 'Lung Cancer', geneSymbol: 'MET', score: 42.1 },
    { id: 10, cancerType: 'Lung Cancer', geneSymbol: 'ROS1', score: 38.5 },
    { id: 11, cancerType: 'Lung Cancer', geneSymbol: 'RET', score: 35.2 },
    { id: 12, cancerType: 'Lung Cancer', geneSymbol: 'NTRK', score: 25.4 }
  ];
};

const fetchCancerTypes = async () => {
  loading.value.cancerTypes = true;
  try {
    const data = await getCancerTypes();
    if (data && data.length) cancerTypes.value = data;
  } catch (err) { console.error(err); }
  finally { loading.value.cancerTypes = false; }
};

const handleSearch = async () => {
  if (!selectedCancerType.value) return;
  loading.value.scores = true;
  hasSearched.value = true;
  error.value = '';

  try {
    const symbols = geneFilterInput.value.split(',').map(g => g.trim().toUpperCase()).filter(Boolean);
    const data = symbols.length
      ? await getGeneScoresByGenes(selectedCancerType.value, symbols)
      : await getGeneScores(selectedCancerType.value);

    geneScores.value = data || [];
    if (!geneScores.value.length) error.value = 'No gene data found for selected parameters.';
  } catch (err: any) {
    error.value = err.message || 'Analysis failed.';
  } finally {
    loading.value.scores = false;
  }
};

onMounted(() => {
  fetchCancerTypes();
});
</script>

<style scoped>
/* ============ Global Variables & Reset ============ */
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@300;400;500;600;700&display=swap');

.gene-query-container {
  min-height: 100vh;
  background-color: #050511;
  color: #e2e8f0;
  font-family: 'Space Grotesk', sans-serif;
  position: relative;
  overflow-x: hidden;
  padding-top: 80px; /* Nav height */
}

/* ============ Background Effects ============ */
.background-noise {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0IiBoZWlnaHQ9IjQiPgo8cmVjdCB3aWR0aD0iNCIgaGVpZ2h0PSI0IiBmaWxsPSIjZmZmIiBmaWxsLW9wYWNpdHk9IjAuMDIiLz4KPC9zdmc+');
  pointer-events: none;
  z-index: 0;
}

.background-glow {
  position: fixed;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  width: 80vw; height: 80vh;
  background: radial-gradient(circle, rgba(76, 201, 240, 0.05) 0%, rgba(5, 5, 17, 0) 70%);
  pointer-events: none;
  z-index: 0;
}

/* ============ Navigation ============ */
.nav-bar {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: rgba(5, 5, 17, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo svg {
  width: 28px; height: 28px;
  color: #4CC9F0;
}

.brand-text {
  font-size: 1.2rem;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-version {
  font-size: 0.7rem;
  background: linear-gradient(90deg, #4CC9F0, #4361EE);
  padding: 2px 6px;
  border-radius: 4px;
  color: #000;
  margin-left: 5px;
}

.nav-links {
  display: flex;
  gap: 30px;
}

.nav-link {
  text-decoration: none;
  color: #94a3b8;
  font-weight: 500;
  font-size: 0.95rem;
  transition: color 0.3s;
}

.nav-link.active {
  color: #4CC9F0;
}

/* ============ Hero Section ============ */
.hero-section {
  text-align: center;
  padding: 60px 20px 40px;
  position: relative;
  z-index: 1;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.05);
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 0.85rem;
  color: #94a3b8;
  margin-bottom: 20px;
}

.status-dot {
  width: 6px; height: 6px;
  background-color: #00ff88;
  border-radius: 50%;
  box-shadow: 0 0 8px #00ff88;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 16px;
}

.gradient-text {
  background: linear-gradient(135deg, #4CC9F0 0%, #7209B7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  color: #64748b;
  max-width: 500px;
  margin: 0 auto;
  font-size: 1.1rem;
}

/* ============ Control Interface ============ */
.main-interface {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 60px;
  position: relative;
  z-index: 2;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
}

.control-bar {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  padding: 24px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.control-group {
  flex: 1;
  min-width: 250px;
}

.search-group {
  flex: 1.5;
}

.control-label {
  display: block;
  font-size: 0.85rem;
  color: #94a3b8;
  margin-bottom: 8px;
  font-weight: 500;
}

/* Cyber Inputs */
.select-container, .input-container {
  position: relative;
}

.cyber-select, .cyber-input {
  width: 100%;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(76, 201, 240, 0.3);
  color: #fff;
  padding: 14px 16px;
  border-radius: 8px;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  transition: all 0.3s;
}

.cyber-select:focus, .cyber-input:focus {
  border-color: #4CC9F0;
  box-shadow: 0 0 15px rgba(76, 201, 240, 0.2);
}

.select-icon {
  position: absolute;
  right: 14px; top: 50%;
  transform: translateY(-50%);
  width: 18px; height: 18px;
  color: #4CC9F0;
  pointer-events: none;
}

/* Cyber Buttons */
.action-group {
  display: flex;
  gap: 12px;
}

.cyber-button {
  padding: 14px 28px;
  border-radius: 8px;
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.5px;
}

.cyber-button.primary {
  background: linear-gradient(90deg, #4361EE, #3A0CA3);
  border: none;
  color: #fff;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.4);
}

.cyber-button.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(67, 97, 238, 0.6);
}

.cyber-button.primary:disabled {
  opacity: 0.7;
  cursor: wait;
}

.cyber-button.secondary {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #94a3b8;
}

.cyber-button.secondary:hover {
  border-color: #fff;
  color: #fff;
}

/* ============ Visualization Dashboard ============ */
.visualization-dashboard {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
  height: 600px; /* Fixed height for dashboard layout */
}

.viz-column {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #4CC9F0;
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 24px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.section-label svg {
  width: 20px; height: 20px;
}

/* 3D Showcase Styles */
.cells-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  padding-right: 5px;
}

.cell-display-unit {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: background 0.3s;
}

.cell-display-unit:hover {
  background: rgba(255, 255, 255, 0.05);
}

.hologram-container {
  width: 80px; height: 80px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cell-model {
  transform: scale(0.6);
}

.gene-meta {
  display: flex;
  flex-direction: column;
}

.meta-symbol {
  font-size: 1.2rem;
  font-weight: 700;
  color: #fff;
}

.meta-score {
  font-size: 0.9rem;
  color: #4CC9F0;
}

/* Chart Display - The "Column Chart" Implementation */
.spectrum-container {
  flex: 1;
  position: relative;
  overflow-x: auto;
  overflow-y: hidden;
  /* Scrollbar styling */
  scrollbar-width: thin;
  scrollbar-color: #4361EE rgba(0,0,0,0.3);
}

.column-chart-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  height: 100%;
  padding-bottom: 10px;
  padding-left: 10px;
  min-width: min-content;
}

.chart-column-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
  width: 50px;
  position: relative;
  animation: growUp 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) backwards;
  cursor: pointer;
}

@keyframes growUp {
  from { opacity: 0; transform: translateY(50px) scaleY(0); }
  to { opacity: 1; transform: translateY(0) scaleY(1); }
}

.column-value {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.chart-column-group:hover .column-value {
  opacity: 1;
}

.visual-column {
  width: 100%;
  border-radius: 8px 8px 0 0;
  position: relative;
  transition: all 0.3s ease;
  min-height: 10px;
}

.column-shine {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(90deg, rgba(255,255,255,0.1), transparent);
  border-radius: inherit;
}

.chart-column-group:hover .visual-column {
  filter: brightness(1.3);
  box-shadow: 0 0 20px currentColor;
}

.column-label {
  margin-top: 12px;
  font-size: 0.9rem;
  color: #94a3b8;
  writing-mode: vertical-rl;
  transform: rotate(180deg);
  text-align: right;
  height: 60px;
  letter-spacing: 1px;
}

/* Tooltip for Columns */
.column-tooltip {
  position: absolute;
  bottom: 120%;
  left: 50%;
  transform: translateX(-50%) translateY(10px);
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid #4CC9F0;
  padding: 10px;
  border-radius: 6px;
  min-width: 120px;
  opacity: 0;
  pointer-events: none;
  transition: all 0.2s;
  z-index: 10;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
}

.chart-column-group:hover .column-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.column-tooltip strong {
  display: block;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding-bottom: 4px;
  margin-bottom: 4px;
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #cbd5e1;
}

/* Stats Footer */
.stats-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255,255,255,0.08);
  display: flex;
  gap: 30px;
}

.stat-pill {
  display: flex;
  flex-direction: column;
}

.stat-pill .label {
  font-size: 0.75rem;
  color: #64748b;
  text-transform: uppercase;
}

.stat-pill .value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #fff;
}

/* Empty State */
.empty-dashboard {
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
}

.empty-visual {
  width: 120px; height: 120px;
  position: relative;
  margin-bottom: 20px;
  border: 1px solid rgba(76, 201, 240, 0.2);
  border-radius: 50%;
}

.grid-line {
  position: absolute;
  background: rgba(76, 201, 240, 0.2);
}

.grid-line.horizontal { width: 100%; height: 1px; top: 50%; }
.grid-line.vertical { height: 100%; width: 1px; left: 50%; }

.scan-line {
  width: 100%; height: 2px;
  background: #4CC9F0;
  box-shadow: 0 0 10px #4CC9F0;
  position: absolute;
  top: 0;
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% { top: 0; opacity: 0; }
  20% { opacity: 1; }
  80% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* Error Banner */
.error-banner {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fca5a5;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.error-banner svg {
  width: 20px; height: 20px;
}

/* Transitions */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.loader {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Responsive */
@media (max-width: 900px) {
  .visualization-dashboard {
    grid-template-columns: 1fr;
    height: auto;
  }

  .viz-column {
    min-height: 400px;
  }

  .cells-grid {
    flex-direction: row;
    overflow-x: auto;
  }
}
</style>
