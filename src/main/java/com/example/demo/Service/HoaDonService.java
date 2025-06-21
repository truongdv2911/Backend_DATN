package com.example.demo.Service;

import com.example.demo.DTOs.CartItemDTO;
import com.example.demo.DTOs.DTOhoaDon;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.Responses.HoaDonResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class HoaDonService {
    private static final Logger logger = LoggerFactory.getLogger(HoaDonService.class);

    private final HoaDonRepository hoaDonRepository;
    private final UserRepository userRepository;
    private final San_pham_Repo san_pham_repo;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;

    private BigDecimal calculateSoTienGiam(BigDecimal tamTinh, PhieuGiamGia phieuGiamGia) {
        if (phieuGiamGia == null) return BigDecimal.ZERO;

        boolean isValid = "Đang hoạt động".equals(phieuGiamGia.getTrangThai())
                && phieuGiamGia.getNgayKetThuc().isAfter(LocalDate.now())
                && phieuGiamGia.getSoLuong() > 0;

        if (!isValid) return BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;
        if ("Theo %".equals(phieuGiamGia.getLoaiPhieuGiam())) {
            if (tamTinh.compareTo(phieuGiamGia.getGiaTriToiThieu()) >= 0) {
                discount = tamTinh.multiply(phieuGiamGia.getGiaTriGiam().divide(BigDecimal.valueOf(100)));
                if (discount.compareTo(phieuGiamGia.getGiamToiDa()) > 0) discount = phieuGiamGia.getGiamToiDa();
            }
        } else if ("Theo số tiền".equals(phieuGiamGia.getLoaiPhieuGiam())) {
            discount = phieuGiamGia.getGiaTriGiam();
            if (discount.compareTo(phieuGiamGia.getGiamToiDa()) > 0) discount = phieuGiamGia.getGiamToiDa();
        }

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            phieuGiamGia.setSoLuong(phieuGiamGia.getSoLuong() - 1);
            if (phieuGiamGia.getSoLuong() <= 0) phieuGiamGia.setTrangThai("Ngừng");
            phieuGiamGiaRepo.save(phieuGiamGia);
        }

        return discount;
    }

    private User getCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return userRepository.findById(6).orElseThrow();
        return userRepository.findById(6).orElseThrow();
    }

    @Transactional
    public HoaDon createHoaDon(DTOhoaDon dto) throws Exception {
        User user = userRepository.findById(dto.getUserId()).orElseThrow();
        User nv = getCurrentNhanVien();

        HoaDon hoaDon = new HoaDon();
        hoaDon.setUser(user);
        hoaDon.setNv(nv);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setDiaChiGiaoHang(dto.getDiaChiGiaoHang());
        hoaDon.setMaVanChuyen(UUID.randomUUID().toString().substring(0, 10));
        hoaDon.setNgayGiao(LocalDate.from(dto.getNgayGiao()));
        hoaDon.setTrangThai(dto.getTrangThai().getValue());
        hoaDon.setPhuongThucThanhToan(dto.getPhuongThucThanhToan());

        PhieuGiamGia pgg = null;
        if (dto.getIdPhieuGiamGia() != null) {
            pgg = phieuGiamGiaRepo.findById(dto.getIdPhieuGiamGia()).orElseThrow();
            hoaDon.setPhieuGiamGia(pgg);
        }

        List<HoaDonChiTiet> chiTietList = new ArrayList<>();
        for (CartItemDTO item : dto.getCartItems()) {
            SanPham sp = san_pham_repo.findById(item.getIdSanPham()).orElseThrow();
            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHd(hoaDon);
            ct.setSp(sp);
            ct.setGia(sp.getGia());
            ct.setSoLuong(item.getSoLuong());
            ct.setTongTien(sp.getGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
            chiTietList.add(ct);
        }

        BigDecimal tamTinh = chiTietList.stream().map(HoaDonChiTiet::getTongTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal giam = calculateSoTienGiam(tamTinh, pgg);

        hoaDon.setTamTinh(tamTinh);
        hoaDon.setSoTienGiam(giam);
        hoaDon.setTongTien(tamTinh.subtract(giam));

        HoaDon saved = hoaDonRepository.save(hoaDon);
        hoaDonChiTietRepository.saveAll(chiTietList);
        return saved;
    }



    private HoaDonResponse convertToResponse(HoaDon hoaDon) {
        HoaDonResponse response = new HoaDonResponse();
        response.setId(hoaDon.getId());
        response.setTamTinh(hoaDon.getTamTinh());
        response.setTongTien(hoaDon.getTongTien());
        response.setSoTienGiam(hoaDon.getSoTienGiam());
        response.setDiaChiGiaoHang(hoaDon.getDiaChiGiaoHang());
        response.setMaVanChuyen(hoaDon.getMaVanChuyen());
        response.setNgayGiao(hoaDon.getNgayGiao());
        response.setNgayTao(hoaDon.getNgayTao());
        response.setTrangThai(hoaDon.getTrangThai());
        response.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        if (hoaDon.getUser() != null) response.setUserId(hoaDon.getUser().getId());
        if (hoaDon.getUser() != null) response.setTen(hoaDon.getUser().getTen());
        if (hoaDon.getUser() != null) response.setSdt(hoaDon.getUser().getSdt());
        if (hoaDon.getNv() != null) response.setNvId(hoaDon.getNv().getId());
        if (hoaDon.getNv() != null) response.setNvName(hoaDon.getNv().getTen());
        if (hoaDon.getPhieuGiamGia() != null) response.setPGGid(hoaDon.getPhieuGiamGia().getId());
        if (hoaDon.getPhieuGiamGia() != null) response.setMaPGG(hoaDon.getPhieuGiamGia().getMaPhieu());
        return response;
    }

    @Transactional(readOnly = true)
    public HoaDonResponse findById(Integer id) throws Exception {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn với ID: " + id));
        return convertToResponse(hoaDon);
    }


    @Transactional(readOnly = true)
    public Page<HoaDonResponse> getAllPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        Page<HoaDon> hoaDonPage = hoaDonRepository.findAll(pageable);
        return hoaDonPage.map(this::convertToResponse);
    }



    @Transactional
    public HoaDon updateHoaDon(Integer id, DTOhoaDon dtOhoaDon) throws Exception {
        logger.info("Bắt đầu cập nhật hóa đơn với ID: {}", id);

        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn với ID: " + id));

        User user = userRepository.findById(dtOhoaDon.getUserId())
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng với ID: " + dtOhoaDon.getUserId()));

        User nv = getCurrentNhanVien();

        List<HoaDonChiTiet> oldDetails = hoaDonChiTietRepository.findByIdOrder(id);
        hoaDonChiTietRepository.deleteAll(oldDetails);
        logger.debug("Đã xóa {} chi tiết hóa đơn cũ", oldDetails.size());

        List<HoaDonChiTiet> newDetails = new ArrayList<>();
        for (CartItemDTO cartItemDto : dtOhoaDon.getCartItems()) {
            SanPham sanPham = san_pham_repo.findById(cartItemDto.getIdSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + cartItemDto.getIdSanPham()));

            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHd(hoaDon);
            chiTiet.setSp(sanPham);
            chiTiet.setGia(sanPham.getGia());
            chiTiet.setSoLuong(cartItemDto.getSoLuong());
            chiTiet.setTongTien(sanPham.getGia().multiply(BigDecimal.valueOf(cartItemDto.getSoLuong())));
            newDetails.add(chiTiet);
        }

        BigDecimal tong = newDetails.stream()
                .map(HoaDonChiTiet::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal soTienGiam = BigDecimal.ZERO;
        PhieuGiamGia phieu = null;
        if (dtOhoaDon.getIdPhieuGiamGia() != null) {
            phieu = phieuGiamGiaRepo.findById(dtOhoaDon.getIdPhieuGiamGia())
                    .orElseThrow(() -> new Exception("Không tìm thấy phiếu giảm giá với ID: " + dtOhoaDon.getIdPhieuGiamGia()));
            soTienGiam = calculateSoTienGiam(tong, phieu);
        }
        hoaDon.setPhieuGiamGia(phieu);

        hoaDon.setUser(user);
        hoaDon.setNv(nv);
        hoaDon.setTamTinh(tong);
        hoaDon.setSoTienGiam(soTienGiam);
        hoaDon.setTongTien(tong.subtract(soTienGiam));
        hoaDon.setTrangThai(String.valueOf(dtOhoaDon.getTrangThai())); // Cập nhật trạng thái từ DTO
        hoaDon.setPhuongThucThanhToan(dtOhoaDon.getPhuongThucThanhToan());
        hoaDon.setNgayGiao(LocalDate.from(dtOhoaDon.getNgayGiao()));
        hoaDon.setDiaChiGiaoHang(dtOhoaDon.getDiaChiGiaoHang());

        hoaDonRepository.save(hoaDon);
        hoaDonChiTietRepository.saveAll(newDetails);

        return hoaDon;
    }

    @Transactional
    public void deleteHoaDon(Integer id) throws Exception {
        logger.debug("Hủy hóa đơn với ID: {}", id);

        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn với ID: " + id));
        hoaDon.setTrangThai(String.valueOf(TrangThaiHoaDon.DELIVERED));
        hoaDonRepository.save(hoaDon);
        logger.info("Hủy hóa đơn thành công, ID: {}", id);
    }
    public Page<HoaDonResponse> searchAdvanced(
            String ma, String trangThai, String phuongThuc, String tenNguoiDung, String sdt,
            LocalDateTime from, LocalDateTime to, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        Page<HoaDon> results = hoaDonRepository.searchAdvanced(ma, trangThai, phuongThuc, tenNguoiDung, sdt, from, to, pageable);
        return results.map(this::convertToResponse);
    }

}

