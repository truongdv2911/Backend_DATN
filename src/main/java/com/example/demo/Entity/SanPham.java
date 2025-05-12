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
@Table(name = "San_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String tenSanPham;

    @Column(nullable = false, length = 200)
    private String maSanPham;

    private Integer doTuoi;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    private Double gia;

    private Double giaKhuyenMai;

    private Integer soLuong;

    private Integer soLuongManhGhep;

    private Integer soLuongTon;

    @Column(length = 255)
    private String anhDaiDien;

    private Integer soLuongVote;

    private Double danhGiaTrungBinh;

    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date ngaySua = new Date();

    @ManyToOne
    @JoinColumn(name = "khuyen_mai_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_KhuyenMai"))
    private KhuyenMai khuyenMai;

    @ManyToOne
    @JoinColumn(name = "danh_muc_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_DanhMuc"))
    private Danh_muc danhMuc;

    @ManyToOne
    @JoinColumn(name = "bo_suu_tap_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_BoSuuTap"))
    private Bo_suu_tap boSuuTap;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Anh_sp> anhSps;

}
