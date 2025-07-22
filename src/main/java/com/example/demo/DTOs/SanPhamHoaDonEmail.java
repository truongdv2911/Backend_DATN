package com.example.demo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamHoaDonEmail {
    private String ten;
    private String ma;
    private BigDecimal gia;
    private int soLuong;
    private BigDecimal tongTien;
}
