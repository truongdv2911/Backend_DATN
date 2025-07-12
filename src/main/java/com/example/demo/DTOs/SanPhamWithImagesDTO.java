package com.example.demo.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SanPhamWithImagesDTO {
    @Valid
    @NotNull(message = "Thông tin sản phẩm không được để trống")
    SanPhamUpdateDTO sanPham;

    @Valid
    @NotEmpty(message = "Danh sách ảnh không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 ảnh")
    List<Anh_sp_DTO> danhSachAnh;
} 