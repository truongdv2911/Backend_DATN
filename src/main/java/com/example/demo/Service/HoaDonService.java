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
    private final ModelMapper modelMapper;
    @Transactional
    public HoaDon createOrder(DTOhoaDon dtOhoaDon) throws Exception {
        try {
            User user = userRepository.findById(dtOhoaDon.getUserId()).orElseThrow(() -> new Exception("khong tim thay nguoi dung"));
            modelMapper.typeMap(DTOhoaDon.class, HoaDon.class)
                    .addMappings(mapper -> mapper.skip(HoaDon::setId));
            HoaDon hoaDon = new HoaDon();
            modelMapper.map(dtOhoaDon, hoaDon);
            hoaDon.setUser(user);
            hoaDon.setNv(null);
            hoaDon.setNgayTao(LocalDateTime.now());
            hoaDon.setDiaChiGiaoHang("hehe hanoi");
            hoaDon.setMaVanChuyen(UUID.randomUUID().toString().substring(0,10));
            hoaDon.setNgayGiao(null);
            hoaDon.setTrangThai(TrangThaiHoaDon.PENDING);
            hoaDon.setPhuongThucThanhToan("cod");
            hoaDonRepository.save(hoaDon);

            List<HoaDonChiTiet> donChiTiets = new ArrayList<>();
            for (CartItemDTO cartItemDto:
                    dtOhoaDon.getCartItems()) {
                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHd(hoaDon);

                Integer idSanPham = cartItemDto.getIdSanPham();
                Integer soLuong = cartItemDto.getSoLuong();
                SanPham sanPham = san_pham_repo.findById(idSanPham).
                        orElseThrow(() -> new Exception("Khong tim thay san pham"));
                BigDecimal gia = sanPham.getGia();
                hoaDonChiTiet.setSp(sanPham);
                hoaDonChiTiet.setGia(gia);
                hoaDonChiTiet.setSoLuong(soLuong);
                hoaDonChiTiet.setTongTien(gia*soLuong);
                donChiTiets.add(hoaDonChiTiet);
            }
            hoaDonChiTietRepository.saveAll(donChiTiets);
            return hoaDon;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public HoaDonResponse findById(Integer id) throws Exception {
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
    public HoaDon updateOrder(Integer id, DTOhoaDon dtOhoaDon) throws Exception {
        HoaDon order = hoaDonRepository.findById(id).orElseThrow(() -> new Exception("khong tim thay hoa don"));
        User user = userRepository.findById(dtOhoaDon.getUserId()).orElseThrow(() -> new Exception("khong tim thay nguoi dung"));
        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper ->
                mapper.skip(Order::setId));
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Integer id) throws Exception {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow(() -> new Exception("khong tim thay hoa don"));
        hoaDon.setTrangThai(TrangThaiHoaDon.CANCELLED);
        hoaDonRepository.save(hoaDon);
    }
}
