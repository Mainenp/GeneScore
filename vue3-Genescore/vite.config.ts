// vite.config.ts

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:9092',
        changeOrigin: true
        // 注意：这里不需要rewrite，因为后端路径已经包含/api
      }
    }
  }
});
