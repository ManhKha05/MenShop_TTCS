package com.ttcs.menshop.auth.repository;

import com.ttcs.menshop.auth.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity , Integer> {

    @Query("""
        SELECT DISTINCT u FROM UserEntity u
        JOIN u.roles r
        WHERE (:keyword IS NULL OR u.fullName LIKE CONCAT('%', :keyword, '%')
                                OR u.phone LIKE CONCAT('%', :keyword, '%')
                                OR u.email LIKE CONCAT('%', :keyword, '%'))
            AND (:role IS NULL OR r.name = :role)
            AND (:status IS NULL OR u.status = :status)
    """)
    Page<UserEntity> search(String keyword, String role, String status, Pageable pageable);

    long count();

    @Query("""
        SELECT COUNT(u) FROM UserEntity u
        WHERE MONTH(u.createdAt) = MONTH(now())
    """)
    long countCurrentMonth();

    long countByRoles_Name(String role);
    long countByRoles_NameAndStatus(String role, String status);

    Optional<UserEntity> findByUsername(String username);

    @Query("""
        select u
        from UserEntity u
        left join fetch u.roles
        where u.email = :email
    """)
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<UserEntity> findTop5ByOrderByCreatedAtDesc();

    @Query("""
        select distinct u
        from UserEntity u
        join u.roles r
        where r.name = :roleName
    """)
    UserEntity findByRoleName(@Param("roleName") String roleName);
}
