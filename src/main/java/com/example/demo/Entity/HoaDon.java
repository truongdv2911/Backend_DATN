package com.example.demo.Entity;

import com.example.demo.DTOs.CartItemDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "tam_tinh")
    private BigDecimal tamTinh;
    @Column(name = "tong_tien")
    private BigDecimal tongTien;
    @Column(name = "so_tien_giam")
    private BigDecimal soTienGiam;
    @Column(name = "dia_chi_giao_hang")
    private String DiaChiGiaoHang;
    @Column(name = "ma_van_chuyen")
    private String maVanChuyen;
    @Column(name = "ngay_giao_hang_du_kien")
    private Date ngayGiao;
    @Column(name = "ngay_lap")
    private LocalDateTime ngayTao;
    @Column(name = "trang_thai")
    private String trangThai;
    @Column(name = "phuong_thuc_thanh_toan")
    private String phuongThucThanhToan;
    @Column(name = "so_dien_thoai")
    private String sdt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "nv_id", referencedColumnName = "id")
    private User nv;

    @ManyToOne
    @JoinColumn(name = "id_phieu_khuyen_mai", referencedColumnName = "id")
    private PhieuGiamGia phieuGiamGia;
}
