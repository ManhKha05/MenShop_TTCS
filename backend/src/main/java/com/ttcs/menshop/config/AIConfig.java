//package com.ttcs.menshop.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import lombok.Getter;
//
//@Configuration
//@Getter
//public class AIConfig {
//
//    @Value("${ai.server.url}")
//    private String baseUrl;
//
//    public String getRecommendEndpoint() {
//        return baseUrl + "/api/recommend";
//    }
//
//    public String getSmartSearchEndpoint() {
//        return baseUrl + "/api/search";
//    }
//}