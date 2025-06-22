package com.example.demo.Service;

import com.example.demo.DTOs.CartItemDTO;
import com.example.demo.DTOs.DTOhoaDon;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.Responses.HoaDonResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HoaDonService {
    private final HoaDonRepository hoaDonRepository;
    private final UserRepository userRepository;
    private final San_pham_Repo san_pham_repo;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;
    private final KhuyenMaiSanPhamRepository khuyenMaiSanPhamRepository;

    @Transactional
    public HoaDon createHoaDon(DTOhoaDon dtOhoaDon) throws Exception {
        try {
            // 1. Lấy thông tin user
            User user = userRepository.findById(dtOhoaDon.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + dtOhoaDon.getUserId()));

            // 2. Tạo hóa đơn
            HoaDon hoaDon = new HoaDon();
            hoaDon.setUser(user);
            hoaDon.setNv(null);
            hoaDon.setNgayTao(LocalDateTime.now());
            hoaDon.setDiaChiGiaoHang(dtOhoaDon.getDiaChiGiaoHang());
            hoaDon.setMaVanChuyen(UUID.randomUUID().toString().substring(0, 10));
            hoaDon.setNgayGiao(null);
            hoaDon.setTrangThai(TrangThaiHoaDon.PENDING);
            hoaDon.setPhuongThucThanhToan(dtOhoaDon.getPhuongThucThanhToan());

            PhieuGiamGia phieuGiamGia = phieuGiamGiaRepo.findById(dtOhoaDon.getIdPhieuGiam()).orElseThrow(()-> new RuntimeException("Không tìm thấy id phiếu giảm"));
            hoaDon.setPhieuGiamGia(phieuGiamGia);

            // 3. Tạo các chi tiết hóa đơn
            List<HoaDonChiTiet> donChiTiets = new ArrayList<>();
            for (CartItemDTO cartItemDto : dtOhoaDon.getCartItems()) {
                SanPham sanPham = san_pham_repo.findById(cartItemDto.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: "
                                + cartItemDto.getIdSanPham()));

                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHd(hoaDon);
                hoaDonChiTiet.setSp(sanPham);
                hoaDonChiTiet.setGia(khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId()) == null
                        ? sanPham.getGia() : khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId()));

                hoaDonChiTiet.setSoLuong(cartItemDto.getSoLuong());
                hoaDonChiTiet.setTongTien(khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId()) == null
                        ? sanPham.getGia() : khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId())
                        .multiply(BigDecimal.valueOf(cartItemDto.getSoLuong())));
                donChiTiets.add(hoaDonChiTiet);
            }

            BigDecimal soTienGiam = BigDecimal.ZERO;
            LocalDate now = LocalDate.now();

            // 4. Tính tổng tiền
            BigDecimal totalHd = donChiTiets.stream()
                    .map(HoaDonChiTiet::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean isValidPhieu = phieuGiamGia.getSoLuong() > 0
                    && phieuGiamGia.getNgayKetThuc().isAfter(now)
                    && "Đang hoạt động".equals(phieuGiamGia.getTrangThai())
                    && totalHd.compareTo(phieuGiamGia.getGiaTriToiThieu()) >= 0;

            if (isValidPhieu) {
                String loai = phieuGiamGia.getLoaiPhieuGiam();

                switch (loai) {
                    case "Theo %":
                        soTienGiam = totalHd.multiply(phieuGiamGia.getGiaTriGiam().divide(BigDecimal.valueOf(100)));
                        if (soTienGiam.compareTo(phieuGiamGia.getGiamToiDa()) > 0) {
                            soTienGiam = phieuGiamGia.getGiamToiDa();
                        }
                        break;

                    case "Theo số tiền":
                        soTienGiam = phieuGiamGia.getGiaTriGiam();
                        if (soTienGiam.compareTo(phieuGiamGia.getGiamToiDa()) > 0) {
                            soTienGiam = phieuGiamGia.getGiamToiDa();; // không thể giảm nhiều hơn tổng hóa đơn
                        }
                        break;
                    default:
                        break; // không áp dụng nếu loại không hợp lệ
                }

                // Trừ số lượng nếu giảm hợp lệ
                if (soTienGiam.compareTo(BigDecimal.ZERO) > 0) {
                    phieuGiamGia.setSoLuong(phieuGiamGia.getSoLuong() - 1);
                    phieuGiamGiaRepo.save(phieuGiamGia);
                }
            }
            hoaDon.setTamTinh(totalHd);
            hoaDon.setSoTienGiam(soTienGiam);
            hoaDon.setTongTien(totalHd.subtract(soTienGiam));

            // 5. Lưu vào database (transaction sẽ commit tại đây nếu không có lỗi)
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            hoaDonChiTietRepository.saveAll(donChiTiets);

            return savedHoaDon;
        } catch (RuntimeException e) {
            throw new RuntimeException("Lỗi khi tạo hóa đơn: " + e.getMessage(), e);
        }
    }

    public HoaDonResponse findById(Integer id) throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(HoaDon.class, HoaDonResponse.class).addMappings(mapper ->{
            mapper.map(src -> src.getUser().getId(), HoaDonResponse::setUserId);
            mapper.map(src -> src.getNv().getId(), HoaDonResponse::setNvId);
//                mapper.map(src -> src.get().getId(), HoaDonResponse::setUserId);
        });
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow(() -> new Exception("khong tim thay hoa don"));
        return modelMapper.map(hoaDon, HoaDonResponse.class);
    }

    public List<HoaDonResponse> getAll(Integer user_id) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(HoaDon.class, HoaDonResponse.class).addMappings(mapper ->{
                mapper.map(src -> src.getUser().getId(), HoaDonResponse::setUserId);
                mapper.map(src -> src.getNv().getId(), HoaDonResponse::setNvId);
//                mapper.map(src -> src.get().getId(), HoaDonResponse::setUserId);
        });
        return hoaDonRepository.findByIdUser(user_id).stream().map(order -> {
            HoaDonResponse orderResponse = modelMapper.map(order, HoaDonResponse.class);
            return orderResponse;
        }).toList();
    }

    @Transactional
    public HoaDon updateHoaDon(Integer id, DTOhoaDon dtOhoaDon, Integer idNV) throws Exception {

        // 1. Lấy dữ liệu
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy hoá đơn"));

        User user = userRepository.findById(dtOhoaDon.getUserId())
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng"));

        User nv = userRepository.findById(idNV)
                .orElseThrow(() -> new Exception("Không tìm thấy nhân viên"));

        // 2. Xoá chi tiết cũ
        List<HoaDonChiTiet> oldDetails = hoaDonChiTietRepository.findByIdOrder(id);
        hoaDonChiTietRepository.deleteAll(oldDetails);

        // 3. Tạo lại danh sách chi tiết mới
        List<HoaDonChiTiet> newDetails = new ArrayList<>();
        for (CartItemDTO cartItemDto : dtOhoaDon.getCartItems()) {
            SanPham sanPham = san_pham_repo.findById(cartItemDto.getIdSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + cartItemDto.getIdSanPham()));

            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHd(hoaDon);
            chiTiet.setSp(sanPham);
            chiTiet.setGia(khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId()) == null
                    ? sanPham.getGia() : khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId()));
            chiTiet.setSoLuong(cartItemDto.getSoLuong());
            chiTiet.setTongTien(khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId()) == null
                    ? sanPham.getGia() : khuyenMaiSanPhamRepository.getGiaKM(sanPham.getId())
                    .multiply(BigDecimal.valueOf(cartItemDto.getSoLuong())));

            newDetails.add(chiTiet);
        }

        // 4. Tính lại tổng tiền
        BigDecimal tong = newDetails.stream()
                .map(HoaDonChiTiet::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal soTienGiam = BigDecimal.ZERO;

        // 5. Áp dụng lại phiếu giảm giá nếu có (optional)
        if (dtOhoaDon.getIdPhieuGiam() != null) {
            PhieuGiamGia phieu = phieuGiamGiaRepo.findById(dtOhoaDon.getIdPhieuGiam())
                    .orElseThrow(() -> new Exception("Không tìm thấy phiếu giảm giá"));

            boolean isValid = phieu.getSoLuong() > 0
                    && phieu.getNgayKetThuc().isAfter(LocalDate.now())
                    && "Đang hoạt động".equals(phieu.getTrangThai())
                    && tong.compareTo(phieu.getGiaTriToiThieu()) >= 0;

            if (isValid) {
                switch (phieu.getLoaiPhieuGiam()) {
                    case "Theo %":
                        soTienGiam = tong.multiply(phieu.getGiaTriGiam().divide(BigDecimal.valueOf(100)));
                        if (soTienGiam.compareTo(phieu.getGiamToiDa()) > 0) {
                            soTienGiam = phieu.getGiamToiDa();
                        }
                        break;

                    case "Theo số tiền":
                        soTienGiam = phieu.getGiaTriGiam();
                        if (soTienGiam.compareTo(phieu.getGiamToiDa()) > 0) {
                            soTienGiam = phieu.getGiamToiDa();
                        }
                        break;
                }
            }
        }

        // 6. Gán dữ liệu vào hoá đơn
        hoaDon.setUser(user);
        hoaDon.setNv(nv);
        hoaDon.setTamTinh(tong);
        hoaDon.setSoTienGiam(soTienGiam);
        hoaDon.setTongTien(tong.subtract(soTienGiam));
        hoaDon.setTrangThai(dtOhoaDon.getTrangThai());
        hoaDon.setPhuongThucThanhToan(dtOhoaDon.getPhuongThucThanhToan());
        hoaDon.setNgayGiao(dtOhoaDon.getNgayGiao());
        hoaDon.setDiaChiGiaoHang(dtOhoaDon.getDiaChiGiaoHang());

        // 7. Lưu dữ liệu
        hoaDonRepository.save(hoaDon);
        hoaDonChiTietRepository.saveAll(newDetails);

        return hoaDon;
    }

    @Transactional
    public void deleteHoaDon(Integer id) throws Exception {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow(() -> new Exception("khong tim thay hoa don"));
        hoaDon.setTrangThai(TrangThaiHoaDon.CANCELLED);
        hoaDonRepository.save(hoaDon);
    }
}
