package com.example.demo.Service;

import com.example.demo.DTOs.GioHangDTO;
import com.example.demo.Entity.GioHang;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Entity.User;
import com.example.demo.Repository.Gio_Hang_Repo;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Gio_hang_Service {

    private final Gio_Hang_Repo gioHangRepository;
    private final UserRepository userRepository;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepository;


    public GioHang createGioHang(@Valid GioHangDTO gioHangDTO) {
        GioHang gioHang = new GioHang();
        gioHang.setSoTienGiam(gioHangDTO.getSoTienGiam());
        gioHang.setTongTien(gioHangDTO.getTongTien());
        gioHang.setTrangThai(gioHangDTO.getTrangThai());

        User user = userRepository.findById(gioHangDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + gioHangDTO.getUserId()));
        gioHang.setUser(user);

        if (gioHangDTO.getPhieuGiamGiaId() != null) {
            PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(gioHangDTO.getPhieuGiamGiaId())
                    .orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id: " + gioHangDTO.getPhieuGiamGiaId()));
            gioHang.setPhieuGiamGia(phieuGiamGia);
        }

        return gioHangRepository.save(gioHang);
    }


    public List<GioHang> getAllGioHangs() {
        return gioHangRepository.findAll();
    }


    public GioHang getGioHangById(Integer id) {
        return gioHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GioHang not found with id: " + id));
    }


    public GioHang updateGioHang(Integer id, @Valid GioHangDTO gioHangDTO) {
        GioHang gioHang = getGioHangById(id);
        gioHang.setSoTienGiam(gioHangDTO.getSoTienGiam());
        gioHang.setTongTien(gioHangDTO.getTongTien());
        gioHang.setTrangThai(gioHangDTO.getTrangThai());

        User user = userRepository.findById(gioHangDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + gioHangDTO.getUserId()));
        gioHang.setUser(user);

        if (gioHangDTO.getPhieuGiamGiaId() != null) {
            PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(gioHangDTO.getPhieuGiamGiaId())
                    .orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id: " + gioHangDTO.getPhieuGiamGiaId()));
            gioHang.setPhieuGiamGia(phieuGiamGia);
        }

        return gioHangRepository.save(gioHang);
    }


    public void deleteGioHang(Integer id) {
        GioHang gioHang = getGioHangById(id);
        gioHangRepository.delete(gioHang);
    }
}
