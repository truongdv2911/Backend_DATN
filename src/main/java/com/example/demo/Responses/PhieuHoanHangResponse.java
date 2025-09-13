package com.example.demo.Responses;

import com.example.demo.Entity.ChiTietHoanHang;
import com.example.demo.Entity.HoaDon;
import com.example.demo.Enum.TrangThaiPhieuHoan;
import com.example.demo.Enum.TrangThaiThanhToan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuHoanHangResponse {
    private Integer id;

    private LocalDateTime ngayHoan;

    private BigDecimal tongTienHoan;

    private String loaiHoan;

    private String lyDo;

    private LocalDateTime ngayDuyet;

    private TrangThaiPhieuHoan trangThai; // CHO_DUYET, DA_DUYET, TU_CHOI

    private TrangThaiThanhToan trangThaiThanhToan; // CHUA_HOAN, DANG_HOAN, DA_HOAN

    private String phuongThucHoan;

    private LocalDateTime ngayHoanTien;

    private String tenNganHang;

    private String soTaiKhoan;

    private String chuTaiKhoan;


    private Integer idHD;

    private List<ChiTietHoanResponse> chiTietHoanHangs = new ArrayList<>();
    private List<AnhResponse> anhs = new ArrayList<>();
}
