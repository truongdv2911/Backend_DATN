package com.example.demo.Responses;

import com.example.demo.Entity.HoaDon;
import com.example.demo.Entity.SanPham;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class HoaDonChiTietResponse {
    private Integer id;
    private Integer soLuong;
    private BigDecimal gia;
    private BigDecimal tongTien;

    private Integer hdId;

    private Integer spId;
}
