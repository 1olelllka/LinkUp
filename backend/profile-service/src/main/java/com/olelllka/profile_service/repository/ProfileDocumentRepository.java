package com.olelllka.profile_service.repository;

import com.olelllka.profile_service.domain.entity.ProfileDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileDocumentRepository extends ElasticsearchRepository<ProfileDocument, UUID> {
    @Query("""
        {
          "bool": {
            "should": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": [
                    "username^3",
                    "name^2",
                    "email"
                  ],
                  "type": "best_fields",
                  "operator": "or",
                  "fuzziness": "AUTO"
                }
              },
              {
                "multi_match": {
                  "query": "?0",
                  "fields": [
                    "username^3",
                    "name^2",
                    "email"
                  ],
                  "type": "phrase_prefix",
                  "operator": "or"
                }
              },
              {
                "query_string": {
                  "query": "?0",
                  "fields": [
                    "username^3",
                    "name^2",
                    "email"
                  ],
                  "analyze_wildcard": true,
                  "auto_generate_synonyms_phrase_query": true
                }
              }
            ],
            "minimum_should_match": 1
          }
        }
    """)
    Page<ProfileDocument> findByParams(String searchTerm, Pageable pageable);
}