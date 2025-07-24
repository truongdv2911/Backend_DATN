package com.example.demo.Controller;

import com.example.demo.DTOs.ViGiamGiaDTO;
import com.example.demo.Entity.ViPhieuGiamGia;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.PhieuGiamGiaResponse;
import com.example.demo.Service.ViPhiGiamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lego-store/vi-phieu-giam-gia")
@RequiredArgsConstructor
public class ViPhieuGiamController {
    private final ViPhiGiamService service;

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
}
