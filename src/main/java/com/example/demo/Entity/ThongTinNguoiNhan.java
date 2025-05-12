package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "thong_tin_nguoi_nhan")
public class ThongTinNguoiNhan extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "ten_nguoi_nhan")
    private String hoTen;
    @Column(name = "so_dien_thoai")
    private String sdt;
    private String duong;
    private String xa;
    private String huyen;
    @Column(name = "thanh_pho")
    private String thanhPho;
    @Column(name = "mac_dinh")
    private Integer isMacDinh;
}
