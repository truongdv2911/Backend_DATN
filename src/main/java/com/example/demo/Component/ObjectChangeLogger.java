package com.example.demo.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ObjectChangeLogger {

    private static boolean areEqual(Object oldValue, Object newValue) {
        if (oldValue instanceof BigDecimal && newValue instanceof BigDecimal) {
            return ((BigDecimal) oldValue).compareTo((BigDecimal) newValue) == 0;
        }
        if (oldValue instanceof Number && newValue instanceof Number) {
            return new BigDecimal(oldValue.toString()).compareTo(new BigDecimal(newValue.toString())) == 0;
        }
        return oldValue.equals(newValue);
    }

    private static Field findFieldIfExists(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static String formatValue(Object value) {
        if (value == null) return "null";

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }

        if (value instanceof Double || value instanceof Float) {
            return BigDecimal.valueOf(((Number) value).doubleValue())
                    .stripTrailingZeros().toPlainString();
        }

        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        return value.toString();
    }

    public static String generateChangeLog(Object oldObj, Object newObj) {
        if (oldObj == null || newObj == null) return "Không có đủ dữ liệu để so sánh";
        StringBuilder sb = new StringBuilder();
        Field[] oldFields = oldObj.getClass().getDeclaredFields();

        for (Field oldField : oldFields) {
            oldField.setAccessible(true);
            try {
                Object oldValue = oldField.get(oldObj);

                Field newField = findFieldIfExists(newObj.getClass(), oldField.getName());
                if (newField == null) continue;
                newField.setAccessible(true);
                Object newValue = newField.get(newObj);
                if (newValue == null) continue;

                if (oldValue == null && newValue == null) continue;
                if (oldValue == null || newValue == null || !areEqual(oldValue, newValue)) {
                    sb.append(oldField.getName())
                            .append(": [").append(formatValue(oldValue))
                            .append("] -> [").append(formatValue(newValue))
                            .append("]; ");
                }
            } catch (Exception ignored) {}
        }

        return sb.length() > 0 ? sb.toString() : "Không có thay đổi";
    }
}
