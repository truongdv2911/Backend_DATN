package com.example.demo.Service;

import com.example.demo.DTOs.SPYeuThichDTO;
import com.example.demo.DTOs.ViGiamGiaDTO;
import com.example.demo.Entity.*;
import com.example.demo.Repository.SPYeuThichRepository;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.PhieuGiamGiaResponse;
import com.example.demo.Responses.SpYeuThichResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SPYeuThichService {
    private final SPYeuThichRepository spYeuThichRepository;
    private final UserRepository userRepository;
    private final San_pham_Repo san_pham_repo;

    public SanPhamYeuThich addSPyeuThich(SPYeuThichDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        SanPham sp = san_pham_repo.findById(dto.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (spYeuThichRepository.existsByUserIdAndSanPhamId(dto.getUserId(), dto.getSanPhamId())) {
            throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
        }
        SanPhamYeuThich sanPhamYeuThich = new SanPhamYeuThich();
        sanPhamYeuThich.setUser(user);
        sanPhamYeuThich.setSanPham(sp);
        return spYeuThichRepository.save(sanPhamYeuThich);
    }

    public List<SpYeuThichResponse> getSanPhamYeuThich(Integer userId) {
        List<SanPhamYeuThich> danhSach = spYeuThichRepository.findByUserId(userId);

        return danhSach.stream()
                .map(vi -> {
                    SanPham sp = vi.getSanPham();
                    SpYeuThichResponse dto = new SpYeuThichResponse();

                    dto.setId(vi.getId());
                    dto.setUserId(vi.getUser().getId());
                    dto.setSpId(vi.getSanPham().getId());
                    dto.setMaSP(sp.getMaSanPham());
                    dto.setTenSP(sp.getTenSanPham());
                    dto.setGia(sp.getGia());
                    dto.setDoTuoi(sp.getDoTuoi());
                    dto.setSoLuongTon(sp.getSoLuongTon());
                    dto.setTrangThai(sp.getTrangThai());
                    dto.setAnhSps(sp.getAnhSps());
                    return dto;
                }).toList();
    }

    public void deleteSp(Integer spId, Integer userId){
        SanPhamYeuThich sanPhamYeuThich = spYeuThichRepository.findByUserIdAndSanPhamId(userId,spId);
        spYeuThichRepository.delete(sanPhamYeuThich);
    }
}
