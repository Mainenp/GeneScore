<template>
  <nav class="bg-white shadow-md">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex items-center">
          <a href="/" class="flex-shrink-0 flex items-center">
            <img src="/logo.png" alt="GeneScoreDB Logo" class="h-10 w-auto mr-3">
            <span class="text-2xl font-bold text-primary">GeneScoreDB</span>
          </a>
        </div>
        <div class="flex items-center space-x-8">
          <router-link to="/" class="text-gray-700 hover:text-primary font-medium">Home</router-link>
          <router-link to="/browse" class="text-gray-700 hover:text-primary font-medium">Browse Cancers</router-link>
          <router-link to="/analysis" class="text-gray-700 hover:text-primary font-medium">Analysis</router-link>
          <router-link to="/dko" class="text-gray-700 hover:text-primary font-medium">Double Knockout Analysis</router-link>
          <router-link to="/downloads" class="text-gray-700 hover:text-primary font-medium">Downloads</router-link>
          <router-link to="/api" class="text-gray-700 hover:text-primary font-medium">API</router-link>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
// NavBar component
</script>

<style scoped>
/* NavBar styles */
</style>