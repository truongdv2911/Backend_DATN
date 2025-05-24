package com.example.demo.Controller;

import com.example.demo.DTOs.Anh_sp_DTO;
import com.example.demo.Entity.AnhSp;
import com.example.demo.Service.Anh_sp_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/anhsp")
public class Anh_sp_Controller {

    @Autowired
    private Anh_sp_Service anhSpService;

    // Create
    @PostMapping
    public AnhSp createAnhSp(@Valid @RequestBody Anh_sp_DTO anhSpDTO) {
        return anhSpService.createAnhSp(anhSpDTO);
    }

    // Read All
    @GetMapping
    public List<AnhSp> getAllAnhSp() {
        return anhSpService.getAllAnhSp();
    }

    // Read One
    @GetMapping("/{id}")
    public AnhSp getAnhSpById(@PathVariable Integer id) {
        return anhSpService.getAnhSpById(id);
    }

    // Update
    @PutMapping("/update/{id}")
    public AnhSp updateAnhSp(@PathVariable Integer id, @Valid @RequestBody Anh_sp_DTO anhSpDTO) {
        return anhSpService.updateAnhSp(id, anhSpDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteAnhSp(@PathVariable Integer id) {
        anhSpService.deleteAnhSp(id);
    }

    // Upload and Create
    @PostMapping("/upload-image")
    public AnhSp uploadAndCreateAnhSp(
            @RequestParam("file") MultipartFile file,
            @RequestParam("moTa") String moTa,
            @RequestParam("thuTu") Integer thuTu,
            @RequestParam("anhChinh") Boolean anhChinh,
            @RequestParam("sanpham") Integer sanphamId) {
        return anhSpService.uploadAndCreateAnhSp(file, moTa, thuTu, anhChinh, sanphamId);
    }

    // View image
    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<?> viewImage(@PathVariable String fileName) {
        try {
            UrlResource resource = anhSpService.loadImage(fileName);
            Path path = resource.getFile().toPath();
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
