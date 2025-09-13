package com.example.demo.Service;

import com.example.demo.DTOs.ChiTietHoanHangDTO;
import com.example.demo.DTOs.KetQuaKiemTraRequest;
import com.example.demo.DTOs.PhieuHoanHangDTO;
import com.example.demo.Entity.*;
import com.example.demo.Enum.TrangThaiPhieuHoan;
import com.example.demo.Enum.TrangThaiThanhToan;
import com.example.demo.Repository.*;
import com.example.demo.Responses.AnhResponse;
import com.example.demo.Responses.ChiTietHoanResponse;
import com.example.demo.Responses.PhieuHoanHangResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HoanHangService {
    private final PhieuHoanRepository phieuHoanHangRepository;
    private final ChiTietHoanRepo chiTietHoanHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final San_pham_Repo sanPhamRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;
    private final VidPhieuHoanRepository phieuHoanRepository;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB

    private static final List<String> IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> VIDEO_TYPES = List.of("video/mp4", "video/quicktime", "video/x-msvideo","video/x-matroska");

    @Transactional
    public PhieuHoanHang taoPhieuHoanHang(PhieuHoanHangDTO dto) {

        HoaDon hoaDon = hoaDonRepository.findById(dto.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Kiểm tra trạng thái hóa đơn - chỉ cho phép hoàn hàng với hóa đơn đã hoàn thành
        if (!hoaDon.getTrangThai().equals("Hoàn tất")) {
            throw new RuntimeException("Chỉ có thể hoàn hàng với đơn hàng đã hoàn thành");
        }

        // Kiểm tra xem đã có phiếu hoàn hàng nào đang chờ duyệt hay không
        Optional<PhieuHoanHang> phieuCu = phieuHoanHangRepository.findByHoaDonAndTrangThai(
                hoaDon, TrangThaiPhieuHoan.CHO_DUYET);
        if (phieuCu.isPresent()) {
            throw new RuntimeException("Đã tồn tại phiếu hoàn hàng đang chờ duyệt cho hóa đơn này");
        }

        // Kiểm tra thời gian 7 ngày
        if (hoaDon.getNgayGiao().isBefore(LocalDateTime.now().minusDays(7))) {
            throw new RuntimeException("Đã quá hạn 7 ngày để hoàn hàng");
        }

        // Tạo phiếu hoàn hàng
        PhieuHoanHang phieu = new PhieuHoanHang();
        phieu.setNgayHoan(LocalDateTime.now());
        phieu.setLoaiHoan(dto.getLoaiHoan());
        phieu.setLyDo(dto.getLyDo());
        phieu.setPhuongThucHoan(dto.getPhuongThucHoan());
        phieu.setTenNganHang(dto.getTenNganHang());
        phieu.setSoTaiKhoan(dto.getSoTaiKhoan());
        phieu.setChuTaiKhoan(dto.getChuTaiKhoan());
        phieu.setTrangThai(TrangThaiPhieuHoan.CHO_DUYET);
        phieu.setTrangThaiThanhToan(TrangThaiThanhToan.CHUA_HOAN);
        phieu.setHoaDon(hoaDon);
        phieuHoanHangRepository.save(phieu);

        // Xử lý chi tiết hoàn
        BigDecimal tongTien = BigDecimal.ZERO;
        for (ChiTietHoanHangDTO ctdto : dto.getChiTietHoanHangs()) {
            SanPham sp = sanPhamRepository.findById(ctdto.getIdSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Kiểm tra số lượng
                Integer soLuongTrongHoaDon = hoaDonChiTietRepository
                        .getTongSoLuongSanPhamTrongHoaDon(dto.getIdHoaDon(), ctdto.getIdSanPham());

                if (soLuongTrongHoaDon == null || soLuongTrongHoaDon < ctdto.getSoLuongHoan()) {
                    throw new RuntimeException("Số lượng hoàn cho sản phẩm ID " + ctdto.getIdSanPham() + " vượt quá số lượng trong hóa đơn");
                }

            BigDecimal giaHoan = hoaDonChiTietRepository
                    .getGiaSanPhamTrongHoaDon(dto.getIdHoaDon(), ctdto.getIdSanPham());
            BigDecimal tongGiaHoan = giaHoan.multiply(BigDecimal.valueOf(ctdto.getSoLuongHoan()));

            ChiTietHoanHang ct = new ChiTietHoanHang();
            ct.setPhieuHoanHang(phieu);
            ct.setSanPham(sp);
            ct.setSoLuongHoan(ctdto.getSoLuongHoan());
            ct.setGiaHoan(giaHoan);
            ct.setTongGiaHoan(tongGiaHoan);

            tongTien = tongTien.add(tongGiaHoan);

            chiTietHoanHangRepository.save(ct);
        }

        phieu.setTongTienHoan(tongTien);
        return phieuHoanHangRepository.save(phieu);
    }

    @Transactional
    public void duyetPhieuHoan(Integer idPhieu) {
        PhieuHoanHang phieu = phieuHoanHangRepository.findById(idPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        if (!phieu.getTrangThai().equals(TrangThaiPhieuHoan.CHO_DUYET)) {
            throw new RuntimeException("Chỉ có thể duyệt phiếu đang ở trạng thái chờ duyệt");
        }
        // Cập nhật trạng thái hóa đơn nếu hoàn toàn bộ
        if (isHoanToanBo(phieu)) {
            HoaDon hoaDon = phieu.getHoaDon();
            hoaDon.setTrangThai("Hoàn toàn bộ");
            hoaDon.setTongTien(hoaDon.getTongTien().subtract(phieu.getTongTienHoan()));
            hoaDonRepository.save(hoaDon);
        } else {
            // Hoàn một phần - cập nhật lại tổng tiền hóa đơn
            HoaDon hoaDon = phieu.getHoaDon();
            hoaDon.setTongTien(hoaDon.getTongTien().subtract(phieu.getTongTienHoan()));
            hoaDonRepository.save(hoaDon);
        }

        phieu.setTrangThai(TrangThaiPhieuHoan.DA_DUYET);
        phieu.setNgayDuyet(LocalDateTime.now());
        phieuHoanHangRepository.save(phieu);

        User khach = userRepository.findById(phieu.getHoaDon().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        String subject = "Phiếu hoàn hàng #" + phieu.getHoaDon().getMaHD() + " đã được duyệt";
        String html = """
                    <div style="font-family:sans-serif; font-size:14px; color:#333;">
                    <div style="background:linear-gradient(90deg, #00c853, #64dd17);
                        color:#fff; padding:15px 20px; border-radius:5px 5px 0 0;
                        text-align:center; font-size:20px; font-weight:bold;">
                        ✅ PHIẾU HOÀN HÀNG ĐÃ ĐƯỢC DUYỆT
                   </div>
                   <div style="padding:15px; background:#fafafa; border:1px solid #ddd;">
                   <p>Xin chào <strong>%s</strong>,</p>
                  <p>Chúng tôi đã duyệt yêu cầu hoàn hàng cho đơn hàng <strong>%s</strong>.</p>
                  <p>Số tiền hoàn: <span style="color:green;">%sđ</span></p>
                  <p>Vui lòng chờ bộ phận kế toán chuyển tiền trong vòng 1-2 ngày làm việc.</p>
                  <p>Trân trọng,<br/>Cửa hàng Lego My Kingdom</p>
                  </div>
                  </div>
                """.formatted(khach.getTen(), phieu.getHoaDon().getMaHD(), phieu.getTongTienHoan());
        emailService.sendDuyetPhieuHoanEmail(khach.getEmail(), subject, html);
    }

    @Transactional
    public void tuChoiPhieuHoan(Integer idPhieu, String lyDo) {
        PhieuHoanHang phieu = phieuHoanHangRepository.findById(idPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        if (!phieu.getTrangThai().equals(TrangThaiPhieuHoan.CHO_DUYET)) {
            throw new RuntimeException("Chỉ có thể từ chối phiếu đang ở trạng thái chờ duyệt");
        }
        phieu.setTrangThai(TrangThaiPhieuHoan.TU_CHOI);
        phieu.setNgayDuyet(LocalDateTime.now());
        phieu.setLyDo(lyDo);
        phieuHoanHangRepository.save(phieu);
        User khach = userRepository.findById(phieu.getHoaDon().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        String subject = "Phiếu hoàn hàng #" + phieu.getHoaDon().getMaHD() + " đã bị từ chối";
        String html = """
                    <div style="font-family:sans-serif; font-size:14px; color:#333;">
                    <div style="background:linear-gradient(90deg, #ff4b2b, #ff416c);
                        color:#fff; padding:15px 20px; border-radius:5px 5px 0 0;
                        text-align:center; font-size:20px; font-weight:bold;">
                        ❌ PHIẾU HOÀN HÀNG BỊ TỪ CHỐI
                    </div>
                    <div style="padding:15px; background:#fafafa; border:1px solid #ddd;">
                       <p>Xin chào <strong>%s</strong>,</p>
                       <p>Rất tiếc, yêu cầu hoàn hàng cho đơn hàng <strong>%s</strong> đã bị từ chối.</p>
                       <p>Lý do: <em>%s</em></p>
                       <p>Trân trọng,<br/>Cửa hàng Lego My Kingdom</p>
                    </div>
                </div>
                """.formatted(khach.getTen(), phieu.getHoaDon().getMaHD(), lyDo);
        emailService.sendDuyetPhieuHoanEmail(khach.getEmail(), subject, html);
    }

    @Transactional
    public void capNhatThanhToan(Integer idPhieu, TrangThaiThanhToan trangThai) {
        PhieuHoanHang phieu = phieuHoanHangRepository.findById(idPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        if (!phieu.getTrangThai().equals(TrangThaiPhieuHoan.DA_DUYET)) {
            throw new RuntimeException("Chỉ có thể cập nhật thanh toán cho phiếu đã được duyệt");
        }
        phieu.setTrangThaiThanhToan(trangThai);
        if (trangThai == TrangThaiThanhToan.DA_HOAN) {
            phieu.setNgayHoanTien(LocalDateTime.now());
        }
        phieuHoanHangRepository.save(phieu);
        String subject = "Xác nhận hoàn tiền thành công";
        String body = buildEmailHoanTienThanhCong(phieu); // Hàm tự tạo HTML email
        emailService.sendDuyetPhieuHoanEmail(phieu.getHoaDon().getUser().getEmail(), subject, body);
    }

    private String buildEmailHoanTienThanhCong(PhieuHoanHang phieu) {
        return String.format("""
                        <div style="font-family:sans-serif; font-size:14px; color:#333;">
                            <div style="background: linear-gradient(90deg, #28a745, #218838);
                                        color: #fff;
                                        padding: 15px 20px;
                                        border-radius: 5px 5px 0 0;
                                        text-align: center;
                                        font-size: 20px;
                                        font-weight: bold;">
                                ✅ HOÀN TIỀN THÀNH CÔNG
                            </div>
                            <div style="padding: 15px; background-color: #fafafa; border: 1px solid #ddd; border-top: none;">
                                <p>Xin chào <strong>%s</strong>,</p>
                                <p>Chúng tôi xin thông báo rằng yêu cầu hoàn tiền của bạn cho đơn hàng <strong>%s</strong> đã được xử lý thành công.</p>
                                <p>Số tiền: <strong style="color:green;">%sđ</strong></p>
                                <p>Ngày hoàn tiền: %s</p>
                                <p>Trân trọng,<br/>Cửa hàng Lego My Kingdom</p>
                            </div>
                        </div>
                        """,
                phieu.getHoaDon().getUser().getTen(),
                phieu.getHoaDon().getMaHD(),
                phieu.getTongTienHoan(),
                phieu.getNgayHoanTien().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }
    // Lấy danh sách phiếu hoàn theo trạng thái
    public List<PhieuHoanHang> getPhieuHoanByTrangThai(TrangThaiPhieuHoan trangThai) {
        return phieuHoanHangRepository.findByTrangThai(trangThai);
    }

    // Lấy phiếu hoàn theo hóa đơn
    public List<PhieuHoanHang> getPhieuHoanByHoaDon(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        return phieuHoanHangRepository.findByHoaDon(hoaDon);
    }

    // Kiểm tra điều kiện có thể hoàn hàng
    public boolean coTheHoanHang(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Kiểm tra trạng thái
        if (!hoaDon.getTrangThai().equals("HOAN_THANH")) {
            return false;
        }

        // Kiểm tra thời gian
        if (hoaDon.getNgayGiao() == null ||
                hoaDon.getNgayGiao().isBefore(LocalDateTime.now().minusDays(7))) {
            return false;
        }

        // Kiểm tra có phiếu hoàn đang chờ duyệt
        Optional<PhieuHoanHang> phieuCu = phieuHoanHangRepository
                .findByHoaDonAndTrangThai(hoaDon, TrangThaiPhieuHoan.CHO_DUYET);

        return phieuCu.isEmpty();
    }

    // Kiểm tra xem có phải hoàn toàn bộ hay không
    private boolean isHoanToanBo(PhieuHoanHang phieu) {
        List<HoaDonChiTiet> hoaDonChiTiets = hoaDonChiTietRepository.findByHd(phieu.getHoaDon());
        List<ChiTietHoanHang> chiTietHoans = chiTietHoanHangRepository.findByPhieuHoanHang(phieu);

        // So sánh tổng số lượng trong hóa đơn với tổng số lượng hoàn
        int tongSoLuongHoaDon = hoaDonChiTiets.stream()
                .mapToInt(HoaDonChiTiet::getSoLuong)
                .sum();

        int tongSoLuongHoan = chiTietHoans.stream()
                .mapToInt(ChiTietHoanHang::getSoLuongHoan)
                .sum();
        return tongSoLuongHoaDon == tongSoLuongHoan;
    }

    public PhieuHoanHangResponse convertPHH(PhieuHoanHang phieus) {
        PhieuHoanHangResponse phieuHoanHangResponses = new PhieuHoanHangResponse();
        List<ChiTietHoanResponse> chiTietHoanResponses = phieus.getChiTietHoanHangs().stream()
                .map(chiTiet -> {
                    ChiTietHoanResponse chiTietHoanResponse = new ChiTietHoanResponse();
                    chiTietHoanResponse.setSoLuongHoan(chiTiet.getSoLuongHoan());
                    chiTietHoanResponse.setTongGiaHoan(chiTiet.getTongGiaHoan());
                    chiTietHoanResponse.setIdSanPham(chiTiet.getSanPham().getId());
                    return chiTietHoanResponse;
                }).toList();
        List<AnhResponse> anhResponses = phieus.getVidPhieuHoans().stream()
                .map(anh -> {
                    AnhResponse anhResponse = new AnhResponse();
                    anhResponse.setId(anh.getId());
                    anhResponse.setUrl(anh.getUrl());
                    return anhResponse;
        }).toList();

        phieuHoanHangResponses.setId(phieus.getId());
        phieuHoanHangResponses.setNgayHoan(phieus.getNgayHoan());
        phieuHoanHangResponses.setTongTienHoan(phieus.getTongTienHoan());
        phieuHoanHangResponses.setLoaiHoan(phieus.getLoaiHoan());
        phieuHoanHangResponses.setLyDo(phieus.getLyDo());
        phieuHoanHangResponses.setNgayDuyet(phieus.getNgayDuyet());
        phieuHoanHangResponses.setTrangThai(phieus.getTrangThai());
        phieuHoanHangResponses.setTrangThaiThanhToan(phieus.getTrangThaiThanhToan());
        phieuHoanHangResponses.setPhuongThucHoan(phieus.getPhuongThucHoan());
        phieuHoanHangResponses.setNgayHoanTien(phieus.getNgayHoanTien());
        phieuHoanHangResponses.setTenNganHang(phieus.getTenNganHang());
        phieuHoanHangResponses.setSoTaiKhoan(phieus.getSoTaiKhoan());
        phieuHoanHangResponses.setChuTaiKhoan(phieus.getChuTaiKhoan());
        phieuHoanHangResponses.setIdHD(phieus.getHoaDon().getId());
        phieuHoanHangResponses.setChiTietHoanHangs(chiTietHoanResponses);
        phieuHoanHangResponses.setAnhs(anhResponses);
        return phieuHoanHangResponses;
    }

    public void uploadAnh(Integer idPhieuHoan, List<MultipartFile> images) throws Exception {
        try {
            PhieuHoanHang dg = phieuHoanHangRepository.findById(idPhieuHoan)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu hoàn"));

            int existing = phieuHoanRepository.countByHoanHangId(idPhieuHoan);
            if (existing + images.size() > 3) {
                throw new RuntimeException("Tối đa 3 ảnh và video cho mỗi lần hoàn");
            }
            for (MultipartFile file : images) {
                if (!IMAGE_TYPES.contains(file.getContentType())) {
                    throw new RuntimeException("Chỉ cho phép định dạng ảnh JPG, PNG, WEBP");
                }
                if (file.getSize() > MAX_IMAGE_SIZE) {
                    throw new RuntimeException("Ảnh vượt quá dung lượng 5MB");
                }

                String fileName = saveFile(file).get("url").toString();
                String mota = saveFile(file).get("public_id").toString();
                VidPhieuHoan af = new VidPhieuHoan();
                af.setUrl(fileName);
                af.setMota(mota);
                af.setHoanHang(dg);
                phieuHoanRepository.save(af);
            }
        }catch (Exception e){
            throw new Exception("loi khi upload anh phiếu hoàn");
        }
    }

    public void uploadVideo(Integer danhGiaId, MultipartFile file) throws Exception {
        try {
            PhieuHoanHang dg = phieuHoanHangRepository.findById(danhGiaId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu hoàn"));

            if (!VIDEO_TYPES.contains(file.getContentType())) {
                throw new RuntimeException("Chỉ cho phép video MP4, MOV, AVI");
            }
            if (file.getSize() > MAX_VIDEO_SIZE) {
                throw new RuntimeException("Video vượt quá dung lượng 50MB");
            }

            String fileName = saveFile(file).get("url").toString();
            String mota = saveFile(file).get("public_id").toString();
            VidPhieuHoan vf = new VidPhieuHoan();
            vf.setUrl(fileName);
            vf.setMota(mota);
            vf.setHoanHang(dg);
            phieuHoanRepository.save(vf);
        }catch (Exception e){
            throw new Exception("loi khi upload video danh gia");
        }
    }

    private Map saveFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new IOException("Tên file không hợp lệ: " + originalFileName);
        }
        // Tạo tên file duy nhất
        Map result = cloudinaryService.upload(file);
        return result;
    }

    @Transactional
    public void kiemTraHang(Integer idPhieu, List<KetQuaKiemTraRequest> ketQuaList) {
        // 1. Load phiếu hoàn
        PhieuHoanHang phieuHoan = phieuHoanHangRepository.findById(idPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu hoàn: " + idPhieu));

        if (!TrangThaiPhieuHoan.DA_DUYET.equals(phieuHoan.getTrangThai())){
            throw new RuntimeException("Trạng thái phiếu chưa phù hợp");
        }

        Map<Integer, Integer> mapChiTiet = new HashMap<>();
        for (ChiTietHoanHang ct : phieuHoan.getChiTietHoanHangs()) {
            mapChiTiet.put(ct.getSanPham().getId(), ct.getSoLuongHoan());
        }

        // Map để cộng dồn số lượng kiểm tra
        Map<Integer, Integer> mapKiemTra = new HashMap<>();

        // 2. Validate từng kết quả
        for (KetQuaKiemTraRequest ketQua : ketQuaList) {
            Integer idSp = ketQua.getIdSanPham();
            Integer slHoan = ketQua.getSoLuongHoan();

            if (!mapChiTiet.containsKey(idSp)) {
                throw new RuntimeException("Sản phẩm " + idSp + " không có trong phiếu hoàn");
            }

            if (slHoan == null || slHoan <= 0) {
                throw new RuntimeException("Số lượng hoàn phải > 0 cho sản phẩm " + idSp);
            }

            // Cộng dồn để so khớp tổng sau cùng
            mapKiemTra.put(idSp, mapKiemTra.getOrDefault(idSp, 0) + slHoan);

            // Xử lý nhập kho hoặc hàng lỗi
            if (ketQua.isSuDungDuoc()) {
                SanPham sp = sanPhamRepository.findById(idSp)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + idSp));

                sp.setSoLuongTon(sp.getSoLuongTon() + slHoan);
                sanPhamRepository.save(sp);
            }
        }

        // 3. Validate tổng số lượng sau khi kiểm tra
        for (Map.Entry<Integer, Integer> entry : mapChiTiet.entrySet()) {
            Integer idSp = entry.getKey();
            Integer soLuongTrongPhieu = entry.getValue();
            Integer soLuongKiemTra = mapKiemTra.getOrDefault(idSp, 0);

            if (!soLuongTrongPhieu.equals(soLuongKiemTra)) {
                throw new RuntimeException(
                        "Tổng số lượng kiểm tra cho sản phẩm " + idSp +
                                " (" + soLuongKiemTra + ") không khớp với số lượng phiếu (" + soLuongTrongPhieu + ")"
                );
            }
        }

        // 4. Cập nhật trạng thái phiếu hoàn
        phieuHoan.setTrangThai(TrangThaiPhieuHoan.DA_KIEM_TRA_HANG);
        phieuHoanHangRepository.save(phieuHoan);
    }
}
