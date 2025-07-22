package com.example.demo.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class HoaDonEmailDTO {
    private String toEmail;
    private String tenKH;
    private String maHD;
    private String ngayTao;
    private String diaChi;
    private String pttt;
    private String ptvc;
    private List<SanPhamHoaDonEmail> listSp;
    private String totalAmount;
}
