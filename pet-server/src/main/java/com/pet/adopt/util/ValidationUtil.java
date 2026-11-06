package com.pet.adopt.util;

import com.pet.adopt.exception.BusinessException;
import org.springframework.util.StringUtils;

/**
 * 入参长度校验工具
 *
 * 数据库里不少字段是 VARCHAR(n)，前端若绕过表单限制直接调接口提交超长文本，
 * MySQL 会抛 SQL 异常，最终只会返回一句「系统异常，请稍后重试」，
 * 用户既看不懂也不知道是哪一项填错了。这里在进库前按字段实际长度拦下来，
 * 统一返回「XX 长度不能超过 N 个字符」这样的可读提示。
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * 校验字符串长度
     *
     * @param value 待校验的值，为空时直接放过（必填校验交给各模块自己处理）
     * @param max   允许的最大长度
     * @param field 字段中文名，用于拼提示语
     */
    public static void maxLength(String value, int max, String field) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (value.trim().length() > max) {
            // 用 400 区分「参数不合法」和真正的系统异常(500)，前端两种都会弹提示
            throw new BusinessException(400, field + "长度不能超过 " + max + " 个字符");
        }
    }
}
