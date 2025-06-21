package com.example.demo.Controller;

import com.example.demo.DTOs.DTOhoaDon;
import com.example.demo.Entity.HoaDon;
import com.example.demo.Responses.HoaDonResponse;
import com.example.demo.Service.HoaDonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lego-store/hoa-don")
public class HoaDonController {
    private static final Logger logger = LoggerFactory.getLogger(HoaDonController.class);

    private final HoaDonService hoaDonService;


    @GetMapping("/paging")
    public ResponseEntity<Page<HoaDonResponse>> getAllHoaDonPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<HoaDonResponse> result = hoaDonService.getAllPaged(page, size);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/{idHD}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOne(@PathVariable("idHD") Integer idHD) {
        logger.debug("Yêu cầu lấy hóa đơn với ID: {}", idHD);

        try {
            HoaDonResponse order = hoaDonService.findById(idHD);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hóa đơn: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Lỗi khi lấy hóa đơn", e.getMessage()));
        }
    }
    @GetMapping("/search")
    public ResponseEntity<Page<HoaDonResponse>> searchAdvanced(
            @RequestParam(required = false) String ma,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String phuongThuc,
            @RequestParam(required = false) String tenNguoiDung,
            @RequestParam(required = false) String sdt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<HoaDonResponse> result = hoaDonService.searchAdvanced(ma, trangThai, phuongThuc, tenNguoiDung, sdt, from, to, page, size);
        return ResponseEntity.ok(result);
    }
//    @GetMapping("/hoa-don/user/{userId}")
//    public ResponseEntity<Page<HoaDonResponse>> getPagedHoaDon(
//            @PathVariable Integer userId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "5") int size
//    ) {
//        Page<HoaDonResponse> result = hoaDonService.getAllPaged(userId, page, size);
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/Create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody DTOhoaDon dtOhoaDon, BindingResult result) {
        logger.info("Yêu cầu tạo hóa đơn mới với userId: {}", dtOhoaDon.getUserId());

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            logger.warn("Dữ liệu không hợp lệ khi tạo hóa đơn: {}", errors);
            return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
        }

        try {
            HoaDon createdHoaDon = hoaDonService.createHoaDon(dtOhoaDon);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdHoaDon);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hóa đơn: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Lỗi khi tạo hóa đơn", e.getMessage()));
        }
    }

//    @GetMapping("/user/{user_id}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
//    public ResponseEntity<?> getAll(@PathVariable("user_id") Integer userId) {
//        logger.debug("Yêu cầu lấy danh sách hóa đơn cho userId: {}", userId);
//
//        try {
//            List<HoaDonResponse> listOrder = hoaDonService.ge(userId);
//            return ResponseEntity.ok(listOrder);
//        } catch (Exception e) {
//            logger.error("Lỗi khi lấy danh sách hóa đơn: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(new ErrorResponse("Lỗi khi lấy danh sách hóa đơn", e.getMessage()));
//        }
//    }



    @PutMapping("/update/{idHD}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrder(@PathVariable("idHD") Integer idHD,
                                         @Valid @RequestBody DTOhoaDon dtOhoaDon,
                                         BindingResult result) {
        logger.info("Yêu cầu cập nhật hóa đơn với ID: {}", idHD);

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            logger.warn("Dữ liệu không hợp lệ khi cập nhật hóa đơn: {}", errors);
            return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
        }

        try {
            HoaDon updatedHoaDon = hoaDonService.updateHoaDon(idHD, dtOhoaDon);
            return ResponseEntity.ok(updatedHoaDon);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật hóa đơn: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Lỗi khi cập nhật hóa đơn", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id) {
        logger.info("Yêu cầu xóa hóa đơn với ID: {}", id);

        try {
            hoaDonService.deleteHoaDon(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Lỗi khi xóa hóa đơn: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Lỗi khi xóa hóa đơn", e.getMessage()));
        }
    }

    // Lớp response tùy chỉnh để chuẩn hóa lỗi
    private record ErrorResponse(String message, Object details) {}
}