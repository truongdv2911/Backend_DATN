package com.example.demo.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class PhieuHoanHangDTO {
    @NotNull(message = "ID hóa đơn không được để trống")
    private Integer idHoaDon;

    @NotBlank(message = "Loại hoàn không được để trống")
    @Pattern(regexp = "HOAN_MOT_PHAN|HOAN_TOAN_BO", message = "Loại hoàn chỉ được 'HOAN_MOT_PHAN' hoặc 'HOAN_TOAN_BO'")
    private String loaiHoan;

    @NotBlank(message = "Lý do hoàn không được để trống")
    private String lyDo;

    @NotBlank(message = "Phương thức hoàn không được để trống")
    private String phuongThucHoan;

    private String tenNganHang;
    private String soTaiKhoan;
    private String chuTaiKhoan;

    @Valid
    @NotEmpty(message = "Danh sách sản phẩm hoàn không được để trống")
    private List<ChiTietHoanHangDTO> chiTietHoanHangs;
}
