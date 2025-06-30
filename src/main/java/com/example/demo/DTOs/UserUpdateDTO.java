package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    @NotEmpty(message = "không để trống tên")
    @Size(min = 6, max = 50, message = "Username phải từ 6 đến 50 ký tự")
    @Pattern(regexp = "^[\\p{L}\\p{N} ]+$", message = "Tên không được chứa ký tự đặc biệt")
    private String ten;
    @NotEmpty(message = "khong de trong email")
    @Email
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email sai định dạng")
    private String email;
    @Pattern(regexp = "\\d{10}", message = "Sai dinh dang sdt")
    private String sdt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate ngaySinh;
    @NotEmpty(message = "Không để trống địa chỉ")
    private String diaChi;
    @NotNull(message = "Không để trống trạng thái")
    private Integer trangThai;
    @NotNull(message = "Không để trống role")
    private Integer role_id;
}
