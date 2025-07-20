package com.example.demo.Controller;

import com.example.demo.DTOs.DTOthongTinNguoiNhan;
import com.example.demo.Entity.ThongTinNguoiNhan;
import com.example.demo.Repository.ThongTinNguoiNhanRepository;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.ThongTinNguoiNhanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createThongTin(@Valid @RequestBody DTOthongTinNguoiNhan dtOthongTinNguoiNhan,
                                            BindingResult result
                                            ){
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            return ResponseEntity.ok( thongTinNguoiNhanService.createThongTin(dtOthongTinNguoiNhan));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateThongTin(@Valid @RequestBody DTOthongTinNguoiNhan dtOthongTinNguoiNhan,
                                            BindingResult result, @PathVariable Integer id
    ){
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            ThongTinNguoiNhan entity =thongTinNguoiNhanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi"));

            if (!isDifferent(dtOthongTinNguoiNhan, entity)) {
                throw new IllegalArgumentException("Không có gì thay đổi để cập nhật");
            }
            return ResponseEntity.ok(thongTinNguoiNhanService.updateThongTin(id, dtOthongTinNguoiNhan));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteThongTin(@PathVariable Integer id){
        try {
           return ResponseEntity.ok(thongTinNguoiNhanService.deleteThongTin(id));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }
    public boolean isDifferent(DTOthongTinNguoiNhan dto, ThongTinNguoiNhan entity) {
        if (!Objects.equals(dto.getHoTen(), entity.getHoTen())) return true;
        if (!Objects.equals(dto.getSdt(), entity.getSdt())) return true;
        if (!Objects.equals(dto.getDuong(), entity.getDuong())) return true;
        if (!Objects.equals(dto.getXa(), entity.getXa())) return true;
        if (!Objects.equals(dto.getHuyen(), entity.getHuyen())) return true;
        if (!Objects.equals(dto.getThanhPho(), entity.getThanhPho())) return true;
        if (!Objects.equals(dto.getIsMacDinh(), entity.getIsMacDinh())) return true;
        if (!Objects.equals(dto.getIdUser(), entity.getUser().getId())) return true;

        return false; // tất cả giống nhau
    }
}
