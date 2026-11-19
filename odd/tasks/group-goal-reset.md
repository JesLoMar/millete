# Group goal movement reset
Objective: permanently remove one group's contributions and withdrawals through an admin-only action. Baseline branch: `v0.2.0`.
Delivery: one feature unit (`ask-on-risk`); Maven group-goal tests and frontend type-check/build.

## Specs
- S1: “quiero añadir un botón dentro de cada meta grupal que sirva para resetear los aportes y sacadas de dinero de una meta. Este botón solo lo podría ver y usar un administrador de una meta grupal.” Scope: existing group-goal detail UI and permission check for that goal on the server.
- S2: “que borre los aportes y retiradas y ponga el progreso a 0 en consecuencia.” Scope: permanent deletion with confirmation, limited to movements of the selected goal; its membership and settings stay intact.

## Tasks
- [ ] T1 · S1–S2 · Inline (one behavior across UI and backend); focused and independent code checks passed. PostgreSQL runtime proof and commit pending.

## Log
L1: Ahora, sobre metas grupales, quiero añadir un botón dentro de cada meta grupal que sirva para resetear los aportes y sacadas de dinero de una meta. Este botón solo lo podría ver y usar un administrador de una meta grupal.

L2: que borre los aportes y retiradas y ponga el progreso a 0 en consecuencia. será lo mas facil

L3: The endpoint is `DELETE /api/v1/goals/{goalId}/contributions`. It checks the active administrator under the membership row lock and permanently deletes only that goal's movements. Contribution writes and full-goal deactivation obtain the goal lock before membership and contribution operations. The detail view confirms permanent deletion and invalidates the existing group-goal query family on success.

L4: The endpoint test failed on the baseline (`405` instead of `204`), then passed along with the focused group-goal command and withdrawal-concurrency tests in an isolated build using the baseline Spring Boot `4.0.6` POM. Frontend type-check and build passed. The workspace's full offline Maven suite cannot resolve uncached Spring Boot `4.0.10`. No local PostgreSQL runtime was started.

L5: The independent verifier confirmed both specs by source inspection and a mocked UI probe, including permission and selected-goal isolation. PostgreSQL write/lock behavior remains unverified. The verifier's pnpm command unexpectedly downloaded dependencies in scratch despite offline settings; that run is not offline proof. Scratch was removed. Advisory: unconfirmed state may reopen on returning to the same goal; a fresh confirmation click is still required.

L6: The user will test in their separate Docker deployment. Next: verify admin/non-admin behavior, zero progress and empty history for a disposable goal, and unchanged data for another goal. No commit was made while proof remains pending.
