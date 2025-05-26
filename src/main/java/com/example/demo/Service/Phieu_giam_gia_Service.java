package com.example.demo.Service;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Phieu_giam_gia_Service {

    @Autowired
    private Phieu_giam_gia_Repo phieuGiamGiaRepo;

    // Create
    public PhieuGiamGia createPhieuGiamGia(@Valid PhieuGiamGiaDTO phieuGiamGiaDTO) {
        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        phieuGiamGia.setMa_phieu(phieuGiamGiaDTO.getMa_phieu());
        phieuGiamGia.setSo_luong(phieuGiamGiaDTO.getSo_luong());
        phieuGiamGia.setLoai_phieu_giam(phieuGiamGiaDTO.getLoai_phieu_giam());
        phieuGiamGia.setGia_tri_giam(phieuGiamGiaDTO.getGia_tri_giam());
        phieuGiamGia.setGiam_toi_da(phieuGiamGiaDTO.getGiam_toi_da());
        phieuGiamGia.setGia_tri_toi_thieu(phieuGiamGiaDTO.getGia_tri_toi_thieu());
        phieuGiamGia.setNgay_bat_dau(phieuGiamGiaDTO.getNgay_bat_dau());
        phieuGiamGia.setNgay_ket_thuc(phieuGiamGiaDTO.getNgay_ket_thuc());
        phieuGiamGia.setTrang_thai(phieuGiamGiaDTO.getTrang_thai());
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }

    // Read All
    public List<PhieuGiamGia> getAllPhieuGiamGia() {
        return phieuGiamGiaRepo.findAll();
    }

    // Read One
    public PhieuGiamGia getPhieuGiamGiaById(Integer id) {
        return phieuGiamGiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id: " + id));
    }

    // Update
    public PhieuGiamGia updatePhieuGiamGia(Integer id, @Valid PhieuGiamGiaDTO phieuGiamGiaDTO) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGia.setMa_phieu(phieuGiamGiaDTO.getMa_phieu());
        phieuGiamGia.setSo_luong(phieuGiamGiaDTO.getSo_luong());
        phieuGiamGia.setLoai_phieu_giam(phieuGiamGiaDTO.getLoai_phieu_giam());
        phieuGiamGia.setGia_tri_giam(phieuGiamGiaDTO.getGia_tri_giam());
        phieuGiamGia.setGiam_toi_da(phieuGiamGiaDTO.getGiam_toi_da());
        phieuGiamGia.setGia_tri_toi_thieu(phieuGiamGiaDTO.getGia_tri_toi_thieu());
        phieuGiamGia.setNgay_bat_dau(phieuGiamGiaDTO.getNgay_bat_dau());
        phieuGiamGia.setNgay_ket_thuc(phieuGiamGiaDTO.getNgay_ket_thuc());
        phieuGiamGia.setTrang_thai(phieuGiamGiaDTO.getTrang_thai());
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }

    // Delete
    public void deletePhieuGiamGia(Integer id) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGiaRepo.delete(phieuGiamGia);
    }
}
