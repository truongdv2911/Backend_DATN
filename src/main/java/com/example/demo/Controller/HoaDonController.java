package com.example.demo.Controller;

import com.example.demo.DTOs.DTOhoaDon;
import com.example.demo.Entity.HoaDon;
import com.example.demo.Repository.HoaDonRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.HoaDonResponse;
import com.example.demo.Service.HoaDonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final HoaDonRepository hoaDonRepository;
    private final UserRepository userRepository;

    @GetMapping("/get-all-hoa-don")
    public ResponseEntity<?> getAll(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(HoaDon.class, HoaDonResponse.class).addMappings(mapper ->{
            mapper.map(src -> src.getUser().getId(), HoaDonResponse::setUserId);
            mapper.map(src -> src.getNv().getId(), HoaDonResponse::setNvId);
            mapper.map(src -> src.getPhieuGiamGia().getId(), HoaDonResponse::setUserId);
        });
        return ResponseEntity.ok(hoaDonRepository.findAll().stream().map(order -> {
            HoaDonResponse orderResponse = modelMapper.map(order, HoaDonResponse.class);
            return orderResponse;
        }).toList());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@Valid @RequestBody DTOhoaDon dtOhoaDon, BindingResult result){
        try {
            if (result.hasErrors()){
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            // Nếu là hóa đơn tại quầy thì lấy user hiện tại làm nhân viên tạo đơn
            if (dtOhoaDon.getLoaiHD() != null && dtOhoaDon.getLoaiHD() == 1) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                    org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
                    String email = userDetails.getUsername();
                    com.example.demo.Entity.User nv = userRepository.findByEmail(email).orElse(null);
                    if (nv != null) {
                        dtOhoaDon.setNvId(nv.getId());
                    }
                }
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
