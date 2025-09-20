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
    private final San_pham_Service sanPhamService;

    // Danh sách tỉnh/thành phố miền Bắc
    private static final Set<String> MIEN_BAC = Set.of(
        "Hà Nội", "Bắc Ninh", "Cao Bằng", "Điện Biên", "Hải Phòng", "Lai Châu", "Lạng Sơn",
        "Lào Cai", "Ninh Bình", "Phú Thọ", "Quảng Ninh", "Sơn La", "Thái Nguyên", "Tuyên Quang"
    );

    // Danh sách tỉnh/thành phố miền Nam
    private static final Set<String> MIEN_NAM = Set.of(
        "Hồ Chí Minh", "Cần Thơ", "An Giang", "Cà Mau", "Đồng Nai", "Đồng Tháp", "Tây Ninh", "Vĩnh Long"
    );
    private static final Set<String> MIEN_TRUNG = Set.of(
        "Đà Nẵng", "Huế", "Đắk Lắk", "Hà Tĩnh", "Khánh Hòa", "Lâm Đồng", "Nghệ An", "Quảng Ngãi", "Quảng Trị", "Thanh Hóa"
    );

    public boolean isMienBac(String province) {
        return MIEN_BAC.contains(province);
    }

    public boolean isMienTrung(String province) {
        return MIEN_TRUNG.contains(province);
    }

    public boolean isMienNam(String province) {
        return MIEN_NAM.contains(province);
    }

    @Transactional
    public HoaDon createHoaDon(DTOhoaDon dtOhoaDon) throws Exception {
        try {
            // 2. Tạo hóa đơn
            //Lấy thông tin user
            BigDecimal phiShip = BigDecimal.ZERO;
            HoaDon hoaDon = new HoaDon();
            if (dtOhoaDon.getUserId() != null) {
                User user = userRepository.findById(dtOhoaDon.getUserId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + dtOhoaDon.getUserId()));
                hoaDon.setUser(user);
            } else {
                hoaDon.setUser(null); // Hóa đơn không gán người dùng
            }

            hoaDon.setMaHD(taoMaHoaDonTuDong());
            // Nếu là hóa đơn tại quầy thì set nhân viên và trạng thái hoàn tất, maVanChuyen = null
            if (dtOhoaDon.getLoaiHD() != null && dtOhoaDon.getLoaiHD() == 1) {
                if("COD".equalsIgnoreCase(dtOhoaDon.getPhuongThucThanhToan())){
                    throw new RuntimeException("Hóa đơn tại quầy không thể dùng thanh toán COD");
                }
                hoaDon.setTrangThai(TrangThaiHoaDon.COMPLETED);
                hoaDon.setMaVanChuyen(null);
                hoaDon.setNgayGiao(LocalDateTime.now());
                hoaDon.setDiaChiGiaoHang("Tại quầy");
                hoaDon.setPhiShip(new BigDecimal(0));
                if (dtOhoaDon.getNvId() != null) {
                    User nv = userRepository.findById(dtOhoaDon.getNvId()).orElse(null);
                    hoaDon.setNv(nv);
                } else {
                    hoaDon.setNv(null);
                }
            } else {
                hoaDon.setTrangThai(TrangThaiHoaDon.PENDING);
                hoaDon.setNv(null);
                hoaDon.setDiaChiGiaoHang(dtOhoaDon.getDiaChiGiaoHang());

                // --- BẮT ĐẦU: Tính phí ship và ngày giao hàng tự động ---
                String[] addressParts = dtOhoaDon.getDiaChiGiaoHang() != null ? dtOhoaDon.getDiaChiGiaoHang().split(",") : new String[0];
                String province = addressParts.length > 0 ? addressParts[addressParts.length - 1].trim() : "";
                String district = addressParts.length > 1 ? addressParts[addressParts.length - 2].trim() : "";
                String fromProvince = "Hà Nội"; // Có thể thay đổi nếu cần lấy động
                String loaiVanChuyen = getLoaiVanChuyen(fromProvince, province);
                String khuVuc = isNoiThanh(province, district) ? "Nội thành" : "Ngoại thành";
                double totalWeight = 0;
                for (CartItemDTO cartItemDto : dtOhoaDon.getCartItems()) {
                    totalWeight += cartItemDto.getSoLuong()*0.5; // Mặc định mỗi sản phẩm 1kg
                }
                phiShip = tinhPhiShip(loaiVanChuyen, khuVuc, totalWeight);
                int soNgayGiao = tinhSoNgayGiao(loaiVanChuyen);
                hoaDon.setMaVanChuyen(UUID.randomUUID().toString().substring(0, 8));

                if (dtOhoaDon.getIsFast() == 1 && ("DAC_BIET".equals(loaiVanChuyen) || "LIEN_MIEN".equals(loaiVanChuyen))){
                    hoaDon.setPhiShip(phiShip.add(BigDecimal.valueOf(15000)));
                    hoaDon.setNgayGiao(LocalDateTime.now().plusDays(soNgayGiao - 1));
                }else {
                    hoaDon.setPhiShip(phiShip);
                    hoaDon.setNgayGiao(LocalDateTime.now().plusDays(soNgayGiao));
                }
                // --- KẾT THÚC: Tính phí ship và ngày giao hàng tự động ---

            }
            hoaDon.setTenNguoiNhan(dtOhoaDon.getTenNguoiNhan());
            hoaDon.setNgayTao(LocalDateTime.now());
            hoaDon.setPhuongThucThanhToan(dtOhoaDon.getPhuongThucThanhToan());
            hoaDon.setSdt(dtOhoaDon.getSdt());
            hoaDon.setLoaiHD(dtOhoaDon.getLoaiHD());
            hoaDon.setQrCodeUrl(dtOhoaDon.getQrCodeUrl());

            // Kiểm tra phiếu giảm giá (nếu có)
            PhieuGiamGia phieuGiamGia = null;
            if (dtOhoaDon.getIdPhieuGiam() != null) {
                phieuGiamGia = phieuGiamGiaRepo.findById(dtOhoaDon.getIdPhieuGiam())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá!"));
                LocalDateTime now = LocalDateTime.now();
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
                Integer soLuongBan = cartItemDto.getSoLuong();
                if (soLuongTon == null || soLuongTon < soLuongBan) {
                    throw new RuntimeException("Sản phẩm '" + sanPham.getTenSanPham() + "' không đủ tồn kho. Hiện còn: " + soLuongTon + ", cần: " + soLuongBan);
                }
                // 🔥 Trừ kho ngay nếu là tại quầy hoặc online chuyển khoản
                boolean truKhoNgay = (dtOhoaDon.getLoaiHD() == 1) ||
                        ("Chuyển khoản".equalsIgnoreCase(dtOhoaDon.getPhuongThucThanhToan()));

                if (truKhoNgay) {
                    Integer soLuongTonConLai = soLuongTon - soLuongBan;
                    sanPham.setTrangThai(soLuongTonConLai > 0 ? "Đang kinh doanh" : "Hết hàng");
                    sanPham.setSoLuongTon(Math.max(0, soLuongTonConLai));
                    san_pham_repo.save(sanPham);
                }
            }

            // 3. Tạo các chi tiết hóa đơn
            List<HoaDonChiTiet> donChiTiets = new ArrayList<>();
            for (CartItemDTO cartItemDto : dtOhoaDon.getCartItems()) {
                SanPham sanPham = san_pham_repo.findById(cartItemDto.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: "
                                + cartItemDto.getIdSanPham()));

                // Lấy giá khuyến mãi nếu có, ngược lại dùng giá gốc
                BigDecimal giaBan = sanPham.getGiaKM() != null ? sanPham.getGiaKM() : sanPham.getGia();

                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHd(hoaDon);
                hoaDonChiTiet.setSp(sanPham);
                hoaDonChiTiet.setGia(giaBan);
                hoaDonChiTiet.setSoLuong(cartItemDto.getSoLuong());
                hoaDonChiTiet.setTongTien(giaBan.multiply(BigDecimal.valueOf(cartItemDto.getSoLuong())));
                donChiTiets.add(hoaDonChiTiet);
            }

            BigDecimal soTienGiam = BigDecimal.ZERO;

            // 4. Tính tổng tiền
            BigDecimal totalHd = donChiTiets.stream()
                    .map(HoaDonChiTiet::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean isValidPhieu = phieuGiamGia != null
                    && totalHd.compareTo(phieuGiamGia.getGiaTriToiThieu()) >= 0;

            if (isValidPhieu) {
                String loai = phieuGiamGia.getLoaiPhieuGiam();

                switch (loai) {
                    case "theo_phan_tram":
                        soTienGiam = totalHd.multiply(phieuGiamGia.getGiaTriGiam().divide(BigDecimal.valueOf(100)));
                        if (soTienGiam.compareTo(phieuGiamGia.getGiamToiDa()) > 0) {
                            soTienGiam = phieuGiamGia.getGiamToiDa();
                        }
                        break;

                    case "theo_so_tien":
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
            hoaDon.setTongTien((totalHd.subtract(soTienGiam)).add(hoaDon.getPhiShip()));

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
        return convertToResponse(hoaDon);
    }

    public List<HoaDonResponse> getAll(Integer user_id) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(HoaDon.class, HoaDonResponse.class).addMappings(mapper ->{
                mapper.map(src -> src.getUser().getId(), HoaDonResponse::setUserId);
                mapper.map(src -> src.getNv().getId(), HoaDonResponse::setNvId);
                mapper.map(src -> src.getPhieuGiamGia().getId(), HoaDonResponse::setUserId);
        });
        return hoaDonRepository.findByIdUser(user_id).stream().map(order -> {
            HoaDonResponse orderResponse = convertToResponse(order);
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
                    && phieu.getNgayKetThuc().isAfter(LocalDateTime.now())
                    && "Đang hoạt động".equals(phieu.getTrangThai())
                    && tong.compareTo(phieu.getGiaTriToiThieu()) >= 0;

            if (isValid) {
                switch (phieu.getLoaiPhieuGiam()) {
                    case "theo_phan_tram":
                        soTienGiam = tong.multiply(phieu.getGiaTriGiam().divide(BigDecimal.valueOf(100)));
                        if (soTienGiam.compareTo(phieu.getGiamToiDa()) > 0) {
                            soTienGiam = phieu.getGiamToiDa();
                        }
                        break;

                    case "theo_so_tien":
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

    public HoaDonResponse convertToResponse(HoaDon hoaDon) {
        HoaDonResponse response = new HoaDonResponse();
        response.setId(hoaDon.getId());
        response.setMaHD(hoaDon.getMaHD());
        response.setLoaiHD(hoaDon.getLoaiHD());
        response.setQrCodeUrl(hoaDon.getQrCodeUrl());
        response.setTamTinh(hoaDon.getTamTinh());
        response.setTongTien(hoaDon.getTongTien());
        response.setSoTienGiam(hoaDon.getSoTienGiam());
        response.setDiaChiGiaoHang(hoaDon.getDiaChiGiaoHang());
        response.setMaVanChuyen(hoaDon.getMaVanChuyen());
        response.setNgayGiao(hoaDon.getNgayGiao());
        response.setNgayTao(hoaDon.getNgayTao());
        response.setTrangThai(hoaDon.getTrangThai());
        response.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        response.setPhiShip(hoaDon.getPhiShip());
        response.setTenNguoiNhan(hoaDon.getTenNguoiNhan());
        response.setSdt1(hoaDon.getSdt());
        if (hoaDon.getUser() != null){
            response.setUserId(hoaDon.getUser().getId());
            response.setTen(hoaDon.getUser().getTen());
            response.setSdt(hoaDon.getUser().getSdt());
        }else {
            response.setSdt(hoaDon.getSdt());
        }
        if (hoaDon.getNv() != null) response.setNvId(hoaDon.getNv().getId());
        if (hoaDon.getNv() != null) response.setNvName(hoaDon.getNv().getTen());
        if (hoaDon.getPhieuGiamGia() != null) response.setIdPhieuGiam(hoaDon.getPhieuGiamGia().getId());
        if (hoaDon.getPhieuGiamGia() != null) response.setMaPGG(hoaDon.getPhieuGiamGia().getMaPhieu());
        return response;
    }



    @Transactional
    public HoaDonResponse updateTrangThai(Integer id, String trangThai, Integer idNV) throws Exception {
        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn với ID: " + id));
        if (!isValidTrangThaiTransition(hoaDon.getTrangThai(), trangThai)) {
            throw new Exception("Chuyển đổi trạng thái từ " + hoaDon.getTrangThai() + " sang " + trangThai + " không hợp lệ");
        }

        // ✅ Bỏ qua trừ số lượng nếu thanh toán = chuyển khoản
        if (!"Chuyển khoản".equalsIgnoreCase(hoaDon.getPhuongThucThanhToan())
                && "Đang xử lý".equalsIgnoreCase(hoaDon.getTrangThai())
                && "Đã xác nhận".equalsIgnoreCase(trangThai)) {
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByIdOrder(id);

            for (HoaDonChiTiet chiTiet : chiTietList) {
                SanPham sp = chiTiet.getSp();
                Integer soLuongTru = chiTiet.getSoLuong();

                if (sp.getSoLuongTon() < soLuongTru) {
                    throw new Exception("Số lượng tồn trong kho không đủ, liên hệ cho user để xác nhận lại");
                }

                Integer soLuongConLai = sp.getSoLuongTon() - soLuongTru;
                sp.setTrangThai(soLuongConLai > 0 ? "Đang kinh doanh" : "Hết hàng");
                sp.setSoLuongTon(soLuongConLai);

                san_pham_repo.save(sp); // Lưu lại số lượng mới
            }
        }

 //       Cập nhật lại số lượng khi bị hủy
        if ((trangThai.equalsIgnoreCase("Đã hủy") || trangThai.equalsIgnoreCase("Thất bại"))
                && !(hoaDon.getTrangThai().equalsIgnoreCase("Đã hủy")
                || hoaDon.getTrangThai().equalsIgnoreCase("Thất bại"))
                && "Chuyển khoản".equalsIgnoreCase(hoaDon.getPhuongThucThanhToan())) {
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByIdOrder(id);

            for (HoaDonChiTiet chiTiet : chiTietList) {
                SanPham sp = chiTiet.getSp();
                Integer soLuongHoanLai = chiTiet.getSoLuong();
                Integer soLuongConLai = sp.getSoLuongTon() + soLuongHoanLai;
                sp.setTrangThai(soLuongConLai > 0 ? "Đang kinh doanh" : "Hết hàng");
                sp.setSoLuongTon(soLuongConLai);
                san_pham_repo.save(sp); // Lưu lại số lượng mới
            }
        }

        hoaDon.setTrangThai(trangThai);

        // Cập nhật nhân viên sửa trạng thái
        User nv = userRepository.findById(idNV)
                .orElseThrow(() -> new Exception("Không tìm thấy nhân viên với ID: " + idNV));
        hoaDon.setNv(nv);

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

    // Xác định loại vận chuyển dựa vào tỉnh xuất phát và tỉnh nhận
    public String getLoaiVanChuyen(String fromProvince, String toProvince) {
        if (fromProvince.equalsIgnoreCase(toProvince)) {
            return "NOI_TINH";
        }
        if ((fromProvince.equalsIgnoreCase("Hà Nội") && toProvince.equalsIgnoreCase("Đà Nẵng"))) {
            return "DAC_BIET";
        }
        if ((fromProvince.equalsIgnoreCase("Hà Nội") && isMienBac(toProvince))) {
            return "NOI_MIEN";
        }
        return "LIEN_MIEN";
    }

    // Hàm xác định nội thành (ví dụ cho Hà Nội, Tp.HCM, bạn có thể mở rộng thêm)
    public boolean isNoiThanh(String province, String district) {
        if (province.equalsIgnoreCase("Hà Nội")) {
            Set<String> noiThanhHN = Set.of("Ba Đình", "Hoàn Kiếm", "Đống Đa", "Hai Bà Trưng", "Cầu Giấy", "Thanh Xuân", "Hoàng Mai", "Long Biên", "Tây Hồ", "Nam Từ Liêm", "Bắc Từ Liêm", "Hà Đông");
            return noiThanhHN.contains(district);
        }
        return false;
    }

    // Hàm tính phí ship dựa vào loại vận chuyển, khu vực, trọng lượng
    public BigDecimal tinhPhiShip(String loaiVanChuyen, String khuVuc, double weightKg) {
        BigDecimal base = BigDecimal.ZERO;
        double extraWeight = 0;
        switch (loaiVanChuyen) {
            case "NOI_TINH":
                if (khuVuc.equals("Nội thành")) {
                    base = new BigDecimal(22000);
                    extraWeight = Math.max(0, weightKg - 3);
                    base = base.add(new BigDecimal(2500 * Math.ceil(extraWeight / 0.5)));
                } else {
                    base = new BigDecimal(30000);
                    extraWeight = Math.max(0, weightKg - 3);
                    base = base.add(new BigDecimal(2500 * Math.ceil(extraWeight / 0.5)));
                }
                break;
            case "NOI_MIEN":
                if (khuVuc.equals("Nội thành")) {
                    base = new BigDecimal(30000);
                } else {
                    base = new BigDecimal(35000);
                }
                extraWeight = Math.max(0, weightKg - 0.5);
                base = base.add(new BigDecimal(2500 * Math.ceil(extraWeight / 0.5)));
                break;
            case "DAC_BIET":
                if (khuVuc.equals("Nội thành")) {
                    base = new BigDecimal(30000);
                } else {
                    base = new BigDecimal(40000);
                }
                extraWeight = Math.max(0, weightKg - 0.5);
                base = base.add(new BigDecimal(5000 * Math.ceil(extraWeight / 0.5)));
                break;
            case "LIEN_MIEN":
                if (khuVuc.equals("Nội thành")) {
                    base = new BigDecimal(32000); // hoặc 32000 tùy loại chuẩn/nhanh
                } else {
                    base = new BigDecimal(37000);
                }
                extraWeight = Math.max(0, weightKg - 0.5);
                base = base.add(new BigDecimal(5000 * Math.ceil(extraWeight / 0.5)));
                break;
        }
        return base;
    }

    // Hàm tính số ngày giao hàng dựa vào loại vận chuyển
    public int tinhSoNgayGiao(String loaiVanChuyen) {
        switch (loaiVanChuyen) {
            case "NOI_TINH": return 1; // 12-24h
            case "NOI_MIEN": return 2; // 24h
            case "DAC_BIET": return 4; // 3-4 ngày
            case "LIEN_MIEN": return 4; // 3-5 ngày hoặc 2 ngày (48h)
            default: return 3;
        }
    }

}
