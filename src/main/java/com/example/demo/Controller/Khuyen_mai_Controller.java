package com.example.demo.Controller;

import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Repository.Khuyen_mai_Repo;
import com.example.demo.Responses.ChiTietKMResponse;
import com.example.demo.Responses.ChiTietPhieuResponse;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.Khuyen_mai_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/khuyenmai")
@RequiredArgsConstructor
public class Khuyen_mai_Controller {


    private final Khuyen_mai_Service khuyenMaiService;
    private final Khuyen_mai_Repo khuyenMaiRepo;


    @PostMapping("/Create")
    public ResponseEntity<?> createKhuyenMai(@Valid @RequestBody KhuyenMaiDTO khuyenMaiDTO, BindingResult result) {
        try {
            if (result.hasErrors()){
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            if ( khuyenMaiDTO.getNgayBatDau().isAfter(khuyenMaiDTO.getNgayKetThuc())) {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "Ngày bắt đầu phải trước ngày kết thúc"));
            }
            KhuyenMai results = khuyenMaiService.createKhuyenMai(khuyenMaiDTO);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllKhuyenMai() {
        try {
            List<KhuyenMai> list = khuyenMaiService.getAllKhuyenMai();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }




    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getKhuyenMaiById(@PathVariable Integer id) {
        try {
            KhuyenMai result = khuyenMaiService.getKhuyenMaiById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
        }
    }

//1111
    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateKhuyenMai(@PathVariable Integer id, @Valid @RequestBody KhuyenMaiDTO khuyenMaiDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            if (khuyenMaiDTO.getNgayBatDau() != null && khuyenMaiDTO.getNgayKetThuc() != null
                    && khuyenMaiDTO.getNgayBatDau().isAfter(khuyenMaiDTO.getNgayKetThuc())) {
                return ResponseEntity.badRequest().body("Ngày bắt đầu phải trước ngày kết thúc");
            }
            KhuyenMai khuyenMai = khuyenMaiRepo.findById(id).orElseThrow(()-> new RuntimeException("khong tim thay id khuyen mai"));
            if (!isDifferent(khuyenMaiDTO, khuyenMai)) {
                throw new IllegalArgumentException("Không có thay đổi nào để cập nhật");
            }
            KhuyenMai result = khuyenMaiService.updateKhuyenMai(id, khuyenMaiDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteKhuyenMai(@PathVariable Integer id) {
        try {
            khuyenMaiService.deleteKhuyenMai(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/getDetail/{id}")
    public ResponseEntity<?> detailKM(@PathVariable Integer id){
        try {
            ChiTietKMResponse response = khuyenMaiService.getDetailKM(id);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("loi"+e.getMessage());
        }
    }

    public static boolean isDifferent(KhuyenMaiDTO dto, KhuyenMai entity) {
        if (dto == null || entity == null) return true;

        return !Objects.equals(dto.getNgayBatDau(), entity.getNgayBatDau())
                || !Objects.equals(dto.getNgayKetThuc(), entity.getNgayKetThuc())
                || !Objects.equals(dto.getTenKhuyenMai(), entity.getTenKhuyenMai())
                || !Objects.equals(dto.getPhanTramKhuyenMai(), entity.getPhanTramKhuyenMai())
                || !Objects.equals(dto.getTrangThai(), entity.getTrangThai());
    }
    private static boolean compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return false; // giống nhau
        if (a == null || b == null) return true;  // khác nhau
        return a.compareTo(b) != 0;
    }
}
