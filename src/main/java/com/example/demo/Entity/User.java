package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Users")
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String ten;
    @Column(unique = true)
    private String email;
    @Column(name = "mat_khau")
    private String matKhau;
    @Column(name = "so_dien_thoai", unique = true)
    private String sdt;
    @Column(name = "ngay_sinh")
    private Date ngaySinh;
    @Column(name = "dia_chi")
    private String diaChi;
    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "facebook_id")
    private String facebookId;
    @Column(name = "google_id")
    private String googleId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;

}
