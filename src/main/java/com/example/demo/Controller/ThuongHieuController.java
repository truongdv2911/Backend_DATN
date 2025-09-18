package com.example.demo.Controller;

import com.example.demo.DTOs.ThuongHieuDTO;
import com.example.demo.DTOs.XuatXuDTO;
import com.example.demo.Entity.ThuongHieu;
import com.example.demo.Entity.XuatXu;
import com.example.demo.Repository.ThuongHieuRepository;
import com.example.demo.Repository.XuatXuRepository;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/lego-store/thuong-hieu")
@RequiredArgsConstructor
public class ThuongHieuController {
    private final ThuongHieuRepository thuongHieuRepository;
    private final LichSuLogService lichSuLogService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok(thuongHieuRepository.findAllActive());
    }

    @PostMapping("/createTH")
    public ResponseEntity<?> createXX(@Valid @RequestBody ThuongHieuDTO thuongHieuDTO, BindingResult result){
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            if (thuongHieuRepository.existsByTen(thuongHieuDTO.getTen())) {
                throw new RuntimeException("Tên thuong hieu đã tồn tại!");
            }
            ThuongHieu resultObj = thuongHieuRepository.save(new ThuongHieu(null, thuongHieuDTO.getTen(), thuongHieuDTO.getMoTa(), 1, null));
            // Log lịch sử tạo mới
            String moTa = "Tạo mới thương hiệu: " + resultObj.getTen() + " - ID: " + resultObj.getId();
            lichSuLogService.saveLog("TẠO MỚI", "ThuongHieu", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(resultObj);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PutMapping("/updateTH/{id}")
    public ResponseEntity<?> updateXuatXu(@PathVariable Integer id, @Valid @RequestBody ThuongHieuDTO thuongHieuDTO, BindingResult result){
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElseThrow(() -> new RuntimeException("Khong tim thay id xuat xu"));
            if (!thuongHieu.getTen().equals(thuongHieuDTO.getTen())
                    && thuongHieuRepository.existsByTen(thuongHieuDTO.getTen())) {
                throw new RuntimeException("Tên xuat xu đã tồn tại!");
            }
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(thuongHieu, thuongHieuDTO);
            String moTa = "Cập nhật thương hiệu ID: " + id + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "ThuongHieu", moTa, lichSuLogService.getCurrentUserId());
            ThuongHieu resultObj = thuongHieuRepository.save(new ThuongHieu(id, thuongHieuDTO.getTen(), thuongHieuDTO.getMoTa(), 1, null));
            return ResponseEntity.ok(resultObj);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @DeleteMapping("/deleteTH/{id}")
    public ResponseEntity<?> deleteXuatXu(@PathVariable Integer id){
        try {
            ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElseThrow(() -> new RuntimeException("Khong tim thay id xuat xu"));
            boolean hasProductsInStock = thuongHieu.getSanPhams().stream()
                    .anyMatch(sanPham -> {
                        Integer soLuongTon = sanPham.getSoLuongTon();
                        return soLuongTon != null && soLuongTon > 0;
                    });
            if (hasProductsInStock) {
                throw new RuntimeException("Không thể xóa thuong hieu. Vẫn còn sản phẩm trong kho (soLuongTon > 0). Vui lòng bán hết tất cả sản phẩm trước khi xóa thuong hieu.");
            }
            thuongHieu.setIsDelete(0);
            thuongHieuRepository.save(thuongHieu);
            // Log lịch sử xóa
            String moTa = "Xóa thương hiệu ID: " + id + ", Tên: " + thuongHieu.getTen();
            lichSuLogService.saveLog("XÓA", "ThuongHieu", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(new ErrorResponse(200,"xoa thanh cong"));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }
}
