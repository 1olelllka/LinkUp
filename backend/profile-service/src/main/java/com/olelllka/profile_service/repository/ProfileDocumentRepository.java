package com.olelllka.profile_service.repository;

import com.olelllka.profile_service.domain.entity.ProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileDocumentRepository extends MongoRepository<ProfileDocument, UUID> {
}
