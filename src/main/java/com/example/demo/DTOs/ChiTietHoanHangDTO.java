package com.example.demo.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChiTietHoanHangDTO {
    @NotNull(message = "ID sản phẩm không được để trống")
    private Integer idSanPham;

    @NotNull(message = "Số lượng hoàn không được để trống")
    @Min(value = 1, message = "Số lượng hoàn phải > 0")
    private Integer soLuongHoan;
}
