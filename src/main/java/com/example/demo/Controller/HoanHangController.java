package com.example.demo.Controller;

import com.example.demo.DTOs.DTOdanhGia;
import com.example.demo.DTOs.KetQuaKiemTraRequest;
import com.example.demo.DTOs.PhieuHoanHangDTO;
import com.example.demo.Entity.DanhGia;
import com.example.demo.Entity.PhieuHoanHang;
import com.example.demo.Enum.TrangThaiPhieuHoan;
import com.example.demo.Enum.TrangThaiThanhToan;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.PhieuHoanHangResponse;
import com.example.demo.Service.HoanHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lego-store/hoan-hang")
@RequiredArgsConstructor
public class HoanHangController {
    private final HoanHangService hoanHangService;

    @PostMapping("/tao-phieu")
    public ResponseEntity<?> taoPhieu(@Valid @RequestBody PhieuHoanHangDTO dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            return ResponseEntity.ok(hoanHangService.taoPhieuHoanHang(dto));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @PostMapping(value = "/tao-phieu-2", consumes = "multipart/form-data")
    public ResponseEntity<?> createDanhGiaWithFileImages(
            @Valid @ModelAttribute PhieuHoanHangDTO dto,
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
            PhieuHoanHang hoanHang = hoanHangService.taoPhieuHoanHang(dto);

            // Upload và tạo ảnh sử dụng AnhSpService
            if (fileAnh != null && !fileAnh.isEmpty() && fileAnh.get(0).getSize() > 0) {
                hoanHangService.uploadAnh(hoanHang.getId(), fileAnh);
            }

            // Nếu có video thì upload
            if (fileVid != null && !fileVid.isEmpty() && fileVid.getSize() > 0) {
                hoanHangService.uploadVideo(hoanHang.getId(), fileVid);
            }
            return ResponseEntity.ok(hoanHangService.convertPHH(hoanHang));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @PutMapping("/{id}/duyet")
    public ResponseEntity<?> duyet(@PathVariable Integer id) {
        try {
            hoanHangService.duyetPhieuHoan(id);
            return ResponseEntity.ok(new ErrorResponse(200,"Duyệt phiếu hoàn hàng thành công"));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @PutMapping("/{id}/tu-choi")
    public ResponseEntity<?> tuChoi(@PathVariable Integer id, @RequestParam String lyDo) {
        try {
            hoanHangService.tuChoiPhieuHoan(id, lyDo);
            return ResponseEntity.ok(new ErrorResponse(200,"Từ chối phiếu hoàn hàng thành công"));
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @PutMapping("/{id}/thanh-toan")
    public ResponseEntity<?> capNhatThanhToan(@PathVariable Integer id, @RequestParam TrangThaiThanhToan trangThai) {
        try {
            hoanHangService.capNhatThanhToan(id, trangThai);
            return ResponseEntity.ok(new ErrorResponse(200,"Cập nhật trạng thái thanh toán thành công"));
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<?> getByTrangThai(@PathVariable TrangThaiPhieuHoan trangThai) {
        try {
            List<PhieuHoanHang> phieus = hoanHangService.getPhieuHoanByTrangThai(trangThai);
            List<PhieuHoanHangResponse> phieuHoanHangResponses = phieus.stream()
                    .map(phieuHoanHang -> {
                        PhieuHoanHangResponse phieuHoanHangResponse = hoanHangService.convertPHH(phieuHoanHang);
                        return phieuHoanHangResponse;
                    }).toList();
            return ResponseEntity.ok(phieuHoanHangResponses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse(500, "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/hoa-don/{idHoaDon}")
    public ResponseEntity<?> getByHoaDon(@PathVariable Integer idHoaDon) {
        try {
            List<PhieuHoanHang> phieus = hoanHangService.getPhieuHoanByHoaDon(idHoaDon);
            List<PhieuHoanHangResponse> phieuHoanHangResponses = phieus.stream()
                    .map(phieuHoanHang -> {
                        PhieuHoanHangResponse phieuHoanHangResponse = hoanHangService.convertPHH(phieuHoanHang);
                        return phieuHoanHangResponse;
                    }).toList();
            return ResponseEntity.ok(phieuHoanHangResponses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse(500, "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/kiem-tra/{idHoaDon}")
    public ResponseEntity<?> kiemTraCoTheHoanHang(@PathVariable Integer idHoaDon) {
        try {
            boolean coThe = hoanHangService.coTheHoanHang(idHoaDon);
            return ResponseEntity.ok(Map.of("coTheHoanHang", coThe));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse(500, "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PutMapping("/kiem-tra-hang/{idPhieu}")
    public ResponseEntity<?> kiemTraHang(
            @PathVariable Integer idPhieu,
            @RequestBody List<KetQuaKiemTraRequest> ketQuaList) {
        try {
            hoanHangService.kiemTraHang(idPhieu, ketQuaList);
            return ResponseEntity.ok(new ErrorResponse(200, "Đã kiểm tra và xử lý hàng hoàn"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }
}
