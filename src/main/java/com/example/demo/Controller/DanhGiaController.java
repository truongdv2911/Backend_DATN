package com.example.demo.Controller;

import com.example.demo.DTOs.DTOdanhGia;
import com.example.demo.Entity.DanhGia;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.DanhGiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("api/lego-store/danh-gia")
@RequiredArgsConstructor
public class DanhGiaController {
    private final DanhGiaService danhGiaService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createDanhGia(@Valid @RequestBody DTOdanhGia dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            return ResponseEntity.ok(danhGiaService.createDanhGia(dto));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @GetMapping("/{sanPhamId}")
    public ResponseEntity<?> getDanhGiaBySanPham(@PathVariable Integer sanPhamId) {
        List<DanhGia> danhGias = danhGiaService.getDanhGiaByIdSp(sanPhamId);
        return ResponseEntity.ok(danhGias);
    }

    @PutMapping("/update/{idDg}/{idNv}")
    public ResponseEntity<?> updateDanhGia(@PathVariable Integer idDg,@PathVariable Integer idNv, @RequestParam("phanHoi") String phanHoi){
        try {
            return ResponseEntity.ok(danhGiaService.updateDanhGia(idDg,phanHoi,idNv));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{idDg}/{idNv}")
    public ResponseEntity<?> deleteDanhGia(@PathVariable Integer idDg, @PathVariable Integer idNv) {
        User user = userRepository.findById(idNv).orElseThrow(() -> new RuntimeException("khong tim thay id nhan vien"));
        if (user.getRole().getId() == 2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403,"Bạn không có quyền xóa đánh giá này."));
        }
        danhGiaService.deleteDanhGia(idDg);
        return ResponseEntity.ok(new ErrorResponse(200,"Đã xóa đánh giá"));
    }

    @PostMapping(value = "/anh/{danhGiaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAnh(
            @PathVariable Integer danhGiaId,
            @RequestParam("images") List<MultipartFile> images) throws Exception {
            danhGiaService.uploadAnh(danhGiaId, images);
        return ResponseEntity.ok(new ErrorResponse(200,"Đã upload ảnh"));
    }

    @PostMapping(value = "/video/{danhGiaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @PathVariable Integer danhGiaId,
            @RequestParam("video") MultipartFile video) throws IOException {
        danhGiaService.uploadVideo(danhGiaId, video);
        return ResponseEntity.ok(new ErrorResponse(200,"Đã upload video"));
    }

    @GetMapping("/images/{imgName}")
    public ResponseEntity<?> viewImage(@PathVariable String imgName){
        try {
            Path imgPath = Paths.get("UploadsFeedback/"+imgName);
            UrlResource resource = new UrlResource(imgPath.toUri());

            if (resource.exists()){
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
            else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
