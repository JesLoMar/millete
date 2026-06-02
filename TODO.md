===================================
IDEAS A CHOLÓN
===================================

- Añadir sugerencias con IA. ChatGPT Finance.
- Arreglar y mejorar la lógica de las Planned Transactions.
- Añadir escaneo de tickets para que se añada directamente la categoría compra o a lo que pertenezca y el dinero gastado.
- Diseño propio mobile.
- Reemplazo de dependencias como shadowcn y componentes rápidos con compoentes propios.
- Importar movimientos con csv bancarios, investigar bancos más usados en España.
- Añadir animaciones y feedback visual a todas las vistas.
- Eliminar la necesidad de correo, invitaciones por notificación in-app.
- Llamar a la Yuli para que me ayude a crear paletas de colores, iconos y diseño en general.
- Crear perfil de usuario y pestaña de settings. (configuraciones, borrados de cuenta, elegir temas e idiomas movidos ahí)
- Hacer comprobaciones de usabilidad y accesibilidad.
- Mejorar y definir mejor el código de conducta, el cómo contribuir al proyecto y el tema de seguridad.
- Agregar serialización a los archivos descargables e importables.
- [ ] Dejar actualizada la documentación. [Actualizado la docu de categorias, plannedtransactions y family de back]
- [ ] Añadir y diseñar wiki (manual de usuario) propia con traducción.
- [ ] Reducir el uso de dependencias en front.
- [ ] Añadir más avisos y alertas. (O simplemente revisar, no llenar por llenar)
- [ ] Diseñar sistema de errores en back.
- [ ] Añadir metas de ahorro. (Sistema de aportación como familias).

===================================
MEJORAS v0.0.2
===================================

- [✓] Módulo Categorías (IDOR en Actualización): Modificar el método update en CategoryService y CategoryController para que reciba el userId extraído del JWT de forma que valide que la categoría pertenece al usuario autenticado antes de aplicar los cambios.

- [✓] Módulo Categorías (IDOR en Eliminación): Modificar el método delete en CategoryService y CategoryController para que también reciba el userId y bloquee el borrado lógico de categorías que pertenezcan a otros identificadores de usuario.

- [✓] Limpieza de Logs de Depuración: Localizar y sustituir todos los restos de System.out.println() (especialmente en los controladores y servicios de dataexport y dataimport) por el uso de loggers paramétricos utilizando la anotación @Slf4j de Lombok o instanciando SLF4J de forma nativa.

- [✓] Lógica de Transacciones Recurrentes: Reemplazar la comparación exacta de fechas en shouldExecuteToday dentro de PlannedTransactionService. Implementar en su lugar un motor de cálculo temporal que use aritmética de fechas (días, semanas, meses entre el inicio de la plantilla y hoy) para que la recurrencia funcione después del primer ciclo.

- [✓] Corrección de Versiones en pom.xml: Ajustar la etiqueta <parent> a una versión comercialmente estable y disponible de la rama de desarrollo, asegurando que las propiedades de versiones de librerías dependientes como jjwt y mapstruct no creen conflictos en tu entorno de integración continua (CI/CD).

- [✓] Validación de Consistencia en Modo de Distribución Familiar: Añadir validaciones en FamilyService.updateMember() para ignorar campos no aplicables según el modo (EQUITATIVE, PROPORTIONAL, CUSTOM). En FamilyUnit.calculateContributions(), lanzar excepción si totalSalary = 0 en modo PROPORTIONAL. En FamilyService.updateFamily(), requerir customPercentage en todos los miembros al cambiar a modo CUSTOM. Todos los mensajes de excepción y logs unificados en inglés. Tests actualizados.

- [✓] Unificación de Mappers a MapStruct: Refactorizar todos los mappers manuales (CategoryMapper, FamilyPersistenceMapper, FamilyContributionMapper, FamilyInvitationEntityMapper, FamilyMemberEntityMapper, FamilyUnitEntityMapper) a interfaces MapStruct con componentModel = SPRING. Eliminado FamilyPersistenceMapper duplicado. Corregidos imports en CategoryPostgresAdapter, FamilyPostgresAdapter y FamilyContributionPostgresAdapter. Resuelto conflicto de constructores en CategoryEntityMapper con @ObjectFactory.

- [✓] Mejorar y optimizar el cómo se manejan las variables entre los repositorios. (Dockerfile, docker-compose.yml).

- [✓] Actualizar la info de la página del login.

- [✓] Módulo de categorías ultra-robustos. 
    ==============================
    ✅	IDOR en update	CategoryService, CategoryRepository, JpaCategoryRepository, CategoryPostgresAdapter
    ✅	IDOR en delete	CategoryService
    ✅	Transacciones filtradas por userId	CategoryService
    ✅	DTO con Bean Validation	RegisterCategoryRequestDTO
    ✅	Color validado con regex ^#[0-9A-Fa-f]{6}$	RegisterCategoryRequestDTO, Category
    ✅	findAll() eliminado	GetCategoryUseCase, CategoryRepository, CategoryService, CategoryPostgresAdapter
    ✅	Errores genéricos 404	CategoryService
    ✅	Command objects inmutables	RegisterCategoryCommand, UpdateCategoryCommand
    ✅	@Validated en controller	CategoryController
    ==============================