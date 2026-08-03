package com.olelllka.auth_service.repository;

import com.olelllka.auth_service.domain.entity.AuthProvider;
import com.olelllka.auth_service.domain.entity.OAuthIdentity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthIdentityRepository extends MongoRepository<OAuthIdentity, UUID> {

    Optional<OAuthIdentity> findByAuthProviderAndProviderSubject(AuthProvider authProvider, String providerSubject);
}
