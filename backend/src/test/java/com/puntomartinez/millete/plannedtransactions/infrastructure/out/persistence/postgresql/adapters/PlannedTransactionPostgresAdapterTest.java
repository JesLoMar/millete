package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.mappers.PlannedTransactionEntityMapper;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.repository.JpaPlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlannedTransactionPostgresAdapter")
class PlannedTransactionPostgresAdapterTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock private JpaPlannedTransactionRepository repository;
    @Mock private PlannedTransactionEntityMapper mapper;
    @InjectMocks private PlannedTransactionPostgresAdapter adapter;

    @SuppressWarnings("unchecked")
    private static Specification<PlannedTransactionEntity> anySpecification() {
        return (Specification<PlannedTransactionEntity>) ArgumentMatchers.any();
    }

    private PlannedTransaction domainTemplate() {
        return PlannedTransaction.create(
                USER_ID, CATEGORY_ID, new BigDecimal("100.00"),
                TransactionType.EXPENSE, "Rent",
                FrequencyType.MONTHS, 1,
                LocalDate.now(), null
        );
    }

    private PlannedTransactionEntity entityTemplate() {
        PlannedTransactionEntity entity = new PlannedTransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setCategoryId(CATEGORY_ID);
        entity.setAmount(new BigDecimal("100.00"));
        entity.setType("EXPENSE");
        entity.setDescription("Rent");
        entity.setFrequencyType("MONTHS");
        entity.setFrequencyInterval(1);
        entity.setStartDate(LocalDate.now());
        entity.setCreatedAt(Instant.now());
        entity.setModifiedAt(Instant.now());
        entity.setActive(true);
        entity.setFailureCount(0);
        return entity;
    }

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("Should map domain to entity, save, and map back")
        void shouldSave() {
            PlannedTransaction domain = domainTemplate();
            PlannedTransactionEntity entity = entityTemplate();
            PlannedTransactionEntity savedEntity = entityTemplate();
            PlannedTransaction savedDomain = domainTemplate();

            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

            PlannedTransaction result = adapter.save(domain);

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
        @DisplayName("Should return mapped domain when entity exists")
        void shouldReturnWhenExists() {
            UUID id = UUID.randomUUID();
            PlannedTransactionEntity entity = entityTemplate();
            PlannedTransaction domain = domainTemplate();

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<PlannedTransaction> result = adapter.findById(id);

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmpty() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            Optional<PlannedTransaction> result = adapter.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByUserId (no pagination)")
    class FindAllByUserIdNoPagination {
        @Test
        @DisplayName("Should map all results ordered by start date desc")
        void shouldMapAll() {
            PlannedTransactionEntity entity = entityTemplate();
            PlannedTransaction domain = domainTemplate();

            when(repository.findByUserIdAndActiveTrueOrderByStartDateDesc(USER_ID))
                    .thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<PlannedTransaction> result = adapter.findAllByUserId(USER_ID);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should return empty list when no templates")
        void shouldReturnEmpty() {
            when(repository.findByUserIdAndActiveTrueOrderByStartDateDesc(USER_ID))
                    .thenReturn(Collections.emptyList());

            List<PlannedTransaction> result = adapter.findAllByUserId(USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllActive (scheduler batch)")
    class FindAllActive {
        @Test
        @DisplayName("Should return batch with pagination and id ascending")
        void shouldReturnBatch() {
            PlannedTransactionEntity entity = entityTemplate();
            PlannedTransaction domain = domainTemplate();
            LocalDate today = LocalDate.now();

            Page<PlannedTransactionEntity> page = new PageImpl<>(List.of(entity));
            when(repository.findByActiveTrueAndNotExpired(eq(today), any(Pageable.class)))
                    .thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<PlannedTransaction> result = adapter.findAllActive(0, 500, today);

            assertThat(result).containsExactly(domain);
            verify(repository).findByActiveTrueAndNotExpired(eq(today), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("findAllByUserId (paginated with filters)")
    class FindAllByUserIdPaginated {

        @Test
        @DisplayName("Should build specification with userId, active, search and type")
        void shouldBuildSpecification() {
            PlannedTransactionEntity entity = entityTemplate();
            PlannedTransaction domain = domainTemplate();

            Page<PlannedTransactionEntity> page = new PageImpl<>(List.of(entity));
            when(repository.findAll(anySpecification(), any(Pageable.class)))
                    .thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<PlannedTransaction> result = adapter.findAllByUserId(
                    USER_ID, 0, 50, "rent", TransactionType.EXPENSE
            );

            assertThat(result).containsExactly(domain);
            verify(repository).findAll(anySpecification(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should work with null search and type")
        void shouldWorkWithNullFilters() {
            PlannedTransactionEntity entity = entityTemplate();
            PlannedTransaction domain = domainTemplate();

            Page<PlannedTransactionEntity> page = new PageImpl<>(List.of(entity));
            when(repository.findAll(anySpecification(), any(Pageable.class)))
                    .thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<PlannedTransaction> result = adapter.findAllByUserId(
                    USER_ID, 0, 50, null, null
            );

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should sort by startDate descending")
        void shouldSortByStartDateDesc() {
            Page<PlannedTransactionEntity> page = new PageImpl<>(Collections.emptyList());
            when(repository.findAll(anySpecification(), any(Pageable.class)))
                    .thenReturn(page);

            adapter.findAllByUserId(USER_ID, 0, 10, null, null);

            ArgumentCaptor<Pageable> pageableCaptor =
                    ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(anySpecification(), pageableCaptor.capture());

            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getSort().getOrderFor("startDate")).isNotNull();
            assertThat(captured.getSort().getOrderFor("startDate").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("countByUserIdAndFilters")
    class Count {
        @Test
        @DisplayName("Should delegate count with specification")
        void shouldDelegateCount() {
            when(repository.count(anySpecification())).thenReturn(7L);

            long result = adapter.countByUserIdAndFilters(
                    USER_ID, "rent", TransactionType.EXPENSE
            );

            assertThat(result).isEqualTo(7L);
        }
    }
}