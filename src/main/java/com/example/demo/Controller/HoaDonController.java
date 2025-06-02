package com.example.demo.Controller;

import com.example.demo.DTOs.DTOhoaDon;
import com.example.demo.Repository.HoaDonRepository;
import com.example.demo.Responses.HoaDonResponse;
import com.example.demo.Service.HoaDonService;
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
@RequestMapping("api/lego-store/hoa-don")
public class HoaDonController {
    private final HoaDonService hoaDonService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@Valid @RequestBody DTOhoaDon dtOhoaDon, BindingResult result){
        try {
            if (result.hasErrors()){
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            return ResponseEntity.ok(hoaDonService.createHoaDon(dtOhoaDon));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getAll(@PathVariable Integer user_id){
        try {
            List<HoaDonResponse> listOrder = hoaDonService.getAll(user_id);
            return ResponseEntity.ok(listOrder);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{idHD}")
    public ResponseEntity<?> getOne(@PathVariable Integer idHD){
        try {
            HoaDonResponse order = hoaDonService.findById(idHD);
            return ResponseEntity.ok(order);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{idHD}/{idNV}")
    public ResponseEntity<?> updateOrder(@Valid @PathVariable Integer idHD,
                                         @PathVariable Integer idNV,
                                         @RequestBody DTOhoaDon dtOhoaDon,
                                         BindingResult result
    ){
        try {
            if (result.hasErrors()){
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            return ResponseEntity.ok(hoaDonService.updateHoaDon(idHD, dtOhoaDon, idNV));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@Valid@PathVariable Integer id) throws Exception {
        hoaDonService.deleteHoaDon(id);
        return ResponseEntity.ok("Xoa thanh cong");
    }
}
