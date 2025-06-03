package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties({"sanPhams"})
@Table(name = "Khuyen_mai")
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khuyen_mai", nullable = false, unique = true, length = 50)
    private String maKhuyenMai;
    @Column(name = "so_luong")
    private Integer soLuong;
    @Column(name = "gia_tri_giam")
    private BigDecimal giaTriGiam;
    @Column(name = "gia_tri_toi_da")
    private BigDecimal giaTriToiDa;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;
    @Column(name = "phan_tram_giam")
    private Integer phanTramGiam;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_bat_dau")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayBatDau;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_ket_thuc")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayKetThuc;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @OneToMany(mappedBy = "khuyenMai", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Quản lý phía cha
    private List<SanPham> sanPhams;

}
