package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.UserAssetEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.UserAssetEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaUserAssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAssetPostgresAdapterTest {

    @Mock
    private JpaUserAssetRepository repository;

    @Mock
    private UserAssetEntityMapper mapper;

    @Test
    void flushesTheAssetBeforeReturningToTheJdbcPriceWriter() {
        UserAsset asset = mock(UserAsset.class);
        UserAsset persistedAsset = mock(UserAsset.class);
        UserAssetEntity entity = new UserAssetEntity();
        UserAssetEntity persistedEntity = new UserAssetEntity();
        when(mapper.toEntity(asset)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(persistedEntity);
        when(mapper.toDomain(persistedEntity)).thenReturn(persistedAsset);

        UserAssetPostgresAdapter adapter = new UserAssetPostgresAdapter(repository, mapper);

        assertThat(adapter.save(asset)).isSameAs(persistedAsset);
    }
}
