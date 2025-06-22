package com.example.demo.Controller;

import com.example.demo.DTOs.KhuyenMaiSanPhamDTO;
import com.example.demo.Service.Khuyen_mai_Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lego-store/khuyen-mai-san-pham")
@RequiredArgsConstructor
public class KhuyenMaiSPController {
    private final Khuyen_mai_Service khuyen_mai_service;

    @PostMapping("/apply-Khuyen-mai")
    public ResponseEntity<?> applyKM( @RequestBody KhuyenMaiSanPhamDTO khuyenMaiSanPhamDTO){
        try {
            khuyen_mai_service.applyKhuyenMai(khuyenMaiSanPhamDTO);
            return ResponseEntity.ok("Ap dung khuyen mai thanh cong");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Loi khi ap dung km: "+ e.getMessage());
        }
    }
}
