package com.example.demo.Service;

import com.example.demo.DTOs.PhieuGiamGiaDTO;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service

@Transactional
public class Phieu_giam_gia_Service {

    @Autowired
    private Phieu_giam_gia_Repo phieuGiamGiaRepo;

    public PhieuGiamGia createPhieuGiamGia(PhieuGiamGiaDTO phieuGiamGiaDTO) {

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
        phieuGiamGia.setMaPhieu(maPhieu);
        phieuGiamGia.setSoLuong(phieuGiamGiaDTO.getSoLuong());
        phieuGiamGia.setLoaiPhieuGiam(phieuGiamGiaDTO.getLoaiPhieuGiam());
        phieuGiamGia.setGiaTriGiam(phieuGiamGiaDTO.getGiaTriGiam());
        phieuGiamGia.setGiamToiDa(phieuGiamGiaDTO.getGiamToiDa());
        phieuGiamGia.setGiaTriToiThieu(phieuGiamGiaDTO.getGiaTriToiThieu());

        if (phieuGiamGiaDTO.getNgayKetThuc().isBefore(phieuGiamGiaDTO.getNgayBatDau())){
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (phieuGiamGiaDTO.getGiaTriGiam().compareTo(phieuGiamGiaDTO.getGiamToiDa()) > 0){
            throw new IllegalArgumentException("Gia tri giam khong duoc lon hon gia tri giam toi da");
        }
        phieuGiamGia.setNgayBatDau(phieuGiamGiaDTO.getNgayBatDau());
        phieuGiamGia.setNgayKetThuc(phieuGiamGiaDTO.getNgayKetThuc());
        phieuGiamGia.setTrangThai(phieuGiamGiaDTO.getTrangThai());
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }


    public List<PhieuGiamGia> getAllPhieuGiamGia() {
        return phieuGiamGiaRepo.findAll();
    }


    public PhieuGiamGia getPhieuGiamGiaById(Integer id) {
        return phieuGiamGiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id: " + id));
    }



    public PhieuGiamGia updatePhieuGiamGia(Integer id, PhieuGiamGiaDTO phieuGiamGiaDTO) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGia.setSoLuong(phieuGiamGiaDTO.getSoLuong());
        phieuGiamGia.setLoaiPhieuGiam(phieuGiamGiaDTO.getLoaiPhieuGiam());
        phieuGiamGia.setGiaTriGiam(phieuGiamGiaDTO.getGiaTriGiam());
        phieuGiamGia.setGiamToiDa(phieuGiamGiaDTO.getGiamToiDa());
        phieuGiamGia.setGiaTriToiThieu(phieuGiamGiaDTO.getGiaTriToiThieu());
        if (phieuGiamGiaDTO.getNgayKetThuc().isBefore(phieuGiamGiaDTO.getNgayBatDau())){
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        phieuGiamGia.setNgayBatDau(phieuGiamGiaDTO.getNgayBatDau());
        phieuGiamGia.setNgayKetThuc(phieuGiamGiaDTO.getNgayKetThuc());
        phieuGiamGia.setTrangThai(phieuGiamGiaDTO.getTrangThai());
        return phieuGiamGiaRepo.save(phieuGiamGia);
    }

    public PhieuGiamGia ThayDoiTrangThaiPhieuGiamGia(Integer id) throws Exception {
        Optional<PhieuGiamGia> phieuGiamGia = phieuGiamGiaRepo.findById(id);
        if (phieuGiamGia.isEmpty()){
            throw new Exception("Khong tim thay id phieu giam gia");
        }

        if (phieuGiamGia.get().getTrangThai().equals("Ngừng")){
            phieuGiamGia.get().setTrangThai("Đang hoạt động");
            return phieuGiamGiaRepo.save(phieuGiamGia.get());
        }else{
            phieuGiamGia.get().setTrangThai("Ngừng");
            return phieuGiamGiaRepo.save(phieuGiamGia.get());
        }
    }

    public void deletePhieuGiamGia(Integer id) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGiaById(id);
        phieuGiamGiaRepo.delete(phieuGiamGia);
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
}
