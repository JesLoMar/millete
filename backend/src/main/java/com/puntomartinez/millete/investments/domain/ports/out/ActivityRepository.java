package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityAudit;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {
    Activity save(Activity activity);
    default Activity saveImported(Activity activity, boolean active) { return save(activity); }
    Optional<ActivityRequest> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
    void saveActivityRequest(UUID userId, String idempotencyKey, String requestHash,
                             UUID activityId, Instant createdAt);
    Optional<Activity> findActivityByIdAndUserId(UUID id, UUID userId);
    List<Activity> findActivitiesByUserIdAndAssetId(UUID userId, UUID assetId);
    List<Activity> findActivitiesByUserId(UUID userId);
    ActivityPage findActiveActivitiesPageByUserId(UUID userId, int offset, int limit);
    ActivityPage findActiveActivitiesPageByUserIdAndAssetId(UUID userId, UUID assetId,
                                                             int offset, int limit);
    List<HistoricalActivity> findActivityHistoryByUserId(UUID userId);
    void saveAudit(ActivityAudit audit);
    List<ActivityAudit> findAuditByActivityId(UUID activityId, UUID userId);
    void lockUserPortfolio(UUID userId);
    void lockAsset(UUID userId, UUID assetId);
    long nextOrderingKey(UUID userId, Instant occurredAt);

    record ActivityPage(List<Activity> activities, int offset, int limit, long totalElements) { }
    record HistoricalActivity(Activity activity, boolean active) { }
    record ActivityRequest(Activity activity, String requestHash) { }
}
