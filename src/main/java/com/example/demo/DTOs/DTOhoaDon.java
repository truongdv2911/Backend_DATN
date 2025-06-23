package com.example.demo.DTOs;

import com.example.demo.Entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
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
    @NotEmpty(message = "không để trống số điện thoại người nhận")
    @Pattern(regexp = "\\d{10}", message = "Sai dinh dang sdt")
    private String sdt;

    @Valid
    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<CartItemDTO> cartItems;

    private Integer userId;

    private Integer nvId;

    private Integer idPhieuGiam;
}
