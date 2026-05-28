package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.in.ListInvestmentsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase.RegisterInvestmentCommand;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentPriceUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.RegisterInvestmentRequestDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.UpdateInvestmentPriceRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentController {

    private final RegisterInvestmentUseCase registerUseCase;
    private final ListInvestmentsUseCase listUseCase;
    private final UpdateInvestmentPriceUseCase updatePriceUseCase;
    private final InvestmentRepository investmentRepository;

    public InvestmentController(RegisterInvestmentUseCase registerUseCase,
                                ListInvestmentsUseCase listUseCase,
                                UpdateInvestmentPriceUseCase updatePriceUseCase,
                                InvestmentRepository investmentRepository) {
        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
        this.updatePriceUseCase = updatePriceUseCase;
        this.investmentRepository = investmentRepository;
    }

    // =======================================================
    // POST: REGISTRAR UNA NUEVA INVERSIÓN
    // =======================================================
    @PostMapping
    public ResponseEntity<InvestmentResponseDTO> registerInvestment(
            @Valid @RequestBody RegisterInvestmentRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        RegisterInvestmentCommand command = new RegisterInvestmentCommand(
                userId,
                request.assetName(),
                request.ticker(),
                request.quantity(),
                request.purchasePrice(),
                request.type(),
                request.purchaseDate()
        );

        Investment savedInvestment = registerUseCase.register(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(savedInvestment));
    }

    // =======================================================
    // GET: LISTAR INVERSIONES DEL USUARIO
    // =======================================================
    @GetMapping
    public ResponseEntity<List<InvestmentResponseDTO>> getAllInvestments(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<Investment> investments = listUseCase.findAllByUserId(userId)
                .stream()
                .filter(Investment::isActive)
                .toList();
        List<InvestmentResponseDTO> responseList = investments.stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    // =======================================================
    // PATCH: ACTUALIZAR EL PRECIO DE MERCADO DE UN ACTIVO
    // =======================================================
    @PatchMapping("/{id}/price")
    public ResponseEntity<InvestmentResponseDTO> updateInvestmentPrice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvestmentPriceRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Investment updatedInvestment = updatePriceUseCase.updatePrice(id, userId, request.newPrice());

        return ResponseEntity.ok(mapToDTO(updatedInvestment));
    }

    // =======================================================
    // DELETE: SOFT DELETE DE UNA INVERSIÓN
    // =======================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestment(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inversión no encontrada"));

        if (!investment.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta inversión");
        }

        investment.setActive(false);
        investment.setModifiedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        return ResponseEntity.noContent().build();
    }

    // =======================================================
    // MÉTODOS AUXILIARES
    // =======================================================
    private InvestmentResponseDTO mapToDTO(Investment inv) {
        return new InvestmentResponseDTO(
                inv.getId(),
                inv.getAssetName(),
                inv.getTicker(),
                inv.getQuantity(),
                inv.getPurchasePrice(),
                inv.getCurrentPrice(),
                inv.getInvestedCapital(),
                inv.getCurrentValue(),
                inv.getProfitOrLoss(),
                inv.getReturnOnInvestmentPercentage(),
                inv.getType(),
                inv.getPurchaseDate(),
                inv.isActive()
        );
    }
}