package com.example.demo.Responses;

import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.SanPham;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KhuyenMaiSPResponse {
    private Integer sanPhamId;

    private Integer khuyenMaiId;

    private BigDecimal giaKhuyenMai;
}
