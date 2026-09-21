package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository.TransactionAggregates;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.mappers.TransactionEntityMapper;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.repository.SpringDataTransactionRepository;
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
@DisplayName("TransactionPostgresAdapter")
class TransactionPostgresAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private SpringDataTransactionRepository repository;

    @Mock
    private TransactionEntityMapper mapper;

    @InjectMocks
    private TransactionPostgresAdapter adapter;

    private Transaction domainTransaction() {
        return Transaction.create(
                USER_ID,
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                TransactionType.EXPENSE,
                "Groceries"
        );
    }

    private TransactionEntity transactionEntity() {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setCategoryId(UUID.randomUUID());
        entity.setAmount(new BigDecimal("50.00"));
        entity.setDate(LocalDateTime.now());
        entity.setType("EXPENSE");
        entity.setDescription("Groceries");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);
        return entity;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should map domain to entity, save it, and map back to domain")
        void shouldSaveTransaction() {
            Transaction domain = domainTransaction();
            TransactionEntity entity = transactionEntity();
            TransactionEntity savedEntity = transactionEntity();
            Transaction savedDomain = domainTransaction();

            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

            Transaction result = adapter.save(domain);

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
            UUID transactionId = UUID.randomUUID();
            TransactionEntity entity = transactionEntity();
            Transaction domain = domainTransaction();

            when(repository.findById(transactionId)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Transaction> result = adapter.findById(transactionId);

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when entity does not exist")
        void shouldReturnEmptyWhenEntityDoesNotExist() {
            UUID transactionId = UUID.randomUUID();

            when(repository.findById(transactionId)).thenReturn(Optional.empty());

            Optional<Transaction> result = adapter.findById(transactionId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("Should map simple findAllByUserId result")
        void shouldMapSimpleFindAllByUserId() {
            TransactionEntity entity = transactionEntity();
            Transaction domain = domainTransaction();

            when(repository.findAllByUserIdOrderByDateDesc(USER_ID))
                    .thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Transaction> result = adapter.findAllByUserId(USER_ID);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should use specification and pagination on paginated findAllByUserId")
        void shouldUseSpecificationAndPagination() {
            TransactionEntity entity = transactionEntity();
            Transaction domain = domainTransaction();

            Pageable expectedPageable = PageRequest.of(
                    0, 10, Sort.by("date").descending()
            );
            Page<TransactionEntity> page = new PageImpl<>(
                    List.of(entity), expectedPageable, 1
            );

            when(repository.findAll(
                    ArgumentMatchers.<Specification<TransactionEntity>>any(),
                    any(Pageable.class)
            )).thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Transaction> result = adapter.findAllByUserId(
                    USER_ID, 0, 10, "search", TransactionType.EXPENSE, null, null
            );

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(
                    ArgumentMatchers.<Specification<TransactionEntity>>any(),
                    pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(result).containsExactly(domain);
            assertThat(capturedPageable.getPageNumber()).isZero();
            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
            assertThat(capturedPageable.getSort().getOrderFor("date")).isNotNull();
            assertThat(capturedPageable.getSort().getOrderFor("date").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("findRecentByUserId")
    class FindRecentByUserId {

        @Test
        @DisplayName("Should return empty list when limit is zero")
        void shouldReturnEmptyWhenLimitIsZero() {
            List<Transaction> result = adapter.findRecentByUserId(USER_ID, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when limit is negative")
        void shouldReturnEmptyWhenLimitIsNegative() {
            List<Transaction> result = adapter.findRecentByUserId(USER_ID, -1);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should use PageRequest with given limit")
        void shouldUsePageRequestWithGivenLimit() {
            TransactionEntity entity = transactionEntity();
            Transaction domain = domainTransaction();

            when(repository.findByUserIdAndActiveTrueOrderByDateDesc(
                    eq(USER_ID), any(Pageable.class)
            )).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Transaction> result = adapter.findRecentByUserId(USER_ID, 5);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findByUserIdAndActiveTrueOrderByDateDesc(
                    eq(USER_ID), pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(result).containsExactly(domain);
            assertThat(capturedPageable.getPageNumber()).isZero();
            assertThat(capturedPageable.getPageSize()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("countByUserIdAndFilters")
    class CountByUserIdAndFilters {

        @Test
        @DisplayName("Should delegate count with specification")
        void shouldDelegateCount() {
            when(repository.count(ArgumentMatchers.<Specification<TransactionEntity>>any()))
                    .thenReturn(5L);

            long result = adapter.countByUserIdAndFilters(
                    USER_ID, "search", TransactionType.EXPENSE, null, null
            );

            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("getAggregatesByUserIdAndDateBetween")
    class GetAggregates {

        @Test
        @DisplayName("Should parse Object array result correctly")
        void shouldParseObjectArrayResult() {
            Object[] result = {
                    new BigDecimal("1000.00"),
                    new BigDecimal("500.00"),
                    15L
            };

            when(repository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(result);

            TransactionAggregates aggregates = adapter.getAggregatesByUserIdAndDateBetween(
                    USER_ID, LocalDateTime.now().minusDays(30), LocalDateTime.now()
            );

            assertThat(aggregates.totalIncome()).isEqualByComparingTo("1000.00");
            assertThat(aggregates.totalExpense()).isEqualByComparingTo("500.00");
            assertThat(aggregates.count()).isEqualTo(15L);
        }

        @Test
        @DisplayName("Should handle null values in result array")
        void shouldHandleNullValuesInResultArray() {
            Object[] result = {null, null, null};

            when(repository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(result);

            TransactionAggregates aggregates = adapter.getAggregatesByUserIdAndDateBetween(
                    USER_ID, LocalDateTime.now().minusDays(30), LocalDateTime.now()
            );

            assertThat(aggregates.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(aggregates.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(aggregates.count()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("clearCategoryFromActiveTransactions")
    class ClearCategory {

        @Test
        @DisplayName("Should delegate to repository")
        void shouldDelegateToRepository() {
            UUID categoryId = UUID.randomUUID();
            LocalDateTime modifiedAt = LocalDateTime.now();

            adapter.clearCategoryFromActiveTransactions(categoryId, USER_ID, modifiedAt);

            verify(repository).clearCategoryFromActiveTransactions(
                    categoryId, USER_ID, modifiedAt
            );
        }
    }

    @Nested
    @DisplayName("findByUserIdAndDateBetween")
    class FindByUserIdAndDateBetween {

        @Test
        @DisplayName("Should map result")
        void shouldMapResult() {
            TransactionEntity entity = transactionEntity();
            Transaction domain = domainTransaction();

            when(repository.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Transaction> result = adapter.findByUserIdAndDateBetween(
                    USER_ID, LocalDateTime.now().minusDays(30), LocalDateTime.now()
            );

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should return empty list when no transactions")
        void shouldReturnEmptyWhenNoTransactions() {
            when(repository.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            List<Transaction> result = adapter.findByUserIdAndDateBetween(
                    USER_ID, LocalDateTime.now().minusDays(30), LocalDateTime.now()
            );

            assertThat(result).isEmpty();
        }
    }
}