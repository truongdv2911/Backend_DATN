package com.example.demo.Controller;

import com.example.demo.DTOs.KMUpdateDTO;
import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Repository.Khuyen_mai_Repo;
import com.example.demo.Responses.ChiTietKMResponse;
import com.example.demo.Responses.ChiTietPhieuResponse;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.Khuyen_mai_Service;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/khuyenmai")
@RequiredArgsConstructor
public class KhuyenMaiController {


    private final Khuyen_mai_Service khuyenMaiService;
    private final Khuyen_mai_Repo khuyenMaiRepo;
    private final LichSuLogService lichSuLogService;


    @PostMapping("/Create")
    public ResponseEntity<?> createKhuyenMai(@Valid @RequestBody KhuyenMaiDTO khuyenMaiDTO, BindingResult result) {
        try {
            if (result.hasErrors()){
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            if ( khuyenMaiDTO.getNgayBatDau().isAfter(khuyenMaiDTO.getNgayKetThuc())) {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "Ngày bắt đầu phải trước ngày kết thúc"));
            }
            KhuyenMai results = khuyenMaiService.createKhuyenMai(khuyenMaiDTO);
            // Log lịch sử tạo mới
            String moTa = "Tạo mới khuyến mãi: " + results.getTenKhuyenMai() + " - ID: " + results.getMaKhuyenMai();
            lichSuLogService.saveLog("TẠO MỚI", "KhuyenMai", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllKhuyenMai() {
        try {
            List<KhuyenMai> list = khuyenMaiService.getAllKhuyenMai();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }




    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getKhuyenMaiById(@PathVariable Integer id) {
        try {
            KhuyenMai result = khuyenMaiService.getKhuyenMaiById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
        }
    }

//1111
    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateKhuyenMai(@PathVariable Integer id, @Valid @RequestBody KMUpdateDTO khuyenMaiDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String message = String.join(", ", bindingResult.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            if (khuyenMaiDTO.getNgayBatDau() != null && khuyenMaiDTO.getNgayKetThuc() != null
                    && khuyenMaiDTO.getNgayBatDau().isAfter(khuyenMaiDTO.getNgayKetThuc())) {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "Ngày bắt đầu phải trước ngày kết thúc"));
            }
            KhuyenMai khuyenMai = khuyenMaiRepo.findById(id).orElseThrow(()-> new RuntimeException("khong tim thay id khuyen mai"));
            if (!isDifferent(khuyenMaiDTO, khuyenMai)) {
                throw new IllegalArgumentException("Không có thay đổi nào để cập nhật");
            }
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(khuyenMai, khuyenMaiDTO);
            String moTa = "Cập nhật khuyến mãi mã: " + khuyenMai.getMaKhuyenMai() + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "KhuyenMai", moTa, lichSuLogService.getCurrentUserId());
            KhuyenMai result = khuyenMaiService.updateKhuyenMai(id, khuyenMaiDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteKhuyenMai(@PathVariable Integer id) {
        try {
            KhuyenMai khuyenMai = khuyenMaiRepo.findById(id).orElse(null);
            khuyenMaiService.deleteKhuyenMai(id);
            // Log lịch sử xóa
            String moTa = "Ngừng khuyến mãi Mã: " + khuyenMai.getMaKhuyenMai() + (khuyenMai != null ? (", Tên: " + khuyenMai.getTenKhuyenMai()) : "");
            lichSuLogService.saveLog("Ngừng", "KhuyenMai", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(new ErrorResponse(200, "Ẩn thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @GetMapping("/getDetail/{id}")
    public ResponseEntity<?> detailKM(@PathVariable Integer id){
        try {
            ChiTietKMResponse response = khuyenMaiService.getDetailKM(id);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    public static boolean isDifferent(KMUpdateDTO dto, KhuyenMai entity) {
        if (dto == null || entity == null) return true;

        return !Objects.equals(dto.getNgayBatDau(), entity.getNgayBatDau())
                || !Objects.equals(dto.getNgayKetThuc(), entity.getNgayKetThuc())
                || !Objects.equals(dto.getTenKhuyenMai(), entity.getTenKhuyenMai())
                || !Objects.equals(dto.getPhanTramKhuyenMai(), entity.getPhanTramKhuyenMai());
    }
    private static boolean compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return false; // giống nhau
        if (a == null || b == null) return true;  // khác nhau
        return a.compareTo(b) != 0;
    }
}
