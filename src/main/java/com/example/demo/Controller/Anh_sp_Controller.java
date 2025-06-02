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
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/anhsp")
public class Anh_sp_Controller {
    @Autowired
    private Anh_sp_Service anhSpService;

    @GetMapping("/sanpham/{sanPhamId}")
    public ResponseEntity<?> getAnhBySanPham(@PathVariable Integer sanPhamId) {
        try {
            List<AnhSp> list = anhSpService.getAnhBySanPhamId(sanPhamId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không tìm thấy ảnh cho sản phẩm id: " + sanPhamId);
        }
    }

//    @PostMapping("/create")
//    public ResponseEntity<?> createAnhSp(@Valid @RequestBody Anh_sp_DTO anhSpDTO, BindingResult bindingResult) {
//        try {
//            if (bindingResult.hasErrors()) {
//                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
//            }
//            AnhSp result = anhSpService.createAnhSp(anhSpDTO);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }

    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllAnhSp() {
        try {
            List<AnhSp> list = anhSpService.getAllAnhSp();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/Readone/{id}")
    public ResponseEntity<?> getAnhSpById(@PathVariable Integer id) {
        try {
            AnhSp result = anhSpService.getAnhSpById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


//    @PutMapping("/Update/{id}")
//    public ResponseEntity<?> updateAnhSp(@PathVariable Integer id, @Valid @RequestBody Anh_sp_DTO anhSpDTO, BindingResult bindingResult) {
//        try {
//            if (bindingResult.hasErrors()) {
//                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
//            }
//            AnhSp result = anhSpService.updateAnhSp(id, anhSpDTO);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteAnhSp(@PathVariable Integer id) {
        try {
            anhSpService.deleteAnhSp(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadAndCreateAnhSp(
            @RequestParam("file") MultipartFile file,
            @RequestParam("moTa") String moTa,
            @RequestParam("thuTu") Integer thuTu,
            @RequestParam("anhChinh") Boolean anhChinh,
            @RequestParam("sanpham") Integer sanphamId) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không được để trống");
            }
            if (moTa != null && moTa.length() > 1000) {
                return ResponseEntity.badRequest().body("Mô tả không được vượt quá 1000 ký tự");
            }
            AnhSp result = anhSpService.uploadAndCreateAnhSp(file, moTa, thuTu, anhChinh, sanphamId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy ảnh: " + e.getMessage());
        }
    }

    @PutMapping("/update-image/{id}")
    public ResponseEntity<?> updateImage(
            @PathVariable Integer id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("moTa") String moTa,
            @RequestParam("thuTu") Integer thuTu,
            @RequestParam("anhChinh") Boolean anhChinh,
            @RequestParam("sanpham") Integer sanPhamId
    ) {
        try {
            AnhSp anhSp = anhSpService.updateAnhSp(id, file, moTa, thuTu, anhChinh, sanPhamId);
            return ResponseEntity.ok(anhSp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
