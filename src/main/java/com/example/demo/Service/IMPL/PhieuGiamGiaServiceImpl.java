package com.example.demo.Service.IMPL;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;

import com.example.demo.Service.Phieu_giam_gia_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaServiceImpl implements Phieu_giam_gia_Service {

    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;

    @Override
    public PhieuGiamGia createPhieuGiamGia(@Valid PhieuGiamGiaDTO phieuGiamGiaDTO) {
        String maPhieu = phieuGiamGiaDTO.getMaPhieu();
        if (maPhieu == null || maPhieu.isBlank()) {
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

    @Override
    public List<PhieuGiamGia> getAllPhieuGiamGia() {
        return phieuGiamGiaRepo.findAll();
    }

    @Override
    public PhieuGiamGia getPhieuGiamGiaById(Integer id) {
        return phieuGiamGiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id: " + id));
    }

    @Override
    public PhieuGiamGia updatePhieuGiamGia(Integer id, @Valid PhieuGiamGiaDTO phieuGiamGiaDTO) {
        String maPhieu = phieuGiamGiaDTO.getMaPhieu();
        if (maPhieu == null || maPhieu.isBlank()) {
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

        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
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

    @Override
    public void deletePhieuGiamGia(Integer id) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGiaRepo.delete(phieuGiamGia);
    }

    @Override
    public String generateMaPhieu() {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder("PGG");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public List<PhieuGiamGia> getByLoaiPhieuGiam(String loaiPhieuGiam) {
        return phieuGiamGiaRepo.findByLoaiPhieuGiam(loaiPhieuGiam);
    }
}