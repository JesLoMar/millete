# Opening positions for own assets
Branch: `feat/owned-asset-opening-positions` from `33a96b9`; delivery: `ask-on-risk`, initial estimate 350–400 authored lines.
Verification: backend Maven, frontend pnpm, and a focused portfolio scenario.

## Specs

- S1 — The user confirmed: “Sí, me encaja y de echo era mi idea inicial”. Implement the preceding proposal: “registrar lo que ya tienes al crear el activo propio”, “sin simular una compra ni tocar Efectivo”, and “cantidad y coste de adquisición, además de la valoración inicial”.
- S2 — Preserve the distinction in “el coste de adquisición se guardaría aparte de la valoración manual”: subsequent manual prices change the value and gain of a recorded position, not its quantity or cost.
- S3 — Apply the preceding proposal to existing assets: “Para los activos ya creados, pondría **Registrar posición** en lugar del botón de compra.” No automatic backfill or inferred balances or costs.
- S4 — Address the UI that “muestra una clave de traducción en vez del texto”; ensure the position prompt and new form labels resolve in all supported languages.

## Tasks

- T1 — S1–S4 — Inline (one integrated flow; no independent writer units) — verified in `119c187`; native review declined for this candidate. Risk: item 4 (transaction and portfolio-lock ordering).

## Log

- L1 (2026-11-25) — Sí, me encaja y de echo era mi idea inicial
- L2 — The user confirmed the preceding concrete proposal; no changes to existing financial data are authorized beyond opening positions the user explicitly submits.
- L3 — Asset, price and opening holding are written in one transaction under the existing per-user portfolio lock. `POST /user-assets` still accepts the previous request body; the browser always submits the opening position. The earlier portfolio view reused a mount-time `asOf` even after invalidation; the current-portfolio query now creates a timestamp on fetch, retaining a stable cache key.
- L4 — Backend focused MockMvc test: isolated scratch baseline failed because no holding was created and incomplete inputs reached the service; patched copy passed 3/3. Frontend `pnpm type-check` and `pnpm build --logLevel warn` and backend `mvn -q '-Dmaven.test.skip=true' package` passed. Full `mvn -q test` is blocked at compile time by existing `InvestmentExportAdapterTest` and `InvestmentImportAdapterTest` symbols.
- L5 — Independent replay with mocked persistence verified lock-before-write, no cash or activity effect, no writes on invalid inputs, and repricing without quantity or cost changes. The verifier found a catalog-only regression when tracking was not configured. Restored the original form and request shape in that state; Chromium confirmed identical baseline/candidate submissions and the configured flow in all seven locales. Clean frontend checks used the installed TypeScript and Vite binaries after the verifier's scratch pnpm command unexpectedly downloaded dependencies. No isolated PostgreSQL rollback or lock-release test was run.
- L6 — Product commit `119c187` contains 385 authored changed lines. Native assessment: high due to the named locking risk; user declined native review of just this candidate. No remote delivery was requested or performed.
