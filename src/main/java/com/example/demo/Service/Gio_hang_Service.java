package com.example.demo.Service;

import com.example.demo.Entity.GioHang;
import com.example.demo.Repository.Gio_Hang_Repo;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class Gio_hang_Service {

    private final Gio_Hang_Repo gioHangRepository;

    public List<GioHang> getAllGioHangs() {
        return gioHangRepository.findAll();
    }

    public Optional<GioHang> getGioHangById(Integer id) {
        return gioHangRepository.findById(id);
    }

    public GioHang createGioHang(GioHang gioHang) {
        return gioHangRepository.save(gioHang);
    }

    public GioHang updateGioHang(Integer id, GioHang updatedGioHang) {
        return gioHangRepository.findById(id).map(gioHang -> {
            gioHang.setSoTienGiam(updatedGioHang.getSoTienGiam());
            gioHang.setTongTien(updatedGioHang.getTongTien());
            gioHang.setTrangThai(updatedGioHang.getTrangThai());
//            gioHang.setUser(updatedGioHang.getUser());
            return gioHangRepository.save(gioHang);
        }).orElseThrow(() -> new RuntimeException("GioHang not found with id " + id));
    }

    public void deleteGioHang(Integer id) {
        gioHangRepository.deleteById(id);
    }
}
