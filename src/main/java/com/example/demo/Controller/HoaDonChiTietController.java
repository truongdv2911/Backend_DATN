package com.example.demo.Controller;

import com.example.demo.DTOs.DTOhoaDonChiTiet;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.HoaDonChiTietService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RestController
@RequestMapping("api/lego-store/hoa-don-chi-tiet")
public class HoaDonChiTietController {
    private final HoaDonChiTietService hoaDonChiTietService;

    @PostMapping("")
    public ResponseEntity<?> createHoaDonCT(@Valid @RequestBody DTOhoaDonChiTiet dtOhoaDonChiTiet, BindingResult result){
        try {
            if (result.hasErrors()){
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            return ResponseEntity.ok(hoaDonChiTietService.createHoaDonChiTiet(dtOhoaDonChiTiet));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOneWithId(@PathVariable Integer id) throws Exception {
        return ResponseEntity.ok(hoaDonChiTietService.findById(id));
    }
    @GetMapping("/hoaDon/{id}")
    public ResponseEntity<?> getListOrderDetailWithIdOrder(@PathVariable Integer id){
        return ResponseEntity.ok(hoaDonChiTietService.getAll(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Integer id, @Valid
                                         @RequestBody DTOhoaDonChiTiet dtOhoaDonChiTiet,
                                         BindingResult result
    ){
        try {
            if (result.hasErrors()){
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            hoaDonChiTietService.updateHoaDonChiTiet(id, dtOhoaDonChiTiet);
            return ResponseEntity.ok("Update thanh cong");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Co loi");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) throws Exception {
        hoaDonChiTietService.deleteHoaDonChiTiet(id);
        return ResponseEntity.ok("Xoa thanh cong");
    }
}
