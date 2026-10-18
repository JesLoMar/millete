import { defineConfig, type PluginOption } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
import tailwindcss from '@tailwindcss/vite';
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig(({ mode }) => {
  const isProduction = mode === 'production' || mode === 'analyze';

  const consoleFunctionsToRemove = isProduction
    ? [
      'console.log',
      'console.info',
      'console.debug',
      'console.table',
    ]
    : [];

  return {
    plugins: [
      react(),
      tailwindcss(),
      ...(mode === 'analyze'
        ? [
          visualizer({
            filename: 'stats.html',
            gzipSize: true,
            emitFile: true,
          }) as PluginOption,
        ]
        : []),
    ],

    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },

    server: {
      port: 5173,
      open: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },

    preview: {
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },

    build: {
      sourcemap: false,
      chunkSizeWarningLimit: 200,

      rolldownOptions: {
        output: {
          minify: {
            compress: {
              dropDebugger: isProduction,
              treeshake: {
                manualPureFunctions: consoleFunctionsToRemove,
              },
            },
            mangle: true,
          },

          comments: false,

          codeSplitting: {
            groups: [
              {
                name: 'react',
                test:
                  /[\\/]node_modules[\\/](react|react-dom|scheduler|react-router|react-router-dom|@remix-run)([\\/]|$)/,
                priority: 20,
              },
              {
                name: 'motion',
                test:
                  /[\\/]node_modules[\\/](framer-motion|motion-dom|motion-utils)([\\/]|$)/,
                priority: 10,
              },
            ],
          },
        },
      },
    },

    define: {
      __APP_VERSION__: JSON.stringify(
        process.env.npm_package_version ?? 'dev',
      ),
    },
  };
});
