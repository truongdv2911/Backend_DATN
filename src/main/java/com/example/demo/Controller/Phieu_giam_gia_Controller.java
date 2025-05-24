package com.example.demo.Controller;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Service.Phieu_giam_gia_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phieugiamgia")
public class Phieu_giam_gia_Controller {

    @Autowired
    private Phieu_giam_gia_Service phieuGiamGiaService;

    // Create
    @PostMapping
    public PhieuGiamGia createPhieuGiamGia(@Valid @RequestBody PhieuGiamGiaDTO phieuGiamGiaDTO) {
        return phieuGiamGiaService.createPhieuGiamGia(phieuGiamGiaDTO);
    }

    // Read All
    @GetMapping
    public List<PhieuGiamGia> getAllPhieuGiamGia() {
        return phieuGiamGiaService.getAllPhieuGiamGia();
    }

    // Read One
    @GetMapping("/{id}")
    public PhieuGiamGia getPhieuGiamGiaById(@PathVariable Integer id) {
        return phieuGiamGiaService.getPhieuGiamGiaById(id);
    }

    // Update
    @PutMapping("/{id}")
    public PhieuGiamGia updatePhieuGiamGia(@PathVariable Integer id, @Valid @RequestBody PhieuGiamGiaDTO phieuGiamGiaDTO) {
        return phieuGiamGiaService.updatePhieuGiamGia(id, phieuGiamGiaDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deletePhieuGiamGia(@PathVariable Integer id) {
        phieuGiamGiaService.deletePhieuGiamGia(id);
    }
}
