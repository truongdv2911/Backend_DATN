package com.example.demo.Controller;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Service.Phieu_giam_gia_Service;
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
@RequestMapping("/api/phieugiamgia")
@RequiredArgsConstructor
public class Phieu_giam_gia_Controller {
    private final Phieu_giam_gia_Service phieuGiamGiaService;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;


    @PostMapping("/Create")
    public ResponseEntity<?> createPhieuGiamGia(@Valid @RequestBody PhieuGiamGiaDTO phieuGiamGiaDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            return ResponseEntity.ok(phieuGiamGiaService.createPhieuGiamGia(phieuGiamGiaDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllPhieuGiamGia() {
        try {
            List<PhieuGiamGia> list = phieuGiamGiaService.getAllPhieuGiamGia();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getPhieuGiamGiaById(@PathVariable Integer id) {
        try {
            PhieuGiamGia result = phieuGiamGiaService.getPhieuGiamGiaById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/loai")
    public ResponseEntity<?> getByLoaiPhieuGiam(@RequestParam String loaiPhieuGiam) {
        try {
            List<PhieuGiamGia> list = phieuGiamGiaService.getByLoaiPhieuGiam(loaiPhieuGiam);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
            phieuGiamGiaService.deletePhieuGiamGia(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
