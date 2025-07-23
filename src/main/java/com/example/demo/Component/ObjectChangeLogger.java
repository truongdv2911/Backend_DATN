package com.example.demo.Component;

import java.lang.reflect.Field;

public class ObjectChangeLogger {
    /**
     * So sánh 2 object (entity cũ và DTO mới) và sinh chuỗi log các trường đã thay đổi.
     * @param oldObj object cũ (entity)
     * @param newObj object mới (DTO)
     * @return Chuỗi mô tả các trường đã thay đổi
     */
    public static String generateChangeLog(Object oldObj, Object newObj) {
        if (oldObj == null || newObj == null) return "Không có đủ dữ liệu để so sánh";
        StringBuilder sb = new StringBuilder();
        Field[] oldFields = oldObj.getClass().getDeclaredFields();

        for (Field oldField : oldFields) {
            oldField.setAccessible(true);
            try {
                Object oldValue = oldField.get(oldObj);

                // Tìm field cùng tên ở object mới
                Field newField;
                try {
                    newField = newObj.getClass().getDeclaredField(oldField.getName());
                } catch (NoSuchFieldException e) {
                    continue; // Nếu không có field cùng tên thì bỏ qua
                }
                newField.setAccessible(true);
                Object newValue = newField.get(newObj);

                // So sánh giá trị
                if (oldValue == null && newValue == null) continue;
                if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
                    sb.append(oldField.getName())
                      .append(": [").append(oldValue).append("] -> [").append(newValue).append("]; ");
                }
            } catch (Exception ignored) {}
        }
        return sb.length() > 0 ? sb.toString() : "Không có thay đổi";
    }
} 