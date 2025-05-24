package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Anh_sp")
public class Anh_sp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "san_pham_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_AnhSp_SanPham"))
    private SanPham sanPham;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    private Integer thuTu;

    private Boolean anhChinh;
}
