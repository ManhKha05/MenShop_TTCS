package com.ttcs.menshop.modules.search.service;

import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ttcs.menshop.modules.search.document.ProductDocument;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestSearchService {

    ElasticsearchOperations elasticsearchOperations;
    AiServiceClient aiServiceClient;

    public List<ProductDocument> searchProducts(String rawKeyword, String strategy, int page, int size) {
        String keyword = optimizeKeyword(rawKeyword);
        log.info("Đã làm sạch từ khóa: [{}] -> [{}]", rawKeyword, keyword);
        try {
            if ("TEXT".equalsIgnoreCase(strategy)) {
                log.info("[EVALUATION] Chạy TEXT SEARCH cho từ khóa: {}", keyword);
                return runTextSearch(keyword, page, size);
            }
            else if ("VECTOR".equalsIgnoreCase(strategy)) {
                log.info("[EVALUATION] Chạy VECTOR SEARCH cho từ khóa: {}", keyword);
                return runVectorSearch(keyword, page, size);
            }
            else {
                log.info("[EVALUATION] Chạy HYBRID SEARCH (RRF) cho từ khóa: {}", keyword);
                return runHybridRrfSearch(keyword, page, size);
            }
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đánh giá (Strategy: {}): {}", strategy, e.getMessage());
            return runTextSearch(keyword, page, size);
        }
    }

    private List<ProductDocument> runTextSearch(String keyword, int page, int size) {
        Query textQuery = BoolQuery.of(b -> b
                .must(MultiMatchQuery.of(m -> m
                        .fields("name^6", "brandName^4", "categoryName^3", "parentCategory^2")
                        .query(keyword)
                        .fuzziness("AUTO")
                        .minimumShouldMatch("2<75%")
                )._toQuery())
        )._toQuery();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(textQuery)
                .withPageable(PageRequest.of(page, size))
                .build();

        return elasticsearchOperations.search(nativeQuery, ProductDocument.class)
                .getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    private List<ProductDocument> runVectorSearch(String keyword, int page, int size) {
        int fetchLimit = (page + 1) * size;

        float[] vector = aiServiceClient.getVectorFromText(keyword);
        List<Float> vectorList = new ArrayList<>(vector.length);
        for (float v : vector) vectorList.add(v);

        KnnSearch knnSearch = KnnSearch.of(k -> k
                .field("semanticVector")
                .queryVector(vectorList)
                .k(fetchLimit)
                .numCandidates(Math.max(50, fetchLimit * 2))
        );

        NativeQuery vectorQuery = NativeQuery.builder()
                .withKnnSearches(List.of(knnSearch))
                .withPageable(PageRequest.of(0, fetchLimit))
                .build();

        return elasticsearchOperations.search(vectorQuery, ProductDocument.class)
                .getSearchHits().stream()
                .skip((long) page * size)
                .limit(size)
                .map(SearchHit::getContent).collect(Collectors.toList());
    }

    private List<ProductDocument> runHybridRrfSearch(String keyword, int page, int size) {
        int fetchSize = 60;

        Query textQuery = BoolQuery.of(b -> b.must(MultiMatchQuery.of(m -> m
                .fields("name^6", "brandName^4", "categoryName^3", "parentCategory^2")
                .query(keyword)
                .fuzziness("AUTO")
                .minimumShouldMatch("2<75%")
        )._toQuery()))._toQuery();

        List<SearchHit<ProductDocument>> textHits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(textQuery).withPageable(PageRequest.of(0, fetchSize)).build(),
                ProductDocument.class).getSearchHits();

        float[] vector = aiServiceClient.getVectorFromText(keyword);
        List<Float> vectorList = new ArrayList<>(vector.length);
        for (float v : vector) vectorList.add(v);

        KnnSearch knnSearch = KnnSearch.of(k -> k.field("semanticVector").queryVector(vectorList)
                .k(fetchSize).numCandidates(fetchSize * 2));

        List<SearchHit<ProductDocument>> vectorHits = elasticsearchOperations.search(
                NativeQuery.builder().withKnnSearches(List.of(knnSearch)).withPageable(PageRequest.of(0, fetchSize)).build(),
                ProductDocument.class).getSearchHits();

        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, ProductDocument> docMap = new HashMap<>();
        final int RANK_CONSTANT = 60;

        for (int i = 0; i < vectorHits.size(); i++) {
            SearchHit<ProductDocument> hit = vectorHits.get(i);
            if (Float.isNaN(hit.getScore()) || hit.getScore() < 0.4f) continue;
            ProductDocument doc = hit.getContent();
            docMap.putIfAbsent(doc.getId(), doc);
            rrfScores.put(doc.getId(), (1.0 / (RANK_CONSTANT + i + 1)) * 1.5);
        }

        for (int i = 0; i < textHits.size(); i++) {
            ProductDocument doc = textHits.get(i).getContent();
            docMap.putIfAbsent(doc.getId(), doc);
            rrfScores.put(doc.getId(), rrfScores.getOrDefault(doc.getId(), 0.0) + (1.0 / (RANK_CONSTANT + i + 1)));
        }

        final double RRF_THRESHOLD = 0.013;

        log.info("--- BẢNG ĐIỂM RRF CHO TỪ KHÓA: [{}] ---", keyword);

        List<ProductDocument> finalResults = rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())

                .peek(entry -> log.info("ID: {} | RRF Score: {}", entry.getKey(), String.format("%.5f", entry.getValue())))

                .filter(entry -> {
                    if (entry.getValue() < RRF_THRESHOLD) {
                        log.debug("Đã lọc bỏ ID: {} do điểm ({}) dưới ngưỡng", entry.getKey(), entry.getValue());
                        return false;
                    }
                    return true;
                })

                .map(entry -> docMap.get(entry.getKey()))
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());

        log.info("Giữ lại {} kết quả sau khi lọc.", finalResults.size());
        return finalResults;
    }

    private String optimizeKeyword(String rawKeyword) {
        if (rawKeyword == null || rawKeyword.trim().isEmpty()) return "";

        String cleanKeyword = rawKeyword.toLowerCase().trim();

        cleanKeyword = cleanKeyword.replaceAll("\\bbo lay zơ\\b", "blazer");
        cleanKeyword = cleanKeyword.replaceAll("\\bxi lip\\b", "lót");
        cleanKeyword = cleanKeyword.replaceAll("\\bca ki\\b", "kaki");

        cleanKeyword = cleanKeyword.replaceAll("\\b(wần|qan)\\b", "quần");
        cleanKeyword = cleanKeyword.replaceAll("\\b(rin|bo|jin)\\b", "jean");
        cleanKeyword = cleanKeyword.replaceAll("\\b(sot|sooc|xot)\\b", "short");
        cleanKeyword = cleanKeyword.replaceAll("\\bxip\\b", "lót");
        cleanKeyword = cleanKeyword.replaceAll("\\bhup\\b", "hộp");
        cleanKeyword = cleanKeyword.replaceAll("\\bcj\\b", "ki");

        cleanKeyword = cleanKeyword.replaceAll("\\bmút\\b", "phao");
        cleanKeyword = cleanKeyword.replaceAll("\\bsiu\\b", "siêu");
        cleanKeyword = cleanKeyword.replaceAll("\\blang\\b", "nắng");
        cleanKeyword = cleanKeyword.replaceAll("\\bkhoat\\b", "khoác");
        cleanKeyword = cleanKeyword.replaceAll("\\bdzo\\b", "gió");
        cleanKeyword = cleanKeyword.replaceAll("\\bthung\\b", "thun");
        cleanKeyword = cleanKeyword.replaceAll("\\bdym\\b", "gym");
        cleanKeyword = cleanKeyword.replaceAll("\\bphog\\b", "phông");
        cleanKeyword = cleanKeyword.replaceAll("\\bkym\\b", "kim");

        cleanKeyword = cleanKeyword.replaceAll("\\bnam\\b", "");
        cleanKeyword = cleanKeyword.replaceAll("\\bthời trang\\b", "");
        cleanKeyword = cleanKeyword.replaceAll("\\bđồ\\b", "");

        return cleanKeyword.replaceAll("\\s+", " ").trim();
    }
}