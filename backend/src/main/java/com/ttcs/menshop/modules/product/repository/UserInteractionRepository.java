//package com.ttcs.menshop.modules.product.repository;
//
//import com.ttcs.menshop.modules.product.entity.UserInteractionEntity;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface UserInteractionRepository extends JpaRepository<UserInteractionEntity, Integer> {
//
//    @Query("SELECT u.product.id " +
//            "FROM UserInteractionEntity u " +
//            "WHERE u.user.id = :userId " +
//            "GROUP BY u.product.id " +
//            "ORDER BY MAX(u.createdAt) DESC")
//    List<Integer> findRecentDistinctProductIds(@Param("userId") Integer userId, Pageable pageable);
//
//    default List<Integer> findTop5RecentDistinctProductIds(Integer userId) {
//        return findRecentDistinctProductIds(userId, PageRequest.of(0, 5));
//    }
//}