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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHits;
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
public class SmartSearchService {

    ElasticsearchOperations elasticsearchOperations;
    AiServiceClient aiServiceClient;

    public Page<ProductDocument> searchSmart(String rawKeyword, int page, int size) {
        String keyword = optimizeKeyword(rawKeyword);
        int safeSize = Math.min(size, 20);
        int fetchSize = 60;
        long startTime = System.currentTimeMillis();

        try {
            List<SearchHit<ProductDocument>> textHits = executeTextSearch(keyword, fetchSize);
            List<SearchHit<ProductDocument>> vectorHits = executeVectorSearch(keyword, fetchSize);

            Map<String, Double> rrfScores = calculateRrf(textHits, vectorHits);

            Map<String, ProductDocument> docMap = new HashMap<>();
            textHits.forEach(hit -> docMap.putIfAbsent(hit.getContent().getId(), hit.getContent()));
            vectorHits.forEach(hit -> docMap.putIfAbsent(hit.getContent().getId(), hit.getContent()));

            final double RRF_THRESHOLD = 0.018;

            List<ProductDocument> allFilteredResults = rrfScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .filter(entry -> entry.getValue() >= RRF_THRESHOLD)
                    .map(entry -> docMap.get(entry.getKey()))
                    .collect(Collectors.toList());

            int totalElements = allFilteredResults.size();

            List<ProductDocument> pagedResults = allFilteredResults.stream()
                    .skip((long) page * safeSize)
                    .limit(safeSize)
                    .collect(Collectors.toList());

            log.info("[Production Hybrid] Raw: [{}], Clean: [{}], Total Found: {}, Page Size: {}, Latency: {}ms",
                    rawKeyword, keyword, totalElements, pagedResults.size(), (System.currentTimeMillis() - startTime));

            return new PageImpl<>(pagedResults, PageRequest.of(page, safeSize), totalElements);

        } catch (Exception e) {
            log.warn("Lỗi luồng AI. Kích hoạt Fallback Fuzzy Search cho từ khóa: {}", keyword);
            return searchFuzzy(rawKeyword, page, safeSize);
        }
    }

    private List<SearchHit<ProductDocument>> executeTextSearch(String keyword, int limit) {
        Query textQuery = BoolQuery.of(b -> b.must(MultiMatchQuery.of(m -> m
                .fields("name^6", "brandName^4", "categoryName^3", "parentCategory^2")
                .query(keyword)
                .fuzziness("AUTO")
                .minimumShouldMatch("2<75%")
        )._toQuery()))._toQuery();

        return elasticsearchOperations.search(
                NativeQuery.builder().withQuery(textQuery).withPageable(PageRequest.of(0, limit)).build(),
                ProductDocument.class).getSearchHits();
    }

    private List<SearchHit<ProductDocument>> executeVectorSearch(String keyword, int limit) {
        float[] vector = aiServiceClient.getVectorFromText(keyword);
        List<Float> vectorList = new ArrayList<>(vector.length);
        for (float v : vector) vectorList.add(v);

        KnnSearch knnSearch = KnnSearch.of(k -> k.field("semanticVector").queryVector(vectorList)
                .k(limit).numCandidates(limit * 2));

        return elasticsearchOperations.search(
                NativeQuery.builder().withKnnSearches(List.of(knnSearch)).withPageable(PageRequest.of(0, limit)).build(),
                ProductDocument.class).getSearchHits();
    }

    private Map<String, Double> calculateRrf(List<SearchHit<ProductDocument>> textHits,
                                             List<SearchHit<ProductDocument>> vectorHits) {
        Map<String, Double> scores = new HashMap<>();
        final int K = 60;

        for (int i = 0; i < vectorHits.size(); i++) {
            SearchHit<ProductDocument> hit = vectorHits.get(i);
            if (Float.isNaN(hit.getScore()) || hit.getScore() < 0.4f) continue;
            scores.put(hit.getContent().getId(), (1.0 / (K + i + 1)) * 1.5);
        }

        for (int i = 0; i < textHits.size(); i++) {
            String id = textHits.get(i).getContent().getId();
            scores.put(id, scores.getOrDefault(id, 0.0) + (1.0 / (K + i + 1)));
        }
        return scores;
    }

    public Page<ProductDocument> searchFuzzy(String rawKeyword, int page, int size) {
        String keyword = optimizeKeyword(rawKeyword);

        Query fuzzyQuery = BoolQuery.of(b -> b.must(MultiMatchQuery.of(m -> m
                .fields("name^6", "brandName^4", "categoryName^3", "parentCategory^2")
                .query(keyword)
                .fuzziness("AUTO")
                .minimumShouldMatch("2<75%")
        )._toQuery()))._toQuery();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(fuzzyQuery).withPageable(PageRequest.of(page, size)).build(),
                ProductDocument.class);

        List<ProductDocument> list = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        long totalHits = searchHits.getTotalHits();

        return new PageImpl<>(list, PageRequest.of(page, size), totalHits);
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