package com.example.demo.Responses;

import com.example.demo.Entity.GioHang;
import com.example.demo.Entity.SanPham;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GioHangChiTietResponse {
    private Integer id;
    private Integer gioHangId;
    private SanPhamResponseDTO sanPham;
    private BigDecimal gia;
    private  BigDecimal tongTien;
    private Integer soLuong;
}
