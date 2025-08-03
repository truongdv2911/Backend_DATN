package com.example.demo.DTOs;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SPYeuThichDTO {
    @NotNull
    private Integer userId;
    @NotNull
    private Integer sanPhamId;
}
