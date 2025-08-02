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
@Table(name = "Anh_feedback")
public class AnhDanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String url;
    private String mota;
    @ManyToOne
    @JoinColumn(name = "danh_gia_id", referencedColumnName = "id")
    private DanhGia danhGia;
}
