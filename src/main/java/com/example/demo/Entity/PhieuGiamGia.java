package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

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
    private String maPhieu;

    private Integer soLuong;

    @Column(length = 20)
    private String loaiPhieuGiam;

    private Double giaTriGiam;

    private Double giamToiDa;

    private Double giaTriToiThieu;

    @Temporal(TemporalType.DATE)
    private Date ngayBatDau;

    @Temporal(TemporalType.DATE)
    private Date ngayKetThuc;

    @Column(nullable = false, length = 20)
    private String trangThai;

}
