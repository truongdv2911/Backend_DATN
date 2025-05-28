package com.example.demo.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Gio_hang")
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "so_tien_giam")
    private BigDecimal soTienGiam;
    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_GioHang_User"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_phieu_giam", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_GioHang_PhieuGiamGia"))
    private PhieuGiamGia phieuGiamGia;

    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GioHangChiTiet> gioHangChiTiets;
}
