package com.example.demo.Responses;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SanPhamResponseDTO {
    private Integer id;
    private String tenSanPham;
    private String maSanPham;
    private Integer doTuoi;
    private String moTa;
    private BigDecimal gia;
    private BigDecimal giaKhuyenMai;
    private Integer soLuongManhGhep;
    private Integer soLuongTon;
    private String anhDaiDien;
    private Integer soLuongVote;
    private Double danhGiaTrungBinh;
    private Integer idDanhMuc;
    private Integer idBoSuuTap;
    private Integer khuyenMaiId;
    private String trangThai;
    private List<String> anhUrls;
}
