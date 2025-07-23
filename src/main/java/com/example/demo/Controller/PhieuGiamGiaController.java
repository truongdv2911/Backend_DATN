package com.example.demo.Controller;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Responses.ChiTietPhieuResponse;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.Phieu_giam_gia_Service;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/phieugiamgia")
@RequiredArgsConstructor
public class PhieuGiamGiaController {
    private final Phieu_giam_gia_Service phieuGiamGiaService;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;
    private final LichSuLogService lichSuLogService;


    @PostMapping("/Create")
    public ResponseEntity<?> createPhieuGiamGia(@Valid @RequestBody PhieuGiamGiaDTO phieuGiamGiaDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            PhieuGiamGia resultObj = phieuGiamGiaService.createPhieuGiamGia(phieuGiamGiaDTO);
            // Log lịch sử tạo mới
            String moTa = "Tạo mới phiếu giảm giá: " + resultObj.getTenPhieu() + " - ID: " + resultObj.getMaPhieu();
            lichSuLogService.saveLog("TẠO MỚI", "PhieuGiamGia", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(resultObj);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllPhieuGiamGia() {
        try {
            List<PhieuGiamGia> list = phieuGiamGiaService.getAllPhieuGiamGia();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getPhieuGiamGiaById(@PathVariable Integer id) {
        try {
            PhieuGiamGia result = phieuGiamGiaService.getPhieuGiamGiaById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
        }
    }

    @GetMapping("/loai")
    public ResponseEntity<?> getByLoaiPhieuGiam(@RequestParam String loaiPhieuGiam) {
        try {
            List<PhieuGiamGia> list = phieuGiamGiaService.getByLoaiPhieuGiam(loaiPhieuGiam);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }
    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updatePhieuGiamGia(@PathVariable Integer id,
                                                @Valid @RequestBody PhieuGiamGiaDTO phieuGiamGiaDTO,
                                                BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            PhieuGiamGia phieuGiamGia = phieuGiamGiaRepo.findById(id).orElseThrow(()-> new RuntimeException("khong tim thay id phieu giam"));
            if (!isDifferent(phieuGiamGiaDTO, phieuGiamGia)){
                return ResponseEntity.badRequest().body("Không có thay đổi nào được thực hiện.");
            }
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(phieuGiamGia, phieuGiamGiaDTO);
            String moTa = "Cập nhật phiếu giảm giá Mã: " + phieuGiamGia.getMaPhieu() + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "PhieuGiamGia", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(phieuGiamGiaService.updatePhieuGiamGia(id, phieuGiamGiaDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

//    @DeleteMapping("/changeStatus/{id}")
//    public ResponseEntity<?> changeStatus(@PathVariable Integer id){
//        try {
//            return ResponseEntity.ok(phieuGiamGiaService.ThayDoiTrangThaiPhieuGiamGia(id));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }

    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deletePhieuGiamGia(@PathVariable Integer id) {
        try {
            PhieuGiamGia phieuGiamGia = phieuGiamGiaRepo.findById(id).orElse(null);
            phieuGiamGiaService.deletePhieuGiamGia(id);
            // Log lịch sử xóa
            String moTa = "Xóa phiếu giảm giá mã: " + phieuGiamGia.getMaPhieu() + (phieuGiamGia != null ? (", Tên: " + phieuGiamGia.getTenPhieu()) : "");
            lichSuLogService.saveLog("XÓA", "PhieuGiamGia", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/getPGGDuocPhepApDung")
    public ResponseEntity<?> getPGG(@RequestParam("tamTinh") BigDecimal tamTinh){
        try {
            return ResponseEntity.ok(phieuGiamGiaRepo.getPGGPhuHop(tamTinh));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loi khi tai phieu"+e.getMessage());
        }
    }

    @GetMapping("/getDetail/{id}")
    public ResponseEntity<?> detailPhieu(@PathVariable Integer id){
        try {
            ChiTietPhieuResponse chiTietPhieuResponse = phieuGiamGiaService.getDetail(id);
            return ResponseEntity.ok(chiTietPhieuResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("loi"+e.getMessage());
        }
    }

    public static boolean isDifferent(PhieuGiamGiaDTO other, PhieuGiamGia phieuGiamGia) {
        if (other == null) return true;

        return !Objects.equals(phieuGiamGia.getSoLuong(), other.getSoLuong())
                || !Objects.equals(phieuGiamGia.getLoaiPhieuGiam(), other.getLoaiPhieuGiam())
                || compareBigDecimal(phieuGiamGia.getGiaTriGiam(), other.getGiaTriGiam())
                || compareBigDecimal(phieuGiamGia.getGiamToiDa(), other.getGiamToiDa())
                || compareBigDecimal(phieuGiamGia.getGiaTriToiThieu(), other.getGiaTriToiThieu())
                || !Objects.equals(phieuGiamGia.getNgayBatDau(), other.getNgayBatDau())
                || !Objects.equals(phieuGiamGia.getNgayKetThuc(), other.getNgayKetThuc());
    }
    private static boolean compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return false; // giống nhau
        if (a == null || b == null) return true;  // khác nhau
        return a.compareTo(b) != 0;
    }
}
