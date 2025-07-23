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
    private Integer soLuongManhGhep;
    private Integer soLuongTon;
    private Integer soLuongVote;
    private Double danhGiaTrungBinh;
    private Integer danhMucId;
    private Integer boSuuTapId;
    private Integer xuatXuId;
    private Integer ThuongHieuId;
    private Integer noiBat;
    private String trangThai;
    private List<AnhResponse> anhUrls;
}
