package com.example.demo.Controller;

import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Service.Khuyen_mai_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khuyenmai")
@RequiredArgsConstructor
public class Khuyen_mai_Controller {


    private final Khuyen_mai_Service khuyenMaiService;


    @PostMapping("/Create")
    public ResponseEntity<?> createKhuyenMai(@Valid @RequestBody KhuyenMaiDTO khuyenMaiDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            if ( khuyenMaiDTO.getNgayBatDau().isAfter(khuyenMaiDTO.getNgayKetThuc())) {
                return ResponseEntity.badRequest().body("Ngày bắt đầu phải trước ngày kết thúc");
            }
            KhuyenMai result = khuyenMaiService.createKhuyenMai(khuyenMaiDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllKhuyenMai() {
        try {
            List<KhuyenMai> list = khuyenMaiService.getAllKhuyenMai();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }




    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getKhuyenMaiById(@PathVariable Integer id) {
        try {
            KhuyenMai result = khuyenMaiService.getKhuyenMaiById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateKhuyenMai(@PathVariable Integer id, @Valid @RequestBody KhuyenMaiDTO khuyenMaiDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            if (khuyenMaiDTO.getNgayBatDau() != null && khuyenMaiDTO.getNgayKetThuc() != null
                    && khuyenMaiDTO.getNgayBatDau().isAfter(khuyenMaiDTO.getNgayKetThuc())) {
                return ResponseEntity.badRequest().body("Ngày bắt đầu phải trước ngày kết thúc");
            }
            KhuyenMai result = khuyenMaiService.updateKhuyenMai(id, khuyenMaiDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteKhuyenMai(@PathVariable Integer id) {
        try {
            khuyenMaiService.deleteKhuyenMai(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
