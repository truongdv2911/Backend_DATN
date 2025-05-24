package com.example.demo.Controller;

import com.example.demo.DTOs.DanhMucDTO;
import com.example.demo.Entity.DanhMuc;
import com.example.demo.Service.Danh_muc_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danhmuc")
public class Danh_muc_Controller {

    @Autowired
    private Danh_muc_Service danhMucService;

    // Create
    @PostMapping
    public DanhMuc createDanhMuc(@Valid @RequestBody DanhMucDTO danhMucDTO) {
        return danhMucService.createDanhMuc(danhMucDTO);
    }

    // Read All
    @GetMapping
    public List<DanhMuc> getAllDanhMuc() {
        return danhMucService.getAllDanhMuc();
    }

    // Read One
    @GetMapping("/{id}")
    public DanhMuc getDanhMucById(@PathVariable Integer id) {
        return danhMucService.getDanhMucById(id);
    }

    // Update
    @PutMapping("/{id}")
    public DanhMuc updateDanhMuc(@PathVariable Integer id, @Valid @RequestBody DanhMucDTO danhMucDTO) {
        return danhMucService.updateDanhMuc(id, danhMucDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteDanhMuc(@PathVariable Integer id) {
        danhMucService.deleteDanhMuc(id);
    }
}
