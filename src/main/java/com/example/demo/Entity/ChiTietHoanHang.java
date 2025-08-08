package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Table(name = "chi_tiet_hoan_hang")
public class ChiTietHoanHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "so_luong_hoan")
    private Integer soLuongHoan;
    @Column(name = "gia_hoan")
    private BigDecimal giaHoan;
    @Column(name = "tong_gia_hoan")
    private BigDecimal tongGiaHoan;

    @ManyToOne
    @JoinColumn(name = "id_phieu_hoan_hang", referencedColumnName = "id")
    private PhieuHoanHang phieuHoanHang;

    @ManyToOne
    @JoinColumn(name = "id_san_pham", referencedColumnName = "id")
    private SanPham sanPham;
}
