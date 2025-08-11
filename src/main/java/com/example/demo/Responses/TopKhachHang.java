package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@AllArgsConstructor
@Data
@NoArgsConstructor
public class TopKhachHang {
    private Integer id;
    private String ten;
    private Integer soDon;
    private BigDecimal tongTien;
}
