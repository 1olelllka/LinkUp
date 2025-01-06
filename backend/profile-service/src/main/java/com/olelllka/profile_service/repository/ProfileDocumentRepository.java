package com.olelllka.profile_service.repository;

import com.olelllka.profile_service.domain.entity.ProfileDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileDocumentRepository extends ElasticsearchRepository<ProfileDocument, UUID> {
}
