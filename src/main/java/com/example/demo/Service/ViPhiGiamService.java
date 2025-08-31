package com.example.demo.Service;

import com.example.demo.DTOs.ViGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Entity.User;
import com.example.demo.Entity.ViPhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.ViPhieuGiamGiaRepository;
import com.example.demo.Responses.PhieuGiamGiaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViPhiGiamService {
    private final ViPhieuGiamGiaRepository repository;
    private final UserRepository usersRepository;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepository;
    @Transactional
    public ViPhieuGiamGia addVoucherToWallet(ViGiamGiaDTO dto) {
        if (repository.existsByUser_IdAndPhieuGiamGia_Id(dto.getUserId(), dto.getPhieuGiamGiaId())) {
            throw new RuntimeException("Bạn đã nhận phiếu này rồi!");
        }

        User user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(dto.getPhieuGiamGiaId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        ViPhieuGiamGia vi = new ViPhieuGiamGia();
        vi.setUser(user);
        vi.setPhieuGiamGia(phieu);
        vi.setNgayNhan(LocalDateTime.now());
        return repository.save(vi);
    }
    @Transactional
    public ViPhieuGiamGia doiDiem(ViGiamGiaDTO dto) {
        if (repository.existsByUser_IdAndPhieuGiamGia_Id(dto.getUserId(), dto.getPhieuGiamGiaId())) {
            throw new RuntimeException("Bạn đã nhận phiếu này rồi!");
        }

        User user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(dto.getPhieuGiamGiaId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        if (phieu.getSoLuong() != null && phieu.getSoLuong() <= 0) {
            throw new RuntimeException("Phiếu đã hết!");
        }
        phieu.setSoLuong(phieu.getSoLuong() - 1);

        if (user.getDiemTichLuy() < phieu.getDiemDoi()){
            throw new RuntimeException("Bạn không đủ điểm để đổi phiếu!");
        }else{
            user.setDiemTichLuy(user.getDiemTichLuy() - phieu.getDiemDoi());
        }
        ViPhieuGiamGia vi = new ViPhieuGiamGia();
        vi.setUser(user);
        vi.setPhieuGiamGia(phieu);
        vi.setNgayNhan(LocalDateTime.now());
        return repository.save(vi);
    }

    public List<PhieuGiamGiaResponse> getPhieuTrongTuiTheoTrangThai(Integer userId, String trangThai) {
        List<ViPhieuGiamGia> danhSach = repository.findByUser_Id(userId);
        LocalDateTime now = LocalDateTime.now();

        return danhSach.stream()
                .map(vi -> {
                    PhieuGiamGia p = vi.getPhieuGiamGia();
                    PhieuGiamGiaResponse dto = new PhieuGiamGiaResponse();

                    dto.setId(vi.getId());
                    dto.setNgayNhan(vi.getNgayNhan());

                    dto.setMaPhieu(p.getMaPhieu());
                    dto.setTenPhieu(p.getTenPhieu());
                    dto.setSoLuong(p.getSoLuong());
                    dto.setLoaiPhieuGiam(p.getLoaiPhieuGiam());
                    dto.setGiaTriGiam(p.getGiaTriGiam());
                    dto.setGiamToiDa(p.getGiamToiDa());
                    dto.setGiaTriToiThieu(p.getGiaTriToiThieu());
                    dto.setNgayBatDau(p.getNgayBatDau());
                    dto.setNgayKetThuc(p.getNgayKetThuc());

                    String status;
                    if (now.isBefore(p.getNgayBatDau())) {
                        status = "inactive";
                    } else if (now.isAfter(p.getNgayKetThuc())) {
                        status = "expired";
                    } else {
                        status = "active";
                    }
                    dto.setTrangThaiThucTe(status);
                    return dto;
                })
                // Đảm bảo khi trạng thái null thì trả về toàn bộ
                .filter(dto -> trangThai == null || dto.getTrangThaiThucTe().equalsIgnoreCase(trangThai.trim()))
                .toList();
    }
}
