package com.example.demo.Service;

import com.example.demo.DTOs.GioHangDTO;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GioHangService {

    private final GioHangRepo gioHangRepository;
    private final Gio_hang_chi_tiet_Repo gioHangChiTietRepo;
    private final UserRepository userRepository;
    private final San_pham_Repo san_pham_repo;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepository;

    @Transactional
    public GioHang getOrCreateCart(Integer userId) {
        GioHang gioHang = gioHangRepository.findByIdUser(userId);
        if (gioHang == null) {
            gioHang = new GioHang();
            gioHang.setUser(userRepository.findById(userId).orElseThrow(()-> new RuntimeException("khong tim thay id user"))); // Giả định UserService có findById
            gioHang.setTrangThai("Chưa thanh toán");
            gioHang.setTongTien(BigDecimal.ZERO);
            gioHang.setSoTienGiam(BigDecimal.ZERO);
            gioHang = gioHangRepository.save(gioHang);
        }
        return gioHang;
    }

    // Thêm sản phẩm vào giỏ hàng
    @Transactional
    public GioHangChiTiet addToCart(Integer userId, Integer sanPhamId, Integer soLuong) {
        GioHang gioHang = getOrCreateCart(userId);
        SanPham sanPham = san_pham_repo.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        GioHangChiTiet gioHangChiTiet = gioHang.getGioHangChiTiets().stream()
                .filter(item -> item.getSanPham().getId().equals(sanPhamId))
                .findFirst()
                .orElse(null);

        if (gioHangChiTiet == null) {
            gioHangChiTiet = new GioHangChiTiet();
            gioHangChiTiet.setGioHang(gioHang);
            gioHangChiTiet.setSanPham(sanPham);
            gioHangChiTiet.setSoLuong(soLuong);
//            gioHangChiTiet.setGia(sanPham.getGiaKhuyenMai()); // Giả định SanPham có trường gia
            gioHangChiTiet.setTongTien(sanPham.getGia().multiply(BigDecimal.valueOf(soLuong)));
        } else {
            gioHangChiTiet.setSoLuong(gioHangChiTiet.getSoLuong() + soLuong);
            gioHangChiTiet.setTongTien(gioHangChiTiet.getGia().multiply(BigDecimal.valueOf(gioHangChiTiet.getSoLuong())));
        }

        gioHangChiTiet = gioHangChiTietRepo.save(gioHangChiTiet);
        updateCartTotal(gioHang);
        return gioHangChiTiet;
    }

    private void updateCartTotal(GioHang gioHang) {
        BigDecimal tongTien = gioHang.getGioHangChiTiets().stream()
                .map(GioHangChiTiet::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        gioHang.setTongTien(tongTien.subtract(gioHang.getSoTienGiam() != null ? gioHang.getSoTienGiam() : BigDecimal.ZERO));
        gioHangRepository.save(gioHang);
    }


    @Transactional
    public GioHangChiTiet updateCartItem(Integer userId, Integer itemId, Integer soLuong) {
        GioHang gioHang = getOrCreateCart(userId);
        GioHangChiTiet gioHangChiTiet = gioHangChiTietRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Mục giỏ hàng không tồn tại"));

        if (!gioHangChiTiet.getGioHang().getId().equals(gioHang.getId())) {
            throw new RuntimeException("Mục giỏ hàng không thuộc về người dùng này");
        }

        gioHangChiTiet.setSoLuong(soLuong);
        gioHangChiTiet.setTongTien(gioHangChiTiet.getGia().multiply(BigDecimal.valueOf(soLuong)));
        gioHangChiTiet = gioHangChiTietRepo.save(gioHangChiTiet);
        updateCartTotal(gioHang);
        return gioHangChiTiet;
    }

    // Áp dụng phiếu giảm giá
    @Transactional
    public GioHang applyDiscount(Integer userId, Integer phieuGiamGiaId) {
        GioHang gioHang = getOrCreateCart(userId);
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(phieuGiamGiaId)
                .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không tồn tại"));

        if (!"Đang hoạt động".equals(phieuGiamGia.getTrangThai())) {
            throw new RuntimeException("Phiếu giảm giá không khả dụng");
        }

        LocalDate currentDate = LocalDate.now();
        if (phieuGiamGia.getNgayBatDau().isAfter(currentDate) ||
                phieuGiamGia.getNgayKetThuc().isBefore(currentDate)) {
            throw new RuntimeException("Phiếu giảm giá đã hết hạn hoặc chưa bắt đầu");
        }

        if (phieuGiamGia.getSoLuong() <= 0) {
            throw new RuntimeException("Phiếu giảm giá đã hết số lượng");
        }

        // Tính tổng tiền giỏ hàng trước khi áp dụng giảm giá
        BigDecimal tongTienTruocGiam = gioHang.getGioHangChiTiets().stream()
                .map(GioHangChiTiet::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Kiểm tra giá trị đơn hàng tối thiểu
        if (tongTienTruocGiam.compareTo(phieuGiamGia.getGiaTriToiThieu()) < 0) {
            throw new RuntimeException("Tổng tiền giỏ hàng không đạt giá trị tối thiểu để áp dụng phiếu giảm giá");
        }

        // Tính số tiền giảm dựa trên loại phiếu
        BigDecimal soTienGiam;
        if ("Theo %".equals(phieuGiamGia.getLoaiPhieuGiam())) {
            // Giảm theo phần trăm
            soTienGiam = tongTienTruocGiam.multiply(phieuGiamGia.getGiaTriGiam().divide(BigDecimal.valueOf(100)));
            // Kiểm tra giới hạn giảm tối đa
            if (phieuGiamGia.getGiamToiDa() != null && soTienGiam.compareTo(phieuGiamGia.getGiamToiDa()) > 0) {
                soTienGiam = phieuGiamGia.getGiamToiDa();
            }
        } else if ("Theo số tiền".equals(phieuGiamGia.getLoaiPhieuGiam())) {
            // Giảm số tiền cố định
            soTienGiam = phieuGiamGia.getGiaTriGiam();
            // Kiểm tra giới hạn giảm tối đa
            if (phieuGiamGia.getGiamToiDa() != null && soTienGiam.compareTo(phieuGiamGia.getGiamToiDa()) > 0) {
                soTienGiam = phieuGiamGia.getGiamToiDa();
            }
        } else {
            throw new RuntimeException("Loại phiếu giảm giá không hợp lệ");
        }

        // Cập nhật giỏ hàng
        gioHang.setPhieuGiamGia(phieuGiamGia);
        gioHang.setSoTienGiam(soTienGiam);

        // Giảm số lượng phiếu
        phieuGiamGia.setSoLuong(phieuGiamGia.getSoLuong() - 1);
        phieuGiamGiaRepository.save(phieuGiamGia);

        // Cập nhật tổng tiền giỏ hàng
        updateCartTotal(gioHang);

        return gioHangRepository.save(gioHang);
    }


    // Xóa sản phẩm khỏi giỏ hàng
    @Transactional
    public void removeCartItem(Integer userId, Integer itemId) {
        GioHang gioHang = getOrCreateCart(userId);
        GioHangChiTiet gioHangChiTiet = gioHangChiTietRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Mục giỏ hàng không tồn tại"));

        if (!gioHangChiTiet.getGioHang().getId().equals(gioHang.getId())) {
            throw new RuntimeException("Mục giỏ hàng không thuộc về người dùng này");
        }

        gioHangChiTietRepo.delete(gioHangChiTiet);
        updateCartTotal(gioHang);
    }
    public GioHang getCart(Integer userId) {
        return getOrCreateCart(userId);
    }
}
