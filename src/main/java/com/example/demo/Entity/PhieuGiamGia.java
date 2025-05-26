package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String ma_phieu;

    private Integer so_luong;

    @Column(length = 20)
    private String loai_phieu_giam;

    private BigDecimal gia_tri_giam;

    private BigDecimal giam_toi_da;

    private BigDecimal gia_tri_toi_thieu;

    @Temporal(TemporalType.DATE)
    private Date ngay_bat_dau;

    @Temporal(TemporalType.DATE)
    private Date ngay_ket_thuc;

    @Column(nullable = false, length = 20)
    private String trang_thai;

}
