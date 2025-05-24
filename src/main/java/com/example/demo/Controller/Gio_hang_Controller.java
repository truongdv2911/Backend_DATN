package com.example.demo.Controller;

import com.example.demo.DTOs.GioHangDTO;
import com.example.demo.Entity.GioHang;
import com.example.demo.Service.Gio_hang_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giohang")
public class Gio_hang_Controller {

    @Autowired
    private Gio_hang_Service gioHangService;

    // Create
    @PostMapping
    public GioHang createGioHang(@Valid @RequestBody GioHangDTO gioHangDTO) {
        return gioHangService.createGioHang(gioHangDTO);
    }

    // Read All
    @GetMapping
    public List<GioHang> getAllGioHangs() {
        return gioHangService.getAllGioHangs();
    }

    // Read One
    @GetMapping("/{id}")
    public GioHang getGioHangById(@PathVariable Integer id) {
        return gioHangService.getGioHangById(id);
    }

    // Update
    @PutMapping("/{id}")
    public GioHang updateGioHang(@PathVariable Integer id, @Valid @RequestBody GioHangDTO gioHangDTO) {
        return gioHangService.updateGioHang(id, gioHangDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteGioHang(@PathVariable Integer id) {
        gioHangService.deleteGioHang(id);
    }
}
