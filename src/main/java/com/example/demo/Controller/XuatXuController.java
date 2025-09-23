package com.example.demo.Controller;

import com.example.demo.DTOs.XuatXuDTO;
import com.example.demo.Entity.SanPham;
import com.example.demo.Entity.ThuongHieu;
import com.example.demo.Entity.XuatXu;
import com.example.demo.Repository.XuatXuRepository;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/lego-store/xuatXu")
public class XuatXuController {
    private final XuatXuRepository xuatXuRepository;
    private final LichSuLogService lichSuLogService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok(xuatXuRepository.findAllActive());
    }

    @PostMapping("/createXuatXu")
    public ResponseEntity<?> createXX(@Valid @RequestBody XuatXuDTO xuatXuDTO, BindingResult result){
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }

            if (xuatXuRepository.existsByTenIgnoreCase(xuatXuDTO.getTen())) {
                throw new RuntimeException("Tên bộ sưu tập đã tồn tại!");
            }
            XuatXu resultObj = xuatXuRepository.save(new XuatXu(null, xuatXuDTO.getTen(), xuatXuDTO.getMoTa(), 1, null));
            // Log lịch sử tạo mới
            String moTa = "Tạo mới xuất xứ: " + resultObj.getTen() + " - ID: " + resultObj.getId();
            lichSuLogService.saveLog("TẠO MỚI", "XuatXu", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(resultObj);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PutMapping("/updateXuatXu/{id}")
    public ResponseEntity<?> updateXuatXu(@PathVariable Integer id, @Valid @RequestBody XuatXuDTO xuatXuDTO, BindingResult result){
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            XuatXu xuatXu = xuatXuRepository.findById(id).orElseThrow(() -> new RuntimeException("Khong tim thay id xuat xu"));
            if (!xuatXu.getTen().equals(xuatXuDTO.getTen())
                    && xuatXuRepository.existsByTenIgnoreCase(xuatXuDTO.getTen())) {
                throw new RuntimeException("Tên xuat xu đã tồn tại!");
            }
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(xuatXu, xuatXuDTO);
            String moTa = "Cập nhật xuất xứ ID: " + id + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "XuatXu", moTa, lichSuLogService.getCurrentUserId());
            List<SanPham> sp = xuatXu.getSanPhams();
            xuatXu.setSanPhams(sp);
            XuatXu resultObj = xuatXuRepository.save(new XuatXu(id, xuatXuDTO.getTen(), xuatXuDTO.getMoTa(), 1, sp));
            return ResponseEntity.ok(resultObj);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @DeleteMapping("/deleteXX/{id}")
    public ResponseEntity<?> deleteXuatXu(@PathVariable Integer id){
        try {
            XuatXu xuatXu = xuatXuRepository.findById(id).orElseThrow(() -> new RuntimeException("Khong tim thay id xuat xu"));
            boolean hasProductsInStock = xuatXu.getSanPhams().stream()
                    .anyMatch(sanPham -> {
                        Integer soLuongTon = sanPham.getSoLuongTon();
                        return soLuongTon != null && soLuongTon > 0;
                    });
            if (hasProductsInStock) {
                throw new RuntimeException("Không thể xóa xuất xứ. Vẫn còn sản phẩm trong kho (số lượng tồn > 0). Vui lòng bán hết tất cả sản phẩm trước khi xóa xuất xứ.");
            }
            xuatXu.setIsDelete(0);
            xuatXuRepository.save(xuatXu);
            // Log lịch sử xóa
            String moTa = "Xóa xuất xứ ID: " + id + ", Tên: " + xuatXu.getTen();
            lichSuLogService.saveLog("XÓA", "XuatXu", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(new ErrorResponse(200,"xoa thanh cong"));
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }
}
