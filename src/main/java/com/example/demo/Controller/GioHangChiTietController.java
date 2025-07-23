package com.example.demo.Controller;

import com.example.demo.DTOs.GioHangChiTietDTO;
import com.example.demo.Entity.GioHangChiTiet;
import com.example.demo.Service.Gio_hang_chi_tiet_Service;
import com.example.demo.Responses.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giohangchitiet")
public class GioHangChiTietController {

    @Autowired
    private Gio_hang_chi_tiet_Service gioHangChiTietService;


    @PostMapping("/Create")
    public ResponseEntity<?> createGioHangChiTiet(@Valid @RequestBody GioHangChiTietDTO gioHangChiTietDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String message = String.join(", ", bindingResult.getAllErrors().stream().map(Object::toString).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            GioHangChiTiet result = gioHangChiTietService.createGioHangChiTiet(gioHangChiTietDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }
    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllGioHangChiTiet() {
        try {
            List<GioHangChiTiet> list = gioHangChiTietService.getAllGioHangChiTiet();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }
    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getGioHangChiTietById(@PathVariable Integer id) {
        try {
            GioHangChiTiet result = gioHangChiTietService.getGioHangChiTietById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateGioHangChiTiet(@PathVariable Integer id, @Valid @RequestBody GioHangChiTietDTO gioHangChiTietDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            GioHangChiTiet result = gioHangChiTietService.updateGioHangChiTiet(id, gioHangChiTietDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteGioHangChiTiet(@PathVariable Integer id) {
        try {
            gioHangChiTietService.deleteGioHangChiTiet(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
