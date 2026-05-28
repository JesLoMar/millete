package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.family.domain.model.FamilyMember;
import com.puntomartinez.millete.family.domain.ports.out.FamilyMemberRepository;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyMemberEntity;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers.FamilyMemberEntityMapper;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository.JpaFamilyMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FamilyMemberPostgresAdapter implements FamilyMemberRepository {

    private final JpaFamilyMemberRepository jpaRepository;
    private final FamilyMemberEntityMapper mapper;

    public FamilyMemberPostgresAdapter(JpaFamilyMemberRepository jpaRepository, FamilyMemberEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FamilyMember save(FamilyMember familyMember) {
        FamilyMemberEntity entity = mapper.toEntity(familyMember);
        FamilyMemberEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FamilyMember> findByFamilyIdAndUserId(UUID familyId, UUID userId) {
        return jpaRepository.findByFamilyIdAndUserId(familyId, userId).map(mapper::toDomain);
    }

    @Override
    public Optional<FamilyMember> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<FamilyMember> findByFamilyId(UUID familyId) {
        return jpaRepository.findByFamilyIdAndActiveTrue(familyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByFamilyIdAndUserId(UUID familyId, UUID userId) {
        jpaRepository.deleteByFamilyIdAndUserId(familyId, userId);
    }

    @Override
    public List<FamilyMember> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}