package com.example.demo.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SearchRequestDTO {
    private String ten;
    private String doTuoi;
    private BigDecimal gia;
    private String xuatXu;
    private String thuongHieu;
    private String boSuuTap;
    private Integer danhGiaToiThieu;
    private Integer soLuongManhGhepMin;
}
