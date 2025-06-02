package com.example.demo.Responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SanPhamResponseDTO {
    private Integer id;
    private String tenSanPham;
    private String maSanPham;
    private Integer doTuoi;
    private String moTa;
    private BigDecimal gia;
    private BigDecimal giaKhuyenMai;
    private Integer soLuong;
    private Integer soLuongManhGhep;
    private Integer soLuongTon;
    private String anhDaiDien;
    private Integer soLuongVote;
    private Double danhGiaTrungBinh;
    private Integer danhMucId;
    private Integer boSuuTapId;
    private Integer khuyenMaiId;
    private String trangThai;
}
