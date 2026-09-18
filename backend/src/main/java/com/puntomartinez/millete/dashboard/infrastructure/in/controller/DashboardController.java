package com.puntomartinez.millete.dashboard.infrastructure.in.controller;

import com.puntomartinez.millete.dashboard.domain.ports.in.GetDashboardDataUseCase;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller del dashboard financiero.
 *
 * <p>ACOPLE CON OTROS MÓDULOS (deuda técnica documentada):
 * <ul>
 *   <li>El puerto {@code GetDashboardDataUseCase} devuelve directamente
 *       DTOs de infraestructura (REST). Lo correcto hexagonalmente sería
 *       definir resultados de aplicación y mapear a DTOs solo en el
 *       controlador. Se mantiene así por pragmatismo hasta que se revise
 *       el frontend y se pueda hacer la migración coordinada.</li>
 *   <li>Los adaptadores de salida dependen de repositorios de otros
 *       bounded contexts (categories, transactions, savingsgoals).
 *       Esto es mejor que depender de entidades JPA directamente, pero
 *       genera acoplamiento con los puertos de salida de otros módulos.
 *       Si se desea una frontera más fuerte, definir APIs de consulta
 *       explícitas para módulos consumidores.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final int MAX_RECENT_TRANSACTIONS = 50;

    private final GetDashboardDataUseCase getDashboardDataUseCase;

    private UUID getUserId(Authentication authentication) {
        return ((JwtUser) authentication.getPrincipal()).getId();
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsResponseDTO> getMetrics(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                getDashboardDataUseCase.getMetrics(
                        getUserId(authentication),
                        period
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<DashboardHistoryResponseDTO> getHistory(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                getDashboardDataUseCase.getHistory(
                        getUserId(authentication),
                        period
                )
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<DashboardCategoriesResponseDTO> getCategories(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                getDashboardDataUseCase.getCategories(
                        getUserId(authentication),
                        period
                )
        );
    }

    @GetMapping("/budgets")
    public ResponseEntity<DashboardBudgetsResponseDTO> getBudgets(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                getDashboardDataUseCase.getBudgets(
                        getUserId(authentication),
                        period
                )
        );
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<DashboardTransactionsResponseDTO> getRecentTransactions(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication
    ) {
        int safeLimit = Math.min(
                Math.max(limit, 1),
                MAX_RECENT_TRANSACTIONS
        );

        return ResponseEntity.ok(
                getDashboardDataUseCase.getRecentTransactions(
                        getUserId(authentication),
                        safeLimit
                )
        );
    }

    /**
     * Devuelve los objetivos de ahorro activos del usuario.
     *
     * <p>LÍMITE: Se devuelven como máximo 20 objetivos, ordenados por
     * prioridad (HIGH → MEDIUM → LOW) y fecha de creación descendente.
     * Si el usuario tiene más de 20 objetivos, los restantes no se
     * muestran en el dashboard.</p>
     */
    @GetMapping("/savings-goals")
    public ResponseEntity<DashboardGoalsResponseDTO> getSavingsGoals(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                getDashboardDataUseCase.getSavingsGoals(
                        getUserId(authentication)
                )
        );
    }
}