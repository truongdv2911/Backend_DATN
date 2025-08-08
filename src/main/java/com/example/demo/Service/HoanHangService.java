package com.example.demo.Service;

import com.example.demo.DTOs.ChiTietHoanHangDTO;
import com.example.demo.DTOs.PhieuHoanHangDTO;
import com.example.demo.Entity.*;
import com.example.demo.Enum.TrangThaiPhieuHoan;
import com.example.demo.Enum.TrangThaiThanhToan;
import com.example.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoanHangService {
    private final PhieuHoanRepository phieuHoanHangRepository;
    private final ChiTietHoanRepo chiTietHoanHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final San_pham_Repo sanPhamRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

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

        phieu.setTrangThai(TrangThaiPhieuHoan.DA_DUYET);
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
}
