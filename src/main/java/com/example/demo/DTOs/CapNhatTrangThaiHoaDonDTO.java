package com.example.demo.DTOs;

import lombok.Data;

import java.util.List;
@Data
public class CapNhatTrangThaiHoaDonDTO {
    private List<Integer> hoaDonIds;
    private String trangThai;
    private Integer idNV;
}
