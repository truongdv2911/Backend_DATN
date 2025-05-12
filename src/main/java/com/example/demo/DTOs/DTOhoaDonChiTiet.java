package com.example.demo.DTOs;

import com.example.demo.Entity.HoaDon;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOhoaDonChiTiet {
    @NotNull(message = "Khong am so luong")
    @Min(value = 0)
    private Integer soLuong;
    @NotNull(message = "Khong am so luong")
    @Min(value = 0)
    private BigDecimal gia;
    @NotNull(message = "Khong am so luong")
    @Min(value = 0)
    private BigDecimal tongTien;

    private Integer hd_id;
}
