package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.InvestmentEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.SpringDataInvestmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentPostgresAdapter")
class InvestmentPostgresAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private SpringDataInvestmentRepository repository;

    @Mock
    private InvestmentEntityMapper mapper;

    @InjectMocks
    private InvestmentPostgresAdapter adapter;

    private Investment domainInvestment() {
        return Investment.create(
                USER_ID, "Apple Inc.", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                InvestmentType.STOCK, LocalDateTime.now().minusDays(30)
        );
    }

    private InvestmentEntity investmentEntity() {
        InvestmentEntity entity = new InvestmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setAssetName("Apple Inc.");
        entity.setTicker("AAPL");
        entity.setQuantity(new BigDecimal("10"));
        entity.setPurchasePrice(new BigDecimal("150.00"));
        entity.setCurrentPrice(new BigDecimal("180.00"));
        entity.setType("STOCK");
        entity.setPurchaseDate(LocalDateTime.now().minusDays(30));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);
        return entity;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should map domain to entity, save, and map back")
        void shouldSaveInvestment() {
            Investment domain = domainInvestment();
            InvestmentEntity entity = investmentEntity();
            InvestmentEntity savedEntity = investmentEntity();
            Investment savedDomain = domainInvestment();

            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

            Investment result = adapter.save(domain);

            assertThat(result).isSameAs(savedDomain);
            verify(mapper).toEntity(domain);
            verify(repository).save(entity);
            verify(mapper).toDomain(savedEntity);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return mapped domain when found")
        void shouldReturnMappedDomainWhenFound() {
            UUID investmentId = UUID.randomUUID();
            InvestmentEntity entity = investmentEntity();
            Investment domain = domainInvestment();

            when(repository.findById(investmentId)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Investment> result = adapter.findById(investmentId);

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            UUID investmentId = UUID.randomUUID();

            when(repository.findById(investmentId)).thenReturn(Optional.empty());

            Optional<Investment> result = adapter.findById(investmentId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("Should use specification with id and userId")
        void shouldUseSpecificationWithIdAndUserId() {
            UUID investmentId = UUID.randomUUID();
            InvestmentEntity entity = investmentEntity();
            Investment domain = domainInvestment();

            when(repository.findOne(
                    ArgumentMatchers.<Specification<InvestmentEntity>>any()
            )).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Investment> result = adapter.findByIdAndUserId(investmentId, USER_ID);

            assertThat(result).contains(domain);
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("Should find all investments sorted by purchase date descending")
        void shouldFindAllInvestmentsSorted() {
            InvestmentEntity entity = investmentEntity();
            Investment domain = domainInvestment();

            when(repository.findAll(
                    ArgumentMatchers.<Specification<InvestmentEntity>>any(),
                    any(Sort.class)
            )).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Investment> result = adapter.findAllByUserId(USER_ID);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should find paginated investments with filters")
        void shouldFindPaginatedInvestmentsWithFilters() {
            InvestmentEntity entity = investmentEntity();
            Investment domain = domainInvestment();

            Page<InvestmentEntity> page = new PageImpl<>(List.of(entity));

            when(repository.findAll(
                    ArgumentMatchers.<Specification<InvestmentEntity>>any(),
                    any(Pageable.class)
            )).thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Investment> result = adapter.findAllByUserId(
                    USER_ID, 0, 50, "apple", InvestmentType.STOCK
            );

            assertThat(result).containsExactly(domain);
        }
    }

    @Nested
    @DisplayName("countByUserIdAndFilters")
    class CountByUserIdAndFilters {

        @Test
        @DisplayName("Should delegate count with specification")
        void shouldDelegateCount() {
            when(repository.count(
                    ArgumentMatchers.<Specification<InvestmentEntity>>any()
            )).thenReturn(5L);

            long result = adapter.countByUserIdAndFilters(USER_ID, "apple", InvestmentType.STOCK);

            assertThat(result).isEqualTo(5L);
        }
    }
}