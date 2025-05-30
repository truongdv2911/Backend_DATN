package com.example.demo.Service;

import com.example.demo.DTOs.CartItemDTO;
import com.example.demo.DTOs.DTOhoaDon;
import com.example.demo.Entity.*;
import com.example.demo.Repository.HoaDonChiTietRepository;
import com.example.demo.Repository.HoaDonRepository;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.HoaDonResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoaDonService {
    private final HoaDonRepository hoaDonRepository;
    private final UserRepository userRepository;
    private final San_pham_Repo san_pham_repo;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

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
            hoaDon.setNgayGiao(dtOhoaDon.getNgayGiao());
            hoaDon.setTrangThai(TrangThaiHoaDon.PENDING);
            hoaDon.setPhuongThucThanhToan(dtOhoaDon.getPhuongThucThanhToan());

            // 3. Tạo các chi tiết hóa đơn
            List<HoaDonChiTiet> donChiTiets = new ArrayList<>();
            for (CartItemDTO cartItemDto : dtOhoaDon.getCartItems()) {
                SanPham sanPham = san_pham_repo.findById(cartItemDto.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + cartItemDto.getIdSanPham()));

                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHd(hoaDon);
                hoaDonChiTiet.setSp(sanPham);
                hoaDonChiTiet.setGia(sanPham.getGia());
                hoaDonChiTiet.setSoLuong(cartItemDto.getSoLuong());
                hoaDonChiTiet.setTongTien(sanPham.getGia().multiply(BigDecimal.valueOf(cartItemDto.getSoLuong())));
                donChiTiets.add(hoaDonChiTiet);
            }

            // 4. Tính tổng tiền
            BigDecimal totalHd = donChiTiets.stream()
                    .map(HoaDonChiTiet::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            hoaDon.setTamTinh(totalHd);
            hoaDon.setSoTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTien(totalHd.subtract(BigDecimal.ZERO));

            // 5. Lưu vào database (transaction sẽ commit tại đây nếu không có lỗi)
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            hoaDonChiTietRepository.saveAll(donChiTiets);

            return savedHoaDon;
        } catch (RuntimeException e) {
            // Transaction sẽ tự động rollback khi có RuntimeException
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

        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow(() -> new Exception("khong tim thay hoa don"));
        User user = userRepository.findById(dtOhoaDon.getUserId()).orElseThrow(() -> new Exception("khong tim thay nguoi dung"));
        User nv = userRepository.findById(idNV).orElseThrow(() -> new Exception("khong tim thay Nhan vien"));

        hoaDon.setTamTinh(dtOhoaDon.getTamTinh());
        hoaDon.setTongTien(dtOhoaDon.getTongTien());
        hoaDon.setTrangThai(dtOhoaDon.getTrangThai());
        hoaDon.setSoTienGiam(dtOhoaDon.getSoTienGiam());
        hoaDon.setPhuongThucThanhToan(dtOhoaDon.getPhuongThucThanhToan());
        hoaDon.setNgayGiao(dtOhoaDon.getNgayGiao());
        hoaDon.setDiaChiGiaoHang(dtOhoaDon.getDiaChiGiaoHang());
        hoaDon.setNv(nv);

        // Xoá danh sách chi tiết cũ
        List<HoaDonChiTiet> oldDetails = hoaDonChiTietRepository.findByIdOrder(id);
        hoaDonChiTietRepository.deleteAll(oldDetails);

        // Tạo lại danh sách chi tiết mới
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

        // Tính lại tổng tiền nếu cần
        BigDecimal tong = newDetails.stream()
                .map(HoaDonChiTiet::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        hoaDon.setTamTinh(tong);
        hoaDon.setTongTien(tong.subtract(hoaDon.getSoTienGiam() != null ? hoaDon.getSoTienGiam() : BigDecimal.ZERO));

        // Lưu hoá đơn + chi tiết
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
