package com.example.demo.Controller;

import com.example.demo.DTOs.KhuyenMaiSanPhamDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.KhuyenMaiSanPham;
import com.example.demo.Repository.KhuyenMaiSanPhamRepository;
import com.example.demo.Repository.Khuyen_mai_Repo;
import com.example.demo.Service.Khuyen_mai_Service;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Responses.ErrorResponse;
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
    private final LichSuLogService lichSuLogService;
    private final Khuyen_mai_Repo khuyenMaiRepo;

    @PostMapping("/apply-Khuyen-mai")
    public ResponseEntity<?> applyKM(@RequestBody KhuyenMaiSanPhamDTO khuyenMaiSanPhamDTO){
        try {
            KhuyenMai khuyenMai = khuyenMaiRepo.findById(khuyenMaiSanPhamDTO.getKhuyenMaiId()).orElseThrow(() -> new RuntimeException("khong tim thay id km"));
            List<String> errors = khuyen_mai_service.applyKhuyenMai(khuyenMaiSanPhamDTO);
            int tong = khuyenMaiSanPhamDTO.getListSanPhamId().size();
            int fail = errors.size();
            int success = tong - fail;

            // Ghi log
            String moTa = String.format(
                "Áp dụng khuyến mãi Mã: %s cho %d sản phẩm. Thành công: %d, Thất bại: %d. Lỗi: %s",
                    khuyenMai.getMaKhuyenMai(), tong, success, fail, errors.toString()
            );
            lichSuLogService.saveLog("ÁP DỤNG KM", "KhuyenMaiSanPham", moTa, lichSuLogService.getCurrentUserId());

            if (errors.isEmpty()) {
                return ResponseEntity.ok(new ErrorResponse(200,"Áp dụng khuyến mãi thành công"));
            } else {
                String message = String.join(", ", errors);
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
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
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }
}
