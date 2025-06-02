package com.example.demo.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class GioHangDTO {

    @NotNull(message = "Số tiền giảm không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền giảm phải lớn hơn hoặc bằng 0")
    BigDecimal soTienGiam;

    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền phải lớn hơn hoặc bằng 0")
     BigDecimal tongTien;

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 20, message = "Trạng thái không được vượt quá 20 ký tự")
     String trangThai;

    @NotNull(message = "User ID không được để trống")
     Integer userId;

     Integer phieuGiamGiaId;
}
