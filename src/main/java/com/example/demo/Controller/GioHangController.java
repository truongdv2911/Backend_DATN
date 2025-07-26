package com.example.demo.Controller;

import com.example.demo.DTOs.GioHangDTO;
import com.example.demo.Entity.GioHang;
import com.example.demo.Entity.GioHangChiTiet;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.GioHangService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giohang")
@RequiredArgsConstructor
public class GioHangController {

    private final UserRepository userRepository;
    private final GioHangService gioHangService;


    @PostMapping("/Create")
    public ResponseEntity<?> createGioHang(@RequestParam("sanPhamId") Integer sanPhamId,
                                           @RequestParam("soLuong") Integer soLuong,
                                           HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("username") == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập");
            }
            String email = (String) session.getAttribute("username");
            User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Khong tim thay email"));
            Integer userId = user.getId();
            GioHangChiTiet item = gioHangService.addToCart(userId, sanPhamId, soLuong);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Cập nhật số lượng sản phẩm
    @PutMapping("/update/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer itemId,
            @RequestParam("soLuong") Integer soLuong,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("username") == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập");
            }
            String email = (String) session.getAttribute("username");
            User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Khong tim thay email"));
            Integer userId = user.getId();
            GioHangChiTiet item = gioHangService.updateCartItem(userId, itemId, soLuong);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }


    // Áp dụng phiếu giảm giá
    @PostMapping("/apply-discount")
    public ResponseEntity<?> applyDiscount(
            @RequestParam("phieuGiamGiaId") Integer phieuGiamGiaId,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("username") == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập");
            }
            String email = (String) session.getAttribute("username");
            User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Khong tim thay email"));
            Integer userId = user.getId();
            GioHang gioHang = gioHangService.applyDiscount(userId, phieuGiamGiaId);
            return ResponseEntity.ok(gioHang);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeCartItem(
            @PathVariable Integer itemId,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("username") == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập");
            }
            String email = (String) session.getAttribute("username");
            User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Khong tim thay email"));
            Integer userId = user.getId();
            gioHangService.removeCartItem(userId, itemId);
            return ResponseEntity.ok(new ErrorResponse(200, "Xóa sản phẩm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xem giỏ hàng
    @GetMapping("/{id}")
    public ResponseEntity<?> getCart(@PathVariable Integer id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("username") == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập");
            }
            String email = (String) session.getAttribute("username");
            User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Khong tim thay email"));
            Integer userId = user.getId();
            GioHang gioHang = gioHangService.getCart(id);
            return ResponseEntity.ok(gioHang);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
