package com.example.demo.Controller;

import com.example.demo.DTOs.SPYeuThichDTO;
import com.example.demo.DTOs.ViGiamGiaDTO;
import com.example.demo.Entity.SanPhamYeuThich;
import com.example.demo.Entity.ViPhieuGiamGia;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.PhieuGiamGiaResponse;
import com.example.demo.Responses.SpYeuThichResponse;
import com.example.demo.Service.SPYeuThichService;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lego-store/san-pham-yeu-thich")
@RequiredArgsConstructor
public class SpYeuThichController {
    private final SPYeuThichService spYeuThichService;

    @PostMapping("/them")
    public ResponseEntity<?> themPhieu(@RequestBody SPYeuThichDTO dto) {
        try {
            SanPhamYeuThich sp = spYeuThichService.addSPyeuThich(dto);
            return ResponseEntity.ok(sp);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getDanhSachPhieuTrongTui(
            @PathVariable Integer userId){
        List<SpYeuThichResponse> list = spYeuThichService.getSanPhamYeuThich(userId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> xoaYeuThich(
            @RequestParam("user_id") Integer userId
            ,@RequestParam("sp_id") Integer spId){
        try {
            spYeuThichService.deleteSp(spId, userId);
            return ResponseEntity.ok(new ErrorResponse(200,"Bỏ yêu thích thành công"));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }
}
