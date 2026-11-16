# Asistente de interpretación

El módulo expone una ruta para interpretar texto proporcionado por el usuario. Controlador: [`AssistantController.java`](../../backend/src/main/java/com/puntomartinez/millete/assistant/infrastructure/in/controller/AssistantController.java).

## API

`POST /api/v1/assistant/interpret` recibe `InterpretUserInputRequestDTO` con texto `input` y responde `InterpretationResponseDTO`. La identidad de usuario se toma del principal `JwtUser`; no se acepta un identificador de usuario del cuerpo.

La respuesta representa una interpretación y posibles acciones/datos extraídos; no equivale a ejecutar por sí misma las acciones financieras. La orquestación está en [`AssistantService.java`](../../backend/src/main/java/com/puntomartinez/millete/assistant/application/services/AssistantService.java). Las resoluciones de transacciones, metas, categorías y recurrencias se realizan mediante los adaptadores de salida del módulo.

La interpretación no persiste una transacción ni crea una meta: el cliente debe pedir confirmación y enviar una operación al endpoint del módulo correspondiente. La respuesta del modelo no sustituye la validación de los módulos financieros.

## Proveedores

El código separa la extracción estructurada del proveedor de decisión. La configuración está en `assistant/infrastructure/out/ai/config` y `assistant/infrastructure/out/verdict/config`; endpoints y credenciales de proveedor se suministran por configuración externa. No se incluyen valores secretos en esta guía. La disponibilidad y configuración efectiva dependen del entorno desplegado.
