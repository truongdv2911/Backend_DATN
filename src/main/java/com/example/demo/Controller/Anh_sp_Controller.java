package com.example.demo.Controller;

import com.example.demo.DTOs.Anh_sp_DTO;

import com.example.demo.Entity.AnhSp;
import com.example.demo.Service.AnhSpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
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
    private AnhSpService anhSpService;


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllAnhSp() {
        try {
            List<Anh_sp_DTO> anhSpList = anhSpService.getAllAnhSp();
            return ResponseEntity.ok(anhSpList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách ảnh: " + e.getMessage());
        }
    }


    @GetMapping("/Readone/{id}")
    public ResponseEntity<?> getAnhSpById(@PathVariable Integer id) {
        try {
            Anh_sp_DTO anhSp = anhSpService.getAnhSpById(id);
            return ResponseEntity.ok(anhSp);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy ảnh với ID: " + id + " - " + e.getMessage());
        }
    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteAnhSp(@PathVariable Integer id) {
        try {
            anhSpService.deleteAnhSp(id);
            return ResponseEntity.ok("Xóa ảnh với ID " + id + " thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không thể xóa ảnh với ID: " + id + " - " + e.getMessage());
        }
    }


    @PostMapping("/upload-images")
    public ResponseEntity<?> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "moTa", required = false) String moTa,
            @RequestParam(value = "anhChinh", required = false) Boolean anhChinh,
            @RequestParam("sanPhamId") Integer sanPhamId) {
        try {
            List<Anh_sp_DTO> uploadedImages = anhSpService.uploadAndCreateAnhSp(files, moTa, anhChinh, sanPhamId);
            return new ResponseEntity<>(uploadedImages, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Lỗi xác thực: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Lỗi khi upload ảnh: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


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
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy ảnh: " + fileName + " - " + e.getMessage());
        }
    }


    @PutMapping("/update-image/{id}")
    public ResponseEntity<?> updateImage(
            @PathVariable Integer id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("moTa") String moTa,
            @RequestParam("thuTu") Integer thuTu,
            @RequestParam("anhChinh") Boolean anhChinh,
            @RequestParam("sanpham") Integer sanPhamId) {

        try {
            Anh_sp_DTO updatedAnhSp = anhSpService.updateAnhSp(id, file, moTa, thuTu, anhChinh, sanPhamId);
            return ResponseEntity.ok(updatedAnhSp);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật ảnh: " + e.getMessage());
        }
    }

//    @GetMapping("/sanpham/{sanPhamId}")
//    public ResponseEntity<?> getAnhBySanPham(@PathVariable Integer sanPhamId) {
//        try {
//            List<Anh_sp_DTO> list = anhSpService.getAnhBySan(sanPhamId);
//            return ResponseEntity.ok(list);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Không tìm thấy ảnh cho sản phẩm ID: " + sanPhamId + " - " + e.getMessage());
//        }
//    }

    @GetMapping("/sanpham/{sanPhamId}")
    public ResponseEntity<?> getAnhBySanPham(@PathVariable Integer sanPhamId) {
        try {
            List<Anh_sp_DTO> anhSpList = anhSpService.getAnhBySanPhamId(sanPhamId);
            return ResponseEntity.ok(anhSpList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy ảnh cho sản phẩm ID: " + sanPhamId + " - " + e.getMessage());
        }
    }


}