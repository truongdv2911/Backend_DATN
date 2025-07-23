package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"sanPhams"})
@Table(name = "Thuong_hieu")
public class ThuongHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_thuong_hieu")
    private String ten;
    @Column(name = "mo_ta")
    private String moTa;

    @OneToMany(mappedBy = "thuongHieu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SanPham> sanPhams;
}
