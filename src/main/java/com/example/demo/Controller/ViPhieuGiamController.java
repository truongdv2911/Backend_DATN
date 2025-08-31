package com.example.demo.Controller;

import com.example.demo.DTOs.PGGUserDTO;
import com.example.demo.DTOs.ViGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Entity.ViPhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.PhieuGiamGiaResponse;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Service.Phieu_giam_gia_Service;
import com.example.demo.Service.ViPhiGiamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lego-store/vi-phieu-giam-gia")
@RequiredArgsConstructor
public class ViPhieuGiamController {
    private final ViPhiGiamService service;
    private final Phieu_giam_gia_Service phieu_giam_gia_service;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;
    private final LichSuLogService lichSuLogService;

    @PostMapping("/them")
    public ResponseEntity<?> themPhieu(@RequestBody ViGiamGiaDTO dto) {
        try {
            ViPhieuGiamGia vi = service.addVoucherToWallet(dto);
            return ResponseEntity.ok(vi);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getDanhSachPhieuTrongTui(
            @PathVariable Integer userId,
            @RequestParam(required = false) String trangThai) {
        List<PhieuGiamGiaResponse> list = service.getPhieuTrongTuiTheoTrangThai(userId, trangThai);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/apply-users")
    public ResponseEntity<?> assignVoucherToUsers(@Valid @RequestBody PGGUserDTO dto) {
        try {
            PhieuGiamGia voucher = phieuGiamGiaRepo.findById(dto.getPhieuGiamGiaId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));

            List<String> errors = phieu_giam_gia_service.assignVoucherToUsers(dto);
            int tong = dto.getListUserId().size();
            int fail = errors.size();
            int success = tong - fail;

            // Ghi log
            String moTa = String.format(
                    "Áp dụng voucher Mã: %s cho %d khách hàng. Thành công: %d, Thất bại: %d. Lỗi: %s",
                    voucher.getMaPhieu(), tong, success, fail, String.join(", ", errors)
            );
            lichSuLogService.saveLog("ÁP DỤNG VOUCHER", "ViPhieuGiamGia", moTa, lichSuLogService.getCurrentUserId());

            if (errors.isEmpty()) {
                return ResponseEntity.ok(new ErrorResponse(200, "Áp dụng voucher thành công"));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, String.join(", ", errors)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/doi-diem-phieu")
    public ResponseEntity<?> doiPhieu(@RequestBody ViGiamGiaDTO dto) {
        try {
            ViPhieuGiamGia vi = service.doiDiem(dto);
            return ResponseEntity.ok(vi);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
        }catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(500, ex.getMessage()));
        }
    }
}
