package com.example.demo.Service;

import com.example.demo.DTOs.DTOdanhGia;
import com.example.demo.Entity.AnhDanhGia;
import com.example.demo.Entity.DanhGia;
import com.example.demo.Entity.User;
import com.example.demo.Entity.VideoDanhGia;
import com.example.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DanhGiaService {
    private final DanhGiaRepository danhGiaRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final UserRepository userRepository;
    private final San_pham_Repo san_pham_repo;
    private final AnhDanhGiaRepository anhDanhGiaRepository;
    private final VideoDanhGiaRepository videoDanhGiaRepository;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB

    private static final List<String> IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> VIDEO_TYPES = List.of("video/mp4", "video/quicktime", "video/x-msvideo","video/x-matroska");


    public DanhGia createDanhGia(DTOdanhGia dtOdanhGia) throws Exception {
        try {
            // validate
            boolean daMua = hoaDonChiTietRepository.hasUserPurchasedSanPham(dtOdanhGia.getUser_id(), dtOdanhGia.getSp_id());
            boolean daDanhGia = danhGiaRepository.existsByUserIdAndSpIdAndAndDhctId(
                    dtOdanhGia.getUser_id(), dtOdanhGia.getSp_id(), dtOdanhGia.getHdct_id()
            );
            if (!daMua) {
                throw new Exception("Bạn chưa mua sản phẩm này nên không thể đánh giá.");
            }

            if (daDanhGia) {
                throw new Exception("Bạn đã đánh giá sản phẩm này trong đơn hàng này.");
            }

            // tao danh gia
            DanhGia dg = new DanhGia();
            dg.setUser(userRepository.findById(dtOdanhGia.getUser_id()).orElseThrow(() ->
                    new RuntimeException("Khong tim thay id user")
                    ));
            dg.setSp(san_pham_repo.findById(dtOdanhGia.getSp_id()).orElseThrow(() ->
                    new RuntimeException("Khong tim thay id san pham")
                    ));
            dg.setDhct(hoaDonChiTietRepository.findById(dtOdanhGia.getHdct_id()).orElseThrow(() ->
                    new RuntimeException("khong tim thay id hoa don")
                    )); // phải đảm bảo liên kết đơn hàng
            dg.setTieuDe(dtOdanhGia.getTieuDe());
            dg.setTextDanhGia(dtOdanhGia.getTextDanhGia());
            dg.setSoSao(dtOdanhGia.getSoSao());
            dg.setNgayDanhGia(LocalDateTime.now());
            danhGiaRepository.save(dg);
            return dg;

        }catch (Exception e){
            throw new Exception("Loi khi tao danh gia", e);
        }
    }

    public List<DanhGia> getDanhGiaByIdSp(Integer idSp){
        return danhGiaRepository.findBySpId(idSp);
    }

    public DanhGia updateDanhGia(Integer idDanhGia, String phanHoi, Integer idNv) throws Exception {
        try {
            User user = userRepository.findById(idNv).orElseThrow(() -> new RuntimeException("khong tim thay id nhan vien"));
            if (user.getRole().getId() == 2) {
                throw new Exception("Bạn không có quyền xóa đánh giá này.");
            }
            DanhGia dg = danhGiaRepository.findById(idDanhGia).orElseThrow(() ->
                    new RuntimeException("Khong tim thay id danh gia"));
            dg.setTextPhanHoi(phanHoi);
            dg.setNv(userRepository.findById(idNv).orElse(null));
            dg.setNgayPhanHoi(LocalDateTime.now());
            danhGiaRepository.save(dg);
            return dg;
        }catch (Exception e){
            throw new Exception("Loi khi sua danh gia", e);
        }
    }

    public String deleteDanhGia(Integer id){
        DanhGia dg = danhGiaRepository.findById(id).orElseThrow(() -> new RuntimeException("khong tim thay id danh gia"));
        danhGiaRepository.delete(dg);
        return "Xoa danh gia thanh cong";
    }

    public void uploadAnh(Integer idDanhGia, List<MultipartFile> images) throws Exception {
        try {
            DanhGia dg = danhGiaRepository.findById(idDanhGia)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

            int existing = anhDanhGiaRepository.countByDanhGiaId(idDanhGia);
            if (existing + images.size() > 3) {
                throw new RuntimeException("Tối đa 3 ảnh cho mỗi đánh giá");
            }
            for (MultipartFile file : images) {
                if (!IMAGE_TYPES.contains(file.getContentType())) {
                    throw new RuntimeException("Chỉ cho phép định dạng ảnh JPG, PNG, WEBP");
                }
                if (file.getSize() > MAX_IMAGE_SIZE) {
                    throw new RuntimeException("Ảnh vượt quá dung lượng 5MB");
                }

                String fileName = saveFile(file);
                AnhDanhGia af = new AnhDanhGia();
                af.setUrl(fileName);
                af.setDanhGia(dg);
                anhDanhGiaRepository.save(af);
            }
        }catch (Exception e){
            throw new Exception("loi khi upload anh danh gia");
        }
    }

    public void uploadVideo(Integer danhGiaId, MultipartFile file) throws IOException {
        DanhGia dg = danhGiaRepository.findById(danhGiaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        if (videoDanhGiaRepository.existsByDanhGiaId(danhGiaId))
            throw new RuntimeException("Mỗi đánh giá chỉ có 1 video");

        if (!VIDEO_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Chỉ cho phép video MP4, MOV, AVI");
        }
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new RuntimeException("Video vượt quá dung lượng 50MB");
        }

        String fileName = saveFile(file);
        VideoDanhGia vf = new VideoDanhGia();
        vf.setUrl(fileName);
        vf.setDanhGia(dg);
        videoDanhGiaRepository.save(vf);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new IOException("Tên file không hợp lệ: " + originalFileName);
        }

        // Tạo tên file duy nhất
        String fileName = UUID.randomUUID() + "_" + originalFileName;

        // Đảm bảo thư mục tồn tại
        Path uploadDir = Paths.get("UploadsFeedback");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Ghi file
        Path path = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return fileName; // hoặc return path.toString() nếu bạn muốn đường dẫn đầy đủ
    }
}
