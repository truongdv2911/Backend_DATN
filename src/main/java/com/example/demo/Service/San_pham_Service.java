package com.example.demo.Service;

import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.San_pham_Repo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class San_pham_Service {

    private final San_pham_Repo sanPhamRepository;

    public List<SanPham> getAllSanPhams() {
        return sanPhamRepository.findAll();
    }

    public Optional<SanPham> getSanPhamById(Integer id) {
        return sanPhamRepository.findById(id);
    }

    public SanPham createSanPham(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }

    public SanPham updateSanPham(Integer id, SanPham updatedSanPham) {
        return sanPhamRepository.findById(id).map(sanPham -> {
            sanPham.setTenSanPham(updatedSanPham.getTenSanPham());
            sanPham.setGia(updatedSanPham.getGia());
            sanPham.setGiaKhuyenMai(updatedSanPham.getGiaKhuyenMai());
            sanPham.setSoLuong(updatedSanPham.getSoLuong());
            sanPham.setDanhMuc(updatedSanPham.getDanhMuc());
            sanPham.setBoSuuTap(updatedSanPham.getBoSuuTap());
            return sanPhamRepository.save(sanPham);
        }).orElseThrow(() -> new RuntimeException("SanPham not found with id " + id));
    }

    public void deleteSanPham(Integer id) {
        sanPhamRepository.deleteById(id);
    }
}
