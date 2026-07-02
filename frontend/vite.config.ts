import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@bank/shared': path.resolve(__dirname, 'packages/shared/src'),
      '@bank/mf-account-creation': path.resolve(__dirname, 'packages/mf-account-creation/src'),
      '@bank/mf-balance-inquiry': path.resolve(__dirname, 'packages/mf-balance-inquiry/src'),
      '@bank/mf-deposit': path.resolve(__dirname, 'packages/mf-deposit/src'),
      '@bank/mf-withdraw': path.resolve(__dirname, 'packages/mf-withdraw/src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/accounts': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
