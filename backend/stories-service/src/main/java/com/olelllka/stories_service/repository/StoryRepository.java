package com.olelllka.stories_service.repository;

import com.olelllka.stories_service.domain.entity.StoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface StoryRepository extends MongoRepository<StoryEntity, String> {

    @Query(value = "{ 'userId': ?0 }", sort = "{ 'createdAt' : -1 }")
    Page<StoryEntity> findStoryByUserId(UUID id, Pageable pageable);

    void deleteByUserId(UUID id);

    List<StoryEntity> findByAvailableTrueAndCreatedAtBefore(Date expiryDate);

    @Query("""
            {
                $and: [
                    {'id' : { $in: ?0 }},
                    {'available' : true}
                ]
            }
            """)
    Page<StoryEntity> findByIdsAndByAvailable(List<String> ids, Pageable pageable);
}
