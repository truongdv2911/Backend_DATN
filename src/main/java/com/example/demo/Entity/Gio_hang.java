package com.example.demo.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Gio_hang")
public class Gio_hang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double soTienGiam;

    private Double tongTien;

    @Column(nullable = false, length = 20)
    private String trangThai;

//    @ManyToOne
//    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_GioHang_User"))
//    private User user;

    @ManyToOne
    @JoinColumn(name = "id_phieu_giam", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_GioHang_PhieuGiamGia"))
    private PhieuGiamGia phieuGiamGia;

    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gio_hang_chi_tiet> gioHangChiTiets;


}
