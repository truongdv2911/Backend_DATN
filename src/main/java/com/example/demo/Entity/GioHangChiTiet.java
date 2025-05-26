package com.example.demo.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Gio_hang_chi_tiet")
public class GioHangChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "gio_hang_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_GioHangChiTiet_GioHang"))
    private GioHang gioHang;

    @ManyToOne
    @JoinColumn(name = "san_pham_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_GioHangChiTiet_SanPham"))
    private SanPham sanPham;

    private BigDecimal gia;

    private  BigDecimal tong_tien;

    private Integer so_luong;

}
