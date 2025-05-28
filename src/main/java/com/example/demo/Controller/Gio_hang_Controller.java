package com.example.demo.Controller;

import com.example.demo.DTOs.GioHangDTO;
import com.example.demo.Entity.GioHang;
import com.example.demo.Service.Gio_hang_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giohang")
@RequiredArgsConstructor
public class Gio_hang_Controller {


    private final Gio_hang_Service gioHangService;


    @PostMapping("/Create")
    public ResponseEntity<?> createGioHang(@Valid @RequestBody GioHangDTO gioHangDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            GioHang result = gioHangService.createGioHang(gioHangDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllGioHangs() {
        try {
            List<GioHang> list = gioHangService.getAllGioHangs();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getGioHangById(@PathVariable Integer id) {
        try {
            GioHang result = gioHangService.getGioHangById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateGioHang(@PathVariable Integer id, @Valid @RequestBody GioHangDTO gioHangDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            GioHang result = gioHangService.updateGioHang(id, gioHangDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteGioHang(@PathVariable Integer id) {
        try {
            gioHangService.deleteGioHang(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
