package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class PhieuGiamGiaDTO {
//    @NotBlank(message = "Mã phiếu không được để trống")
    @Size(max = 50, message = "Mã phiếu không được vượt quá 50 ký tự")
     String maPhieu;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
     Integer soLuong;

    @NotBlank(message = "Loại phiếu giảm không được để trống")
    @Size(max = 20, message = "Loại phiếu giảm không được vượt quá 20 ký tự")
     String loaiPhieuGiam;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
     BigDecimal giaTriGiam;

    @DecimalMin(value = "0.0", message = "Giảm tối đa phải lớn hơn hoặc bằng 0")
     BigDecimal giamToiDa;

    @DecimalMin(value = "0.0", message = "Giá trị tối thiểu phải lớn hơn hoặc bằng 0")
     BigDecimal giaTriToiThieu;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu ít nhất từ bây giờ")
     LocalDate ngayBatDau;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc ít nhất phải ngày mai")
    LocalDate ngayKetThuc;

//    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 20, message = "Trạng thái không được vượt quá 20 ký tự")
     String trangThai;
}
