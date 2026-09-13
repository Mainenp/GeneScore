<template>
  <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Gene Network</h2>

    <!-- Network Visualization -->
    <div class="h-96 border border-gray-200 rounded-lg overflow-hidden">
      <svg ref="networkSvg" class="w-full h-full"></svg>
    </div>

    <!-- Network Statistics -->
    <div class="mt-6 pt-6 border-t border-gray-200">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-blue-50 rounded-lg p-4">
          <p class="text-sm font-semibold text-gray-600">Total Nodes</p>
          <p class="text-2xl font-bold text-blue-600">{{ safeNetworkData.nodes.length }}</p>
        </div>
        <div class="bg-green-50 rounded-lg p-4">
          <p class="text-sm font-semibold text-gray-600">Total Edges</p>
          <p class="text-2xl font-bold text-green-600">{{ safeNetworkData.links.length }}</p>
        </div>
        <div class="bg-purple-50 rounded-lg p-4">
          <p class="text-sm font-semibold text-gray-600">Network Density</p>
          <p class="text-2xl font-bold text-purple-600">{{ networkDensity.toFixed(3) }}</p>
        </div>
      </div>
    </div>

    <!-- Network Legend -->
    <div class="mt-4 pt-4 border-t border-gray-200">
      <h3 class="text-sm font-semibold text-gray-700 mb-2">Network Legend</h3>
      <div class="flex flex-wrap gap-4">
        <div class="flex items-center">
          <div class="w-4 h-4 rounded-full bg-blue-500 mr-2"></div>
          <span class="text-xs text-gray-600">Query Gene</span>
        </div>
        <div class="flex items-center">
          <div class="w-4 h-4 rounded-full bg-green-500 mr-2"></div>
          <span class="text-xs text-gray-600">Interacting Gene</span>
        </div>
        <div class="flex items-center">
          <div class="w-4 h-4 rounded-full bg-purple-500 mr-2"></div>
          <span class="text-xs text-gray-600">Protein-Protein Interaction</span>
        </div>
        <div class="flex items-center">
          <div class="w-4 h-4 rounded-full bg-orange-500 mr-2"></div>
          <span class="text-xs text-gray-600">Regulatory Interaction</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import * as d3 from 'd3';

interface NetworkNode {
  id: string;
  symbol: string;
  type: string;
  score?: number;
}

interface NetworkLink {
  source: string;
  target: string;
  type: string;
  score?: number;
}

interface NetworkData {
  nodes: NetworkNode[];
  links: NetworkLink[];
}

const props = defineProps<{
  networkData: NetworkData;
  geneSymbol: string;
}>();

const safeNetworkData = computed(() => {
  if (Array.isArray(props.networkData)) {
    return { nodes: [], links: [] };
  }

  if (!props.networkData) {
    return { nodes: [], links: [] };
  }

  return {
    nodes: props.networkData.nodes || [],
    links: props.networkData.links || []
  };
});

const safeGeneSymbol = computed(() => {
  return props.geneSymbol || '';
});

const networkSvg = ref<SVGSVGElement | null>(null);

const networkDensity = computed(() => {
  if (!safeNetworkData.value.nodes || !safeNetworkData.value.links) return 0;
  const n = safeNetworkData.value.nodes.length;
  if (n <= 1) return 0;
  const possibleEdges = n * (n - 1) / 2;
  return safeNetworkData.value.links.length / possibleEdges;
});

const drawNetwork = () => {
  if (!networkSvg.value) return;

  // 清除旧的图表
  d3.select(networkSvg.value).selectAll('*').remove();

  // 清除旧的tooltip
  d3.selectAll('.gene-tooltip').remove();

  // 创建tooltip
  const tooltip = d3.select('body')
    .append('div')
    .attr('class', 'gene-tooltip')
    .style('position', 'absolute')
    .style('background', 'rgba(0, 0, 0, 0.8)')
    .style('color', 'white')
    .style('padding', '6px 10px')
    .style('border-radius', '4px')
    .style('font-size', '12px')
    .style('pointer-events', 'none')
    .style('opacity', 0)
    .style('z-index', 1000);

  // 获取尺寸，加入备用值以防初始渲染时 clientWidth 为 0
  const width = networkSvg.value.clientWidth || 800;
  const height = networkSvg.value.clientHeight || 384;

  // 检查数据是否有效
  if (!safeNetworkData.value.nodes || !safeNetworkData.value.links || safeNetworkData.value.nodes.length === 0) {
    d3.select(networkSvg.value)
      .append('text')
      .attr('x', width / 2)
      .attr('y', height / 2)
      .attr('text-anchor', 'middle')
      .attr('fill', 'gray')
      .text('No network data available');
    return;
  }

  // 🔴 核心修复：对节点和连线进行深拷贝，脱离 Vue 的 Proxy 响应式系统
  // 这样 D3 在内部注入 x, y, vx, vy 时就不会导致错误，也不会触发 watch 无限循环重绘
  const nodes = JSON.parse(JSON.stringify(safeNetworkData.value.nodes));
  const links = JSON.parse(JSON.stringify(safeNetworkData.value.links));

  // 创建力导向模拟器（使用拷贝后的 nodes 和 links）
  const simulation = d3.forceSimulation(nodes)
    .force('link', d3.forceLink(links).id((d: any) => d.id).distance(100))
    .force('charge', d3.forceManyBody().strength(-300))
    .force('center', d3.forceCenter(width / 2, height / 2))
    .force('collision', d3.forceCollide().radius(30));

  // 绘制连线
  const link = d3.select(networkSvg.value)
    .append('g')
    .selectAll('line')
    .data(links) // 使用拷贝后的 links
    .enter()
    .append('line')
    .attr('stroke', (d: any) => {
      switch (d.type) {
        case 'protein-protein': return '#9333ea';
        case 'regulatory': return '#f97316';
        default: return '#64748b';
      }
    })
    .attr('stroke-width', 1.5)
    .attr('stroke-opacity', 0.6);

  // 绘制节点
  const node = d3.select(networkSvg.value)
    .append('g')
    .selectAll('circle')
    .data(nodes) // 使用拷贝后的 nodes
    .enter()
    .append('circle')
    .attr('r', (d: any) => {
      const geneName = d.symbol || d.id || d.label;
      return geneName === safeGeneSymbol.value || d.type === 'query' ? 8 : 6;
    })
    .attr('fill', (d: any) => {
      const geneName = d.symbol || d.id || d.label;
      return geneName === safeGeneSymbol.value || d.type === 'query' ? '#3b82f6' : '#22c55e';
    })
    .attr('stroke', '#fff')
    .attr('stroke-width', 2)
    .style('cursor', 'pointer')
    .on('mouseover', (event: any, d: any) => {
      const geneName = d.symbol || d.id || d.label;
      tooltip
        .style('opacity', 1)
        .html(geneName)
        .style('left', (event.pageX + 10) + 'px')
        .style('top', (event.pageY - 10) + 'px');
    })
    .on('mouseout', () => {
      tooltip.style('opacity', 0);
    })
    .call(d3.drag()
      .on('start', (event: any, d: any) => {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
      })
      .on('drag', (event: any, d: any) => {
        d.fx = event.x;
        d.fy = event.y;
      })
      .on('end', (event: any, d: any) => {
        if (!event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
      }));

  // 绘制标签
  const label = d3.select(networkSvg.value)
    .append('g')
    .selectAll('text')
    .data(nodes) // 使用拷贝后的 nodes
    .enter()
    .append('text')
    .text((d: any) => d.symbol || d.id || d.label)
    .attr('font-size', 10)
    .attr('dx', 12)
    .attr('dy', 4);

  // 在每一帧更新位置
  simulation.on('tick', () => {
    link
      .attr('x1', (d: any) => d.source.x)
      .attr('y1', (d: any) => d.source.y)
      .attr('x2', (d: any) => d.target.x)
      .attr('y2', (d: any) => d.target.y);

    node
      .attr('cx', (d: any) => d.x)
      .attr('cy', (d: any) => d.y);

    label
      .attr('x', (d: any) => d.x)
      .attr('y', (d: any) => d.y);
  });
};

// 监听数据变化
watch(() => props.networkData, () => {
  nextTick(() => {
    drawNetwork();
  });
}, { deep: true });

// 挂载后初始化图表
onMounted(() => {
  nextTick(() => {
    drawNetwork();
  });
});
</script>

<style scoped>
.network-container {
  overflow: hidden;
}
</style>
