package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_hoan_hang")
public class VidPhieuHoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String url;
    @Column(name = "mo_ta")
    private String mota;

    @ManyToOne
    @JoinColumn(name = "id_phieu_hoan", referencedColumnName = "id")
    private PhieuHoanHang hoanHang;
}
