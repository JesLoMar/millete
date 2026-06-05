# Hoja de Ruta: Migración Móvil de Millete (De Web a App)

Este documento detalla la estrategia para llevar el frontend actual de Millete (desarrollado en Vite + React 19 + Tailwind v4) hacia plataformas móviles (Android/iOS), dividida en dos fases para optimizar tiempo de desarrollo y asegurar un rendimiento óptimo.

---

## Fase 1: MVP Rápido con Capacitor (Lanzamiento Corto Plazo)

Objetivo: Conseguir un instalador nativo (.apk / .ipa) en el menor tiempo posible reutilizando la web actual dentro de un contenedor nativo.

### Ajustes Técnicos Necesarios en el Proyecto Web:

1. Diseño Mobile-First (UI/UX):
* Transformar componentes como Sidebar.tsx y TopNav.tsx en una barra de navegación inferior (Bottom Navigation), típica en apps financieras.
* Modificar modales basados en Radix UI (AddCategoryDialog.tsx, etc.) para que se abran desde abajo como un BottomSheet o que ocupen el 100% de la pantalla.
* Adaptar tablas de datos con volumen (CategoryTable.tsx, TransactionTable.tsx) para que se rendericen en formato "tarjetas" (cards) individuales en pantallas pequeñas.

2. Desactivar Comportamientos Web Nativos:
Añadir reglas globales de CSS para que el contenedor web responda como una app nativa, evitando scrolls elásticos molestos y selecciones de texto accidentales. Puedes pegar estas reglas en tu archivo de estilos principal:

body {
  overflow: hidden;
  position: fixed;
  width: 100%;
  height: 100%;
}

* {
  -webkit-user-select: none;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

input, textarea {
  -webkit-user-select: text;
  user-select: text;
}

### Comandos Clave de Despliegue:

* Instalar dependencias: npm i @capacitor/core @capacitor/cli
* Inicializar proyecto: npx cap init (Recuerda configurar el webDir como "dist" en tu archivo capacitor.config.json).
* Añadir plataformas: npm i @capacitor/android @capacitor/ios y luego npx cap add android o npx cap add ios.
* Flujo de sincronización diario: npm run build && npx cap sync

---

## Fase 2: App Nativa con React Native + Expo (Versión 1.0.0 Oficial)

Objetivo: Reescribir la capa de interfaz de usuario para conseguir un rendimiento excelente, fluidez nativa (scrolls e interacciones complejas de datos) y animaciones de primer nivel.

### Lo que SI se reutiliza (Migración Directa):

Gracias a la arquitectura basada en features de Millete, el núcleo lógico se puede copiar y pegar casi en su totalidad:

* Gestión de Estado y API: Custom hooks (useDashboardQueries, useInvestmentQueries, etc.) que usan @tanstack/react-query y axios.
* Esquemas y Formularios: Validaciones de zod y hooks de react-hook-form.
* Internacionalización: Archivos de traducción de i18next dentro de la ruta src/assets/locales/.

### Lo que SE DEBE REESCRIBIR (Capa Visual):

1. HTML a Componentes Nativos: Sustituir etiquetas HTML comunes como div, p, o span por los componentes correspondientes de React Native como View y Text.
2. Enrutado: Cambiar react-router-dom por Expo Router (enrutado nativo basado en archivos, organizando las vistas bajo la carpeta app/).
3. Librería de Componentes: Eliminar Radix UI. Implementar Gluestack UI o NativeWind v4 para poder seguir utilizando clases de Tailwind adaptadas al entorno móvil.
4. Gráficos: Reemplazar recharts (dependiente del DOM web) por una solución móvil de alto rendimiento como Victory Native o Gifted Charts para los componentes CategoryDonut.tsx y HistoryChart.tsx.

---

## Buenas Prácticas para Aplicar desde ya en la Web

Para que el salto de la Fase 1 a la Fase 2 sea directo y limpio:

* Aislar la lógica de la UI: No mezclar JSX o lógica puramente visual dentro de los custom hooks de las features. Los hooks solo deben exponer datos, estados de carga (isLoading, error) y funciones de mutación.
* Encapsular Axios: Mantener la capa de servicios (auth.service.ts, etc.) totalmente libre de dependencias del navegador (como localStorage o cookies web directas) para que sigan funcionando de la misma manera en entornos móviles nativos.