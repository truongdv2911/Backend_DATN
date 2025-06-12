package com.example.demo.DTOs;

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

    @Min(value = 6, message = "Độ tuổi phải lớn hơn hoặc bằng 6")
    @Max(value = 50, message = "Độ tuổi phải bé hơn hoặc bằng 50")
     Integer doTuoi;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
     String moTa;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "10.0", message = "Giá phải lớn hơn hoặc bằng 10.0")
     BigDecimal gia;

    @DecimalMin(value = "0.0", message = "Giá khuyến mãi phải lớn hơn hoặc bằng 0")
    BigDecimal giaKhuyenMai;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
     Integer soLuong;

    @Min(value = 1, message = "Số lượng mảnh ghép phải lớn hơn 0")
     Integer soLuongManhGhep;

    @Min(value = 1, message = "Số lượng tồn phải lớn hơn 0")
     Integer soLuongTon;

    @Size(max = 255, message = "Ảnh đại diện không được vượt quá 255 ký tự")
     String anhDaiDien;

    @Min(value = 0, message = "Số lượng vote phải lớn hơn hoặc bằng 0")
     Integer soLuongVote;

    @DecimalMin(value = "0.0", message = "Đánh giá trung bình phải lớn hơn hoặc bằng 0")
     Double danhGiaTrungBinh;

     Integer khuyenMaiId;
     Integer danhMucId;
     Integer boSuuTapId;

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    String trangThai;
}
