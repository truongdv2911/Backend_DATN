package com.example.demo.Service;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class Phieu_giam_gia_Service {

    @Autowired
    private Phieu_giam_gia_Repo phieuGiamGiaRepo;





    public PhieuGiamGia createPhieuGiamGia(@Valid PhieuGiamGiaDTO phieuGiamGiaDTO) {
        String maPhieu = phieuGiamGiaDTO.getMaPhieu();
        if (maPhieu == null || maPhieu.isBlank()) {
            // Giới hạn số lần thử sinh mã để tránh vòng lặp vô hạn
            int maxTry = 10;
            int count = 0;
            do {
                maPhieu = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã phiếu mới, vui lòng thử lại!");
                }
            } while (phieuGiamGiaRepo.existsByMaPhieu(maPhieu));
        } else {
            if (phieuGiamGiaRepo.existsByMaPhieu(maPhieu)) {
                throw new RuntimeException("Mã phiếu đã tồn tại!");
            }
        }

        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        phieuGiamGia.setMaPhieu(maPhieu);
        phieuGiamGia.setSoLuong(phieuGiamGiaDTO.getSoLuong());
        phieuGiamGia.setLoaiPhieuGiam(phieuGiamGiaDTO.getLoaiPhieuGiam());
        phieuGiamGia.setGiaTriGiam(phieuGiamGiaDTO.getGiaTriGiam());
        phieuGiamGia.setGiamToiDa(phieuGiamGiaDTO.getGiamToiDa());
        phieuGiamGia.setGiaTriToiThieu(phieuGiamGiaDTO.getGiaTriToiThieu());
        phieuGiamGia.setNgayBatDau(phieuGiamGiaDTO.getNgayBatDau());
        phieuGiamGia.setNgayKetThuc(phieuGiamGiaDTO.getNgayKetThuc());
        phieuGiamGia.setTrangThai(phieuGiamGiaDTO.getTrangThai());
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }


    public List<PhieuGiamGia> getAllPhieuGiamGia() {
        return phieuGiamGiaRepo.findAll();
    }


    public PhieuGiamGia getPhieuGiamGiaById(Integer id) {
        return phieuGiamGiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id: " + id));
    }


    public PhieuGiamGia updatePhieuGiamGia(Integer id, @Valid PhieuGiamGiaDTO phieuGiamGiaDTO) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGia.setMaPhieu(phieuGiamGiaDTO.getMaPhieu());
        phieuGiamGia.setSoLuong(phieuGiamGiaDTO.getSoLuong());
        phieuGiamGia.setLoaiPhieuGiam(phieuGiamGiaDTO.getLoaiPhieuGiam());
        phieuGiamGia.setGiaTriGiam(phieuGiamGiaDTO.getGiaTriGiam());
        phieuGiamGia.setGiamToiDa(phieuGiamGiaDTO.getGiamToiDa());
        phieuGiamGia.setGiaTriToiThieu(phieuGiamGiaDTO.getGiaTriToiThieu());
        phieuGiamGia.setNgayBatDau(phieuGiamGiaDTO.getNgayBatDau());
        phieuGiamGia.setNgayKetThuc(phieuGiamGiaDTO.getNgayKetThuc());
        phieuGiamGia.setTrangThai(phieuGiamGiaDTO.getTrangThai());
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }


    public void deletePhieuGiamGia(Integer id) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGiaRepo.delete(phieuGiamGia);
    }
    public String generateMaPhieu() {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder("PGG");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
