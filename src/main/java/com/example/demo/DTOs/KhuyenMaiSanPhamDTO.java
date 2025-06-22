package com.example.demo.DTOs;

import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.SanPham;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhuyenMaiSanPhamDTO {
    @NotNull(message = "ID khuyến mãi không được để trống")
    private Integer khuyenMaiId;
    @NotEmpty(message = "Danh sách sản phẩm không được trống")
    private List<Integer> listSanPhamId;
}
