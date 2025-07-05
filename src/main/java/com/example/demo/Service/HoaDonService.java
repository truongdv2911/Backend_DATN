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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            hoaDon.setMaHD(taoMaHoaDonTuDong());
            // Nếu là hóa đơn tại quầy thì set nhân viên và trạng thái hoàn tất, maVanChuyen = null
            if (dtOhoaDon.getLoaiHD() != null && dtOhoaDon.getLoaiHD() == 1) {
                hoaDon.setTrangThai(TrangThaiHoaDon.COMPLETED);
                hoaDon.setMaVanChuyen(null);
                hoaDon.setNgayGiao(null);
                hoaDon.setUser(null);
                hoaDon.setDiaChiGiaoHang("Tại quầy");
                if (dtOhoaDon.getNvId() != null) {
                    User nv = userRepository.findById(dtOhoaDon.getNvId()).orElse(null);
                    hoaDon.setNv(nv);
                } else {
                    hoaDon.setNv(null);
                }
            } else {
                hoaDon.setTrangThai(TrangThaiHoaDon.PENDING);
                hoaDon.setNv(null);
                hoaDon.setUser(user);
                hoaDon.setDiaChiGiaoHang(dtOhoaDon.getDiaChiGiaoHang());
                hoaDon.setMaVanChuyen(UUID.randomUUID().toString().substring(0, 8));
                hoaDon.setNgayGiao(LocalDateTime.now().plusDays(3));
            }
            hoaDon.setNgayTao(LocalDateTime.now());
            hoaDon.setPhuongThucThanhToan(dtOhoaDon.getPhuongThucThanhToan());
            hoaDon.setSdt(dtOhoaDon.getSdt());
            hoaDon.setLoaiHD(dtOhoaDon.getLoaiHD());

            // Kiểm tra phiếu giảm giá (nếu có)
            PhieuGiamGia phieuGiamGia = null;
            if (dtOhoaDon.getIdPhieuGiam() != null) {
                phieuGiamGia = phieuGiamGiaRepo.findById(dtOhoaDon.getIdPhieuGiam())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá!"));
                LocalDate now = LocalDate.now();
                if (phieuGiamGia.getSoLuong() == null || phieuGiamGia.getSoLuong() <= 0) {
                    throw new RuntimeException("Phiếu giảm giá đã hết lượt sử dụng!");
                }
                if (!"active".equals(phieuGiamGia.getTrangThai())) {
                    throw new RuntimeException("Phiếu giảm giá không còn hoạt động!");
                }
                if (phieuGiamGia.getNgayKetThuc() != null && phieuGiamGia.getNgayKetThuc().isBefore(now)) {
                    throw new RuntimeException("Phiếu giảm giá đã hết hạn!");
                }
            }
            hoaDon.setPhieuGiamGia(phieuGiamGia);

            // Kiểm tra tồn kho cho tất cả loại hóa đơn
            for (CartItemDTO cartItemDto : dtOhoaDon.getCartItems()) {
                SanPham sanPham = san_pham_repo.findById(cartItemDto.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + cartItemDto.getIdSanPham()));
                Integer soLuongTon = sanPham.getSoLuongTon();
                int soLuongBan = cartItemDto.getSoLuong();
                if (soLuongTon == null || soLuongTon < soLuongBan) {
                    throw new RuntimeException("Sản phẩm '" + sanPham.getTenSanPham() + "' không đủ tồn kho. Hiện còn: " + soLuongTon + ", cần: " + soLuongBan);
                }
            }

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

                // Trừ số lượng tồn kho cho cả hai loại hóa đơn
                Integer soLuongTon = sanPham.getSoLuongTon();
                if (soLuongTon != null) {
                    int soLuongBan = cartItemDto.getSoLuong();
                    sanPham.setSoLuongTon(Math.max(0, soLuongTon - soLuongBan));
                    san_pham_repo.save(sanPham);
                }
            }

            BigDecimal soTienGiam = BigDecimal.ZERO;
            LocalDate now = LocalDate.now();

            // 4. Tính tổng tiền
            BigDecimal totalHd = donChiTiets.stream()
                    .map(HoaDonChiTiet::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean isValidPhieu = phieuGiamGia != null
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

            // Sau khi tính tổng tiền
            if (phieuGiamGia != null) {
                if (totalHd.compareTo(phieuGiamGia.getGiaTriToiThieu()) < 0) {
                    throw new RuntimeException("Tổng tiền chưa đủ điều kiện áp dụng phiếu giảm giá!");
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
            mapper.map(src -> src.getPhieuGiamGia().getId(), HoaDonResponse::setUserId);
        });
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow(() -> new Exception("khong tim thay hoa don"));
        return modelMapper.map(hoaDon, HoaDonResponse.class);
    }

    public List<HoaDonResponse> getAll(Integer user_id) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(HoaDon.class, HoaDonResponse.class).addMappings(mapper ->{
                mapper.map(src -> src.getUser().getId(), HoaDonResponse::setUserId);
                mapper.map(src -> src.getNv().getId(), HoaDonResponse::setNvId);
                mapper.map(src -> src.getPhieuGiamGia().getId(), HoaDonResponse::setUserId);
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
        hoaDon.setSdt(dtOhoaDon.getSdt());
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
    public String taoMaHoaDonTuDong() {
        Pageable limit1 =  PageRequest.of(0, 1);
        List<String> maList = hoaDonRepository.findTopMaHoaDon(limit1);

        int nextNumber = 1;

        if (!maList.isEmpty()) {
            String maMax = maList.get(0);

            if (maMax != null && maMax.startsWith("HD")) {
                try {
                    String numberStr = maMax.substring(2); // bỏ "HD"
                    nextNumber = Integer.parseInt(numberStr) + 1;
                } catch (NumberFormatException e) {
                    // Lỡ gặp mã sai định dạng: vẫn sinh từ 1
                    nextNumber = 1;
                }
            }
        }
        // Pad số về dạng 3 chữ số: "HD001", "HD042", ...
        return String.format("HD%03d", nextNumber);
    }

    private HoaDonResponse convertToResponse(HoaDon hoaDon) {
        HoaDonResponse response = new HoaDonResponse();
        response.setId(hoaDon.getId());
        response.setMaHD(hoaDon.getMaHD());
        response.setLoaiHD(hoaDon.getLoaiHD());
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
        if (hoaDon.getPhieuGiamGia() != null) response.setIdPhieuGiam(hoaDon.getPhieuGiamGia().getId());
        if (hoaDon.getPhieuGiamGia() != null) response.setMaPGG(hoaDon.getPhieuGiamGia().getMaPhieu());
        return response;
    }



    @Transactional
    public HoaDonResponse updateTrangThai(Integer id, String trangThai) throws Exception {
        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn với ID: " + id));
        if (!isValidTrangThaiTransition(hoaDon.getTrangThai(), trangThai)) {
            throw new Exception("Chuyển đổi trạng thái từ " + hoaDon.getTrangThai() + " sang " + trangThai + " không hợp lệ");
        }
        hoaDon.setTrangThai(trangThai);
        HoaDon updatedHoaDon = hoaDonRepository.save(hoaDon);
        return convertToResponse(updatedHoaDon);
    }
    private boolean isValidTrangThaiTransition(String current, String next) {
        if (current == null || next == null) {
            return false;
        }

        if (current.equals(TrangThaiHoaDon.PENDING)) {
            return next.equals(TrangThaiHoaDon.PROCESSING) || next.equals(TrangThaiHoaDon.CANCELLED);
        } else if (current.equals(TrangThaiHoaDon.PROCESSING)) {
            return next.equals(TrangThaiHoaDon.PACKING) ;
        } else if (current.equals(TrangThaiHoaDon.PACKING)) {
            return next.equals(TrangThaiHoaDon.SHIPPED);
        } else if (current.equals(TrangThaiHoaDon.SHIPPED)) {
            return next.equals(TrangThaiHoaDon.DELIVERED) || next.equals(TrangThaiHoaDon.FAILED);
        } else if (current.equals(TrangThaiHoaDon.DELIVERED)) {
            return next.equals(TrangThaiHoaDon.COMPLETED) ;
        } else if (current.equals(TrangThaiHoaDon.COMPLETED) || current.equals(TrangThaiHoaDon.CANCELLED)) {
            return false; // Không cho phép thay đổi từ trạng thái cuối
        } else if (current.equals(TrangThaiHoaDon.FAILED)) {
            return next.equals(TrangThaiHoaDon.CANCELLED) || next.equals(TrangThaiHoaDon.PENDING);
        }

        return false; // Trạng thái không hợp lệ
    }
    @Transactional(readOnly = true)
    public Map<String, Long> countByTrangThai() {
        List<Object[]> result = hoaDonRepository.countByTrangThaiGroup();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : result) {
            String trangThai = (String) row[0];
            Long count = (Long) row[1];
            map.put(trangThai, count);
        }
        return map;
    }
    @Transactional(readOnly = true)
    public Page<HoaDonResponse> getAllPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        Page<HoaDon> hoaDonPage = hoaDonRepository.findAll(pageable);
        return hoaDonPage.map(this::convertToResponse);
    }


}
