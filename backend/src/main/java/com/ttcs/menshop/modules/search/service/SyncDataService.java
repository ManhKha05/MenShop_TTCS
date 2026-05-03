package com.ttcs.menshop.modules.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.search.document.ProductDocument;
import com.ttcs.menshop.modules.search.repository.ProductSearchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SyncDataService {

    ProductRepository productRepository;
    ProductSearchRepository productSearchRepository;
    AiServiceClient aiServiceClient;
    ElasticsearchOperations elasticsearchOperations;
    ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public String syncAllProductsToElastic() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);

        if (indexOps.exists()) {
            log.info("Đang xóa Index cũ...");
            indexOps.delete();
        }
        log.info("Đang tạo mới Index và Mapping...");
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping());

        List<ProductEntity> mysqlProducts = productRepository.findAll();
        List<ProductDocument> elasticDocuments = mysqlProducts.stream().map(product -> {
            ProductDocument doc = new ProductDocument();
            doc.setId(String.valueOf(product.getId()));
            doc.setName(product.getName());
            doc.setDescription(product.getDescription());
            doc.setPrice(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
            doc.setSalePrice(product.getSalePrice() != null ? product.getSalePrice().doubleValue() : 0.0);
            doc.setSoldCount(product.getSoldCount());
            doc.setRatingAvg(product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() : 0.0);

            try {
                if (product.getAttributesJson() != null && !product.getAttributesJson().isEmpty()) {
                    String jsonString = objectMapper.writeValueAsString(product.getAttributesJson());
                    doc.setAttributesJson(jsonString);
                } else {
                    doc.setAttributesJson("");
                }
            } catch (Exception e) {
                log.warn("Lỗi convert JSON của SP ID {}: {}", product.getId(), e.getMessage());
                doc.setAttributesJson("");
            }

            if (product.getStatus() != null) {
                doc.setStatus(product.getStatus().toString());
            }

            if (product.getCategory() != null) {
                doc.setCategoryName(product.getCategory().getName());
                if (product.getCategory().getParent() != null) {
                    doc.setParentCategory(product.getCategory().getParent().getName());
                } else {
                    doc.setParentCategory(product.getCategory().getName());
                }
            }

            if (product.getBrand() != null) {
                doc.setBrandName(product.getBrand().getName());
            }

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                doc.setImageUrl(product.getImages().get(0).getImageUrl());
            } else {
                doc.setImageUrl("https://link-anh-demo.com/ao-thun.jpg");
            }

            return doc;
        }).collect(Collectors.toList());

        productSearchRepository.saveAll(elasticDocuments);
        log.info("Đã đồng bộ thành công {} sản phẩm sang Elasticsearch (Chưa có Vector)!", elasticDocuments.size());
        return "Đã đồng bộ thành công " + elasticDocuments.size() + " sản phẩm sang Elasticsearch!";
    }

    public String syncAllVectors() {
        log.info("Bắt đầu tiến trình sinh Vector từ AI Server...");
        Iterable<ProductDocument> products = productSearchRepository.findAll();
        List<ProductDocument> docsToUpdate = new ArrayList<>();
        int count = 0;

        for (ProductDocument product : products) {
            StringBuilder attributeBuilder = new StringBuilder();
            try {
                if (product.getAttributesJson() != null && !product.getAttributesJson().trim().isEmpty()) {
                    Map<String, String> attributesMap = objectMapper.readValue(
                            product.getAttributesJson(),
                            new TypeReference<Map<String, String>>() {
                            }
                    );

                    for (Map.Entry<String, String> entry : attributesMap.entrySet()) {
                        attributeBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(". ");
                    }
                }
            } catch (Exception e) {
                log.warn("Bỏ qua lỗi parse JSON thuộc tính của sản phẩm ID {}: {}", product.getId(), e.getMessage());
            }

            String name = product.getName() != null ? product.getName() : "";
            String cat = product.getCategoryName() != null ? product.getCategoryName() : "";
            String parent = product.getParentCategory() != null ? product.getParentCategory() : "";
            String brand = product.getBrandName() != null ? product.getBrandName() : "";

            String categoryContext = parent.equalsIgnoreCase(cat) ? cat : (parent + " - " + cat);

            StringBuilder textBuilder = new StringBuilder();

            if (!name.isEmpty()) {
                textBuilder.append("Sản phẩm: ").append(name).append(". ");
            }
            if (!categoryContext.isEmpty()) {
                textBuilder.append("Danh mục: ").append(categoryContext).append(". ");
            }
            if (!brand.isEmpty()) {
                textBuilder.append("Thương hiệu: ").append(brand).append(". ");
            }
            if (attributeBuilder.length() > 0) {
                textBuilder.append("Đặc điểm: ").append(attributeBuilder);
            }

            String textToVectorize = textBuilder.toString().replaceAll("\\s+", " ").trim();

            try {
                log.info("Đang sinh vector cho SP ID: {}", product.getId());

                float[] vectorArray = aiServiceClient.getVectorFromText(textToVectorize);
                product.setSemanticVector(vectorArray);
                docsToUpdate.add(product);
                count++;

            } catch (Exception e) {
                log.error("Không thể sinh vector cho sản phẩm ID {}: {}", product.getId(), e.getMessage());
            }
        }

        if (!docsToUpdate.isEmpty()) {
            productSearchRepository.saveAll(docsToUpdate);
            log.info("Đã nạp thành công Vector cho {} sản phẩm vào Core của Elasticsearch!", count);
        }

        return "Hoàn tất đồng bộ Vector cho " + count + " sản phẩm.";
    }
}
