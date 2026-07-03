# AuthForm — Análisis del warning `react-hooks/incompatible-library`

## ¿Qué dice el linter?

El warning aparece en `AuthForm.tsx:50`:

```ts
const usernameWatch = watch("usernameRegistro")
```

La regla `react-hooks/incompatible-library` detecta que `watch` de React Hook Form devuelve una función que **no puede ser memoizada de forma segura** por React Compiler. Esto significa que si el componente se memoiza (con `React.memo` o por el compilador automático), los valores observados por `watch` podrían quedar obsoletos (stale UI).

## ¿Por qué está mal hecho?

`watch` de React Hook Form es una función que **no es estable** entre renders. Cada vez que se llama, devuelve el valor actual del campo, pero la función misma se recrea. React Compiler (y `useMemo`) no pueden trackear dependencias de forma segura con APIs que mutan estado internamente sin notificar a React.

El problema concreto en `AuthForm`:
- `watch("usernameRegistro")` y `watch("emailRegistro")` se usan para calcular `hasIdentifier`
- Este valor se pasa a `<RegisterFields hasIdentifier={hasIdentifier} />`
- Si `AuthForm` se memoiza, `hasIdentifier` podría no actualizarse cuando el usuario escribe

## Impacto real

- **Stale UI:** Si React Compiler memoiza `RegisterFields`, el botón de submit podría quedar deshabilitado/habilitado incorrectamente cuando el usuario escribe en los campos de registro.
- **Confusión para el usuario:** El formulario no responde visualmente a los cambios, o responde con delay.

## ¿Cómo debería hacerse?

### Opción A: Usar `useWatch` de React Hook Form (recomendada)

React Hook Form proporciona `useWatch`, que está diseñado para ser usado dentro de componentes memoizados:

```tsx
import { useWatch } from "react-hook-form"

function RegisterFields({ control }: { control: Control<CombinedAuthFormData> }) {
  const username = useWatch({ control, name: "usernameRegistro" })
  const email = useWatch({ control, name: "emailRegistro" })
  const hasIdentifier = !!username?.trim() || !!email?.trim()
  // ...
}
```

**Ventaja:** `useWatch` se suscribe a cambios del formulario de forma reactiva, compatible con React Compiler.

### Opción B: Pasar `control` a los sub-componentes

En lugar de calcular `hasIdentifier` en `AuthForm`, pasar el `control` de `useForm` a `RegisterFields` y que este use `useWatch` internamente:

```tsx
// AuthForm.tsx
const { control, register, handleSubmit, reset, formState: { errors, isValid } } = useForm(...)

// ...
<RegisterFields control={control} register={register} errors={errors} />
```

### Opción C: Usar estado de React en lugar de `watch`

Si React Hook Form no es estrictamente necesario para estos campos, usar `useState` + `onChange`:

```tsx
const [username, setUsername] = useState("")
const [email, setEmail] = useState("")
const hasIdentifier = !!username.trim() || !!email.trim()
```

**Desventaja:** Pierdes la integración con `useForm` (validación, reset, etc.).

## Recomendación

**Opción A** es la más limpia: extraer `useWatch` a un sub-componente o hook separado. Esto requiere reestructurar `AuthForm` para que `RegisterFields` reciba `control` en lugar de `hasIdentifier` calculado.

El cambio mínimo sería:
1. Añadir `control` al destructuring de `useForm`
2. Pasar `control` a `RegisterFields`
3. Dentro de `RegisterFields`, usar `useWatch({ control, name: "usernameRegistro" })`
4. Eliminar `usernameWatch`, `emailWatch` y `hasIdentifier` de `AuthForm`
