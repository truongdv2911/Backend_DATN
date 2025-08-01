package com.example.demo.Controller;


import com.example.demo.DTOs.CapNhatTrangThaiHoaDonDTO;
import com.example.demo.DTOs.DTOhoaDon;
import com.example.demo.DTOs.HoaDonEmailDTO;
import com.example.demo.Entity.HoaDon;
import com.example.demo.Repository.HoaDonRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.HoaDonResponse;
import com.example.demo.Service.EmailService;
import com.example.demo.Service.HoaDonService;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
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

import java.util.ArrayList;
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
    private final EmailService emailService;
    private final LichSuLogService lichSuLogService;

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
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
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
            // Log lịch sử tạo mới
            if (dtOhoaDon.getNvId() != null){
                String moTa = "Tạo mới đơn hàng: " + hoaDon.getMaHD() + " - ID: " + hoaDon.getId();
                lichSuLogService.saveLog("TẠO MỚI", "HoaDon", moTa, lichSuLogService.getCurrentUserId());
            }
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/send-order-email")
    public ResponseEntity<?> sendEmail(@RequestBody HoaDonEmailDTO req){
        try {
            emailService.sendOrderEmail(req);
            return ResponseEntity.ok(new ErrorResponse(200,"Đã gửi hóa đơn điện tử"));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getAll(@PathVariable Integer user_id){
        try {
            List<HoaDonResponse> listOrder = hoaDonService.getAll(user_id);
            return ResponseEntity.ok(listOrder);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }
    @GetMapping("/{idHD}")
    public ResponseEntity<?> getOne(@PathVariable Integer idHD){
        try {
            HoaDonResponse order = hoaDonService.findById(idHD);
            return ResponseEntity.ok(order);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
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
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            HoaDon hoaDonOld = hoaDonRepository.findById(idHD).orElseThrow(() -> new RuntimeException("khong tim thay hoa don"));
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(hoaDonOld, dtOhoaDon);
            String moTa = "Cập nhật hóa đơn ID: " + idHD + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "HoaDon", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(hoaDonService.updateHoaDon(idHD, dtOhoaDon, idNV));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@Valid@PathVariable Integer id) throws Exception {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElse(null);
        hoaDonService.deleteHoaDon(id);
        // Log lịch sử xóa
        String moTa = "Xóa hóa đơn ID: " + id + (hoaDon != null ? (", Mã: " + hoaDon.getMaHD()) : "");
        lichSuLogService.saveLog("XÓA", "HoaDon", moTa, lichSuLogService.getCurrentUserId());
        return ResponseEntity.ok(new ErrorResponse(200, "Ẩn thành công"));
    }
    @GetMapping("/status-count")
    public ResponseEntity<Map<String, Long>> getCountByStatus() {
        return ResponseEntity.ok(hoaDonService.countByTrangThai());
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @RequestBody CapNhatTrangThaiHoaDonDTO dto) {
        List<String> errors = new ArrayList<>();
        List<HoaDonResponse> ketQuaThanhCong = new ArrayList<>();

        for (Integer id : dto.getHoaDonIds()) {
            try {
                HoaDon hoaDonOld = hoaDonRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
                String trangThaiCu = hoaDonOld.getTrangThai();

                HoaDonResponse response = hoaDonService.updateTrangThai(id, dto.getTrangThai(), dto.getIdNV());
                ketQuaThanhCong.add(response);

                String moTa = "Cập nhật trạng thái hóa đơn Mã: " + response.getMaHD()
                        + ". Trạng thái: [" + trangThaiCu + "] -> [" + dto.getTrangThai() + "]";
                lichSuLogService.saveLog("CẬP NHẬT", "HoaDon", moTa, lichSuLogService.getCurrentUserId());

            } catch (Exception e) {
                String loi = "Hóa đơn ID " + id + " lỗi: " + e.getMessage();
                errors.add(loi);
                hoaDonRepository.findById(id).ifPresent(hoaDon -> {
                    // Ghi log thất bại
                    String moTaLoi = "Cập nhật trạng thái hóa đơn Mã: " + hoaDon.getMaHD()
                            + " thất bại. Lý do: " + e.getMessage();
                    lichSuLogService.saveLog("CẬP NHẬT THẤT BẠI", "HoaDon", moTaLoi, lichSuLogService.getCurrentUserId());
                });
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("thanhCong", ketQuaThanhCong);
        result.put("loi", errors);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/paging")
    public ResponseEntity<Page<HoaDonResponse>> getAllHoaDonPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<HoaDonResponse> result = hoaDonService.getAllPaged(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-one-hoa-don/{id}")
    public ResponseEntity<?> getone(@PathVariable Integer id){
        return ResponseEntity.ok(hoaDonRepository.findById(id).orElseThrow(() -> new RuntimeException("không tìm thấy id hóa đơn")));
    }

}
