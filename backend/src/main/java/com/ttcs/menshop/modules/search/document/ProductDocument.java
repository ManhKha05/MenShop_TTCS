package com.ttcs.menshop.modules.search.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Document(indexName = "products")
@Setting(shards = 1, replicas = 0)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, index = false)
    private String attributesJson;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Text)
    private String parentCategory;

    @Field(type = FieldType.Text)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String brandName;

    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Double)
    private Double salePrice;

    @Field(type = FieldType.Integer)
    private Integer soldCount;

    @Field(type = FieldType.Double)
    private Double ratingAvg;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Dense_Vector, dims = 1024, index = true, similarity = "cosine")
    @JsonProperty("semanticVector")
    private float[] semanticVector;
}