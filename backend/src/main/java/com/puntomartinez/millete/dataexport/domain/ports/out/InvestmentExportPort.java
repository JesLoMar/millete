package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;

import java.util.List;
import java.util.UUID;

public interface InvestmentExportPort {

    List<InvestmentSnapshot> findAllByUserId(UUID userId);
}