package com.example.demo.Service;

import com.example.demo.DTOs.DTOhoaDonChiTiet;
import com.example.demo.Entity.HoaDon;
import com.example.demo.Entity.HoaDonChiTiet;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.HoaDonChiTietRepository;
import com.example.demo.Repository.HoaDonRepository;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.HoaDonChiTietResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class HoaDonChiTietService {
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final San_pham_Repo san_pham_repo;
    private final HoaDonRepository hoaDonRepository;

    @Transactional
    public HoaDonChiTietResponse createOrderDetail(DTOhoaDonChiTiet dtOhoaDonChiTiet) throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        HoaDon hoaDon = hoaDonRepository.findById(dtOhoaDonChiTiet.getHdId()).orElseThrow(() ->
                new Exception("Khong thay hoa don"));
        SanPham sanPham = san_pham_repo.findById(dtOhoaDonChiTiet.getSpId()).orElseThrow(() ->
                new Exception("Khong tim thay san pham"));
        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet(null,dtOhoaDonChiTiet.getSoLuong(),dtOhoaDonChiTiet.getGia()
                ,dtOhoaDonChiTiet.getTongTien(), hoaDon, sanPham);
        hoaDonChiTietRepository.save(hoaDonChiTiet);
        return modelMapper.map(hoaDonChiTiet, HoaDonChiTietResponse.class);
    }

    public HoaDonChiTietResponse findById(Integer id) throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(id).orElseThrow(() ->  new Exception("Khong thay san pham"));
        return modelMapper.map(hoaDonChiTiet, HoaDonChiTietResponse.class);
    }

    public List<HoaDonChiTietResponse> getAll(Integer idOrder) {
        ModelMapper modelMapper = new ModelMapper();
        return hoaDonChiTietRepository.findByIdOrder(idOrder).stream().map(orderDetail -> {
            HoaDonChiTietResponse orderDetailResponse = modelMapper.map(orderDetail,HoaDonChiTietResponse.class);
            return orderDetailResponse;
        }).toList();
    }

    @Transactional
    public HoaDonChiTiet updateOrder(Integer id, DTOhoaDonChiTiet dtOhoaDonChiTiet) throws Exception {
        HoaDon hoaDon = hoaDonRepository.findById(dtOhoaDonChiTiet.getHdId()).orElseThrow(() ->
                new Exception("Khong thay hoa don"));
        SanPham sanPham = san_pham_repo.findById(dtOhoaDonChiTiet.getSpId()).orElseThrow(() ->
                new Exception("Khong tim thay san pham"));
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.save(new HoaDonChiTiet(id, dtOhoaDonChiTiet.getSoLuong()
                ,dtOhoaDonChiTiet.getGia(),dtOhoaDonChiTiet.getTongTien(), hoaDon, sanPham));
        return hoaDonChiTiet;
    }

    @Transactional
    public void deleteOrderDetail(Integer id) throws Exception {
        hoaDonChiTietRepository.deleteById(id);
    }
}
