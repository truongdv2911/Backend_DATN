package com.example.demo.Controller;

import com.example.demo.DTOs.GioHangChiTietDTO;
import com.example.demo.Entity.GioHangChiTiet;
import com.example.demo.Service.Gio_hang_chi_tiet_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giohangchitiet")
public class Gio_hang_chi_tiet_Controller {

    @Autowired
    private Gio_hang_chi_tiet_Service gioHangChiTietService;

    // Create
    @PostMapping
    public GioHangChiTiet createGioHangChiTiet(@Valid @RequestBody GioHangChiTietDTO gioHangChiTietDTO) {
        return gioHangChiTietService.createGioHangChiTiet(gioHangChiTietDTO);
    }

    // Read All
    @GetMapping
    public List<GioHangChiTiet> getAllGioHangChiTiet() {
        return gioHangChiTietService.getAllGioHangChiTiet();
    }

    // Read One
    @GetMapping("/{id}")
    public GioHangChiTiet getGioHangChiTietById(@PathVariable Integer id) {
        return gioHangChiTietService.getGioHangChiTietById(id);
    }

    // Update
    @PutMapping("/{id}")
    public GioHangChiTiet updateGioHangChiTiet(@PathVariable Integer id, @Valid @RequestBody GioHangChiTietDTO gioHangChiTietDTO) {
        return gioHangChiTietService.updateGioHangChiTiet(id, gioHangChiTietDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteGioHangChiTiet(@PathVariable Integer id) {
        gioHangChiTietService.deleteGioHangChiTiet(id);
    }
}
