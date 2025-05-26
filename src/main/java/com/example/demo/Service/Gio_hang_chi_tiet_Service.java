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
@RequiredArgsConstructor
public class Gio_hang_chi_tiet_Service {

    private final Gio_hang_chi_tiet_Repo gioHangChiTietRepo;
    private final Gio_Hang_Repo gioHangRepo;
    private final San_pham_Repo sanPhamRepo;

    // Create
    public GioHangChiTiet createGioHangChiTiet(@Valid GioHangChiTietDTO gioHangChiTietDTO) {
        GioHangChiTiet gioHangChiTiet = new GioHangChiTiet();

        // Validate and set GioHang
        GioHang gioHang = gioHangRepo.findById(gioHangChiTietDTO.getGioHangId())
                .orElseThrow(() -> new RuntimeException("GioHang not found with id: " + gioHangChiTietDTO.getGioHangId()));
        gioHangChiTiet.setGioHang(gioHang);

        // Validate and set SanPham
        SanPham sanPham = sanPhamRepo.findById(gioHangChiTietDTO.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + gioHangChiTietDTO.getSanPhamId()));
        gioHangChiTiet.setSanPham(sanPham);

        gioHangChiTiet.setGia(BigDecimal.valueOf(gioHangChiTietDTO.getGia()));
        gioHangChiTiet.setTong_tien(BigDecimal.valueOf(gioHangChiTietDTO.getTongTien()));
        gioHangChiTiet.setSo_luong(gioHangChiTietDTO.getSoLuong());

        return gioHangChiTietRepo.save(gioHangChiTiet);
    }

    // Read All
    public List<GioHangChiTiet> getAllGioHangChiTiet() {
        return gioHangChiTietRepo.findAll();
    }

    // Read One
    public GioHangChiTiet getGioHangChiTietById(Integer id) {
        return gioHangChiTietRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("GioHangChiTiet not found with id: " + id));
    }

    // Update
    public GioHangChiTiet updateGioHangChiTiet(Integer id, @Valid GioHangChiTietDTO gioHangChiTietDTO) {
        GioHangChiTiet gioHangChiTiet = getGioHangChiTietById(id);

        // Validate and set GioHang
        GioHang gioHang = gioHangRepo.findById(gioHangChiTietDTO.getGioHangId())
                .orElseThrow(() -> new RuntimeException("GioHang not found with id: " + gioHangChiTietDTO.getGioHangId()));
        gioHangChiTiet.setGioHang(gioHang);

        // Validate and set SanPham
        SanPham sanPham = sanPhamRepo.findById(gioHangChiTietDTO.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + gioHangChiTietDTO.getSanPhamId()));
        gioHangChiTiet.setSanPham(sanPham);

        gioHangChiTiet.setGia(BigDecimal.valueOf(gioHangChiTietDTO.getGia()));
        gioHangChiTiet.setTong_tien(BigDecimal.valueOf(gioHangChiTietDTO.getTongTien()));
        gioHangChiTiet.setSo_luong(gioHangChiTietDTO.getSoLuong());

        return gioHangChiTietRepo.save(gioHangChiTiet);
    }

    // Delete
    public void deleteGioHangChiTiet(Integer id) {
        GioHangChiTiet gioHangChiTiet = getGioHangChiTietById(id);
        gioHangChiTietRepo.delete(gioHangChiTiet);
    }
}
