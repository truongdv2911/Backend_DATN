package com.example.demo.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SPYeuThichDTO {
    @NotNull(message = "khong de trong id wl")
    private Integer wishlistId;
    @NotNull(message = "Không để trống í sp")
    private Integer sanPhamId;
}
