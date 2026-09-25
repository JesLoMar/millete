package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers.SavingsGoalEntityMapper;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.repository.JpaSavingsGoalRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import java.time.Instant;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsGoalPostgresAdapter")
class SavingsGoalPostgresAdapterTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private JpaSavingsGoalRepository jpaRepository;

    @Mock
    private SavingsGoalEntityMapper mapper;

    @InjectMocks
    private SavingsGoalPostgresAdapter adapter;

    private SavingsGoal domainGoal() {
        return SavingsGoal.create(TIME, 
                USER_ID,
                "Vacation",
                new BigDecimal("1000.00"),
                LocalDate.now().plusDays(30),
                null,
                null
        );
    }

    private SavingsGoalEntity goalEntity() {
        SavingsGoalEntity entity = new SavingsGoalEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setName("Vacation");
        entity.setTargetAmount(new BigDecimal("1000.00"));
        entity.setCurrentAmount(BigDecimal.ZERO);
        entity.setDeadline(LocalDate.now().plusDays(30));
        entity.setPriority("MEDIUM");
        entity.setLink(null);
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        entity.setModifiedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        entity.setActive(true);
        return entity;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should map domain to entity, save it, and map saved entity back to domain")
        void shouldSaveSavingsGoal() {
            SavingsGoal domain = domainGoal();
            SavingsGoalEntity entity = goalEntity();
            SavingsGoalEntity savedEntity = goalEntity();
            SavingsGoal savedDomain = domainGoal();

            when(mapper.toEntity(domain)).thenReturn(entity);
            when(jpaRepository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

            SavingsGoal result = adapter.save(domain);

            assertThat(result).isSameAs(savedDomain);
            verify(mapper).toEntity(domain);
            verify(jpaRepository).save(entity);
            verify(mapper).toDomain(savedEntity);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return mapped domain when entity exists")
        void shouldReturnMappedDomainWhenEntityExists() {
            UUID goalId = UUID.randomUUID();
            SavingsGoalEntity entity = goalEntity();
            SavingsGoal domain = domainGoal();

            when(jpaRepository.findById(goalId)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<SavingsGoal> result = adapter.findById(goalId);

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when entity does not exist")
        void shouldReturnEmptyWhenEntityDoesNotExist() {
            UUID goalId = UUID.randomUUID();

            when(jpaRepository.findById(goalId)).thenReturn(Optional.empty());

            Optional<SavingsGoal> result = adapter.findById(goalId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("Should return mapped domain when entity exists")
        void shouldReturnMappedDomainWhenEntityExists() {
            UUID goalId = UUID.randomUUID();
            SavingsGoalEntity entity = goalEntity();
            SavingsGoal domain = domainGoal();

            when(jpaRepository.findByIdAndUserId(goalId, USER_ID))
                    .thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<SavingsGoal> result = adapter.findByIdAndUserId(goalId, USER_ID);

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when entity does not exist")
        void shouldReturnEmptyWhenEntityDoesNotExist() {
            UUID goalId = UUID.randomUUID();

            when(jpaRepository.findByIdAndUserId(goalId, USER_ID))
                    .thenReturn(Optional.empty());

            Optional<SavingsGoal> result = adapter.findByIdAndUserId(goalId, USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("Should map findAllByUserId result to domain list")
        void shouldMapFindAllByUserIdResult() {
            SavingsGoalEntity entity = goalEntity();
            SavingsGoal domain = domainGoal();

            when(jpaRepository.findAllByUserId(USER_ID)).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<SavingsGoal> result = adapter.findAllByUserId(USER_ID);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should find all savings goals using pagination sorted by createdAt descending")
        void shouldFindAllSavingsGoalsUsingPaginationAndSort() {
            SavingsGoalEntity entity = goalEntity();
            SavingsGoal domain = domainGoal();

            Pageable expectedPageable = PageRequest.of(
                    0,
                    10,
                    Sort.by("createdAt").descending()
            );
            Page<SavingsGoalEntity> page = new PageImpl<>(List.of(entity), expectedPageable, 1);

            when(jpaRepository.findAll(
                    ArgumentMatchers.<Specification<SavingsGoalEntity>>any(),
                    any(Pageable.class)
            )).thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<SavingsGoal> result = adapter.findAllByUserId(USER_ID, 0, 10, "vacation");

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(jpaRepository).findAll(
                    ArgumentMatchers.<Specification<SavingsGoalEntity>>any(),
                    pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(result).containsExactly(domain);
            assertThat(capturedPageable.getPageNumber()).isZero();
            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
            assertThat(capturedPageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(capturedPageable.getSort().getOrderFor("createdAt").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("countByUserIdAndFilters")
    class CountByUserIdAndFilters {

        @Test
        @DisplayName("Should count savings goals using specification")
        void shouldCountSavingsGoalsUsingSpecification() {
            when(jpaRepository.count(ArgumentMatchers.<Specification<SavingsGoalEntity>>any()))
                    .thenReturn(3L);

            long result = adapter.countByUserIdAndFilters(USER_ID, "vacation");

            assertThat(result).isEqualTo(3L);
        }
    }
}