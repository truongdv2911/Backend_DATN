package com.example.demo.Service;

import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Repository.Khuyen_mai_Repo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public interface Khuyen_mai_Service {
    KhuyenMai createKhuyenMai(KhuyenMaiDTO khuyenMaiDTO);
    List<KhuyenMai> getAllKhuyenMai();
    KhuyenMai getKhuyenMaiById(Integer id);
    KhuyenMai updateKhuyenMai(Integer id, KhuyenMaiDTO khuyenMaiDTO);
    void deleteKhuyenMai(Integer id);
    String generateMaPhieu();
}
