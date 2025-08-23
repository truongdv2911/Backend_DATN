package com.example.demo.Responses;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietHoanResponse {
    private Integer idSanPham;

    private Integer soLuongHoan;

    private BigDecimal tongGiaHoan;
}
