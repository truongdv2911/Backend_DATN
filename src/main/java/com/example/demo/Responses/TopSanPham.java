package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSanPham {
    private Integer id;
    private String tenSanPham;
    private Integer soLuongBan;
    private BigDecimal doanhThu;
}
