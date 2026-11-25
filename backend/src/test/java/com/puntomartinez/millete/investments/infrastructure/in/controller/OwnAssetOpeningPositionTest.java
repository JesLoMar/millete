package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.application.services.UserAssetService;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.ports.in.CreateHoldingUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.AssetSectorCatalogPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.AssetResponseMapper;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OwnAssetOpeningPositionTest {
    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-11-25T12:00:00Z");

    @Mock private UserAssetRepository assets;
    @Mock private UserAssetPriceRepository prices;
    @Mock private AssetSectorCatalogPort sectors;
    @Mock private UserCurrencyPort currencies;
    @Mock private TimeProvider time;
    @Mock private CreateHoldingUseCase holdings;
    @Mock private InvestmentPortfolioLockPort portfolioLock;
    @InjectMocks private UserAssetService service;

    @Test
    void registraPosicionSinConfundirCosteTotalConPrecioUnitario() throws Exception {
        prepararAlta();
        enviar(", \"openingPosition\": {\"quantity\": 2, \"acquisitionCost\": 150000}")
                .andExpect(status().isCreated());
        var asset = ArgumentCaptor.forClass(UserAsset.class);
        var holding = ArgumentCaptor.forClass(CreateHoldingUseCase.CreateHoldingCommand.class);
        var orden = inOrder(portfolioLock, assets, prices, holdings);
        orden.verify(portfolioLock).lock(userId);
        orden.verify(assets).save(asset.capture());
        orden.verify(prices).save(argThat(price ->
                price.getUserAssetId().equals(asset.getValue().getId())
                        && price.getUnitPrice().amount().compareTo(new BigDecimal("100000")) == 0));
        orden.verify(holdings).create(eq(userId), holding.capture());
        assertThat(holding.getValue().assetReference()).isEqualTo(AssetReference.user(asset.getValue().getId()));
        assertThat(holding.getValue().snapshotAt()).isEqualTo(now);
        assertThat(holding.getValue().quantity()).isEqualByComparingTo("2");
        assertThat(holding.getValue().acquisitionCost()).isEqualByComparingTo("150000");
    }

    @Test
    void conservaAltaAnteriorSinPosicionParaClientesExistentes() throws Exception {
        prepararAlta();
        enviar("").andExpect(status().isCreated());
        verifyNoInteractions(holdings, portfolioLock);
    }

    @Test
    void rechazaPosicionIncompletaAntesDeGuardar() throws Exception {
        enviar(", \"openingPosition\": {\"quantity\": 2}")
                .andExpect(status().isBadRequest());
        verifyNoInteractions(assets, prices, holdings, portfolioLock);
    }

    private void prepararAlta() {
        when(time.now()).thenReturn(now);
        when(currencies.currencyAt(userId, now)).thenReturn(Optional.of(CurrencyCode.of("EUR")));
        when(assets.save(any(UserAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private org.springframework.test.web.servlet.ResultActions enviar(String extra) throws Exception {
        var controller = new AssetApiController(
                null, null, null, service, null, null, null, null, new AssetResponseMapper());
        var authentication = new UsernamePasswordAuthenticationToken(
                new JwtUser(userId, "test", "test@example.com"), null, List.of());
        return MockMvcBuilders.standaloneSetup(controller).build().perform(post("/api/v1/investments/user-assets")
                .principal(authentication).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Piso","type":"REAL_ESTATE","sector":"Inmobiliario",
                        "origin":"PRE_EXISTING","initialPrice":100000%s}
                        """.formatted(extra)));
    }
}
