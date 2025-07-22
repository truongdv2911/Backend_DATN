package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Users")
public class User extends BaseEntity implements UserDetails {
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
    private LocalDate ngaySinh;
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

    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_expiration_time")
    private LocalDateTime otpExpirationTime;

    public User(Integer id, String ten, String email, String matKhau, String sdt, LocalDate ngaySinh, String diaChi, int trangThai, String facebookId, String googleId, Role role) {
        this.id = id;
        this.ten = ten;
        this.email = email;
        this.matKhau = matKhau;
        this.sdt = sdt;
        this.ngaySinh = ngaySinh;
        this.diaChi = diaChi;
        this.trangThai = trangThai;
        this.facebookId = facebookId;
        this.googleId = googleId;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + getRole().getName()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return matKhau;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
