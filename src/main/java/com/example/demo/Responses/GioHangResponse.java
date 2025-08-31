package com.example.demo.Responses;

import com.example.demo.Entity.GioHangChiTiet;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GioHangResponse {
    private Integer id;
    private BigDecimal soTienGiam;
    private BigDecimal tongTien;
    private String trangThai;
    private Integer userId;
    private Integer phieuGiamGiaId;
    private List<GioHangChiTietResponse> gioHangChiTiets;
}
