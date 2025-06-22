package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "khuyenMai_sanPham",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_san_pham", "id_khuyen_mai"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhuyenMaiSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_khuyen_mai", nullable = false)
    private KhuyenMai khuyenMai;

    @Column(name = "gia_khuyen_mai", nullable = false)
    private BigDecimal giaKhuyenMai;
}
