package com.example.demo.DTOs;

import com.example.demo.Entity.TrangThaiHoaDon;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOhoaDon {
    @Min(value = 0, message = "Tạm tính phải lớn hơn hoặc bằng 0")
    private BigDecimal tamTinh;

    @Min(value = 0, message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal tongTien;

    @Min(value = 0, message = "Số tiền giảm phải lớn hơn hoặc bằng 0")
    private BigDecimal soTienGiam;

    @NotEmpty(message = "Địa chỉ giao hàng không được để trống")
    private String diaChiGiaoHang;

    @NotEmpty(message = "Mã vận chuyển không được để trống")
    private String maVanChuyen;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Ngày giao không được để trống")
    private LocalDateTime ngayGiao;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Ngày tạo không được để trống")
    private LocalDateTime ngayTao;

    @NotNull(message = "Trạng thái không được để trống")
    private TrangThaiHoaDon trangThai;

    @NotEmpty(message = "Phương thức thanh toán không được để trống")
    private String phuongThucThanhToan;

    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<CartItemDTO> cartItems;

    @Min(value = 1, message = "ID người dùng phải lớn hơn 0")
    private Integer userId;

    private Integer idPhieuGiamGia;
}
