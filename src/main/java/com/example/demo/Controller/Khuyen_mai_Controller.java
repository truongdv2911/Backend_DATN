package com.example.demo.Controller;

import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Service.Khuyen_mai_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khuyenmai")
public class Khuyen_mai_Controller {

    @Autowired
    private Khuyen_mai_Service khuyenMaiService;

    // Create
    @PostMapping
    public KhuyenMai createKhuyenMai(@Valid @RequestBody KhuyenMaiDTO khuyenMaiDTO) {
        return khuyenMaiService.createKhuyenMai(khuyenMaiDTO);
    }

    // Read All
    @GetMapping
    public List<KhuyenMai> getAllKhuyenMai() {
        return khuyenMaiService.getAllKhuyenMai();
    }

    // Read One
    @GetMapping("/{id}")
    public KhuyenMai getKhuyenMaiById(@PathVariable Integer id) {
        return khuyenMaiService.getKhuyenMaiById(id);
    }

    // Update
    @PutMapping("/{id}")
    public KhuyenMai updateKhuyenMai(@PathVariable Integer id, @Valid @RequestBody KhuyenMaiDTO khuyenMaiDTO) {
        return khuyenMaiService.updateKhuyenMai(id, khuyenMaiDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteKhuyenMai(@PathVariable Integer id) {
        khuyenMaiService.deleteKhuyenMai(id);
    }
}
