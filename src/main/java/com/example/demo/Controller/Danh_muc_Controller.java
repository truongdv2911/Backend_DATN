package com.example.demo.Controller;

import com.example.demo.DTOs.DanhMucDTO;
import com.example.demo.Entity.DanhMuc;
import com.example.demo.Service.Danh_muc_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danhmuc")
public class Danh_muc_Controller {

    @Autowired
    private Danh_muc_Service danhMucService;


    @PostMapping("/Create")
    public ResponseEntity<?> createDanhMuc(@Valid @RequestBody DanhMucDTO danhMucDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            DanhMuc result = danhMucService.createDanhMuc(danhMucDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/ReadAll")
    public ResponseEntity<?> getAllDanhMuc() {
        try {
            List<DanhMuc> list = danhMucService.getAllDanhMuc();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/ReadOne/{id}")
    public ResponseEntity<?> getDanhMucById(@PathVariable Integer id) {
        try {
            DanhMuc result = danhMucService.getDanhMucById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateDanhMuc(@PathVariable Integer id, @Valid @RequestBody DanhMucDTO danhMucDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            DanhMuc result = danhMucService.updateDanhMuc(id, danhMucDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<?> deleteDanhMuc(@PathVariable Integer id) {
        try {
            danhMucService.deleteDanhMuc(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
