package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
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

    @Column(nullable = false, unique = true, length = 50)
    private String ma_khuyen_mai;

    private Integer so_luong;

    private BigDecimal gia_tri_giam;

    private BigDecimal gia_tri_toi_da;

    @Column(columnDefinition = "TEXT")
    private String mo_ta;

    private Integer phan_tram_giam;

    @Temporal(TemporalType.DATE)
    private Date ngay_bat_dau;

    @Temporal(TemporalType.DATE)
    private Date ngay_ket_thuc;

    @Column(nullable = false, length = 20)
    private String trang_thai;

    @OneToMany(mappedBy = "khuyenMai", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Quản lý phía cha
    private List<SanPham> sanPhams;

}
