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
@Table(name = "Xuat_xu")
public class XuatXu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "ten_xuat_xu")
    private String ten;
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "is_delete")
    private Integer isDelete;

    @OneToMany(mappedBy = "xuatXu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SanPham> sanPhams;
}
