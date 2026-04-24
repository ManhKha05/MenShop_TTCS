package com.ttcs.menshop.modules.category.repository;


import com.ttcs.menshop.modules.category.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    List<CategoryEntity> findByParentIsNullAndStatus(String status);

    @Query("""
        SELECT c FROM CategoryEntity c
        WHERE (:keyword IS NULL OR c.name LIKE CONCAT('%', :keyword, '%'))
            AND (:parentId IS NULL OR c.parent.id = :parentId)
            AND (:status IS NULL OR c.status = :status)
    """)
    Page<CategoryEntity> search(String keyword, Integer parentId, String status, Pageable pageable);

    long count();
    long countByStatus(String status);
    long countByParentId(Integer parentId);

    @Query("""
        SELECT c FROM CategoryEntity c
        WHERE c.id NOT IN (
            SELECT DISTINCT c2.parent.id FROM CategoryEntity c2 WHERE c2.parent.id IS NOT NULL
        )
    """)
    List<CategoryEntity> findLeafCategories();

    @Query("SELECT DISTINCT c FROM CategoryEntity c JOIN c.products p WHERE p.status = 'ACTIVE' AND p.shop.id = :shopId")
    List<CategoryEntity> findDistinctByShopId(Integer shopId);

    @Query("""
        select c
        from CategoryEntity c
        left join fetch c.parent
        where c.id = :id
          and c.status = 'ACTIVE'
    """)
    Optional<CategoryEntity> findActiveById(@Param("id") Integer id);

    @Query("""
        select c
        from CategoryEntity c
        where c.parent.id = :parentId
          and c.status = 'ACTIVE'
        order by c.name asc
    """)
    List<CategoryEntity> findChildrenByParentId(@Param("parentId") Integer parentId);

    @Query("""
        select c
        from CategoryEntity c
        where c.parent.id = :parentId
          and c.status = 'ACTIVE'
          and c.id <> :currentId
        order by c.name asc
    """)
    List<CategoryEntity> findRelatedByParentId(
            @Param("parentId") Integer parentId,
            @Param("currentId") Integer currentId
    );

}
