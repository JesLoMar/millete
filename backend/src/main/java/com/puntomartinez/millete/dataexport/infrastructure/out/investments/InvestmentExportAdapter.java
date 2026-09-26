package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentExportPort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

/** The legacy flat investments array is retained only for import compatibility. */
@Component
public class InvestmentExportAdapter implements InvestmentExportPort {
    @Override public List<InvestmentSnapshot> findAllByUserId(UUID userId) { return List.of(); }
}
