package com.example.demo.Responses;

import com.example.demo.Entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonResponse {
    private Integer id;
    private String maHD;
    private Integer loaiHD;
    private BigDecimal tamTinh;
    private BigDecimal tongTien;
    private BigDecimal soTienGiam;
    private String DiaChiGiaoHang;
    private String maVanChuyen;
    private LocalDateTime ngayGiao;
    private LocalDateTime ngayTao;
    private String trangThai;
    private String phuongThucThanhToan;
    private String sdt1;

    private Integer userId;
    private String ten;
    private String sdt;

    private Integer nvId;
    private String nvName;
    private Integer idPhieuGiam;
    private String maPGG;
    private String qrCodeUrl;
}
