package com.example.demo.Controller;

import com.example.demo.DTOs.SanPhamDTO;
import com.example.demo.Entity.SanPham;
import com.example.demo.Service.San_pham_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sanpham")
@RequiredArgsConstructor
public class San_pham_Controller {

    private final San_pham_Service sanPhamService;

    // Create
    @PostMapping
    public SanPham createSanPham(@Valid @RequestBody SanPhamDTO sanPhamDTO) {
        return sanPhamService.createSanPham(sanPhamDTO);
    }

    // Read All with Pagination
//    @GetMapping
//    public Page<SanPham> getAllSanPhams(@RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return sanPhamService.getAllSanPhams(page, size);
//    }

    // Read One
    @GetMapping("/{id}")
    public SanPham getSanPhamById(@PathVariable Integer id) {
        return sanPhamService.getSanPhamById(id);
    }

    // Update
    @PutMapping("/{id}")
    public SanPham updateSanPham(@PathVariable Integer id, @Valid @RequestBody SanPhamDTO sanPhamDTO) {
        return sanPhamService.updateSanPham(id, sanPhamDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteSanPham(@PathVariable Integer id) {
        sanPhamService.deleteSanPham(id);
    }
}
