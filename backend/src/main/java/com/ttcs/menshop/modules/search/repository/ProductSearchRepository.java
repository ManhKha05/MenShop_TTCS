package com.ttcs.menshop.modules.search.repository;

import com.ttcs.menshop.modules.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByName(String name);

    List<ProductDocument> findByCategoryName(String categoryName);

    Page<ProductDocument> findByNameContaining(String name, Pageable pageable);

    List<ProductDocument> findByNameAndCategoryName(String name, String categoryName);

}