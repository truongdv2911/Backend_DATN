package com.example.demo.Service;

import com.example.demo.DTOs.DanhMucDTO;
import com.example.demo.Entity.DanhMuc;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.Danh_muc_Repo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class   Danh_muc_Service {

    private final Danh_muc_Repo danhMucRepo;


    public DanhMuc createDanhMuc(@Valid DanhMucDTO danhMucDTO) {
        if (danhMucRepo.existsByTenDanhMuc(danhMucDTO.getTenDanhMuc())) {
            throw new RuntimeException("Tên danh mục đã tồn tại!");
        }
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setTenDanhMuc(danhMucDTO.getTenDanhMuc());
        danhMuc.setMoTa(danhMucDTO.getMoTa());
        danhMuc.setIsDelete(1);
        return danhMucRepo.save(danhMuc);
    }


    public List<DanhMuc> getAllDanhMuc() {
        return danhMucRepo.findAllActive();
    }


    public DanhMuc getDanhMucById(Integer id) {
        return danhMucRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
    }


    public DanhMuc updateDanhMuc(Integer id, @Valid DanhMucDTO danhMucDTO) {
        DanhMuc danhMuc = getDanhMucById(id);
        if (!danhMuc.getTenDanhMuc().equals(danhMucDTO.getTenDanhMuc())
                && danhMucRepo.existsByTenDanhMuc(danhMucDTO.getTenDanhMuc())) {
            throw new RuntimeException("Tên danh mục đã tồn tại!");
        }
        danhMuc.setTenDanhMuc(danhMucDTO.getTenDanhMuc());
        danhMuc.setMoTa(danhMucDTO.getMoTa());
        List<SanPham> sp = danhMuc.getSanPhams();
        danhMuc.setSanPhams(sp);
        return danhMucRepo.save(danhMuc);
    }


    public void deleteDanhMuc(Integer id) {
        DanhMuc danhMuc = getDanhMucById(id);
        
        // Kiểm tra xem có sản phẩm nào trong danh mục còn tồn kho không
        boolean hasProductsInStock = danhMuc.getSanPhams().stream()
                .anyMatch(sanPham -> {
                    Integer soLuongTon = sanPham.getSoLuongTon();
                    return soLuongTon != null && soLuongTon > 0;
                });
        
        if (hasProductsInStock) {
            throw new RuntimeException("Không thể xóa danh mục. Vẫn còn sản phẩm trong kho (soLuongTon > 0). Vui lòng bán hết tất cả sản phẩm trước khi xóa danh mục.");
        }

        danhMuc.setIsDelete(0);
        danhMucRepo.save(danhMuc);
    }
}
