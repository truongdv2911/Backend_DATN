package com.example.demo.Controller;

import com.example.demo.DTOs.DTOthongTinNguoiNhan;
import com.example.demo.Repository.ThongTinNguoiNhanRepository;
import com.example.demo.Service.ThongTinNguoiNhanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lego-store/thong-tin-nguoi-nhan")
@RequiredArgsConstructor
public class ThongTinNguoiNhanController {
    private final ThongTinNguoiNhanRepository thongTinNguoiNhanRepository;
    private final ThongTinNguoiNhanService thongTinNguoiNhanService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getIn4sTheoId(@PathVariable Integer id){
        try {
            return ResponseEntity.ok(thongTinNguoiNhanRepository.GetListById(id));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createThongTin(@Valid @RequestBody DTOthongTinNguoiNhan dtOthongTinNguoiNhan,
                                            BindingResult result
                                            ){
        try {
            if (result.hasErrors()) {
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            return ResponseEntity.ok( thongTinNguoiNhanService.createThongTin(dtOthongTinNguoiNhan));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateThongTin(@Valid @RequestBody DTOthongTinNguoiNhan dtOthongTinNguoiNhan,
                                            BindingResult result, @PathVariable Integer id
    ){
        try {
            if (result.hasErrors()) {
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            return ResponseEntity.ok(thongTinNguoiNhanService.updateThongTin(id, dtOthongTinNguoiNhan));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteThongTin(@PathVariable Integer id){
        try {
           return ResponseEntity.ok(thongTinNguoiNhanService.deleteThongTin(id));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
