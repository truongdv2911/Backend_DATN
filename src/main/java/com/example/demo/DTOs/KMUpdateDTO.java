package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class KMUpdateDTO {
    @NotNull
    private String tenKhuyenMai;

    @NotNull(message = "Phần trăm giảm không được để trống")
    @Min(value = 0, message = "Phần trăm giảm phải lớn hơn hoặc bằng 0")
    @Max(value = 100, message = "Phần trăm giảm không được vượt quá 100%")
    private Double phanTramKhuyenMai;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime ngayBatDau;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày kết thúc ít nhất phải ngày mai")
    private LocalDateTime ngayKetThuc;

    private LocalDateTime ngayTao = LocalDateTime.now();

}
