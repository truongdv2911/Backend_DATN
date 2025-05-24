package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private String ten_san_pham;

    @Column(nullable = false, length = 200)
    private String ma_san_pham;

    private Integer do_tuoi;

    @Column(columnDefinition = "TEXT")
    private String mo_ta;

    private Double gia;

    private Double gia_khuyen_mai;

    private Integer so_luong;

    private Integer so_luong_manh_ghep;

    private Integer so_luong_ton;

    @Column(length = 255)
    private String anh_dai_dien;

    private Integer so_luong_vote;

    private Double danh_gia_trung_binh;

    @Temporal(TemporalType.TIMESTAMP)
    private Date ngay_tao = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date ngay_sua = new Date();

    @ManyToOne
    @JsonIgnore // Bỏ qua khi tuần tự hóa
    @JoinColumn(name = "khuyen_mai_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_KhuyenMai"))
    private KhuyenMai khuyenMai;

    @ManyToOne
    @JoinColumn(name = "danh_muc_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_DanhMuc"))
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "bo_suu_tap_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_SanPham_BoSuuTap"))
    private BoSuuTap boSuuTap;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnhSp> anhSps;

}
