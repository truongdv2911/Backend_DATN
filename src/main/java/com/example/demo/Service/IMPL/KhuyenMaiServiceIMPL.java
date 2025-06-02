package com.example.demo.Service.IMPL;


import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Repository.Khuyen_mai_Repo;
import com.example.demo.Service.Khuyen_mai_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class KhuyenMaiServiceIMPL implements Khuyen_mai_Service {
    private final Khuyen_mai_Repo khuyenMaiRepo;

    @Override
    public KhuyenMai createKhuyenMai(@Valid KhuyenMaiDTO khuyenMaiDTO) {
        String maKhuyenMai = khuyenMaiDTO.getMaKhuyenMai();
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            int maxTry = 10;
            int count = 0;
            do {
                maKhuyenMai = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã phiếu mới, vui lòng thử lại!");
                }
            } while (khuyenMaiRepo.existsByMaKhuyenMai(maKhuyenMai));
        } else {
            if (khuyenMaiRepo.existsByMaKhuyenMai(maKhuyenMai)) {
                throw new RuntimeException("Mã phiếu đã tồn tại!");
            }
        }

        KhuyenMai khuyenMai = new KhuyenMai();
        khuyenMai.setMaKhuyenMai(maKhuyenMai);
        khuyenMai.setSoLuong(khuyenMaiDTO.getSoLuong());
        khuyenMai.setGiaTriGiam(khuyenMaiDTO.getGiaTriGiam());
        khuyenMai.setGiaTriToiDa(khuyenMaiDTO.getGiaTriToiDa());
        khuyenMai.setMoTa(khuyenMaiDTO.getMoTa());
        khuyenMai.setPhanTramGiam(khuyenMaiDTO.getPhanTramGiam());
        khuyenMai.setTrangThai(khuyenMaiDTO.getTrangThai());
        return khuyenMaiRepo.save(khuyenMai);
    }

    @Override
    public List<KhuyenMai> getAllKhuyenMai() {
        return khuyenMaiRepo.findAll();
    }

    @Override
    public KhuyenMai getKhuyenMaiById(Integer id) {
        return khuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + id));
    }

    @Override
    public KhuyenMai updateKhuyenMai(Integer id, @Valid KhuyenMaiDTO khuyenMaiDTO) {
        String maKhuyenMai = khuyenMaiDTO.getMaKhuyenMai();
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            int maxTry = 10;
            int count = 0;
            do {
                maKhuyenMai = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã phiếu mới, vui lòng thử lại!");
                }
            } while (khuyenMaiRepo.existsByMaKhuyenMai(maKhuyenMai));
        } else {
            if (khuyenMaiRepo.existsByMaKhuyenMai(maKhuyenMai)) {
                throw new RuntimeException("Mã phiếu đã tồn tại!");
            }
        }

        KhuyenMai khuyenMai = getKhuyenMaiById(id);
        khuyenMai.setMaKhuyenMai(maKhuyenMai);
        khuyenMai.setSoLuong(khuyenMaiDTO.getSoLuong());
        khuyenMai.setGiaTriGiam(khuyenMaiDTO.getGiaTriGiam());
        khuyenMai.setGiaTriToiDa(khuyenMaiDTO.getGiaTriToiDa());
        khuyenMai.setMoTa(khuyenMaiDTO.getMoTa());
        khuyenMai.setPhanTramGiam(khuyenMaiDTO.getPhanTramGiam());
        khuyenMai.setTrangThai(khuyenMaiDTO.getTrangThai());
        return khuyenMaiRepo.save(khuyenMai);
    }

    @Override
    public void deleteKhuyenMai(Integer id) {
        KhuyenMai khuyenMai = getKhuyenMaiById(id);
        khuyenMaiRepo.delete(khuyenMai);
    }

    @Override
    public String generateMaPhieu() {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder("KM");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
