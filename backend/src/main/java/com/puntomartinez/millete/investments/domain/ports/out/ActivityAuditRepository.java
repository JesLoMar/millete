package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.ActivityAudit;

import java.util.List;

import java.util.UUID;

public interface ActivityAuditRepository {

    ActivityAudit save(ActivityAudit audit);

    List<ActivityAudit> findAllByUserId(UUID userId);
}