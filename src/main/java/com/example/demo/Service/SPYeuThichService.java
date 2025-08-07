package com.example.demo.Service;

import com.example.demo.DTOs.SPYeuThichDTO;
import com.example.demo.DTOs.ViGiamGiaDTO;
import com.example.demo.DTOs.WishListDTO;
import com.example.demo.Entity.*;
import com.example.demo.Repository.SPYeuThichRepository;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WishListRepository;
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
    private final WishListRepository wishListRepository;

    // tao wishlist
    public WishList createWL(WishListDTO wishListDTO){
        if (wishListRepository.existsByTen(wishListDTO.getTen())){
            throw new RuntimeException("Ten wish list bi trung");
        }
        User user = userRepository.findById(wishListDTO.getUserId()).orElseThrow(() -> new RuntimeException("khong tim thay id user"));
        return wishListRepository.save(new WishList(null, wishListDTO.getTen(), user));
    }

    public WishList updateWL(Integer id, String name){
        WishList wishList = wishListRepository.findById(id).orElseThrow(() -> new RuntimeException("khong tim thay id wishlist"));
        if (!wishList.getTen().equals(name)
                && wishListRepository.existsByTen(name)) {
            throw new RuntimeException("Tên wishlist đã tồn tại!");
        }
        wishList.setTen(name);
        return wishListRepository.save(wishList);
    }

    public void deleteWL(Integer id){
        WishList wishList = wishListRepository.findById(id).orElseThrow(() -> new RuntimeException("khong tim thay id wishlist"));
        List<SanPhamYeuThich> sanPhamYeuThichs = spYeuThichRepository.findByWishListId(id);
        for (SanPhamYeuThich sanPhamYeuThich:
                sanPhamYeuThichs) {
            spYeuThichRepository.delete(sanPhamYeuThich);
        }
        wishListRepository.delete(wishList);
    }


    public SanPhamYeuThich addSPyeuThich(SPYeuThichDTO dto) {

        WishList wishList = wishListRepository.findById(dto.getWishlistId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy wishlist"));

        SanPham sp = san_pham_repo.findById(dto.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (spYeuThichRepository.existsByWishListIdAndSanPhamId(dto.getWishlistId(), dto.getSanPhamId())){
            throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
        }
        SanPhamYeuThich sanPhamYeuThich = new SanPhamYeuThich();
        sanPhamYeuThich.setWishList(wishList);
        sanPhamYeuThich.setSanPham(sp);
        return spYeuThichRepository.save(sanPhamYeuThich);
    }

    public List<SpYeuThichResponse> getSanPhamYeuThich(Integer wlId) {
        List<SanPhamYeuThich> danhSach = spYeuThichRepository.findByWishListId(wlId);

        return danhSach.stream()
                .map(vi -> {
                    SanPham sp = vi.getSanPham();
                    SpYeuThichResponse dto = new SpYeuThichResponse();

                    dto.setId(vi.getId());
                    dto.setWishListId(vi.getWishList().getId());
                    dto.setSpId(vi.getSanPham().getId());
                    dto.setMaSP(sp.getMaSanPham());
                    dto.setTenSP(sp.getTenSanPham());
                    dto.setGia(sp.getGia());
                    dto.setGiaKM(sp.getGiaKM() != null ? sp.getGiaKM() : sp.getGia());
                    dto.setDoTuoi(sp.getDoTuoi());
                    dto.setSoLuongTon(sp.getSoLuongTon());
                    dto.setDanhMucId(sp.getDanhMuc().getId());
                    dto.setTenDanhMuc(sp.getDanhMuc().getTenDanhMuc());
                    dto.setBoSuuTapId(sp.getBoSuuTap().getId());
                    dto.setTenBoSuuTap(sp.getBoSuuTap().getTenBoSuuTap());
                    dto.setXuatXuId(sp.getXuatXu().getId());
                    dto.setTenXuatXu(sp.getXuatXu().getTen());
                    dto.setThuongHieuId(sp.getThuongHieu().getId());
                    dto.setTenThuongHieu(sp.getThuongHieu().getTen());
                    dto.setTrangThai(sp.getTrangThai());
                    dto.setAnhSps(sp.getAnhSps());
                    return dto;
                }).toList();
    }

    public void deleteSp(Integer spId, Integer wlId){
        wishListRepository.findById(wlId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy wishlist"));

        san_pham_repo.findById(spId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        SanPhamYeuThich sanPhamYeuThich = spYeuThichRepository.findByWishListIdAndSanPhamId(wlId,spId);
        spYeuThichRepository.delete(sanPhamYeuThich);
    }
}
