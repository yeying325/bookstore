package com.bookstore.bookstorebackend.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 把数据库里的“科幻,中国文学”这种字符串，
 * 和 Java 里的 List<String> 互相转换。
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    /** 多个标签之间用英文逗号分隔 */
    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        List<String> cleaned = new ArrayList<>();
        for (String item : attribute) {
            if (item != null && !item.isBlank()) {
                cleaned.add(item.trim());
            }
        }
        return cleaned.isEmpty() ? null : String.join(SPLIT_CHAR, cleaned);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String item : Arrays.asList(dbData.split(SPLIT_CHAR))) {
            if (!item.isBlank()) {
                result.add(item.trim());
            }
        }
        return result;
    }
}
