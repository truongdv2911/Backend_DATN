package com.example.demo.Controller;

import com.example.demo.DTOs.KhuyenMaiSanPhamDTO;
import com.example.demo.Entity.KhuyenMaiSanPham;
import com.example.demo.Repository.KhuyenMaiSanPhamRepository;
import com.example.demo.Service.Khuyen_mai_Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lego-store/khuyen-mai-san-pham")
@RequiredArgsConstructor
public class KhuyenMaiSPController {
    private final Khuyen_mai_Service khuyen_mai_service;
    private final KhuyenMaiSanPhamRepository kmspRepo;

    @PostMapping("/apply-Khuyen-mai")
    public ResponseEntity<?> applyKM(@RequestBody KhuyenMaiSanPhamDTO khuyenMaiSanPhamDTO){
        try {
            List<String> errors = khuyen_mai_service.applyKhuyenMai(khuyenMaiSanPhamDTO);
            if (errors.isEmpty()) {
                return ResponseEntity.ok("Áp dụng khuyến mãi thành công");
            } else {
                return ResponseEntity.ok().body(errors); // hoặc trả về 207 Multi-Status nếu muốn
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("getListKhuyenMaiTheoSP/{idSP}")
    public ResponseEntity<?> getListKMSP(@PathVariable Integer idSP){
        try {
            List<KhuyenMaiSanPham> dsKhuyenMaiHienTai = kmspRepo.findBySanPham_Id(idSP);
            List<com.example.demo.Responses.KhuyenMaiSPResponse> responseList = dsKhuyenMaiHienTai.stream().map(kmsp -> {
                com.example.demo.Responses.KhuyenMaiSPResponse resp = new com.example.demo.Responses.KhuyenMaiSPResponse();
                resp.setSanPhamId(kmsp.getSanPham() != null ? kmsp.getSanPham().getId() : null);
                resp.setKhuyenMaiId(kmsp.getKhuyenMai() != null ? kmsp.getKhuyenMai().getId() : null);
                resp.setGiaKhuyenMai(kmsp.getGiaKhuyenMai());
                return resp;
            }).toList();
            return ResponseEntity.ok(responseList);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("loi:"+ e.getMessage());
        }
    }
}
