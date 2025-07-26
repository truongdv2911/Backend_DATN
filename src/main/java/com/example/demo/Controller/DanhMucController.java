package com.example.demo.Controller;

import com.example.demo.DTOs.DanhMucDTO;
import com.example.demo.Entity.DanhMuc;
import com.example.demo.Repository.Danh_muc_Repo;
import com.example.demo.Service.Danh_muc_Service;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/danhmuc")
public class DanhMucController {

    @Autowired
    private Danh_muc_Service danhMucService;

    @Autowired
    private Danh_muc_Repo danh_muc_repo;

    @Autowired
    private LichSuLogService lichSuLogService;


    @PostMapping("/Create")
    public ResponseEntity<?> createDanhMuc(@Valid @RequestBody DanhMucDTO danhMucDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String message = String.join(", ", bindingResult.getAllErrors().stream().map(Object::toString).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            DanhMuc result = danhMucService.createDanhMuc(danhMucDTO);
            // Log lịch sử tạo mới
            String moTa = "Tạo mới danh mục: " + result.getTenDanhMuc() + " - ID: " + result.getId();
            lichSuLogService.saveLog("TẠO MỚI", "DanhMuc", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllDanhMuc() {
        try {
            List<DanhMuc> list = danhMucService.getAllDanhMuc();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getDanhMucById(@PathVariable Integer id) {
        try {
            DanhMuc result = danhMucService.getDanhMucById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
        }
    }


    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateDanhMuc(@PathVariable Integer id, @Valid @RequestBody DanhMucDTO danhMucDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String message = String.join(", ", bindingResult.getAllErrors().stream().map(Object::toString).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            DanhMuc danhMuc = danh_muc_repo.findById(id).orElseThrow(()-> new RuntimeException("khong tim thay id danh muc"));
            if (!isDifferent(danhMucDTO, danhMuc)) {
                throw new IllegalArgumentException("Không có thay đổi nào để cập nhật");
            }
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(danhMuc, danhMucDTO);
            String moTa = "Cập nhật danh mục ID: " + id + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "DanhMuc", moTa, lichSuLogService.getCurrentUserId());
            DanhMuc result = danhMucService.updateDanhMuc(id, danhMucDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteDanhMuc(@PathVariable Integer id) {
        try {
            DanhMuc danhMuc = danh_muc_repo.findById(id).orElse(null);
            danhMucService.deleteDanhMuc(id);
            // Log lịch sử xóa
            String moTa = "Xóa danh mục ID: " + id + (danhMuc != null ? (", Tên: " + danhMuc.getTenDanhMuc()) : "");
            lichSuLogService.saveLog("XÓA", "DanhMuc", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(new ErrorResponse(200,"Xóa thành công"));
        } catch (RuntimeException e) {
            // Xử lý trường hợp không thể xóa do còn sản phẩm trong kho
            if (e.getMessage().contains("Vẫn còn sản phẩm trong kho")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }

    public static boolean isDifferent(DanhMucDTO dto, DanhMuc entity) {
        if (dto == null || entity == null) return true;

        return !Objects.equals(dto.getTenDanhMuc(), entity.getTenDanhMuc())
                || !Objects.equals(dto.getMoTa(), entity.getMoTa());
    }
}
