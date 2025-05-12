package com.example.demo.DTOs;

import jakarta.persistence.Column;

public class Anh_sp_DTO {
    private Integer id;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    private Integer thuTu;

    private Boolean anhChinh;
}
