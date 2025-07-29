package com.example.demo.DTOs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGGUserDTO {
    @NotNull(message = "ID phiếu giảm không được để trống")
    private Integer phieuGiamGiaId;
    @NotEmpty(message = "Danh sách khách hàng không được trống")
    private List<Integer> listUserId;
}
