package com.example.demo.Service;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service

public interface Phieu_giam_gia_Service {

    PhieuGiamGia createPhieuGiamGia(@Valid PhieuGiamGiaDTO phieuGiamGiaDTO);
    List<PhieuGiamGia> getAllPhieuGiamGia();
    PhieuGiamGia getPhieuGiamGiaById(Integer id);
    PhieuGiamGia updatePhieuGiamGia(Integer id, @Valid PhieuGiamGiaDTO phieuGiamGiaDTO);
    void deletePhieuGiamGia(Integer id);
    String generateMaPhieu();
    List<PhieuGiamGia> getByLoaiPhieuGiam(String loaiPhieuGiam);
}
