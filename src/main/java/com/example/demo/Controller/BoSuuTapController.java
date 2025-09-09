package com.example.demo.Controller;

import com.example.demo.DTOs.BoSuuTapDTO;
import com.example.demo.Entity.BoSuuTap;
import com.example.demo.Repository.Bo_suu_tap_Repo;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.Bo_suu_tap_Service;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/bosuutap")
@RequiredArgsConstructor
public class BoSuuTapController {
    private final Bo_suu_tap_Service boSuuTapService;
    private final Bo_suu_tap_Repo bo;
    private final LichSuLogService lichSuLogService;

    @PostMapping("/Create")
    public ResponseEntity<?> createBoSuuTap(@Valid @RequestBody BoSuuTapDTO boSuuTapDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            BoSuuTap result = boSuuTapService.createBoSuuTap(boSuuTapDTO);
            // Log lịch sử tạo mới
            String moTa = "Tạo mới bộ sưu tập: " + result.getTenBoSuuTap() + " - ID: " + result.getId();
            lichSuLogService.saveLog("TẠO MỚI", "BoSuuTap", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllBoSuuTap() {
        try {
            List<BoSuuTap> list = boSuuTapService.getAllBoSuuTap();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getBoSuuTapById(@PathVariable Integer id) {
        try {
            BoSuuTap result = boSuuTapService.getBoSuuTapById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateBoSuuTap(@PathVariable Integer id, @Valid @RequestBody BoSuuTapDTO boSuuTapDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            BoSuuTap boSuuTap = bo.findById(id).orElseThrow(()-> new RuntimeException("khong tim thay id bo suu tap"));
            if (!isDifferent(boSuuTapDTO, boSuuTap)) {
                throw new IllegalArgumentException("Không có thay đổi nào để cập nhật");
            }
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(boSuuTap, boSuuTapDTO);
            String moTa = "Cập nhật bộ sưu tập ID: " + id + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "BoSuuTap", moTa, lichSuLogService.getCurrentUserId());
            BoSuuTap result = boSuuTapService.updateBoSuuTap(id, boSuuTapDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteBoSuuTap(@PathVariable Integer id) {
        try {
            BoSuuTap boSuuTap = bo.findById(id).orElse(null);
            boSuuTapService.deleteBoSuuTap(id);
            // Log lịch sử xóa
            String moTa = "Xóa bộ sưu tập ID: " + id + (boSuuTap != null ? (", Tên: " + boSuuTap.getTenBoSuuTap()) : "");
            lichSuLogService.saveLog("XÓA", "BoSuuTap", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(new ErrorResponse(200,"Xóa thành công"));
        } catch (RuntimeException e) {
            // Xử lý trường hợp không thể xóa do còn sản phẩm trong kho
            if (e.getMessage().contains("Vẫn còn sản phẩm trong kho")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    public static boolean isDifferent(BoSuuTapDTO dto, BoSuuTap entity) {
        if (dto == null || entity == null) return true;

        return !Objects.equals(dto.getTenBoSuuTap(), entity.getTenBoSuuTap())
                || !Objects.equals(dto.getMoTa(), entity.getMoTa())
                || !Objects.equals(dto.getNamPhatHanh(), entity.getNamPhatHanh());
    }
}
