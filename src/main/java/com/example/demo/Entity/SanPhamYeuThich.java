package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "San_pham_yeu_thich")
public class SanPhamYeuThich{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "wlist_id", referencedColumnName = "id")
    private WishList wishList;

    @ManyToOne
    @JoinColumn(name = "sp_id", referencedColumnName = "id")
    private SanPham sanPham;
}
