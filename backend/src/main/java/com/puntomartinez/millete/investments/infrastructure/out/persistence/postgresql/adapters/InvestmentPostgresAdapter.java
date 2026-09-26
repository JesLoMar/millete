package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/** PostgreSQL adapter. Bulk replay writes use JDBC so delete/rebuild is atomic and bounded. */
@Repository
public class InvestmentPostgresAdapter implements AssetRepository, ActivityRepository,
        HoldingRepository, PortfolioRepository, MarketDataRepository {
    private final JdbcTemplate jdbc;

    public InvestmentPostgresAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Asset save(Asset a) {
        jdbc.update("""
                INSERT INTO assets(id,user_id,name,symbol,type,sector_id,currency,created_at,modified_at,active)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET name=excluded.name,symbol=excluded.symbol,type=excluded.type,
                  sector_id=excluded.sector_id,currency=excluded.currency,modified_at=excluded.modified_at,active=excluded.active
                """, a.getId(), a.getUserId(), a.getName(), a.getSymbol(), a.getType().name(), a.getSectorId(),
                a.getCurrency(), ts(a.getCreatedAt()), ts(a.getModifiedAt()), a.isActive());
        return a;
    }

    @Override public Optional<Asset> findAssetByIdAndUserId(UUID id, UUID userId) {
        return one("SELECT * FROM assets WHERE id=? AND user_id=?", assetMapper(), id, userId);
    }

    @Override public List<Asset> findAssetsByUserId(UUID userId, boolean includeInactive) {
        return jdbc.query("SELECT * FROM assets WHERE user_id=? AND (? OR active=true) ORDER BY name", assetMapper(), userId, includeInactive);
    }

    @Override public boolean sectorExists(UUID sectorId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM asset_sectors WHERE id=? AND active=true)", Boolean.class, sectorId));
    }

    @Override public List<AssetSector> findActiveSectors() {
        return jdbc.query("SELECT id,code,display_name,active FROM asset_sectors WHERE active=true ORDER BY display_name",
                (rs, n) -> new AssetSector(uuid(rs, "id"), rs.getString("code"), rs.getString("display_name"), rs.getBoolean("active")));
    }

    @Override public Activity save(Activity a) {
        jdbc.update("""
                INSERT INTO activities(id,user_id,asset_id,type,occurred_at,ordering_key,quantity,unit_price,amount,currency,
                  secondary_amount,secondary_currency,ratio,local_currency,fx_rate_to_local,fx_rate_source,fx_rate_timestamp,
                  amount_in_local,comment,created_at,modified_at,active)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,true)
                ON CONFLICT(id) DO UPDATE SET asset_id=excluded.asset_id,type=excluded.type,occurred_at=excluded.occurred_at,
                  ordering_key=excluded.ordering_key,quantity=excluded.quantity,unit_price=excluded.unit_price,amount=excluded.amount,
                  currency=excluded.currency,secondary_amount=excluded.secondary_amount,secondary_currency=excluded.secondary_currency,
                  ratio=excluded.ratio,local_currency=excluded.local_currency,fx_rate_to_local=excluded.fx_rate_to_local,
                  fx_rate_source=excluded.fx_rate_source,fx_rate_timestamp=excluded.fx_rate_timestamp,
                  amount_in_local=excluded.amount_in_local,comment=excluded.comment,modified_at=excluded.modified_at
                """, a.getId(), a.getUserId(), a.getAssetId(), a.getType().name(), ts(a.getOccurredAt()), a.getOrderingKey(),
                a.getQuantity(), a.getUnitPrice(), a.getAmount(), a.getCurrency(), a.getSecondaryAmount(), a.getSecondaryCurrency(),
                a.getRatio(), a.getLocalCurrency(), a.getFxRateToLocal(), a.getFxRateSource(), ts(a.getFxRateTimestamp()),
                a.getAmountInLocal(), a.getComment(),
                ts(a.getCreatedAt()), ts(a.getModifiedAt()));
        return a;
    }

    @Override public Optional<Activity> findActivityByIdAndUserId(UUID id, UUID userId) {
        return one("SELECT a.*,t.id AS linked_tx FROM activities a LEFT JOIN transactions t ON t.investment_activity_id=a.id WHERE a.id=? AND a.user_id=? AND a.active=true", activityMapper(), id, userId);
    }

    @Override public List<Activity> findActivitiesByUserIdAndAssetId(UUID userId, UUID assetId) {
        return jdbc.query("SELECT a.*,t.id AS linked_tx FROM activities a LEFT JOIN transactions t ON t.investment_activity_id=a.id WHERE a.user_id=? AND a.asset_id=? AND a.active=true ORDER BY occurred_at,ordering_key,id", activityMapper(), userId, assetId);
    }

    @Override public List<Activity> findActivitiesByUserId(UUID userId) {
        return jdbc.query("SELECT a.*,t.id AS linked_tx FROM activities a LEFT JOIN transactions t ON t.investment_activity_id=a.id WHERE a.user_id=? AND a.active=true ORDER BY occurred_at,ordering_key,id", activityMapper(), userId);
    }

    @Override public void saveAudit(ActivityAudit a) {
        jdbc.update("INSERT INTO activity_audit(id,activity_id,user_id,before_data,after_data,reason,changed_at) VALUES(?,?,?,?::jsonb,?::jsonb,?,?)",
                a.id(), a.activityId(), a.userId(), a.beforeJson(), a.afterJson(), a.reason(), ts(a.changedAt()));
    }

    @Override public List<ActivityAudit> findAuditByActivityId(UUID activityId, UUID userId) {
        return jdbc.query("SELECT * FROM activity_audit WHERE activity_id=? AND user_id=? ORDER BY changed_at,id",
                (rs, n) -> new ActivityAudit(uuid(rs,"id"), uuid(rs,"activity_id"), uuid(rs,"user_id"),
                        rs.getString("before_data"), rs.getString("after_data"), rs.getString("reason"), instant(rs,"changed_at")), activityId, userId);
    }

    @Override public void lockUserPortfolio(UUID userId) {
        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", Object.class, "investment-user:" + userId);
    }

    @Override public void lockAsset(UUID userId, UUID assetId) {
        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", Object.class, "investment-asset:" + userId + ":" + assetId);
    }

    @Override public long nextOrderingKey(UUID userId, Instant occurredAt) {
        Long value = jdbc.queryForObject("SELECT COALESCE(MAX(ordering_key),0)+1 FROM activities WHERE user_id=? AND occurred_at=?", Long.class, userId, ts(occurredAt));
        return value == null ? 1 : value;
    }

    @Override public Holding save(Holding h) {
        jdbc.update("INSERT INTO holdings(id,user_id,asset_id,snapshot_at,quantity,acquisition_cost,currency,history_incomplete,superseded,created_at) VALUES(?,?,?,?,?,?,?,?,false,?) ON CONFLICT(id) DO UPDATE SET snapshot_at=excluded.snapshot_at,quantity=excluded.quantity,acquisition_cost=excluded.acquisition_cost,currency=excluded.currency,history_incomplete=excluded.history_incomplete",
                h.id(), h.userId(), h.assetId(), ts(h.snapshotAt()), h.quantity(), h.acquisitionCost(), h.currency(), h.historyIncomplete(), ts(h.createdAt()));
        return h;
    }

    @Override public Optional<Holding> findHoldingByIdAndUserId(UUID id, UUID userId) {
        return one("SELECT * FROM holdings WHERE id=? AND user_id=? AND superseded=false", holdingMapper(), id, userId);
    }

    @Override public List<Holding> findHoldingsByUserIdAndAssetId(UUID userId, UUID assetId) {
        return jdbc.query("SELECT * FROM holdings WHERE user_id=? AND asset_id=? AND superseded=false ORDER BY snapshot_at,id", holdingMapper(), userId, assetId);
    }

    @Override public List<Holding> findHoldingsByUserId(UUID userId) {
        return jdbc.query("SELECT * FROM holdings WHERE user_id=? AND superseded=false ORDER BY snapshot_at,id", holdingMapper(), userId);
    }

    @Override public List<Holding> findHoldingsIncludingSupersededByUserId(UUID userId) {
        return jdbc.query("SELECT * FROM holdings WHERE user_id=? ORDER BY snapshot_at,id", holdingMapper(), userId);
    }

    @Override public void markSuperseded(UUID holdingId, UUID userId) {
        jdbc.update("UPDATE holdings SET superseded=true WHERE id=? AND user_id=?", holdingId, userId);
    }

    @Override public List<Lot> findLots(UUID userId, UUID assetId) {
        return jdbc.query("SELECT * FROM lots WHERE user_id=? AND asset_id=? ORDER BY acquired_at,id", lotMapper(), userId, assetId);
    }

    @Override public void replaceDerivedLots(UUID userId, UUID assetId, List<Lot> lots, List<LotConsumption> consumptions) {
        jdbc.update("DELETE FROM lot_consumptions WHERE user_id=? AND sell_activity_id IN (SELECT id FROM activities WHERE user_id=? AND asset_id=? AND type='SELL')", userId, userId, assetId);
        jdbc.update("DELETE FROM lots WHERE user_id=? AND asset_id=?", userId, assetId);
        if (!lots.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO lots(id,user_id,asset_id,source_activity_id,source_holding_id,acquired_at,original_quantity,remaining_quantity,total_cost,currency,synthetic) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    lots, 250, (ps, l) -> {
                        ps.setObject(1,l.getId()); ps.setObject(2,l.getUserId()); ps.setObject(3,l.getAssetId());
                        ps.setObject(4,l.getSourceActivityId());
                        ps.setObject(5,l.getSourceHoldingId());
                        ps.setTimestamp(6, ts(l.getAcquiredAt())); ps.setBigDecimal(7,l.getOriginalQuantity());
                        ps.setBigDecimal(8,l.getRemainingQuantity()); ps.setBigDecimal(9,l.getTotalCost());
                        ps.setString(10,l.getCurrency()); ps.setBoolean(11,l.isSynthetic());
                    });
        }
        if (!consumptions.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO lot_consumptions(id,user_id,sell_activity_id,lot_id,quantity,cost_basis,currency) VALUES(?,?,?,?,?,?,?)",
                    consumptions, 250, (ps, c) -> {
                        ps.setObject(1,c.id());
                        ps.setObject(2,userId); ps.setObject(3,c.sellActivityId()); ps.setObject(4,c.lotId());
                        ps.setBigDecimal(5,c.quantity()); ps.setBigDecimal(6,c.costBasis()); ps.setString(7,c.currency());
                    });
        }
    }

    @Override public List<LotConsumption> findConsumptionsBySellActivity(UUID sellActivityId) {
        return jdbc.query("SELECT * FROM lot_consumptions WHERE sell_activity_id=? ORDER BY lot_id",
                (rs,n) -> new LotConsumption(uuid(rs,"id"), uuid(rs,"sell_activity_id"), uuid(rs,"lot_id"), rs.getBigDecimal("quantity"), rs.getBigDecimal("cost_basis"), rs.getString("currency")), sellActivityId);
    }

    @Override public Map<String, BigDecimal> calculateCashBalances(UUID userId) {
        return Map.of(); // The application replays the complete ledger to enforce chronology.
    }

    @Override public List<Lot> findAllOpenLots(UUID userId) {
        return jdbc.query("SELECT * FROM lots WHERE user_id=? AND remaining_quantity>0 ORDER BY acquired_at,id", lotMapper(), userId);
    }

    @Override public AssetPrice savePrice(AssetPrice p) {
        jdbc.update("INSERT INTO asset_prices(id,user_id,asset_id,price_timestamp,open,high,low,close,adjusted_close,volume,currency,source,fetched_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(asset_id,price_timestamp,source) DO UPDATE SET open=excluded.open,high=excluded.high,low=excluded.low,close=excluded.close,adjusted_close=excluded.adjusted_close,volume=excluded.volume,currency=excluded.currency,fetched_at=excluded.fetched_at",
                p.id(), p.userId(), p.assetId(), ts(p.timestamp()), p.open(), p.high(), p.low(), p.close(), p.adjustedClose(), p.volume(), p.currency(), p.source(), ts(p.fetchedAt()));
        return p;
    }

    @Override public FxRate saveFxRate(FxRate f) {
        jdbc.update("INSERT INTO fx_rates(id,base_currency,quote_currency,rate_timestamp,rate,source,fetched_at) VALUES(?,?,?,?,?,?,?) ON CONFLICT(base_currency,quote_currency,rate_timestamp,source) DO UPDATE SET rate=excluded.rate,fetched_at=excluded.fetched_at",
                f.id(), f.baseCurrency(), f.quoteCurrency(), ts(f.timestamp()), f.rate(), f.source(), ts(f.fetchedAt()));
        return f;
    }

    @Override public Optional<AssetPrice> latestPriceAt(UUID userId, UUID assetId, Instant at) {
        return one("SELECT * FROM asset_prices WHERE user_id=? AND asset_id=? AND price_timestamp<=? AND close IS NOT NULL ORDER BY price_timestamp DESC,fetched_at DESC LIMIT 1", priceMapper(), userId, assetId, ts(at));
    }

    @Override public Optional<FxRate> latestFxAt(String base, String quote, Instant at) {
        return one("SELECT * FROM fx_rates WHERE base_currency=? AND quote_currency=? AND rate_timestamp<=? ORDER BY rate_timestamp DESC,fetched_at DESC LIMIT 1", fxMapper(), base.toUpperCase(Locale.ROOT), quote.toUpperCase(Locale.ROOT), ts(at));
    }

    @Override public List<AssetPrice> pricesForAsset(UUID userId, UUID assetId, Instant from, Instant to) {
        return jdbc.query("SELECT * FROM asset_prices WHERE user_id=? AND asset_id=? AND price_timestamp>=? AND price_timestamp<=? ORDER BY price_timestamp",
                priceMapper(), userId, assetId, ts(from), ts(to));
    }

    @Override public List<FxRate> fxRates(Instant from, Instant to) {
        return jdbc.query("SELECT * FROM fx_rates WHERE rate_timestamp>=? AND rate_timestamp<=? ORDER BY rate_timestamp",
                fxMapper(), ts(from), ts(to));
    }

    private <T> Optional<T> one(String sql, RowMapper<T> mapper, Object... args) {
        List<T> values = jdbc.query(sql, mapper, args);
        return values.stream().findFirst();
    }

    private RowMapper<Asset> assetMapper() {
        return (rs,n) -> Asset.reconstitute(uuid(rs,"id"),uuid(rs,"user_id"),rs.getString("name"),rs.getString("symbol"),
                AssetType.valueOf(rs.getString("type")),uuid(rs,"sector_id"),rs.getString("currency"),instant(rs,"created_at"),instant(rs,"modified_at"),rs.getBoolean("active"));
    }
    private RowMapper<Activity> activityMapper() {
        return (rs,n) -> Activity.reconstitute(uuid(rs,"id"),uuid(rs,"user_id"),ActivityType.valueOf(rs.getString("type")),
                instant(rs,"occurred_at"),instant(rs,"created_at"),instant(rs,"modified_at"),rs.getLong("ordering_key"),
                uuid(rs,"asset_id"),rs.getBigDecimal("quantity"),rs.getBigDecimal("unit_price"),rs.getBigDecimal("amount"),rs.getString("currency"),
                rs.getBigDecimal("secondary_amount"),rs.getString("secondary_currency"),rs.getBigDecimal("ratio"),rs.getString("local_currency"),
                rs.getBigDecimal("fx_rate_to_local"),rs.getString("fx_rate_source"),instant(rs,"fx_rate_timestamp"),
                rs.getBigDecimal("amount_in_local"),rs.getString("comment"),uuid(rs,"linked_tx"));
    }
    private RowMapper<Holding> holdingMapper() {
        return (rs,n) -> new Holding(uuid(rs,"id"),uuid(rs,"user_id"),uuid(rs,"asset_id"),instant(rs,"snapshot_at"),
                rs.getBigDecimal("quantity"),rs.getBigDecimal("acquisition_cost"),rs.getString("currency"),rs.getBoolean("history_incomplete"),rs.getBoolean("superseded"),instant(rs,"created_at"));
    }
    private RowMapper<Lot> lotMapper() {
        return (rs,n) -> new Lot(uuid(rs,"id"),uuid(rs,"user_id"),uuid(rs,"asset_id"),uuid(rs,"source_activity_id"),uuid(rs,"source_holding_id"),
                instant(rs,"acquired_at"),rs.getBigDecimal("original_quantity"),rs.getBigDecimal("remaining_quantity"),rs.getBigDecimal("total_cost"),rs.getString("currency"),rs.getBoolean("synthetic"));
    }
    private RowMapper<AssetPrice> priceMapper() {
        return (rs,n) -> new AssetPrice(uuid(rs,"id"),uuid(rs,"user_id"),uuid(rs,"asset_id"),instant(rs,"price_timestamp"),
                rs.getBigDecimal("open"),rs.getBigDecimal("high"),rs.getBigDecimal("low"),rs.getBigDecimal("close"),rs.getBigDecimal("adjusted_close"),rs.getBigDecimal("volume"),rs.getString("currency"),rs.getString("source"),instant(rs,"fetched_at"));
    }
    private RowMapper<FxRate> fxMapper() {
        return (rs,n) -> new FxRate(uuid(rs,"id"),rs.getString("base_currency"),rs.getString("quote_currency"),instant(rs,"rate_timestamp"),rs.getBigDecimal("rate"),rs.getString("source"),instant(rs,"fetched_at"));
    }
    private static Timestamp ts(Instant value) { return value == null ? null : Timestamp.from(value); }
    private static Instant instant(ResultSet rs, String col) throws SQLException { Timestamp v = rs.getTimestamp(col); return v == null ? null : v.toInstant(); }
    private static UUID uuid(ResultSet rs, String col) throws SQLException { Object value = rs.getObject(col); return value == null ? null : (UUID)value; }
}
