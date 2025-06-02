package com.example.demo.Service;

import com.example.demo.DTOs.GioHangChiTietDTO;
import com.example.demo.Entity.GioHang;
import com.example.demo.Entity.GioHangChiTiet;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.Gio_hang_chi_tiet_Repo;
import com.example.demo.Repository.Gio_Hang_Repo;

import com.example.demo.Repository.San_pham_Repo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service

public interface Gio_hang_chi_tiet_Service {
    GioHangChiTiet createGioHangChiTiet(GioHangChiTietDTO gioHangChiTietDTO);

    List<GioHangChiTiet> getAllGioHangChiTiet();

    GioHangChiTiet getGioHangChiTietById(Integer id);

    GioHangChiTiet updateGioHangChiTiet(Integer id, GioHangChiTietDTO gioHangChiTietDTO);

    void deleteGioHangChiTiet(Integer id);
}
