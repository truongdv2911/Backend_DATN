package com.example.demo.DTOs;

import lombok.Data;

@Data
public class SearchRequestDTO {
    private String ten;
    private String doTuoi;
    private String gia;
    private String xuatXu;
    private String thuongHieu;
    private String boSuuTap;
    private Integer danhGiaToiThieu;
    private Integer soLuongManhGhepMin;
}
