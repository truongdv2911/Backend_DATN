package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KhuyenMaiHieuQuaDTO {
    private Integer idKhuyenMai;
    private String tenKhuyenMai;
    private Integer soDonApDung;
    private BigDecimal tongDoanhThuGoc;
    private BigDecimal tongDoanhThuSauGiam;
    private BigDecimal tongTienGiam;

}
