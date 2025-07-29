package com.example.demo.Service;

import com.example.demo.DTOs.KhuyenMaiDTO;
import com.example.demo.DTOs.PGGUpdateDTO;
import com.example.demo.DTOs.PGGUserDTO;
import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Entity.User;
import com.example.demo.Entity.ViPhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.ViPhieuGiamGiaRepository;
import com.example.demo.Responses.ChiTietPhieuResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class Phieu_giam_gia_Service {

    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;
    private final UserRepository userRepository;
    private final ViPhieuGiamGiaRepository viPhieuGiamGiaRepository;

    public PhieuGiamGia createPhieuGiamGia(PhieuGiamGiaDTO phieuGiamGiaDTO) {
        if (phieuGiamGiaRepo.existsByTenPhieu(phieuGiamGiaDTO.getTenPhieu())) {
            throw new RuntimeException("Tên phiếu giảm đã tồn tại!");
        }
        String maPhieu = phieuGiamGiaDTO.getMaPhieu();
        if (maPhieu == null || maPhieu.isBlank()) {
            int maxTry = 10;
            int count = 0;
            do {
                maPhieu = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã phiếu mới, vui lòng thử lại!");
                }
            } while (phieuGiamGiaRepo.existsByMaPhieu(maPhieu));
        } else {
            if (phieuGiamGiaRepo.existsByMaPhieu(maPhieu)) {
                throw new RuntimeException("Mã phiếu đã tồn tại!");
            }
        }

        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();

        // Validate loại phiếu giảm giá
        String loai = phieuGiamGiaDTO.getLoaiPhieuGiam().trim();
        if ("theo_phan_tram".equalsIgnoreCase(loai)) {
            if (phieuGiamGiaDTO.getGiaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Giá trị giảm theo % không được lớn hơn 100%");
            }
            if (phieuGiamGiaDTO.getGiamToiDa() == null || phieuGiamGiaDTO.getGiamToiDa().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Giảm tối đa phải lớn hơn 0 với phiếu giảm theo %");
            }
            phieuGiamGia.setGiamToiDa(phieuGiamGiaDTO.getGiamToiDa());
        } else if ("theo_so_tien".equalsIgnoreCase(loai)) {
            phieuGiamGia.setGiamToiDa(phieuGiamGiaDTO.getGiaTriGiam());
        }

        phieuGiamGia.setMaPhieu(maPhieu);
        phieuGiamGia.setTenPhieu(phieuGiamGiaDTO.getTenPhieu());
        phieuGiamGia.setSoLuong(phieuGiamGiaDTO.getSoLuong());
        phieuGiamGia.setLoaiPhieuGiam(phieuGiamGiaDTO.getLoaiPhieuGiam().trim());
        phieuGiamGia.setGiaTriGiam(phieuGiamGiaDTO.getGiaTriGiam());
        phieuGiamGia.setGiaTriToiThieu(phieuGiamGiaDTO.getGiaTriToiThieu());
        phieuGiamGia.setNoiBat(phieuGiamGiaDTO.getNoiBat());
        if (phieuGiamGiaDTO.getNgayKetThuc().isBefore(phieuGiamGiaDTO.getNgayBatDau())){
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        phieuGiamGia.setNgayBatDau(phieuGiamGiaDTO.getNgayBatDau());
        phieuGiamGia.setNgayKetThuc(phieuGiamGiaDTO.getNgayKetThuc());
        phieuGiamGia.setTrangThai(tinhTrangThai2(phieuGiamGiaDTO));
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }

    private String tinhTrangThai(PGGUpdateDTO dto) {
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
    private String tinhTrangThai2(PhieuGiamGiaDTO dto) {
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


    public List<PhieuGiamGia> getAllPhieuGiamGia() {
        return phieuGiamGiaRepo.findAllPhieuKhongBiXoa();
    }

    public List<PhieuGiamGia> getphieuNoibat(Integer isNoiBat) {
        return phieuGiamGiaRepo.findByPhieuNoiBat(isNoiBat);
    }


    public PhieuGiamGia getPhieuGiamGiaById(Integer id) {
        return phieuGiamGiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id: " + id));
    }



    public PhieuGiamGia updatePhieuGiamGia(Integer id, PGGUpdateDTO phieuGiamGiaDTO) {
        // Validate loại phiếu giảm giá
        String loai = phieuGiamGiaDTO.getLoaiPhieuGiam().trim();
        if ("theo_phan_tram".equalsIgnoreCase(loai)) {
            if (phieuGiamGiaDTO.getGiaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Giá trị giảm theo % không được lớn hơn 100%");
            }
            if (phieuGiamGiaDTO.getGiamToiDa() == null || phieuGiamGiaDTO.getGiamToiDa().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Giảm tối đa phải lớn hơn 0 với phiếu giảm theo %");
            }
        } else if ("theo_so_tien".equalsIgnoreCase(loai)) {
            if (phieuGiamGiaDTO.getGiamToiDa() != null && phieuGiamGiaDTO.getGiaTriGiam().compareTo(phieuGiamGiaDTO.getGiamToiDa()) > 0) {
                throw new IllegalArgumentException("Giá trị giảm không được lớn hơn giảm tối đa");
            }
        }
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        if (!phieuGiamGia.getTenPhieu().equals(phieuGiamGiaDTO.getTenPhieu())
                && phieuGiamGiaRepo.existsByTenPhieu(phieuGiamGiaDTO.getTenPhieu())) {
            throw new RuntimeException("Tên phiếu giảm đã tồn tại!");
        }
        phieuGiamGia.setTenPhieu(phieuGiamGiaDTO.getTenPhieu());
        phieuGiamGia.setSoLuong(phieuGiamGiaDTO.getSoLuong());
        phieuGiamGia.setLoaiPhieuGiam(phieuGiamGiaDTO.getLoaiPhieuGiam().trim());
        phieuGiamGia.setGiaTriGiam(phieuGiamGiaDTO.getGiaTriGiam());
        phieuGiamGia.setGiamToiDa(phieuGiamGiaDTO.getGiamToiDa());
        phieuGiamGia.setGiaTriToiThieu(phieuGiamGiaDTO.getGiaTriToiThieu());
        if (phieuGiamGiaDTO.getNgayKetThuc().isBefore(phieuGiamGiaDTO.getNgayBatDau())){
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        phieuGiamGia.setNoiBat(phieuGiamGiaDTO.getNoiBat());
        phieuGiamGia.setNgayBatDau(phieuGiamGiaDTO.getNgayBatDau());
        phieuGiamGia.setNgayKetThuc(phieuGiamGiaDTO.getNgayKetThuc());
        phieuGiamGia.setTrangThai(tinhTrangThai(phieuGiamGiaDTO));
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }

//    public PhieuGiamGia ThayDoiTrangThaiPhieuGiamGia(Integer id) throws Exception {
//        Optional<PhieuGiamGia> phieuGiamGia = phieuGiamGiaRepo.findById(id);
//        if (phieuGiamGia.isEmpty()){
//            throw new Exception("Khong tim thay id phieu giam gia");
//        }
//
//        if (phieuGiamGia.get().getTrangThai().equals("Ngừng")){
//            phieuGiamGia.get().setTrangThai("Đang hoạt động");
//            return phieuGiamGiaRepo.save(phieuGiamGia.get());
//        }else{
//            phieuGiamGia.get().setTrangThai("Ngừng");
//            return phieuGiamGiaRepo.save(phieuGiamGia.get());
//        }
//    }

    public void deletePhieuGiamGia(Integer id) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGia.setTrangThai("isDelete");
        phieuGiamGiaRepo.save(phieuGiamGia);
    }


    public String generateMaPhieu() {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder("PGG");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    public List<PhieuGiamGia> getByLoaiPhieuGiam(String loaiPhieuGiam) {
        return phieuGiamGiaRepo.findByLoaiPhieuGiam(loaiPhieuGiam);
    }

    public ChiTietPhieuResponse getDetail(Integer id) {
        Object row = phieuGiamGiaRepo.getChiTietPhieu(id);
        List<Object> objects = phieuGiamGiaRepo.getDanhSachNguoiDungSuDungPhieu(id);
        if (row == null) {
            throw new RuntimeException("Không tìm thấy phiếu hoặc chưa có dữ liệu thống kê.");
        }
        Object[] data = (Object[]) row;

        ChiTietPhieuResponse response = new ChiTietPhieuResponse();
        response.setId((Integer) data[0]);
        response.setMaPhieu((String) data[1]);
        response.setTenPhieu((String) data[2]);
        response.setGiaTriGiam((BigDecimal) data[3]);
        response.setTrangThai((String) data[4]);
        response.setSoLuongPhieu((Integer) data[5]);
        response.setSoLuotSuDung(((Number) data[6]).intValue());
        response.setSoNguoiSuDung(((Number) data[7]).intValue());
        response.setTongTienBanDuoc((BigDecimal) data[8]);
        response.setTongTienGiam((BigDecimal) data[9]);
        response.setTongTienTruocGiam((BigDecimal) data[10]);
        response.setUserDungPGG(objects);
        return response;
    }

    public List<String> assignVoucherToUsers(PGGUserDTO dto) {
        List<String> errors = new ArrayList<>();
        // 1. Validate voucher
        PhieuGiamGia pgg = phieuGiamGiaRepo.findById(dto.getPhieuGiamGiaId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));

        if ("expired".equalsIgnoreCase(pgg.getTrangThai()) || "isDelete".equalsIgnoreCase(pgg.getTrangThai())) {
            throw new IllegalStateException("Phiếu giảm giá đã hết hạn");
        }
        // 2. Duyệt danh sách user
        for (Integer userId : dto.getListUserId()) {
            Map<String, Object> failEntry = new HashMap<>();
            failEntry.put("userId", userId);

            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

                boolean daCo = viPhieuGiamGiaRepository.existsByUserIdAndPhieuGiamGiaId(userId, pgg.getId());
                if (daCo) {
                    errors.add("User ID " + userId + ": đã có voucher này");
                    continue;
                }

                // Lưu vào ví
                ViPhieuGiamGia v = ViPhieuGiamGia.builder()
                        .user(user)
                        .phieuGiamGia(pgg)
                        .build();
                viPhieuGiamGiaRepository.save(v);
            } catch (Exception e) {
                errors.add("User ID " + userId + ": " + e.getMessage());
            }
        }
        return errors;
    }
}
