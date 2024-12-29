package com.olelllka.profile_service.repository;

import com.olelllka.profile_service.domain.entity.ProfileEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileRepository extends Neo4jRepository<ProfileEntity, UUID> {
}
