package com.example.demo.Controller;

import com.example.demo.DTOs.SanPhamDTO;

import com.example.demo.Responses.SanPhamResponseDTO;
import com.example.demo.Service.San_pham_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sanpham")
@RequiredArgsConstructor
public class San_pham_Controller {

    private final San_pham_Service sanPhamService;

    @PostMapping("/Create")
    public ResponseEntity<?> createSanPham(@Valid @RequestBody SanPhamDTO sanPhamDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErrors = result.getFieldErrors().stream()
                        .map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErrors);
            }
            SanPhamResponseDTO responseDTO = sanPhamService.createSanPhamResponse(sanPhamDTO);
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
            SanPhamResponseDTO responseDTO = sanPhamService.getSanPhamResponseById(id);
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
            return ResponseEntity.ok(sanPhamService.updateSanPhamResponse(id, sanPhamDTO));
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


}
