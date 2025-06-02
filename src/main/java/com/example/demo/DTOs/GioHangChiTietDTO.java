package com.example.demo.DTOs;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class GioHangChiTietDTO {

    @NotNull(message = "GioHang ID không được để trống")
     Integer gioHangId;

    @NotNull(message = "SanPham ID không được để trống")
     Integer sanPhamId;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", message = "Giá phải lớn hơn hoặc bằng 0")
     BigDecimal gia;

    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền phải lớn hơn hoặc bằng 0")
     BigDecimal tongTien;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
     Integer soLuong;
}
