package com.olelllka.profile_service.repository;

import com.olelllka.profile_service.domain.entity.ProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends Neo4jRepository<ProfileEntity, UUID> {

    @Query("""
    MATCH (p:Profile {id: $followerId})-[:FOLLOWS]->(f:Profile {id: $followeeId})
    RETURN COUNT(*) > 0
    """)
    boolean isFollowing(UUID followerId, UUID followeeId);

    @Query("""
        MATCH (p1:Profile {id: $followerId})
        WITH p1
        MATCH (p2:Profile {id: $followeeId})
        WHERE p1.id <> p2.id
        MERGE (p1)-[:FOLLOWS]->(p2)
        MERGE (p2)-[:FOLLOWED_BY]->(p1)
        """)
    void follow(UUID followerId, UUID followeeId);

    @Query("""
            MATCH (p1:Profile {id: $followerId})
            WITH p1
            MATCH (p2:Profile {id: $followeeId})
            WHERE p1.id <> p2.id
            MATCH (p1)-[r1:FOLLOWS]->(p2)
            MATCH (p2)-[r2:FOLLOWED_BY]->(p1)
            DELETE r1, r2
            """)
    void unfollow(UUID followerId, UUID followeeId);

    @Query("""
    MATCH (p:Profile {id: $profileId})
    OPTIONAL MATCH (p)-[:FOLLOWS*..1]->(following:Profile)
    OPTIONAL MATCH (p)<-[:FOLLOWS*..1]-(followers:Profile)
    RETURN p, collect(following) as following, collect(followers) as followers
    """)
    Optional<ProfileEntity> findByIdWithRelationships(UUID profileId);

    @Query("""
    MATCH (p:Profile {id: $profileId})
    SET p.username = COALESCE($username, p.username),
        p.name = COALESCE($name, p.name),
        p.email = COALESCE($email, p.email),
        p.gender = COALESCE($gender, p.gender),
        p.photo = COALESCE($photo, p.photo),
        p.aboutMe = COALESCE($aboutMe, p.aboutMe),
        p.dateOfBirth = COALESCE($dateOfBirth, p.dateOfBirth)
    RETURN p
    """)
    ProfileEntity updateProfile(
            UUID profileId,
            String username,
            String name,
            String email,
            String gender,
            String photo,
            String aboutMe,
            LocalDate dateOfBirth
    );

    @Query(value = """
    MATCH (p:Profile {id: $profileId})-[:FOLLOWED_BY]->(f:Profile)
    RETURN f
    SKIP $skip
    LIMIT $limit
    """, countQuery = """
    MATCH (p:Profile {id: $profileId})-[:FOLLOWED_BY]->(f:Profile)
    RETURN count(f)
    """)
    Page<ProfileEntity> findAllFollowersByProfileId(UUID profileId, Pageable pageable);

    @Query(value = """
    MATCH (p:Profile {id: $profileId})-[:FOLLOWS]->(f:Profile)
    RETURN f
    SKIP $skip
    LIMIT $limit
    """, countQuery = """
    MATCH (p:Profile {id: $profileId})-[:FOLLOWS]->(f:Profile)
    RETURN count(f)
    """)
    Page<ProfileEntity> findAllFolloweeByProfileId(UUID profileId, Pageable pageable);

    @Query(value = """
            MATCH (p:Profile)
            WHERE p.username CONTAINS $search OR p.name CONTAINS $search
            RETURN p
            SKIP $skip
            LIMIT $limit
            """, countQuery = """
            MATCH (p:Profile)
            WHERE p.username CONTAINS $search OR p.name CONTAINS $search
            RETURN count(p)
            """)
    Page<ProfileEntity> findProfileByParam(String search, Pageable pageable);
}