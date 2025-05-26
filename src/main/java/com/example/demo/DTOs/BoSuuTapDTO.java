package com.example.demo.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
public class BoSuuTapDTO {

     Integer id;

    @NotBlank(message = "Tên bộ sưu tập không được để trống")
    @Size(max = 100, message = "Tên bộ sưu tập không được vượt quá 100 ký tự")
     String tenBoSuuTap;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
     String moTa;

    @NotNull(message = "Năm phát hành không được để trống")
    @Min(value = 1900, message = "Năm phát hành phải lớn hơn hoặc bằng 1900")
    @Max(value = 2100, message = "Năm phát hành không được vượt quá 2100")
     Integer namPhatHanh;
}
