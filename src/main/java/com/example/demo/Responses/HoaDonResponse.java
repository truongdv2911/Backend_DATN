package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonResponse {
    private Integer id;
    private BigDecimal tamTinh;
    private BigDecimal tongTien;
    private BigDecimal soTienGiam;
    private String diaChiGiaoHang;
    private String maVanChuyen;
    private LocalDate ngayGiao;
    private LocalDateTime ngayTao;
    private String trangThai;
    private String phuongThucThanhToan;

    private Integer userId;

    private String ten;
    private String sdt;

    private Integer nvId;
    private String nvName;

    private Integer PGGid;
    private String maPGG;
}
