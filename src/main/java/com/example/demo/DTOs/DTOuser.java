package com.example.demo.DTOs;

import com.example.demo.Entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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
public class DTOuser {
    @NotEmpty(message = "không để trống tên")
    private String ten;
    @NotEmpty(message = "khong de trong email")
    @Email
    private String email;
    @NotEmpty(message = "Khong de trong mat khau")
    private String matKhau;
    private String sdt;
    private Date ngaySinh;
    private LocalDateTime ngayTao;
    private String diaChi;
    private Integer trangThai;

    private String facebookId;
    private String googleId;
    private Integer role_id;
}
