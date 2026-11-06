package com.pet.adopt.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime 反序列化：兼容纯日期、"yyyy-MM-dd HH:mm:ss" 与 ISO-8601("yyyy-MM-dd'T'HH:mm:ss")
 * 纯日期会自动补足 00:00:00
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter SPACE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter T_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(p.getLongValue()),
                    java.time.ZoneId.systemDefault());
        }
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String value = text.trim();
        try {
            // 纯日期自动补足 00:00:00
            return LocalDate.parse(value, DATE_FORMAT).atStartOfDay();
        } catch (Exception ignored) {
            // 继续尝试带时间的格式
        }
        try {
            return LocalDateTime.parse(value, SPACE_FORMAT);
        } catch (Exception ignored) {
            // 继续尝试 ISO 格式
        }
        try {
            return LocalDateTime.parse(value, T_FORMAT);
        } catch (Exception ignored) {
            // 继续尝试标准 ISO 解析
        }
        return LocalDateTime.parse(value);
    }
}
