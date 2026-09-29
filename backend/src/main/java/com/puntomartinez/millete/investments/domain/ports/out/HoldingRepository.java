package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Holding;

import java.util.List;
import java.util.UUID;

public interface HoldingRepository {

    List<Holding> findAllByUserId(UUID userId);
}