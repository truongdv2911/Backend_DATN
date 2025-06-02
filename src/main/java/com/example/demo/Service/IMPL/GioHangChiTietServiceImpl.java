package com.example.demo.Service.IMPL;

import com.example.demo.DTOs.GioHangChiTietDTO;
import com.example.demo.Entity.GioHang;
import com.example.demo.Entity.GioHangChiTiet;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.Gio_Hang_Repo;
import com.example.demo.Repository.Gio_hang_chi_tiet_Repo;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Service.Gio_hang_Service;
import com.example.demo.Service.Gio_hang_chi_tiet_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GioHangChiTietServiceImpl implements Gio_hang_chi_tiet_Service {

    private final Gio_hang_chi_tiet_Repo gioHangChiTietRepo;
    private final Gio_Hang_Repo gioHangRepo;
    private final San_pham_Repo sanPhamRepo;

    @Override
    public GioHangChiTiet createGioHangChiTiet(@Valid GioHangChiTietDTO dto) {
        GioHangChiTiet chiTiet = new GioHangChiTiet();

        GioHang gioHang = gioHangRepo.findById(dto.getGioHangId())
                .orElseThrow(() -> new RuntimeException("GioHang not found with id: " + dto.getGioHangId()));
        SanPham sanPham = sanPhamRepo.findById(dto.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + dto.getSanPhamId()));

        chiTiet.setGioHang(gioHang);
        chiTiet.setSanPham(sanPham);
        chiTiet.setGia(dto.getGia());
        chiTiet.setSoLuong(dto.getSoLuong());
        chiTiet.setTongTien(dto.getTongTien());

        return gioHangChiTietRepo.save(chiTiet);
    }

    @Override
    public List<GioHangChiTiet> getAllGioHangChiTiet() {
        return gioHangChiTietRepo.findAll();
    }

    @Override
    public GioHangChiTiet getGioHangChiTietById(Integer id) {
        return gioHangChiTietRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("GioHangChiTiet not found with id: " + id));
    }

    @Override
    public GioHangChiTiet updateGioHangChiTiet(Integer id, @Valid GioHangChiTietDTO dto) {
        GioHangChiTiet chiTiet = getGioHangChiTietById(id);

        GioHang gioHang = gioHangRepo.findById(dto.getGioHangId())
                .orElseThrow(() -> new RuntimeException("GioHang not found with id: " + dto.getGioHangId()));
        SanPham sanPham = sanPhamRepo.findById(dto.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + dto.getSanPhamId()));

        chiTiet.setGioHang(gioHang);
        chiTiet.setSanPham(sanPham);
        chiTiet.setGia(dto.getGia());
        chiTiet.setSoLuong(dto.getSoLuong());
        chiTiet.setTongTien(dto.getTongTien());

        return gioHangChiTietRepo.save(chiTiet);
    }

    @Override
    public void deleteGioHangChiTiet(Integer id) {
        GioHangChiTiet chiTiet = getGioHangChiTietById(id);
        gioHangChiTietRepo.delete(chiTiet);
    }
}
