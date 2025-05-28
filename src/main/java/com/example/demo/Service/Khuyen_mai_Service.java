package com.example.demo.Service;

import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Repository.Khuyen_mai_Repo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class Khuyen_mai_Service {

    @Autowired
    private Khuyen_mai_Repo khuyenMaiRepo;

    // Create
    public KhuyenMai createKhuyenMai(@Valid KhuyenMaiDTO khuyenMaiDTO) {
        KhuyenMai khuyenMai = new KhuyenMai();
        khuyenMai.setMaKhuyenMai(khuyenMaiDTO.getMaKhuyenMai());
        khuyenMai.setSoLuong(khuyenMaiDTO.getSoluong());
        khuyenMai.setGiaTriGiam(khuyenMaiDTO.getGiaTriGiam() );
        khuyenMai.setGiaTriToiDa(khuyenMaiDTO.getGiaTriToiDa());
        khuyenMai.setMoTa(khuyenMaiDTO.getMoTa());
        khuyenMai.setPhanTramGiam(khuyenMaiDTO.getPhanTramGiam());
        khuyenMai.setTrangThai(khuyenMaiDTO.getTrangThai());
        return khuyenMaiRepo.save(khuyenMai);
    }

    // Read All
    public List<KhuyenMai> getAllKhuyenMai() {
        return khuyenMaiRepo.findAll();
    }

    // Read One
    public KhuyenMai getKhuyenMaiById(Integer id) {
        return khuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + id));
    }

    // Update
    public KhuyenMai updateKhuyenMai(Integer id, @Valid KhuyenMaiDTO khuyenMaiDTO) {
        KhuyenMai khuyenMai = getKhuyenMaiById(id);
        khuyenMai.setMaKhuyenMai(khuyenMaiDTO.getMaKhuyenMai());
        khuyenMai.setSoLuong(khuyenMaiDTO.getSoluong());
        khuyenMai.setGiaTriGiam(khuyenMaiDTO.getGiaTriGiam() );
        khuyenMai.setGiaTriToiDa(khuyenMaiDTO.getGiaTriToiDa());
        khuyenMai.setMoTa(khuyenMaiDTO.getMoTa());
        khuyenMai.setPhanTramGiam(khuyenMaiDTO.getPhanTramGiam());
        khuyenMai.setTrangThai(khuyenMaiDTO.getTrangThai());
        return khuyenMaiRepo.save(khuyenMai);
    }

    // Delete
    public void deleteKhuyenMai(Integer id) {
        KhuyenMai khuyenMai = getKhuyenMaiById(id);
        khuyenMaiRepo.delete(khuyenMai);
    }
}
