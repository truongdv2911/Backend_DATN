package com.example.demo.DTOs;

import com.example.demo.Entity.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOuser {
    @NotEmpty(message = "không để trống tên")
    @Size(min = 6, max = 50, message = "Username phải từ 6 đến 50 ký tự")
    @Pattern(regexp = "^[\\p{L}\\p{N} ]+$", message = "Tên không được chứa ký tự đặc biệt")
    private String ten;
    @NotEmpty(message = "khong de trong email")
    @Email
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email sai định dạng")
    private String email;
    @NotEmpty(message = "Khong de trong mat khau")
    @Size(min = 6, max = 30, message = "Mật khẩu phải từ 6 đến 30 ký tự")
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{6,}$",
            message = "Mật khẩu phải có ít nhất 1 ký tự đặc biệt hoặc sai định dạng"
    )
    private String matKhau;
    @Pattern(regexp = "\\d{10}", message = "Sai dinh dang sdt")
    private String sdt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate ngaySinh;
    private LocalDateTime ngayTao;
    private String diaChi;
    private Integer trangThai;

    private String facebookId;
    private String googleId;
    private Integer role_id;
}
