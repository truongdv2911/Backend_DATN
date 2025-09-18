package com.example.demo.Controller;

import com.example.demo.Repository.Anh_sp_Repo;
import com.example.demo.Repository.HangThanhLyRepository;
import com.example.demo.Responses.AnhResponse;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.HangThanhLyResponse;
import com.example.demo.Responses.SanPhamResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lego-store/hang-thanh-ly")
public class HangThanhLyController {
    private final HangThanhLyRepository hangThanhLyRepository;
    private final Anh_sp_Repo anhRepository;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAll(){
        try {
            List<HangThanhLyResponse> hangThanhLyResponses = hangThanhLyRepository.findAll().stream().map( htl -> {
                HangThanhLyResponse dto = new HangThanhLyResponse();
                // Lấy ảnh của sản phẩm (tùy cách bạn lấy)
                List<AnhResponse> anhResponses = anhRepository
                        .findBySanPhamId(htl.getSanPham().getId())      // hoặc service khác
                        .stream()
                        .map(AnhResponse::fromEntity)                  // nếu có hàm fromEntity
                        .toList();

                // Dùng fromEntity với danh sách ảnh
                SanPhamResponseDTO spDto =
                        SanPhamResponseDTO.fromEntity(htl.getSanPham(), anhResponses);

                dto.setSoLuong(htl.getSoLuong());
                dto.setNgayNhap(htl.getNgayNhap());
                dto.setGhiChu(htl.getGhiChu());
                dto.setSanPhamResponseDTO(spDto);
                return dto;
            }).toList();
            return ResponseEntity.ok(hangThanhLyResponses);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }
}
