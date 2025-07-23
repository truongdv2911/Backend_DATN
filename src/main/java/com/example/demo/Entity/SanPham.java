package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
@Table(name = "San_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Integer id;

    @Column(name = "ten_san_pham", nullable = false, length = 200)
     String tenSanPham;

    @Column(name = "ma_san_pham", nullable = false, length = 200)
     String maSanPham;
    @Column(name = "do_tuoi")
     Integer doTuoi;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
     String moTa;
    @Column(name = "gia")
     BigDecimal gia;

    @Column(name = "so_luong_manh_ghep")
     Integer soLuongManhGhep;
    @Column(name = "so_luong_ton")
     Integer soLuongTon;

    @Column(name = "anh_dai_dien", length = 255)
     String anhDaiDien;
    @Column(name = "so_luong_vote")
     Integer soLuongVote;
    @Column(name = "danh_gia_trung_binh")
     Double danhGiaTrungBinh;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngay_tao")
     Date ngayTao = new Date();
    @Column(name = "ngay_sua")
    @Temporal(TemporalType.TIMESTAMP)
     Date ngaySua = new Date();

    @ManyToOne
    @JoinColumn(name = "danh_muc_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_DanhMuc"))
     DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "bo_suu_tap_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_BoSuuTap"))
     BoSuuTap boSuuTap;

    @ManyToOne
    @JoinColumn(name = "xuat_xu_id", referencedColumnName = "id")
    XuatXu xuatXu;

    @ManyToOne
    @JoinColumn(name = "thuong_hieu_id", referencedColumnName = "id")
    ThuongHieu thuongHieu;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sanPham")
    List<AnhSp> anhSps;
    @Column(name = "trang_thai")
     String trangThai;
    @Column(name = "is_noi_bat")
    Integer noiBat;

}
