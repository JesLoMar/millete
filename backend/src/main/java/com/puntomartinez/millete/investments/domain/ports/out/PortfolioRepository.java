package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Lot;
import com.puntomartinez.millete.investments.domain.model.LotConsumption;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PortfolioRepository {
    List<Lot> findLots(UUID userId, UUID assetId);
    void replaceDerivedLots(UUID userId, UUID assetId, List<Lot> lots,
                            List<LotConsumption> consumptions);
    List<LotConsumption> findConsumptionsBySellActivity(UUID sellActivityId);
    Map<String, BigDecimal> calculateCashBalances(UUID userId);
    List<Lot> findAllOpenLots(UUID userId);
}
