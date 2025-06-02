package com.example.demo.DTOs;

import com.example.demo.Entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOhoaDon {
    @Min(value = 0)
    private BigDecimal tamTinh;
    @Min(value = 0)
    private BigDecimal tongTien;
    @Min(value = 0)
    private BigDecimal soTienGiam;
    @NotEmpty
    private String DiaChiGiaoHang;
    @NotEmpty
    private String maVanChuyen;
    private Date ngayGiao;
    private LocalDateTime ngayTao;
    private String trangThai;
    @NotEmpty
    private String phuongThucThanhToan;

    private List<CartItemDTO> cartItems;

    private Integer userId;

    private Integer nvId;

    private Integer idPhieuGiam;
}
