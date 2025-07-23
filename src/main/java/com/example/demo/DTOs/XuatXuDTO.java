package com.example.demo.DTOs;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XuatXuDTO {
    @NotBlank(message = "không để trống tên xuất xứ")
    private String ten;
    private String moTa;
}
