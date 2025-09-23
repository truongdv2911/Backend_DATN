package com.example.demo.Responses;

import com.example.demo.Entity.SanPham;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamResponse2 {
    private Integer id;
    private String tenSanPham;
    private String maSanPham;
    private Integer doTuoi;
    private String moTa;
    private BigDecimal gia;
    private BigDecimal giaKM;
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

    public static SanPhamResponse2 fromEntity(SanPham sp, List<AnhResponse> anhResponses) {
        if (sp == null) return null;

        return new SanPhamResponse2(
                sp.getId(),
                sp.getTenSanPham(),
                sp.getMaSanPham(),
                sp.getDoTuoi(),
                sp.getMoTa(),
                sp.getGia(),
                sp.getGiaKM(),
                sp.getSoLuongManhGhep(),
                sp.getSoLuongTon(),
                sp.getSoLuongVote(),
                sp.getDanhGiaTrungBinh(),
                sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null,
                sp.getBoSuuTap() != null ? sp.getBoSuuTap().getId() : null,
                sp.getXuatXu() != null ? sp.getXuatXu().getId() : null,
                sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null,
                sp.getNoiBat(),
                sp.getTrangThai(),
                anhResponses
        );
    }
}
