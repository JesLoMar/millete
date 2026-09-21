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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Mock
    private JpaPlannedTransactionRepository repository;

    @Mock
    private PlannedTransactionEntityMapper mapper;

    @InjectMocks
    private PlannedTransactionPostgresAdapter adapter;

    private PlannedTransaction domainPlannedTransaction() {
        return PlannedTransaction.create(
                USER_ID, UUID.randomUUID(), new BigDecimal("100.00"),
                TransactionType.EXPENSE, "Rent",
                FrequencyType.MONTHS, 1,
                LocalDate.now(), null
        );
    }

    private PlannedTransactionEntity plannedTransactionEntity() {
        PlannedTransactionEntity entity = new PlannedTransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setCategoryId(UUID.randomUUID());
        entity.setAmount(new BigDecimal("100.00"));
        entity.setType("EXPENSE");
        entity.setDescription("Rent");
        entity.setFrequencyType("MONTHS");
        entity.setFrequencyInterval(1);
        entity.setStartDate(LocalDate.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);
        entity.setFailureCount(0);
        return entity;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should map domain to entity, save it, and map back to domain")
        void shouldSavePlannedTransaction() {
            PlannedTransaction domain = domainPlannedTransaction();
            PlannedTransactionEntity entity = plannedTransactionEntity();
            PlannedTransactionEntity savedEntity = plannedTransactionEntity();
            PlannedTransaction savedDomain = domainPlannedTransaction();

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
        void shouldReturnMappedDomainWhenEntityExists() {
            UUID ptxId = UUID.randomUUID();
            PlannedTransactionEntity entity = plannedTransactionEntity();
            PlannedTransaction domain = domainPlannedTransaction();

            when(repository.findById(ptxId)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<PlannedTransaction> result = adapter.findById(ptxId);

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when entity does not exist")
        void shouldReturnEmptyWhenEntityDoesNotExist() {
            UUID ptxId = UUID.randomUUID();

            when(repository.findById(ptxId)).thenReturn(Optional.empty());

            Optional<PlannedTransaction> result = adapter.findById(ptxId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("Should map simple findAllByUserId result")
        void shouldMapSimpleFindAllByUserId() {
            PlannedTransactionEntity entity = plannedTransactionEntity();
            PlannedTransaction domain = domainPlannedTransaction();

            when(repository.findByUserIdAndActiveTrueOrderByStartDateDesc(USER_ID))
                    .thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<PlannedTransaction> result = adapter.findAllByUserId(USER_ID);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should use specification and pagination on paginated findAllByUserId")
        void shouldUseSpecificationAndPagination() {
            PlannedTransactionEntity entity = plannedTransactionEntity();
            PlannedTransaction domain = domainPlannedTransaction();

            Pageable expectedPageable = PageRequest.of(
                    0, 50, Sort.by("startDate").descending()
            );
            Page<PlannedTransactionEntity> page = new PageImpl<>(
                    List.of(entity), expectedPageable, 1
            );

            when(repository.findAll(
                    ArgumentMatchers.<Specification<PlannedTransactionEntity>>any(),
                    any(Pageable.class)
            )).thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<PlannedTransaction> result = adapter.findAllByUserId(
                    USER_ID, 0, 50, "search", TransactionType.EXPENSE
            );

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(
                    ArgumentMatchers.<Specification<PlannedTransactionEntity>>any(),
                    pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(result).containsExactly(domain);
            assertThat(capturedPageable.getPageNumber()).isZero();
            assertThat(capturedPageable.getPageSize()).isEqualTo(50);
            assertThat(capturedPageable.getSort().getOrderFor("startDate")).isNotNull();
            assertThat(capturedPageable.getSort().getOrderFor("startDate").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("findAllActive")
    class FindAllActive {

        @Test
        @DisplayName("Should find active and not expired templates")
        void shouldFindActiveAndNotExpiredTemplates() {
            PlannedTransactionEntity entity = plannedTransactionEntity();
            PlannedTransaction domain = domainPlannedTransaction();
            LocalDate today = LocalDate.now();

            when(repository.findByActiveTrueAndNotExpired(eq(today), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(entity)));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<PlannedTransaction> result = adapter.findAllActive(0, 500, today);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should return empty list when no active templates")
        void shouldReturnEmptyWhenNoActiveTemplates() {
            LocalDate today = LocalDate.now();

            when(repository.findByActiveTrueAndNotExpired(eq(today), any(Pageable.class)))
                    .thenReturn(Page.empty());

            List<PlannedTransaction> result = adapter.findAllActive(0, 500, today);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByUserIdAndFilters")
    class CountByUserIdAndFilters {

        @Test
        @DisplayName("Should delegate count with specification")
        void shouldDelegateCount() {
            when(repository.count(ArgumentMatchers.<Specification<PlannedTransactionEntity>>any()))
                    .thenReturn(5L);

            long result = adapter.countByUserIdAndFilters(
                    USER_ID, "search", TransactionType.EXPENSE
            );

            assertThat(result).isEqualTo(5L);
        }
    }
}