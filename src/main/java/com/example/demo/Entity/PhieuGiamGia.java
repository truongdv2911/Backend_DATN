package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 50)
    private String maPhieu;
    @Column(name = "ten_phieu")
    private String tenPhieu;
    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "loai_phieu_giam", length = 20)
    private String loaiPhieuGiam;
    @Column(name = "gia_tri_giam")
    private BigDecimal giaTriGiam;
    @Column(name = "giam_toi_da")
    private BigDecimal giamToiDa;
    @Column(name = "gia_tri_toi_thieu")
    private BigDecimal giaTriToiThieu;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "is_noi_bat")
    private Integer noiBat;

}
