package com.example.demo.DTOs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class KhuyenMaiDTO {


    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    Integer soLuong;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
     BigDecimal giaTriGiam;

    @DecimalMin(value = "0.0", message = "Giá trị tối đa phải lớn hơn hoặc bằng 0")
     BigDecimal giaTriToiDa;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
     String moTa;

    @NotNull(message = "Phần trăm giảm không được để trống")
    @Min(value = 0, message = "Phần trăm giảm phải lớn hơn hoặc bằng 0")
    @Max(value = 100, message = "Phần trăm giảm không được vượt quá 100")
     Integer phanTramGiam;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu ít nhất từ bây giờ")
    LocalDate ngayBatDau;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày kết thúc ít nhất phải ngày mai")
    LocalDate ngayKetThuc;

    String trangThai;

}
