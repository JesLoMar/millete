package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Lot;
import com.puntomartinez.millete.investments.domain.model.LotConsumption;
import com.puntomartinez.millete.investments.domain.model.Position;
import com.puntomartinez.millete.investments.domain.ports.out.PortfolioProjectionRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.LotConsumptionEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.LotEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.PositionEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.LotConsumptionEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.LotEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.PositionEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaLotConsumptionRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaLotRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaPositionRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public final class PortfolioProjectionPostgresAdapter
        implements PortfolioProjectionRepository {

    private final JpaLotRepository lotRepository;
    private final JpaLotConsumptionRepository lotConsumptionRepository;
    private final JpaPositionRepository positionRepository;

    private final LotEntityMapper lotMapper;
    private final LotConsumptionEntityMapper lotConsumptionMapper;
    private final PositionEntityMapper positionMapper;

    public PortfolioProjectionPostgresAdapter(
            JpaLotRepository lotRepository,
            JpaLotConsumptionRepository lotConsumptionRepository,
            JpaPositionRepository positionRepository,
            LotEntityMapper lotMapper,
            LotConsumptionEntityMapper lotConsumptionMapper,
            PositionEntityMapper positionMapper
    ) {
        this.lotRepository = lotRepository;
        this.lotConsumptionRepository = lotConsumptionRepository;
        this.positionRepository = positionRepository;
        this.lotMapper = lotMapper;
        this.lotConsumptionMapper = lotConsumptionMapper;
        this.positionMapper = positionMapper;
    }

    @Override
    public void replace(
            UUID userId,
            List<Lot> lots,
            List<LotConsumption> consumptions,
            List<Position> positions
    ) {
        validateUserId(userId);

        validateOwnership(
                userId,
                lots,
                consumptions,
                positions
        );

        try {
            lotConsumptionRepository.deleteAllByUserId(
                    userId
            );

            lotRepository.deleteAllByUserId(
                    userId
            );

            positionRepository.deleteAllByUserId(
                    userId
            );

            saveLots(lots);
            saveConsumptions(
                    userId,
                    consumptions
            );
            savePositions(positions);

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo reemplazar la proyección "
                            + "del portfolio de Investments.",
                    exception
            );
        }
    }

    private void saveLots(
            List<Lot> lots
    ) {
        if (lots == null || lots.isEmpty()) {
            return;
        }

        List<LotEntity> entities =
                lots.stream()
                        .map(lotMapper::toEntity)
                        .toList();

        lotRepository.saveAll(entities);
    }

    private void saveConsumptions(
            UUID userId,
            List<LotConsumption> consumptions
    ) {
        if (consumptions == null
                || consumptions.isEmpty()) {
            return;
        }

        List<LotConsumptionEntity> entities =
                consumptions.stream()
                        .map(consumption ->
                                lotConsumptionMapper.toEntity(
                                        consumption,
                                        userId
                                )
                        )
                        .toList();

        lotConsumptionRepository.saveAll(
                entities
        );
    }

    private void savePositions(
            List<Position> positions
    ) {
        if (positions == null
                || positions.isEmpty()) {
            return;
        }

        List<PositionEntity> entities =
                positions.stream()
                        .map(positionMapper::toEntity)
                        .toList();

        positionRepository.saveAll(
                entities
        );
    }

    private void validateOwnership(
            UUID userId,
            List<Lot> lots,
            List<LotConsumption> consumptions,
            List<Position> positions
    ) {
        if (lots != null) {
            for (Lot lot : lots) {
                if (lot == null) {
                    throw new IllegalArgumentException(
                            "La lista de Lots no puede contener null."
                    );
                }

                if (!userId.equals(
                        lot.getUserId()
                )) {
                    throw new IllegalArgumentException(
                            "Todos los Lots deben pertenecer al usuario."
                    );
                }
            }
        }

        if (consumptions != null) {
            for (LotConsumption consumption : consumptions) {
                if (consumption == null) {
                    throw new IllegalArgumentException(
                            "La lista de LotConsumptions "
                                    + "no puede contener null."
                    );
                }
            }
        }

        if (positions != null) {
            for (Position position : positions) {
                if (position == null) {
                    throw new IllegalArgumentException(
                            "La lista de Positions no puede contener null."
                    );
                }

                if (!userId.equals(
                        position.userId()
                )) {
                    throw new IllegalArgumentException(
                            "Todas las Positions deben pertenecer al usuario."
                    );
                }
            }
        }
    }

    private void validateUserId(
            UUID userId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId es obligatorio."
            );
        }
    }
}