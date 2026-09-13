<!-- src/components/GeneScoreTable.vue -->

<template>
  <div class="table-container">
    <table class="gene-table">
      <thead>
      <tr>
        <th>序号</th>
        <th>基因符号</th>
        <th>癌症类型</th>
        <th>评分</th>
        <th>评分等级</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(item, index) in data" :key="item.id">
        <td>{{ index + 1 }}</td>
        <td class="gene-symbol">{{ item.geneSymbol }}</td>
        <td>{{ item.cancerType }}</td>
        <td class="score">{{ item.score.toFixed(2) }}</td>
        <td>
            <span :class="['score-badge', getScoreLevel(item.score)]">
              {{ getScoreLevelText(item.score) }}
            </span>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import type { GeneScore } from '@/types/gene';

// Props定义
defineProps<{
  data: GeneScore[];
}>();

/** 根据评分获取等级样式类 */
const getScoreLevel = (score: number): string => {
  if (score >= 90) return 'level-high';
  if (score >= 70) return 'level-medium';
  if (score >= 50) return 'level-low';
  return 'level-very-low';
};

/** 根据评分获取等级文本 */
const getScoreLevelText = (score: number): string => {
  if (score >= 90) return '高相关';
  if (score >= 70) return '中相关';
  if (score >= 50) return '低相关';
  return '极低';
};
</script>

<style scoped>
.table-container {
  overflow-x: auto;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.gene-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.gene-table th,
.gene-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.gene-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 13px;
  letter-spacing: 0.5px;
}

.gene-table tbody tr:hover {
  background: #f8f9fa;
}

.gene-symbol {
  font-weight: 700;
  color: #2980b9;
  font-family: 'Courier New', monospace;
}

.score {
  font-weight: 600;
  font-size: 16px;
}

.score-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.level-high {
  background: #d4edda;
  color: #155724;
}

.level-medium {
  background: #fff3cd;
  color: #856404;
}

.level-low {
  background: #ffeeba;
  color: #856404;
}

.level-very-low {
  background: #f8d7da;
  color: #721c24;
}
</style>
