package com.pet.adopt.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * LocalDate 反序列化：兼容纯日期与带时间的字符串
 * 支持："yyyy-MM-dd"、"yyyy-MM-dd HH:mm:ss"、"yyyy-MM-dd'T'HH:mm:ss"
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter SPACE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter T_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(p.getLongValue()),
                    ZoneId.systemDefault()).toLocalDate();
        }
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String value = text.trim();
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (Exception ignored) {
            // 继续尝试带时间的格式
        }
        try {
            return LocalDate.parse(value, SPACE_FORMAT);
        } catch (Exception ignored) {
            // 继续尝试带时间的格式
        }
        try {
            return LocalDate.parse(value, T_FORMAT);
        } catch (Exception ignored) {
            // 继续尝试标准 ISO 解析
        }
        return LocalDate.parse(value);
    }
}
