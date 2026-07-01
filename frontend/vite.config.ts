import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
import tailwindcss from '@tailwindcss/vite';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  loadEnv(mode, process.cwd(), 'VITE_');
  const isProduction = mode === 'production';
  
  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      open: true,
    },
    build: {
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: isProduction,
          drop_debugger: isProduction,
          pure_funcs: isProduction ? ['console.log', 'console.info', 'console.debug', 'console.table'] : [],
        },
        format: {
          comments: false,
        },
      },
      target: 'es2020',
      sourcemap: false,
      chunkSizeWarningLimit: 200,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (id.includes('node_modules')) {
              if (id.includes('react-router') || id.includes('@remix-run')) {
                return 'router';
              }
              if (
                id.includes('i18next') ||
                id.includes('react-i18next') ||
                id.includes('i18next-browser-languagedetector') ||
                id.includes('i18next-resources-to-backend')
              ) {
                return 'i18n';
              }
              if (
                id.includes('react-hook-form') ||
                id.includes('zod') ||
                id.includes('@hookform')
              ) {
                return 'forms';
              }
              if (
                id.includes('@tanstack') ||
                id.includes('axios')
              ) {
                return 'data';
              }
              if (
                id.includes('framer-motion') ||
                id.includes('motion-dom') ||
                id.includes('motion-utils')
              ) {
                return 'motion';
              }
              if (id.includes('lucide-react')) {
                return 'icons';
              }
              if (id.includes('sonner')) {
                return 'notifications';
              }
              if (id.includes('clsx') || id.includes('tailwind-merge')) {
                return 'utils';
              }
              if (
                id.includes('@radix-ui') ||
                id.includes('@floating-ui') ||
                id.includes('class-variance-authority')
              ) {
                return 'ui';
              }
              if (
                id.includes('react') ||
                id.includes('scheduler') ||
                id.includes('use-sync-external-store')
              ) {
                return 'vendor';
              }
            }
          },
        },
      },
    },
    optimizeDeps: {
      include: ['react', 'react-dom', 'react-router-dom', '@tanstack/react-query', 'axios'],
    },
    define: {
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version),
    },
  };
});