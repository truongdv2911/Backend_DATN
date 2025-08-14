package com.example.demo.DTOs;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HoaDonEmailDTO {
    private Integer idHD;
    private String toEmail;
    private String tenKH;
    private String maHD;
    private String ngayTao;
    private String diaChi;
    private String pttt;
    private String ptvc;
    private List<SanPhamHoaDonEmail> listSp;
    private BigDecimal totalAmount;
    private BigDecimal phiShip;
    private BigDecimal tienGiam;
}
