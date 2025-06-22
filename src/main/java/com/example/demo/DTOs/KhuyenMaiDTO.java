package com.example.demo.DTOs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
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

    private String maKhuyenMai;

    private String tenKhuyenMai;

    @NotNull(message = "Phần trăm giảm không được để trống")
    @Min(value = 0, message = "Phần trăm giảm phải lớn hơn hoặc bằng 0")
    @Max(value = 60, message = "Phần trăm giảm không được vượt quá 60%")
    private Double phanTramKhuyenMai;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu ít nhất từ bây giờ")
    private LocalDateTime ngayBatDau;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày kết thúc ít nhất phải ngày mai")
    private LocalDateTime ngayKetThuc;

    private LocalDateTime ngayTao = LocalDateTime.now();

    private String trangThai;

}
