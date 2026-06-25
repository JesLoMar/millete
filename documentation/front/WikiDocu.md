# Wiki — Documentación técnica (Frontend)

## Estructura de archivos

- **components/WikiContent.tsx** — Renderiza el contenido de una sección
- **components/WikiLayout.tsx** — Layout con sidebar y área de contenido
- **components/WikiSidebar.tsx** — Sidebar con navegación de secciones
- **pages/page.tsx** — Página principal de la wiki
- **types/index.ts** — Tipos de datos de la wiki

---

## pages/page.tsx

Página de wiki/ayuda. Usa `useParams` para obtener la sección actual (`/wiki/:section?`).

### Estados

- **Sin sección:** muestra título "Guía de uso" e instrucciones para seleccionar una sección.
- **Con sección:** renderiza `<WikiContent sectionKey={section} />`.

### Traducciones

Namespace `wiki`. Si una sección no existe en el idioma actual, muestra mensaje genérico con fallback.

---

## components/WikiSidebar.tsx

Sidebar de navegación de la wiki.

### Funcionalidad

- Lista las secciones disponibles desde las traducciones (`t('sections', { returnObjects: true })`).
- Cada sección es un enlace a `/wiki/:sectionKey`.
- Resalta la sección activa según la URL.

---

## components/WikiContent.tsx

Renderiza el contenido de una sección de la wiki.

### Props

- **sectionKey:** string — clave de la sección a renderizar.

### Funcionamiento

1. Lee todas las secciones desde i18n con `returnObjects: true`.
2. Busca la sección correspondiente a `sectionKey`.
3. Si no existe, muestra mensaje "Sección no encontrada".
4. Si existe, renderiza:
   - Título de la sección (`section.title`).
   - Descripción (`section.description`).
   - Lista de temas (`section.topics`), cada uno con título, contenido (con saltos de línea preservados) e imagen opcional.

---

## components/WikiLayout.tsx

Layout compartido de la wiki.

### Estructura

- Sidebar izquierdo (`WikiSidebar`).
- Área de contenido principal a la derecha.
- Diseño responsive: en móvil el sidebar puede ocultarse o colapsar.

---

## types/index.ts

- **WikiTopic:** title, content, image?.
- **WikiSection:** title, description, topics: WikiTopic[].
- **WikiSections:** sections: Record<string, WikiSection>.

---

## Contenido

El contenido de la wiki se define en los archivos de traducción i18n bajo la clave `wiki.sections`:

```json
{
  "sections": {
    "getting-started": {
      "title": "Primeros pasos",
      "description": "...",
      "topics": [
        { "title": "Crear una cuenta", "content": "..." }
      ]
    }
  }
}
```

---

## Notas de implementación (v0.1.0)

- La wiki es estática y se sirve desde archivos JSON de i18n.
- Soporta imágenes opcionales por tema.
- No requiere autenticación (ruta pública opcional, actualmente protegida).
