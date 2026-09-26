package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityAudit;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {
    Activity save(Activity activity);
    Optional<Activity> findActivityByIdAndUserId(UUID id, UUID userId);
    List<Activity> findActivitiesByUserIdAndAssetId(UUID userId, UUID assetId);
    List<Activity> findActivitiesByUserId(UUID userId);
    void saveAudit(ActivityAudit audit);
    List<ActivityAudit> findAuditByActivityId(UUID activityId, UUID userId);
    void lockUserPortfolio(UUID userId);
    void lockAsset(UUID userId, UUID assetId);
    long nextOrderingKey(UUID userId, Instant occurredAt);
}
