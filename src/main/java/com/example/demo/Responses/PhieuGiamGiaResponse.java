package com.example.demo.Responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PhieuGiamGiaResponse {
    private Long id;
    private LocalDateTime ngayNhan;
    private String maPhieu;
    private String tenPhieu;
    private Integer soLuong;
    private String loaiPhieuGiam;
    private BigDecimal giaTriGiam;
    private BigDecimal giamToiDa;
    private BigDecimal giaTriToiThieu;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String trangThaiThucTe;
}
