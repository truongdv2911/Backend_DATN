package com.example.demo.Responses;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class SanPhamKMResponse {
    private Integer id;
    private String tenSanPham;
    private String maSanPham;
    private Integer doTuoi;
    private String moTa;
    private BigDecimal gia;
    private Integer soLuongManhGhep;
    private Integer soLuongTon;
    private Integer soLuongVote;
    private Double danhGiaTrungBinh;
    private Integer idDanhMuc;
    private Integer idBoSuuTap;
    private String trangThai;
    private BigDecimal GiaKhuyenMai;
    private Double PhanTramKhuyenMai;
}
