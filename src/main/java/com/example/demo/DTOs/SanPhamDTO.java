package com.example.demo.DTOs;

import com.example.demo.Entity.SanPham;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class SanPhamDTO {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm không được vượt quá 200 ký tự")
     String tenSanPham;
//
//    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(max = 200, message = "Mã sản phẩm không được vượt quá 200 ký tự")
     String maSanPham;

    @NotNull(message = "Độ tuổi không được để trống")
    @Min(value = 6, message = "Độ tuổi phải lớn hơn hoặc bằng 6")
    @Max(value = 50, message = "Độ tuổi phải bé hơn hoặc bằng 50")
     Integer doTuoi;

    @NotBlank(message = "không được để trống mô tả")
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
     String moTa;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "10.0", message = "Giá phải lớn hơn hoặc bằng 10.0")
     BigDecimal gia;

    @NotNull(message = "Số lượng mảnh ghép không được để trống")
    @Min(value = 1, message = "Số lượng mảnh ghép phải lớn hơn 0")
     Integer soLuongManhGhep;

    @NotNull(message = "Số lượng tồn ghép không được để trống")
    @Min(value = 1, message = "Số lượng tồn phải lớn hơn 0")
     Integer soLuongTon;

    @Min(value = 0, message = "Số lượng vote phải lớn hơn hoặc bằng 0")
     Integer soLuongVote;

    @DecimalMin(value = "0.0", message = "Đánh giá trung bình phải lớn hơn hoặc bằng 0")
     Double danhGiaTrungBinh;

    @NotNull(message = "Danh mục không được để trống")
     Integer danhMucId;
    @NotNull(message = "Bộ sưu tập không được để trống")
     Integer boSuuTapId;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    String trangThai;

    public SanPhamDTO(SanPham sp) {
        this.tenSanPham = sp.getTenSanPham();
        this.maSanPham = sp.getMaSanPham();
        this.doTuoi = sp.getDoTuoi();
        this.moTa = sp.getMoTa();
        this.soLuongManhGhep = sp.getSoLuongManhGhep();
        this.gia = sp.getGia();
        this.soLuongTon = sp.getSoLuongTon();
        this.soLuongVote = sp.getSoLuongVote();
        this.danhGiaTrungBinh = sp.getDanhGiaTrungBinh();
        this.trangThai = sp.getTrangThai();

        if (sp.getDanhMuc() != null) {
            this.danhMucId = sp.getDanhMuc().getId();
        }

        if (sp.getBoSuuTap() != null) {
            this.boSuuTapId = sp.getBoSuuTap().getId();
        }
    }
}
