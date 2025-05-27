package com.example.demo.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class PhieuGiamGiaDTO {
    @NotBlank(message = "Mã phiếu không được để trống")
    @Size(max = 50, message = "Mã phiếu không được vượt quá 50 ký tự")
     String ma_phieu;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
     Integer so_luong;

    @NotBlank(message = "Loại phiếu giảm không được để trống")
    @Size(max = 20, message = "Loại phiếu giảm không được vượt quá 20 ký tự")
     String loai_phieu_giam;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
     BigDecimal gia_tri_giam;

    @DecimalMin(value = "0.0", message = "Giảm tối đa phải lớn hơn hoặc bằng 0")
     BigDecimal giam_toi_da;

    @DecimalMin(value = "0.0", message = "Giá trị tối thiểu phải lớn hơn hoặc bằng 0")
     BigDecimal gia_tri_toi_thieu;

    @NotNull(message = "Ngày bắt đầu không được để trống")
     Date ngay_bat_dau;

    @NotNull(message = "Ngày kết thúc không được để trống")
     Date ngay_ket_thuc;

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 20, message = "Trạng thái không được vượt quá 20 ký tự")
     String trang_thai;
}
