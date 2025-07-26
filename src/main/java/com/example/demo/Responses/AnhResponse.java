package com.example.demo.Responses;

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
}
