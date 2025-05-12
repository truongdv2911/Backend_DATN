package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Khuyen_mai")
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String maKhuyenMai;

    private Integer soLuong;

    private Double giaTriGiam;

    private Double giaTriToiDa;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    private Integer phanTramGiam;

    @Temporal(TemporalType.DATE)
    private Date ngayBatDau;

    @Temporal(TemporalType.DATE)
    private Date ngayKetThuc;

    @Column(nullable = false, length = 20)
    private String trangThai;

    @OneToMany(mappedBy = "khuyenMai", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SanPham> sanPhams;

}
