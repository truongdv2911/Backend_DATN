package com.example.demo.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThuongHieuDTO {
    @NotBlank(message = "không để trống tên thương hiệu")
    private String ten;
    private String moTa;
}
