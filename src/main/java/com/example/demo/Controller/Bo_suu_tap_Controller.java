package com.example.demo.Controller;

import com.example.demo.DTOs.BoSuuTapDTO;
import com.example.demo.Entity.BoSuuTap;
import com.example.demo.Service.Bo_suu_tap_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bosuutap")
public class Bo_suu_tap_Controller {

    @Autowired
    private Bo_suu_tap_Service boSuuTapService;

    // Create
    @PostMapping
    public BoSuuTap createBoSuuTap(@Valid @RequestBody BoSuuTapDTO boSuuTapDTO) {
        return boSuuTapService.createBoSuuTap(boSuuTapDTO);
    }

    // Read All
    @GetMapping
    public List<BoSuuTap> getAllBoSuuTap() {
        return boSuuTapService.getAllBoSuuTap();
    }

    // Read One
    @GetMapping("/{id}")
    public BoSuuTap getBoSuuTapById(@PathVariable Integer id) {
        return boSuuTapService.getBoSuuTapById(id);
    }

    // Update
    @PutMapping("/{id}")
    public BoSuuTap updateBoSuuTap(@PathVariable Integer id, @Valid @RequestBody BoSuuTapDTO boSuuTapDTO) {
        return boSuuTapService.updateBoSuuTap(id, boSuuTapDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteBoSuuTap(@PathVariable Integer id) {
        boSuuTapService.deleteBoSuuTap(id);
    }
}
