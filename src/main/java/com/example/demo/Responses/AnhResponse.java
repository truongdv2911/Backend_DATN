package com.example.demo.Responses;

import com.example.demo.Entity.AnhSp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnhResponse {
    private Integer id;
    private String url;
    private Boolean anhChinh;

    public static AnhResponse fromEntity(AnhSp entity) {
        if (entity == null) return null;
        return new AnhResponse(
                entity.getId(),
                entity.getUrl(),
                entity.getAnhChinh() != null ? true : false
        );
    }
}
