package com.example.demo.Service;

import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.DTOs.KhuyenMaiSanPhamDTO;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.KhuyenMaiSanPham;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.KhuyenMaiSanPhamRepository;
import com.example.demo.Repository.Khuyen_mai_Repo;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.ChiTietKMResponse;
import com.example.demo.Responses.ChiTietPhieuResponse;
import com.example.demo.Responses.SanPhamKMResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class Khuyen_mai_Service {
    private final Khuyen_mai_Repo khuyenMaiRepo;
    private final KhuyenMaiSanPhamRepository kmspRepo;
    private final San_pham_Repo sanPhamRepo;


    public ChiTietKMResponse getDetailKM(Integer idKM) {
        Object row = khuyenMaiRepo.getDetailKM(idKM);
        List<Object> sanPhamKMResponses = khuyenMaiRepo.getSpInKM(idKM);
        if (row == null) {
            throw new RuntimeException("Không tìm thấy phiếu hoặc chưa có dữ liệu thống kê.");
        }
        Object[] data = (Object[]) row;

        ChiTietKMResponse response = new ChiTietKMResponse();
        response.setId((Integer) data[0]);
        response.setTenKhuyenMai((String) data[1]);
        response.setPhanTramKhuyenMai(((Number) data[2]).intValue());
        response.setNgayBatDau(((Timestamp) data[3]).toLocalDateTime());
        response.setNgayKetThuc(((Timestamp) data[4]).toLocalDateTime());
        response.setSoSanPhamApDung(((Number) data[5]).intValue());
        response.setTongSoLuongBan(((Number) data[6]).intValue());
        response.setTongTienTruocGiam((BigDecimal) data[7]);
        response.setTongSoTienGiam((BigDecimal) data[8]);
        response.setTongTienSauGiam((BigDecimal) data[9]);
        response.setSoHoaDon((Integer) data[10]);
        response.setSanPhamKMResponses(sanPhamKMResponses);
        return response;
    }

    public KhuyenMai createKhuyenMai(@Valid KhuyenMaiDTO khuyenMaiDTO) throws Exception {
        try {
            if (khuyenMaiRepo.existsByTenKhuyenMai(khuyenMaiDTO.getTenKhuyenMai())) {
                throw new RuntimeException("Tên khuyến mại đã tồn tại!");
            }
            String maKhuyenMai = "";
            int maxTry = 10;
            int count = 0;
            do {
                maKhuyenMai = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã phiếu mới, vui lòng thử lại!");
                }
            } while (khuyenMaiRepo.existsByMaKhuyenMai(maKhuyenMai));

            KhuyenMai khuyenMai = new KhuyenMai();
            khuyenMai.setMaKhuyenMai(maKhuyenMai);
            khuyenMai.setTenKhuyenMai(khuyenMaiDTO.getTenKhuyenMai());
            khuyenMai.setPhanTramKhuyenMai(khuyenMaiDTO.getPhanTramKhuyenMai());
            khuyenMai.setNgayBatDau(khuyenMaiDTO.getNgayBatDau());
            khuyenMai.setNgayKetThuc(khuyenMaiDTO.getNgayKetThuc());
            khuyenMai.setTrangThai(tinhTrangThai(khuyenMaiDTO));
            return khuyenMaiRepo.save(khuyenMai);
        }catch (Exception e){
            throw new Exception( e.getMessage());
        }
    }

    public List<KhuyenMai> getAllKhuyenMai() {
        return khuyenMaiRepo.findAllKhuyenMaiKhongBiXoa();
    }

    public KhuyenMai getKhuyenMaiById(Integer id) {
        return khuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + id));
    }

    public KhuyenMai updateKhuyenMai(Integer id, @Valid KhuyenMaiDTO khuyenMaiDTO) {
//        String maKhuyenMai = khuyenMaiDTO.getMaKhuyenMai();
//        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
//            int maxTry = 10;
//            int count = 0;
//            do {
//                maKhuyenMai = generateMaPhieu();
//                count++;
//                if (count > maxTry) {
//                    throw new RuntimeException("Không thể sinh mã phiếu mới, vui lòng thử lại!");
//                }
//            } while (khuyenMaiRepo.existsByMaKhuyenMai(maKhuyenMai));
//        } else {
//            if (khuyenMaiRepo.existsByMaKhuyenMai(khuyenMai.getMaKhuyenMai())) {
//                throw new RuntimeException("Mã phiếu đã tồn tại!");
//            }
//        }

        KhuyenMai khuyenMai = getKhuyenMaiById(id);
        if (!khuyenMai.getTenKhuyenMai().equals(khuyenMaiDTO.getTenKhuyenMai())
                && khuyenMaiRepo.existsByTenKhuyenMai(khuyenMaiDTO.getTenKhuyenMai())) {
            throw new RuntimeException("Tên khuyến mại đã tồn tại!");
        }
        khuyenMai.setTenKhuyenMai(khuyenMaiDTO.getTenKhuyenMai());
        khuyenMai.setPhanTramKhuyenMai(khuyenMaiDTO.getPhanTramKhuyenMai());
        khuyenMai.setNgayBatDau(khuyenMaiDTO.getNgayBatDau());
        khuyenMai.setNgayKetThuc(khuyenMaiDTO.getNgayKetThuc());
        khuyenMai.setTrangThai(khuyenMaiDTO.getTrangThai());
        return khuyenMaiRepo.save(khuyenMai);
    }

    public void deleteKhuyenMai(Integer id) {
        KhuyenMai khuyenMai = khuyenMaiRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy id km"));
        khuyenMai.setTrangThai("isDelete");
        khuyenMaiRepo.save(khuyenMai);
        khuyenMaiRepo.deleteByKhuyenMaiId(id);
    }

    public String generateMaPhieu() {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder("KM");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String tinhTrangThai(KhuyenMaiDTO dto) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime ngayBatDau = dto.getNgayBatDau();
        LocalDateTime ngayKetThuc = dto.getNgayKetThuc();

        if (ngayBatDau != null && ngayKetThuc != null) {
            if ((today.isEqual(ngayBatDau) || today.isAfter(ngayBatDau)) &&
                    (today.isEqual(ngayKetThuc) || today.isBefore(ngayKetThuc))) {
                return "active";
            }

            if (today.isBefore(ngayBatDau)) {
                return "inactive";
            }

            if (today.isAfter(ngayKetThuc)) {
                return "expired";
            }
        }
        return "Chưa xác định";
    }

    public void applyKhuyenMai(KhuyenMaiSanPhamDTO request) {
        KhuyenMai km = khuyenMaiRepo.findById(request.getKhuyenMaiId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

        // Chỉ cho phép apply nếu trạng thái là 'active' hoặc 'inactive'
        String status = km.getTrangThai();
        if (!"active".equalsIgnoreCase(status) && !"inactive".equalsIgnoreCase(status)) {
            throw new RuntimeException("Chỉ được áp dụng khuyến mại có trạng thái active hoặc inactive!");
        }

        for (Integer idSp : request.getListSanPhamId()) {
            if (kmspRepo.existsBySanPham_IdAndKhuyenMai_Id(idSp, km.getId())) continue;

            SanPham sp = sanPhamRepo.findById(idSp)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + idSp));

            List<KhuyenMaiSanPham> dsKhuyenMaiHienTai = kmspRepo.findBySanPham_Id(sp.getId());

            for (KhuyenMaiSanPham kmsp : dsKhuyenMaiHienTai) {
                KhuyenMai kmCu = kmsp.getKhuyenMai();
                if (isOverlapping(km.getNgayBatDau(), km.getNgayKetThuc(),
                        kmCu.getNgayBatDau(), kmCu.getNgayKetThuc())) {
                    throw new RuntimeException("Sản phẩm \"" + sp.getTenSanPham() + "\" đã có khuyến mãi giao thời gian");
                }
            }

            // ✅ Tính giá khuyến mãi theo %
            BigDecimal giaKhuyenMai;
            LocalDateTime now = LocalDateTime.now();

            if (!now.isBefore(km.getNgayBatDau()) && !now.isAfter(km.getNgayKetThuc())) {
                // ĐANG trong thời gian khuyến mãi
                giaKhuyenMai = sp.getGia()
                        .multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(km.getPhanTramKhuyenMai()).divide(BigDecimal.valueOf(100))))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                // CHƯA ĐẾN hoặc ĐÃ QUA khuyến mãi → giữ giá gốc
                giaKhuyenMai = sp.getGia();
            }

            KhuyenMaiSanPham kmsp = KhuyenMaiSanPham.builder()
                    .sanPham(sp)
                    .khuyenMai(km)
                    .giaKhuyenMai(giaKhuyenMai)
                    .build();
            kmspRepo.save(kmsp);
        }
    }

    boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                          LocalDateTime start2, LocalDateTime end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
}
