package com.example.demo.DTOs;

import lombok.Data;

@Data
public class KetQuaKiemTraRequest {
    private Integer idSanPham;
    private Integer soLuongHoan;
    private boolean suDungDuoc;
}
