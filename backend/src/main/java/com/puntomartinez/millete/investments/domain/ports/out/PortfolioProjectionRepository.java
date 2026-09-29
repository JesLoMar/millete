package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Lot;
import com.puntomartinez.millete.investments.domain.model.LotConsumption;
import com.puntomartinez.millete.investments.domain.model.Position;

import java.util.List;
import java.util.UUID;

public interface PortfolioProjectionRepository {

    void replace(
            UUID userId,
            List<Lot> lots,
            List<LotConsumption> consumptions,
            List<Position> positions
    );
}