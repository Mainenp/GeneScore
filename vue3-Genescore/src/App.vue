<!-- src/App.vue -->
<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-50">
    <!-- Main Content -->
    <main class="transition-all duration-300">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
</script>

<style>
@tailwind base;
@tailwind components;
@tailwind utilities;

/* Global Styles */
* {
  @apply transition-colors duration-200;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: 'Inter', system-ui, sans-serif;
  @apply bg-gradient-to-br from-slate-50 via-white to-slate-50 text-gray-800;
}

/* Smooth Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Scrollbar Styling */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  @apply bg-gray-100;
}

::-webkit-scrollbar-thumb {
  @apply bg-gray-400 rounded-full hover:bg-gray-500;
}

/* Selection Styling */
::selection {
  @apply bg-blue-500 text-white;
}

/* Focus Styles for Accessibility */
:focus-visible {
  @apply outline-2 outline-offset-2 outline-blue-500;
}

/* Button Base Styles */
button {
  @apply transition-all duration-200 ease-out;
}

button:active {
  @apply scale-95;
}

/* Link Styles */
a {
  @apply text-blue-600 hover:text-blue-700 transition-colors;
}

a:focus-visible {
  @apply outline-2 outline-offset-2 outline-blue-500 rounded;
}

/* Card Hover Effects */
.card-hover {
  @apply transition-all duration-300 hover:shadow-lg hover:scale-105;
}

/* Gradient Text */
.gradient-text {
  @apply bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent;
}

/* Loading Animation */
@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

.shimmer {
  animation: shimmer 2s infinite;
  background: linear-gradient(
    to right,
    #f0f0f0 8%,
    #f8f8f8 18%,
    #f0f0f0 33%
  );
  background-size: 800px 104px;
}

/* Responsive Typography */
@screen sm {
  body {
    @apply text-base;
  }
}

@screen md {
  body {
    @apply text-base;
  }
}

/* Print Styles */
@media print {
  body {
    @apply bg-white;
  }
}
</style>