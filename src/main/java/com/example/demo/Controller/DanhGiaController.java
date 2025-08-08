package com.example.demo.Controller;

import com.example.demo.DTOs.DTOdanhGia;
import com.example.demo.DTOs.SanPhamUpdateDTO;
import com.example.demo.Entity.DanhGia;
import com.example.demo.Entity.SanPham;
import com.example.demo.Entity.User;
import com.example.demo.Entity.VideoDanhGia;
import com.example.demo.Repository.AnhDanhGiaRepository;
import com.example.demo.Repository.DanhGiaRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.VideoDanhGiaRepository;
import com.example.demo.Responses.DanhGiaResponse;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.SanPhamResponseDTO;
import com.example.demo.Service.AnhSpService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/lego-store/danh-gia")
@RequiredArgsConstructor
public class DanhGiaController {
    private final DanhGiaService danhGiaService;
    private final DanhGiaRepository danhGiaRepository;
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

    @PostMapping(value = "/CreateWithFileImages", consumes = "multipart/form-data")
    public ResponseEntity<?> createDanhGiaWithFileImages(
            @Valid @ModelAttribute DTOdanhGia dto,
            @RequestParam("fileAnh") List<MultipartFile> fileAnh,
            @RequestParam("fileVid") MultipartFile fileVid,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
            return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
        }
        try {
            // Tạo sản phẩm trước
            DanhGia danhGia = danhGiaService.createDanhGia(dto);

            // Upload và tạo ảnh sử dụng AnhSpService
            if (fileAnh != null && !fileAnh.isEmpty() && fileAnh.get(0).getSize() > 0) {
                danhGiaService.uploadAnh(danhGia.getId(), fileAnh);
            }

            // Nếu có video thì upload
            if (fileVid != null && !fileVid.isEmpty() && fileVid.getSize() > 0) {
                danhGiaService.uploadVideo(danhGia.getId(), fileVid);
            }
            return ResponseEntity.ok(danhGiaService.convertToResponseDTO(danhGia));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @GetMapping("/{sanPhamId}")
    public ResponseEntity<?> getDanhGiaBySanPham(@PathVariable Integer sanPhamId) {
        List<DanhGiaResponse> responseList = danhGiaService.getDanhGiaByIdSp(sanPhamId)
                .stream().map(danhGiaService::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/update/{idDg}/{idNv}")
    public ResponseEntity<?> updateDanhGia(@PathVariable Integer idDg,@PathVariable Integer idNv,
                                           @ModelAttribute DTOdanhGia dto,
                                           @RequestParam("fileAnh") List<MultipartFile> fileAnh,
                                           @RequestParam("fileVid") MultipartFile fileVid){
        try {
            if (fileAnh != null && !fileAnh.isEmpty() && fileAnh.get(0).getSize() > 0) {
                danhGiaService.uploadAnh(idDg, fileAnh);
            }
            if (fileVid != null && !fileVid.isEmpty() && fileVid.getSize() > 0) {
                danhGiaService.uploadVideo(idDg, fileVid);
            }
            return ResponseEntity.ok(danhGiaService.updateDanhGia(idDg,dto,idNv));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
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

    @DeleteMapping("/delete-anh/{idAnh}/{idNv}")
    public ResponseEntity<?> deleteAnh(@PathVariable Integer idAnh, @PathVariable Integer idNv) {
        User user = userRepository.findById(idNv).orElseThrow(() -> new RuntimeException("khong tim thay id nhan vien"));
        danhGiaService.deleteAnhDG(idAnh);
        return ResponseEntity.ok(new ErrorResponse(200,"Đã xóa anh đánh giá"));
    }

    @DeleteMapping("/delete-vid/{idVid}/{idNv}")
    public ResponseEntity<?> deleteVideo(@PathVariable Integer idVid, @PathVariable Integer idNv) {
        try {
            User user = userRepository.findById(idNv).orElseThrow(() -> new RuntimeException("khong tim thay id nhan vien"));
            danhGiaService.deleteVideoDG(idVid);
            return ResponseEntity.ok(new ErrorResponse(200,"Đã xóa video danh gia"));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }

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
            @RequestParam("video") MultipartFile video) throws Exception {
        danhGiaService.uploadVideo(danhGiaId, video);
        return ResponseEntity.ok(new ErrorResponse(200,"Đã upload video"));
    }

//    @GetMapping("/images/{imgName}")
//    public ResponseEntity<?> viewImage(@PathVariable String imgName){
//        try {
//            Path imgPath = Paths.get("UploadsFeedback/"+imgName);
//            UrlResource resource = new UrlResource(imgPath.toUri());
//
//            if (resource.exists()){
//                return ResponseEntity.ok()
//                        .contentType(MediaType.IMAGE_JPEG)
//                        .body(resource);
//            }
//            else{
//                return ResponseEntity.notFound().build();
//            }
//        }catch (Exception e){
//            return ResponseEntity.notFound().build();
//        }
//    }
//    @GetMapping("/videos/{videoName}")
//    public ResponseEntity<?> viewVideo(@PathVariable String videoName) {
//        try {
//            Path videoPath = Paths.get("UploadsFeedback/" + videoName);
//            UrlResource resource = new UrlResource(videoPath.toUri());
//
//            if (resource.exists()) {
//                // Xác định loại MIME dựa vào đuôi file
//                String contentType = Files.probeContentType(videoPath);
//                if (contentType == null) {
//                    contentType = "application/octet-stream"; // fallback
//                }
//
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType(contentType))
//                        .body(resource);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể tải video.");
//        }
//    }
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(){
        List<DanhGiaResponse> responseList = danhGiaRepository.findAll().stream()
                .map(danhGiaService::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }
}
