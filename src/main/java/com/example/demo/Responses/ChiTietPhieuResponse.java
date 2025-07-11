package com.example.demo.Responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChiTietPhieuResponse {
    private Integer id;
    private String maPhieu;
    private String tenPhieu;
    private BigDecimal giaTriGiam;
    private Integer soLuotSuDung;
    private Integer soNguoiSuDung;
    private BigDecimal TongTienBanDuoc;
    private BigDecimal TongTienGiam;
    private BigDecimal TongTienTruocGiam;
}
