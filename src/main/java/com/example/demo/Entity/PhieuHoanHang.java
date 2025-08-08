package com.example.demo.Entity;

import com.example.demo.Enum.TrangThaiPhieuHoan;
import com.example.demo.Enum.TrangThaiThanhToan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "phieu_hoan_hang")
public class PhieuHoanHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "ngay_hoan")
    private LocalDateTime ngayHoan;
    @Column(name = "tong_tien_hoan")
    private BigDecimal tongTienHoan;
    @Column(name = "loai_hoan")
    private String loaiHoan;
    @Column(name = "ly_do")
    private String lyDo;
    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;
    @Column(name = "trang_thai")
    @Enumerated(EnumType.STRING)
    private TrangThaiPhieuHoan trangThai; // CHO_DUYET, DA_DUYET, TU_CHOI

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_thanh_toan")
    private TrangThaiThanhToan trangThaiThanhToan; // CHUA_HOAN, DANG_HOAN, DA_HOAN
    @Column(name = "phuong_thuc_hoan")
    private String phuongThucHoan;
    @Column(name = "ngay_hoan_tien")
    private LocalDateTime ngayHoanTien;
    @Column(name = "ten_ngan_hang")
    private String tenNganHang;
    @Column(name = "so_tai_khoan")
    private String soTaiKhoan;
    @Column(name = "chu_tai_khoan")
    private String chuTaiKhoan;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", referencedColumnName = "id")
    private HoaDon hoaDon;

    @OneToMany(mappedBy = "phieuHoanHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietHoanHang> chiTietHoanHangs = new ArrayList<>();
}
