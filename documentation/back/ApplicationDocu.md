# Aplicación y guías del backend

Esta carpeta documenta el backend en su estado actual. Los contratos HTTP y las reglas descritas se basan en los controladores y servicios enlazados; ante cambios, esos archivos son la referencia.

## Índice de guías

| Guía | Área |
|---|---|
| [Base de datos](BDDocu.md) | Migraciones Flyway y esquema |
| [Infraestructura compartida](SharedInfrastructureDocu.md) | Seguridad, tiempo, errores y tareas programadas |
| [Usuarios](UsersDocu.md) | Autenticación, perfil y sesiones |
| [Categorías](CategoriesDocu.md) | Categorías personales |
| [Transacciones](TransactionsDocu.md) | Movimientos financieros |
| [Dashboard](DashboardDocu.md) | Consultas de resumen |
| [Metas grupales](GroupGoalsDocu.md) | Aportaciones e invitaciones |
| [Metas de ahorro](SavingGoalsDocu.md) | Objetivos personales |
| [Transacciones planificadas](PlannedTransactionsDocu.md) | Recurrencias |
| [Importación y exportación](ImportExportDocu.md) | Snapshots y formatos descargables |
| [Inversiones](InvestmentsDocu.md) | Libro de actividades, valoración y datos de mercado |
| [Asistente](AssistantDocu.md) | Interpretación de texto |
| [Notificaciones](NotificationsDocu.md) | Bandeja, lectura y acciones |

## Configuración y ejecución

La aplicación se construye con Spring Boot **4.0.6** y Java **25**; la versión del proyecto es **0.2.0**. La configuración común está en [`application.yml`](../../backend/src/main/resources/application.yml). No existe un `application-prod.yml`: el despliegue obtiene configuración por variables de entorno en [`docker-compose.yml`](../../docker-compose.yml). Los nombres de variables y sus valores efectivos deben verificarse en esos archivos y en el entorno; esta guía no reproduce valores privados.

Propiedades importantes incluyen `spring.datasource.*`, `spring.jpa.hibernate.ddl-auto` y `spring.flyway.*`. Hibernate valida el esquema y Flyway aplica migraciones. Los límites de multipart están configurados en Spring y la importación aplica además su propia validación. No se asume que los valores locales sean aptos para producción.

Para compilar el backend en este repositorio se utiliza Maven del sistema: `mvn "-Dmaven.test.skip=true" package`. Las pruebas no se saltan por defecto de esta forma en `mvn test`; la compilación actual de pruebas tiene referencias a tipos de inversión retirados. Este es un límite observado en el árbol de código, no el resultado de una ejecución en esta tarea.

## Límites del documento

Esta guía describe el comportamiento implementado. No declara propuestas, endpoints ausentes ni garantías que no estén expresadas por las implementaciones. Las tareas de detectar o corregir defectos quedan fuera de estas guías.
