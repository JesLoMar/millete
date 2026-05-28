package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentService - Servicio de inversiones")
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentService investmentService;

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Registrar inversión")
    void shouldRegisterInvestment() {
        RegisterInvestmentUseCase.RegisterInvestmentCommand command = new RegisterInvestmentUseCase.RegisterInvestmentCommand(
                userId, "Nvidia", "NVDA", new BigDecimal("10"), new BigDecimal("100.00"),
                Investment.InvestmentType.STOCK, LocalDateTime.now());

        when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> inv.getArgument(0));

        Investment result = investmentService.register(command);

        assertThat(result.getAssetName()).isEqualTo("Nvidia");
        assertThat(result.getTicker()).isEqualTo("NVDA");
        assertThat(result.getQuantity()).isEqualByComparingTo("10");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCurrentPrice()).isEqualByComparingTo("100.00");
        verify(investmentRepository).save(any(Investment.class));
    }

    @Test
    @DisplayName("Listar inversiones por usuario")
    void shouldFindAllByUserId() {
        Investment inv1 = mock(Investment.class);
        Investment inv2 = mock(Investment.class);
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of(inv1, inv2));

        List<Investment> result = investmentService.findAllByUserId(userId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Actualizar precio de inversión")
    void shouldUpdatePrice() {
        UUID id = UUID.randomUUID();
        Investment inv = mock(Investment.class);
        when(inv.getUserId()).thenReturn(userId);
        when(investmentRepository.findById(id)).thenReturn(Optional.of(inv));
        when(investmentRepository.save(any(Investment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal newPrice = new BigDecimal("200.00");
        Investment result = investmentService.updatePrice(id, userId, newPrice);

        verify(inv).updateCurrentPrice(newPrice);
        verify(investmentRepository).save(inv);
    }

    @Test
    @DisplayName("Actualizar precio de otro usuario lanza error")
    void shouldThrowWhenUpdatingOtherUserInvestment() {
        UUID id = UUID.randomUUID();
        Investment inv = mock(Investment.class);
        when(inv.getUserId()).thenReturn(UUID.randomUUID());
        when(investmentRepository.findById(id)).thenReturn(Optional.of(inv));

        assertThatRuntimeException()
                .isThrownBy(() -> investmentService.updatePrice(id, userId, new BigDecimal("200.00")))
                .withMessage("No tienes permiso para actualizar esta inversión.");
    }

    @Test
    @DisplayName("Actualizar precio de inversión inexistente lanza error")
    void shouldThrowWhenInvestmentNotFound() {
        UUID id = UUID.randomUUID();
        when(investmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatRuntimeException()
                .isThrownBy(() -> investmentService.updatePrice(id, userId, new BigDecimal("200.00")))
                .withMessage("Inversión no encontrada.");
    }
}