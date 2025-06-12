package com.example.demo.Controller;

import com.example.demo.DTOs.SanPhamDTO;

import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.SanPhamResponseDTO;
import com.example.demo.Service.San_pham_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/sanpham")
@RequiredArgsConstructor
public class San_pham_Controller {

    private final San_pham_Service sanPhamService;
    private final San_pham_Repo san_pham_repo;

    @PostMapping("/Create")
    public ResponseEntity<?> createSanPham(@Valid @RequestBody SanPhamDTO sanPhamDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErrors = result.getFieldErrors().stream()
                        .map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErrors);
            }
            SanPhamResponseDTO responseDTO = sanPhamService.createSanPham(sanPhamDTO);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal Server Error"));
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllSanPhams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<SanPhamResponseDTO> responseDTOs = sanPhamService.getAllSanPhamResponses(page, size);
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getSanPhamById(@PathVariable Integer id) {
        try {
            SanPham responseDTO = sanPhamService.getSanPhamById(id);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateSanPham(@PathVariable Integer id, @Valid @RequestBody SanPhamDTO sanPhamDTO,BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErrors = result.getFieldErrors().stream()
                        .map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErrors);
            }
            SanPham sanPham = san_pham_repo.findById(id).orElseThrow(()-> new RuntimeException("khong tim thay id san pham"));
            if (isDifferent(sanPhamDTO, sanPham)){
                return ResponseEntity.badRequest().body("Không có thay đổi nào được thực hiện.");
            }
            return ResponseEntity.ok(sanPhamService.updateSanPham(id, sanPhamDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteSanPham(@PathVariable Integer id) {
        try {
            sanPhamService.deleteSanPham(id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    public static boolean isDifferent(SanPhamDTO sanPhamDTO, SanPham sanPham) {
        return !Objects.equals(sanPhamDTO.getTenSanPham(), sanPham.getTenSanPham()) ||
                !Objects.equals(sanPhamDTO.getMaSanPham(), sanPham.getMaSanPham()) ||
                !Objects.equals(sanPhamDTO.getDoTuoi(), sanPham.getDoTuoi()) ||
                !Objects.equals(sanPhamDTO.getMoTa(), sanPham.getMoTa()) ||
                !Objects.equals(sanPhamDTO.getGia(), sanPham.getGia()) ||
                !Objects.equals(sanPhamDTO.getGiaKhuyenMai(), sanPham.getGiaKhuyenMai()) ||
                !Objects.equals(sanPhamDTO.getSoLuong(), sanPham.getSoLuong()) ||
                !Objects.equals(sanPhamDTO.getSoLuongManhGhep(), sanPham.getSoLuongManhGhep()) ||
                !Objects.equals(sanPhamDTO.getSoLuongTon(), sanPham.getSoLuongTon()) ||
                !Objects.equals(sanPhamDTO.getAnhDaiDien(), sanPham.getAnhDaiDien()) ||
                !Objects.equals(sanPhamDTO.getSoLuongVote(), sanPham.getSoLuongVote()) ||
                !Objects.equals(sanPhamDTO.getDanhGiaTrungBinh(), sanPham.getDanhGiaTrungBinh()) ||
                !Objects.equals(sanPhamDTO.getKhuyenMaiId(), sanPham.getKhuyenMai().getId()) ||
                !Objects.equals(sanPhamDTO.getDanhMucId(), sanPham.getDanhMuc().getId()) ||
                !Objects.equals(sanPhamDTO.getBoSuuTapId(), sanPham.getBoSuuTap().getId()) ||
                !Objects.equals(sanPhamDTO.getTrangThai(), sanPham.getTrangThai());
    }

}
