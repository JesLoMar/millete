package com.puntomartinez.millete.investments.domain.ports.out;

import java.util.List;
import java.util.UUID;

public interface InvestmentHealthTargetPort {

    List<UUID> findUserIdsToCheck();
}