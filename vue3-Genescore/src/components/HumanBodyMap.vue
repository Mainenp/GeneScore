<template>
  <div class="w-full h-full flex flex-col items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 rounded-xl p-4">
    <!-- Human2.png 图像容器 - 更大的尺寸 -->
    <div class="relative inline-block" ref="containerRef">
      <img 
        src="/Human2.png" 
        alt="Human Body Map" 
        class="rounded-lg shadow-md"
        style="width: 1200px; height: auto;"
        ref="imageRef"
      />
      
      <!-- 11 个蓝色节点 - 调整大小匹配原图 -->
      <div 
        v-for="node in hotspots" 
        :key="node.id"
        :style="getPosition(node.id)"
        class="absolute w-6 h-6 rounded-full bg-blue-500/70 hover:bg-blue-600/90 cursor-pointer transition-all duration-200 transform hover:scale-110 border-2 border-white flex items-center justify-center"
        :class="{ 'ring-4 ring-yellow-400': selectedNodeId === node.id }"
        @mouseenter="hoveredNode = node"
        @mouseleave="hoveredNode = null"
        @click="selectNode(node.id)"
      >
        <span class="text-white text-xs font-bold" v-if="getScore(node.tissue) !== null">
          {{ Math.round(getScore(node.tissue) || 0) }}
        </span>
      </div>
    </div>

    <!-- 悬停/选中提示 -->
    <transition name="fade">
      <div
        v-if="hoveredNode || selectedNode"
        class="mt-6 bg-white rounded-lg shadow-lg p-4 border-l-4 border-blue-500 max-w-sm"
      >
        <h3 class="font-bold text-lg text-slate-900 mb-2">
          {{ (hoveredNode || selectedNode)?.name }}
        </h3>
        <div class="space-y-2 text-sm text-slate-700">
          <div class="flex justify-between items-center">
            <span class="font-medium">Cancer Score:</span>
            <span class="font-bold text-blue-600 text-lg">
              {{ formatScore(getScore((hoveredNode || selectedNode)?.tissue)) }}
            </span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 图例 -->
    <div class="mt-4 flex flex-wrap gap-3 text-xs justify-center">
      <span class="text-slate-600">Score range:</span>
      <div class="flex items-center gap-1">
        <div class="w-3 h-3 rounded bg-blue-300"></div>
        <span>0-30</span>
      </div>
      <div class="flex items-center gap-1">
        <div class="w-3 h-3 rounded bg-blue-500"></div>
        <span>30-70</span>
      </div>
      <div class="flex items-center gap-1">
        <div class="w-3 h-3 rounded bg-blue-700"></div>
        <span>70-100</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

interface HotspotNode {
  id: string;
  name: string;
  tissue: string;
}

interface Props {
  geneScores?: Record<string, number | null>;
}

const props = withDefaults(defineProps<Props>(), {
  geneScores: () => ({})
});

// 调试：监控 prop 变化
watch(() => props.geneScores, (newVal) => {
  console.log('HumanBodyMap: geneScores updated:', newVal);
}, { immediate: true, deep: true });

const imageRef = ref<HTMLImageElement | null>(null);
const containerRef = ref<HTMLDivElement | null>(null);
const hoveredNode = ref<HotspotNode | null>(null);
const selectedNode = ref<HotspotNode | null>(null);
const selectedNodeId = ref<string>('');

// 位置配置 - 用户调整好的位置
const positions = ref<Record<string, { top: number; left: number }>>({
  brain: { top: 16.5, left: 48.5 },
  lung: { top: 40, left: 47 },
  breast: { top: 43.5, left: 53.5 },
  liver: { top: 49.5, left: 46 },
  stomach1: { top: 51.5, left: 51 },
  pancreas: { top: 54, left: 47.5 },
  bowel: { top: 56.5, left: 53.5 },
  kidney: { top: 61, left: 46 },
  ovary: { top: 67, left: 47 },
  prostate: { top: 70.5, left: 49 },
  skin: { top: 75, left: 53.5 }
});

// 11 个蓝色节点定义 - 第二个 stomach 改为 Bowel
const hotspots = computed<HotspotNode[]>(() => [
  { id: 'brain', name: 'Brain', tissue: 'Brain' },
  { id: 'lung', name: 'Lung', tissue: 'Lung' },
  { id: 'breast', name: 'Breast', tissue: 'Breast' },
  { id: 'liver', name: 'Liver', tissue: 'Liver' },
  { id: 'stomach1', name: 'Stomach', tissue: 'Stomach' },
  { id: 'pancreas', name: 'Pancreas', tissue: 'Pancreas' },
  { id: 'bowel', name: 'Bowel', tissue: 'Bowel' },
  { id: 'kidney', name: 'Kidney', tissue: 'Kidney' },
  { id: 'ovary', name: 'Ovary', tissue: 'Ovary' },
  { id: 'prostate', name: 'Prostate', tissue: 'Prostate' },
  { id: 'skin', name: 'Skin', tissue: 'Skin' }
]);

// 当前选中节点的位置值
const currentTop = ref(16.5);
const currentLeft = ref(48.5);

// 选择节点
function selectNode(id: string) {
  selectedNodeId.value = id;
  selectedNode.value = hotspots.value.find(h => h.id === id) || null;
  const pos = positions.value[id];
  if (pos) {
    currentTop.value = pos.top;
    currentLeft.value = pos.left;
  }
}

function getPosition(id: string) {
  const pos = positions.value[id];
  if (!pos) return 'top: 50%; left: 50%; transform: translate(-50%, -50%);';
  
  return `top: ${pos.top}%; left: ${pos.left}%; transform: translate(-50%, -50%);`;
}

function getScore(tissue: string | undefined): number | null {
  if (!tissue) return null;
  const score = props.geneScores[tissue];
  // 调试
  if (score !== undefined) {
    console.log(`getScore(${tissue}) =`, score);
  }
  return score !== undefined ? score : null;
}

function formatScore(score: number | null): string {
  if (score === null) return 'N/A';
  return `${Math.round(score)}/100`;
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>