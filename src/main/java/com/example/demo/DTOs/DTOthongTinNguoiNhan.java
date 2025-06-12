package com.example.demo.DTOs;

import com.example.demo.Entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOthongTinNguoiNhan extends BaseEntity {
    @NotEmpty(message = "không để trống tên người nhận")
    @Size(min = 6, max = 50, message = "Username phải từ 6 đến 50 ký tự")
    @Pattern(regexp = "^[\\p{L}\\p{N} ]+$", message = "Tên không được chứa ký tự đặc biệt")
    private String hoTen;
    @NotEmpty(message = "không để trống số điện thoại người nhận")
    @Pattern(regexp = "\\d{10}", message = "Sai dinh dang sdt")
    private String sdt;
    @NotEmpty(message = "không để trống đường")
    private String duong;
    @NotEmpty(message = "không để trống xã")
    private String xa;
    @NotEmpty(message = "không để trống huyện")
    private String huyen;
    @NotEmpty(message = "không để trống thành phố")
    private String thanhPho;
    private LocalDateTime ngayTao;
    @Min(0)
    @Max(1)
    private Integer isMacDinh;
    @NotNull(message = "ID người dùng không được để trống")
    @Min(value = 0, message = "ID người dùng phải là số tự nhiên")
    @Digits(integer = 10, fraction = 0, message = "ID người dùng phải là số nguyên")
    private Integer idUser;
}
