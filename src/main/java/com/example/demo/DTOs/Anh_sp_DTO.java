package com.example.demo.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class Anh_sp_DTO {
    @NotNull(message = "ID ảnh không được để trống")
    Integer id;

    @NotBlank(message = "URL không được để trống")
    @Size(max = 500, message = "URL không được vượt quá 500 ký tự")
     String url;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
     String moTa;

    @NotNull(message = "Thứ tự không được để trống")
     Integer thuTu;

    @NotNull(message = "Trường ảnh chính không được để trống")
     Boolean anhChinh;

    @NotNull(message = "ID sản phẩm không được để trống")
     Integer sanpham;
}
