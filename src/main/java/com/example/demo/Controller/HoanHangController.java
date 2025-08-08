package com.example.demo.Controller;

import com.example.demo.DTOs.PhieuHoanHangDTO;
import com.example.demo.Entity.PhieuHoanHang;
import com.example.demo.Enum.TrangThaiPhieuHoan;
import com.example.demo.Enum.TrangThaiThanhToan;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.HoanHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{id}/duyet")
    public ResponseEntity<?> duyet(@PathVariable Integer id) {
        try {
            hoanHangService.duyetPhieuHoan(id);
            return ResponseEntity.ok("Duyệt phiếu hoàn hàng thành công");
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
            return ResponseEntity.ok("Từ chối phiếu hoàn hàng thành công");
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
            return ResponseEntity.ok("Cập nhật trạng thái thanh toán thành công");
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
            return ResponseEntity.ok(phieus);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse(500, "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/hoa-don/{idHoaDon}")
    public ResponseEntity<?> getByHoaDon(@PathVariable Integer idHoaDon) {
        try {
            List<PhieuHoanHang> phieus = hoanHangService.getPhieuHoanByHoaDon(idHoaDon);
            return ResponseEntity.ok(phieus);
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
}
