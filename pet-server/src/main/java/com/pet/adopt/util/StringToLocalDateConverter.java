package com.pet.adopt.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 查询参数日期转换：兼容 "yyyy-MM-dd"、"yyyy-MM-dd HH:mm:ss"、"yyyy-MM-dd'T'HH:mm:ss"
 * Spring Boot 会自动把该 Converter 注册到 MVC 的 ConversionService 中
 */
@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter SPACE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter T_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public LocalDate convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        String value = source.trim();
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
