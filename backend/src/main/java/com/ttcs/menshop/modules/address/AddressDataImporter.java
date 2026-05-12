//package com.ttcs.menshop.modules.address;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.http.*;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Map;
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class GhnProvinceResponse {
//    private int code;
//    private String message;
//    private List<Province> data;
//}
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class Province {
//    @JsonProperty("ProvinceID")
//    private Integer provinceID;
//
//    @JsonProperty("ProvinceName")
//    private String provinceName;
//}
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class GhnDistrictResponse {
//    private int code;
//    private String message;
//    private List<District> data;
//}
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class District {
//    @JsonProperty("DistrictID")
//    private Integer districtID;
//
//    @JsonProperty("ProvinceID")
//    private Integer provinceID;
//
//    @JsonProperty("DistrictName")
//    private String districtName;
//}
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class GhnWardResponse {
//    private int code;
//    private String message;
//    private List<Ward> data;
//}
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class Ward {
//    @JsonProperty("WardCode")
//    private String wardCode;
//
//    @JsonProperty("WardName")
//    private String wardName;
//}
//
//@Component
//@RequiredArgsConstructor
//public class AddressDataImporter implements CommandLineRunner {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    private static final String TOKEN = "2861ac26-4c0c-11f1-a973-aee5264794df";
//    private static final String BASE_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api";
//
//    @Override
//    public void run(String... args) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Token", TOKEN);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
//
//        ResponseEntity<GhnProvinceResponse> provinceRes = restTemplate.exchange(
//                BASE_URL + "/master-data/province",
//                HttpMethod.GET,
//                getEntity,
//                GhnProvinceResponse.class
//        );
//
//        GhnProvinceResponse provinceBody = provinceRes.getBody();
//
//        if (provinceBody == null || provinceBody.getData() == null) {
//            System.out.println("Không lấy được province từ GHN");
//            return;
//        }
//
//        for (Province p : provinceBody.getData()) {
//            jdbcTemplate.update("""
//        INSERT INTO ghn_province(province_id, province_name)
//        VALUES (?, ?)
//        ON DUPLICATE KEY UPDATE
//            province_name = VALUES(province_name)
//    """, p.getProvinceID(), p.getProvinceName());
//
//            String districtUrl = BASE_URL + "/master-data/district?province_id=" + p.getProvinceID();
//
//            ResponseEntity<GhnDistrictResponse> districtRes = restTemplate.exchange(
//                    districtUrl,
//                    HttpMethod.GET,
//                    getEntity,
//                    GhnDistrictResponse.class
//            );
//
//            GhnDistrictResponse districtBody = districtRes.getBody();
//
//            if (districtBody == null || districtBody.getData() == null) {
//                System.out.println("Không lấy được district của province_id = " + p.getProvinceID());
//                continue;
//            }
//
//            for (District d : districtBody.getData()) {
//                if (d.getDistrictID() == null) continue;
//
//                jdbcTemplate.update("""
//            INSERT INTO ghn_district(
//                district_id,
//                district_name,
//                province_id
//            )
//            VALUES (?, ?, ?)
//            ON DUPLICATE KEY UPDATE
//                district_name = VALUES(district_name),
//                province_id = VALUES(province_id)
//        """,
//                        d.getDistrictID(),
//                        d.getDistrictName(),
//                        p.getProvinceID()
//                );
//
//                HttpEntity<Map<String, Integer>> wardEntity =
//                        new HttpEntity<>(
//                                Map.of("district_id", d.getDistrictID()),
//                                headers
//                        );
//
//                ResponseEntity<GhnWardResponse> wardRes = restTemplate.exchange(
//                        BASE_URL + "/master-data/ward",
//                        HttpMethod.POST,
//                        wardEntity,
//                        GhnWardResponse.class
//                );
//
//                GhnWardResponse wardBody = wardRes.getBody();
//
//                if (wardBody == null || wardBody.getData() == null) {
//                    System.out.println("Không có ward cho district_id = " + d.getDistrictID());
//                    continue;
//                }
//
//                for (Ward w : wardBody.getData()) {
//                    if (w.getWardCode() == null) continue;
//
//                    jdbcTemplate.update("""
//                INSERT INTO ghn_ward(
//                    ward_code,
//                    ward_name,
//                    district_id
//                )
//                VALUES (?, ?, ?)
//                ON DUPLICATE KEY UPDATE
//                    ward_name = VALUES(ward_name),
//                    district_id = VALUES(district_id)
//            """,
//                            w.getWardCode(),
//                            w.getWardName(),
//                            d.getDistrictID()
//                    );
//                }
//            }
//        }
//
//        System.out.println("Import GHN done!");
//    }
//}