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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      return ResponseEntity.ok(hoaDonRepository.findAll());
    }

    @GetMapping("/get-phi-ship")
    public ResponseEntity<?> getPhiShip(
            @RequestParam String diaChi,
            @RequestParam(required = false, defaultValue = "0") int isFast
    ) {
        String[] addressParts = diaChi.split(",");
        String province = addressParts.length > 0 ? addressParts[addressParts.length - 1].trim() : "";
        String district = addressParts.length > 1 ? addressParts[addressParts.length - 2].trim() : "";
        String loaiVanChuyen = hoaDonService.getLoaiVanChuyen("Hà Nội", province);
        String khuVuc = hoaDonService.isNoiThanh(province, district) ? "Nội thành" : "Ngoại thành";
        java.math.BigDecimal phiShip = hoaDonService.tinhPhiShip(loaiVanChuyen, khuVuc, 0.5);
        int soNgayGiao = hoaDonService.tinhSoNgayGiao(loaiVanChuyen);
        if (isFast == 1 && ("DAC_BIET".equals(loaiVanChuyen) || "LIEN_MIEN".equals(loaiVanChuyen))) {
            phiShip = phiShip.add(java.math.BigDecimal.valueOf(15000));
            soNgayGiao = Math.max(1, soNgayGiao - 1);
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("phiShip", phiShip);
        result.put("soNgayGiao", soNgayGiao);
        return ResponseEntity.ok(result);
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
            HoaDon hoaDon = hoaDonService.createHoaDon(dtOhoaDon);
            HoaDonResponse response = hoaDonService.convertToResponse(hoaDon);
            return ResponseEntity.ok(response);
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
    @GetMapping("/status-count")
    public ResponseEntity<Map<String, Long>> getCountByStatus() {
        return ResponseEntity.ok(hoaDonService.countByTrangThai());
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable Integer id,
            @RequestParam String trangThai,
            @RequestParam Integer idNV) {
        try {
            HoaDonResponse response = hoaDonService.updateTrangThai(id, trangThai, idNV);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Cập nhật trạng thái thất bại");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    @GetMapping("/paging")
    public ResponseEntity<Page<HoaDonResponse>> getAllHoaDonPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<HoaDonResponse> result = hoaDonService.getAllPaged(page, size);
        return ResponseEntity.ok(result);
    }

}
