package com.example.demo.Controller;

import com.example.demo.DTOs.SanPhamUpdateDTO;
import com.example.demo.DTOs.SanPhamWithImagesDTO;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.SanPhamKMResponse;
import com.example.demo.Responses.SanPhamResponseDTO;
import com.example.demo.Service.San_pham_Service;
import com.example.demo.Service.AnhSpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/sanpham")
@RequiredArgsConstructor
public class San_pham_Controller {

    private final San_pham_Service sanPhamService;
    private final San_pham_Repo san_pham_repo;
    private final AnhSpService anhSpService;

    @PostMapping("/Create")
    public ResponseEntity<?> createSanPham(@Valid @RequestBody SanPhamUpdateDTO sanPhamDTO, BindingResult result) {
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

    @PostMapping(value = "/CreateWithFileImages", consumes = "multipart/form-data")
    public ResponseEntity<?> createSanPhamWithFileImages(
            @Valid @ModelAttribute SanPhamUpdateDTO sanPhamDTO,
            @RequestParam("files") MultipartFile[] files,
            BindingResult result
            ) {
        if (result.hasErrors()) {
            List<String> listErrors = result.getFieldErrors().stream()
                    .map(errors -> errors.getDefaultMessage()).toList();
            return ResponseEntity.badRequest().body(listErrors);
        }
        try {
            // Tạo sản phẩm trước
            SanPhamResponseDTO sanPhamResponse = sanPhamService.createSanPham(sanPhamDTO);
            
            // Lấy sản phẩm vừa tạo
            SanPham sanPham = sanPhamService.getSanPhamById(sanPhamResponse.getId());
            
            // Upload và tạo ảnh sử dụng AnhSpService
            anhSpService.uploadAndCreateAnhSp(files, null, null, sanPham.getId());
            
            return ResponseEntity.ok(sanPhamService.convertToResponseDTO(sanPham));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal Server Error"));
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllSanPhams() {
        try {
            List<SanPhamResponseDTO> responseDTOs = sanPhamService.getAllSanPhamResponses();
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ReadAllV2")
    public ResponseEntity<?> getAllSanPhamsV2() {
        try {
            List<SanPhamKMResponse> responseDTOs = sanPhamService.getSanPhamKhuyenMaiFull();
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
    public ResponseEntity<?> updateSanPham(@PathVariable Integer id, @Valid @RequestBody SanPhamUpdateDTO sanPhamDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErrors = result.getFieldErrors().stream()
                        .map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErrors);
            }
            SanPham sanPham = san_pham_repo.findById(id).orElseThrow(()-> new RuntimeException("khong tim thay id san pham"));
            if (!isDifferent(sanPhamDTO, sanPham)){
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
    public static boolean isDifferent(SanPhamUpdateDTO sanPhamDTO, SanPham sanPham) {
        return !Objects.equals(sanPhamDTO.getTenSanPham(), sanPham.getTenSanPham()) ||
                !Objects.equals(sanPhamDTO.getDoTuoi(), sanPham.getDoTuoi()) ||
                !Objects.equals(sanPhamDTO.getMoTa(), sanPham.getMoTa()) ||
                !(sanPhamDTO.getGia().compareTo(sanPham.getGia()) == 0) ||
                !Objects.equals(sanPhamDTO.getSoLuongManhGhep(), sanPham.getSoLuongManhGhep()) ||
                !Objects.equals(sanPhamDTO.getSoLuongTon(), sanPham.getSoLuongTon()) ||
                !Objects.equals(sanPhamDTO.getSoLuongVote(), sanPham.getSoLuongVote()) ||
                !Objects.equals(sanPhamDTO.getDanhGiaTrungBinh(), sanPham.getDanhGiaTrungBinh()) ||
                !Objects.equals(sanPhamDTO.getDanhMucId(),
                        sanPham.getDanhMuc() != null ? sanPham.getDanhMuc().getId() : null) ||
                !Objects.equals(sanPhamDTO.getBoSuuTapId(),
                        sanPham.getBoSuuTap() != null ? sanPham.getBoSuuTap().getId() : null);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchSanPham(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime toDate,
            @RequestParam(required = false) Integer idDanhMuc,
            @RequestParam(required = false) Integer idBoSuuTap,
            @RequestParam(required = false) Integer tuoiMin,
            @RequestParam(required = false) Integer tuoiMax
    ) {
        List<SanPhamKMResponse> result = sanPhamService.timKiemSanPham(
                keyword, giaMin, giaMax,
                idDanhMuc, idBoSuuTap, tuoiMin, tuoiMax
        );
        return ResponseEntity.ok(result);
    }

}
