package com.example.demo.Service;

import com.example.demo.DTOs.DTOdanhGia;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.Responses.AnhResponse;
import com.example.demo.Responses.DanhGiaResponse;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
    private final CloudinaryService cloudinaryService;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB

    private static final List<String> IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> VIDEO_TYPES = List.of("video/mp4", "video/quicktime", "video/x-msvideo","video/x-matroska");


    public DanhGia createDanhGia(DTOdanhGia dtOdanhGia) throws Exception {
        try {
            // validate
            boolean daMua = hoaDonChiTietRepository.hasUserPurchasedSanPham(dtOdanhGia.getUser_id(), dtOdanhGia.getSp_id());
            if (!daMua) {
                throw new RuntimeException("Bạn chưa mua sản phẩm này nên không thể đánh giá.");
            }

            // Tìm HDCT phù hợp để đánh giá
            List<Integer> hdctToRate = findHdctForRating(dtOdanhGia.getUser_id(), dtOdanhGia.getSp_id());
            if (hdctToRate == null || hdctToRate.isEmpty()) {
                throw new Exception("Đơn hàng của bạn chưa để điều kiện để đánh giá.");
            }

            // Kiểm tra đã đánh giá chưa
            for (Integer i:
                    hdctToRate) {
                boolean daDanhGia = danhGiaRepository.existsByUserAndSpAndDhct(
                        userRepository.findById(dtOdanhGia.getUser_id()).orElse(null),
                        san_pham_repo.findById(dtOdanhGia.getSp_id()).orElse(null),
                        hoaDonChiTietRepository.findById(i).orElse(null));
                if (daDanhGia) {
                    throw new Exception("Bạn đã đánh giá sản phẩm này trong đơn hàng này.");
                }
            }

            // tao danh gia
            DanhGia dg = new DanhGia();
            dg.setUser(userRepository.findById(dtOdanhGia.getUser_id()).orElseThrow(() ->
                    new RuntimeException("Khong tim thay id user")
                    ));
            dg.setSp(san_pham_repo.findById(dtOdanhGia.getSp_id()).orElseThrow(() ->
                    new RuntimeException("Khong tim thay id san pham")
                    ));
            dg.setDhct(hoaDonChiTietRepository.findById(hdctToRate.get(0)).orElse(null));
            dg.setTieuDe(dtOdanhGia.getTieuDe());
            dg.setTextDanhGia(dtOdanhGia.getTextDanhGia());
            dg.setSoSao(dtOdanhGia.getSoSao());
            dg.setNgayDanhGia(LocalDateTime.now());
            danhGiaRepository.save(dg);

            SanPham sanPham = san_pham_repo.findById(dtOdanhGia.getSp_id()).orElseThrow(() ->
                    new RuntimeException("Khong tim thay id san pham"));
            List<DanhGia> danhGias = danhGiaRepository.findAllBySpId(sanPham.getId());
            sanPham.setSoLuongVote(danhGias.size());
            sanPham.setDanhGiaTrungBinh(setDanhGiaTrungBinh(danhGias));
            return dg;

        }catch (Exception e){
            throw new Exception( e.getMessage());
        }
    }

    public double setDanhGiaTrungBinh(List<DanhGia> danhGias) {
        if (danhGias == null || danhGias.isEmpty()) {
            return 0.0;
        }

        double tongSoSao = 0.0;
        for (DanhGia dg : danhGias) {
            tongSoSao += dg.getSoSao(); // giả sử soSao là int hoặc double
        }

        // Làm tròn 1 chữ số sau dấu phẩy
        return Math.round((tongSoSao / danhGias.size()) * 10.0) / 10.0;
    }

    public DanhGiaResponse updateDanhGia(Integer idDanhGia, DTOdanhGia dto, Integer idNv) throws Exception {
        try {
            User user = userRepository.findById(idNv).orElseThrow(() -> new RuntimeException("khong tim thay id nhan vien"));
            DanhGia dg = danhGiaRepository.findById(idDanhGia).orElseThrow(() ->
                    new RuntimeException("Khong tim thay id danh gia"));

            Integer sanPhamId = dg.getSp().getId();
            if (user.getRole().getId() == 3) {
                // Kiểm tra thời gian đánh giá (không được sửa sau 3 ngày)
                LocalDateTime ngayDanhGia = dg.getNgayDanhGia();
                LocalDateTime now = LocalDateTime.now();
                long daysBetween = ChronoUnit.DAYS.between(ngayDanhGia, now);

                if (daysBetween > 3) {
                    throw new RuntimeException("Không thể sửa đánh giá sau 3 ngày kể từ ngày đánh giá");
                }
                if (dto.getSoSao() == null){
                    throw new RuntimeException("Không để trống số sao");
                }
                dg.setSoSao(dto.getSoSao());
                dg.setTextDanhGia(dto.getTextDanhGia());
                dg.setTieuDe(dto.getTieuDe());

                // Cập nhật điểm sao trung bình cho sản phẩm khi khách hàng sửa đánh giá
                updateSanPhamRating(sanPhamId);
            }else {
                dg.setSoSao(dto.getSoSao());
                dg.setTextDanhGia(dto.getTextDanhGia());
                dg.setTieuDe(dto.getTieuDe());
                dg.setTextPhanHoi(dto.getTextPhanHoi());
                dg.setNv(user);
                dg.setNgayPhanHoi(LocalDateTime.now());
            }
            DanhGia danhGia = danhGiaRepository.save(dg);
            DanhGiaResponse response = convertToResponseDTO(danhGia);
            return response;
        }catch (Exception e){
            throw new Exception("Loi khi sua danh gia", e);
        }
    }

    private void updateSanPhamRating(Integer sanPhamId) {
        SanPham sanPham = san_pham_repo.findById(sanPhamId).orElseThrow(() ->
                new RuntimeException("Khong tim thay id san pham"));

        List<DanhGia> danhGias = danhGiaRepository.findAllBySpId(sanPham.getId());
        sanPham.setSoLuongVote(danhGias.size());
        sanPham.setDanhGiaTrungBinh(setDanhGiaTrungBinh(danhGias));
        san_pham_repo.save(sanPham); // Đừng quên save sản phẩm
    }

    private List<Integer> findHdctForRating(Integer userId, Integer spId) {
        List<Integer> allHdct = hoaDonChiTietRepository.findByUserAndSanPhamOrderByDateDesc(userId, spId);
        return allHdct.isEmpty() ? null : allHdct;
    }

    public List<DanhGia> getDanhGiaByIdSp(Integer idSp){
        return danhGiaRepository.findBySpId(idSp);
    }

    public String deleteDanhGia(Integer id){
        DanhGia dg = danhGiaRepository.findById(id).orElseThrow(() -> new RuntimeException("khong tim thay id danh gia"));
        try {
            if (dg.getVideoFeedback() != null) {
                deleteVideoDG(dg.getVideoFeedback().getId());
            }
            List<AnhDanhGia> anhDanhGias = dg.getAnhFeedbacks();
            if (anhDanhGias != null && !anhDanhGias.isEmpty()){
                for (AnhDanhGia anhDanhGia : anhDanhGias){
                    deleteAnhDG(anhDanhGia.getId());
                }
            }
        }catch (Exception e){
            throw new RuntimeException("Loi khi xoa danh gia");
        }
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

                String fileName = saveFile(file).get("url").toString();
                String mota = saveFile(file).get("public_id").toString();
                AnhDanhGia af = new AnhDanhGia();
                af.setUrl(fileName);
                af.setMota(mota);
                af.setDanhGia(dg);
                anhDanhGiaRepository.save(af);
            }
        }catch (Exception e){
            throw new Exception("loi khi upload anh danh gia");
        }
    }

    public void deleteAnhDG(Integer id) {
        AnhDanhGia anh = anhDanhGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
        String publicId = anh.getMota();
        try {
            cloudinaryService.delete(publicId, "image");
        }catch (Exception e){
            throw new RuntimeException("Loi khi xoa anh");
        }
        anhDanhGiaRepository.deleteById(id);
    }

    public void deleteVideoDG(Integer id) {
        try {
            VideoDanhGia videoDanhGia = videoDanhGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
            DanhGia danhGia = danhGiaRepository.findByVideoFeedbackId(id);
            danhGia.setVideoFeedback(null); // Gỡ liên kết
            danhGiaRepository.save(danhGia);
            String publicId = videoDanhGia.getMota();
            videoDanhGiaRepository.delete(videoDanhGia);
            cloudinaryService.delete(publicId, "video");
        }catch (Exception e){
            throw new RuntimeException("Loi khi xoa video");
        }
    }

    public void uploadVideo(Integer danhGiaId, MultipartFile file) throws Exception {
        try {
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

            String fileName = saveFile(file).get("url").toString();
            String mota = saveFile(file).get("public_id").toString();
            VideoDanhGia vf = new VideoDanhGia();
            vf.setUrl(fileName);
            vf.setMota(mota);
            vf.setDanhGia(dg);
            videoDanhGiaRepository.save(vf);
        }catch (Exception e){
            throw new Exception("loi khi upload video danh gia");
        }
    }

    private Map saveFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new IOException("Tên file không hợp lệ: " + originalFileName);
        }
        // Tạo tên file duy nhất
        Map result = cloudinaryService.upload(file);
        return result;
    }

    public DanhGiaResponse convertToResponseDTO(DanhGia danhGia) {
        List<AnhDanhGia> listAnh = anhDanhGiaRepository.findByDanhGiaId(danhGia.getId());

        List<AnhResponse> anhUrls = listAnh.stream()
                .map(anh -> {
                    AnhResponse response = new AnhResponse();
                    response.setId(anh.getId());
                    response.setUrl(anh.getUrl());
                    return response;
                })
                .toList();

        VideoDanhGia video = videoDanhGiaRepository.findByDanhGiaId(danhGia.getId());
        AnhResponse videoResponse = null;

        if (video != null) {
            videoResponse = new AnhResponse();
            videoResponse.setId(video.getId());
            videoResponse.setUrl(video.getUrl());
        }

        DanhGiaResponse dto = new DanhGiaResponse();
        dto.setId(danhGia.getId());
        dto.setTenKH(userRepository.findById(danhGia.getUser().getId()).orElse(null).getTen());
        dto.setTieuDe(danhGia.getTieuDe());
        dto.setTextDanhGia(danhGia.getTextDanhGia());
        dto.setTextPhanHoi(danhGia.getTextPhanHoi());
        dto.setSoSao(danhGia.getSoSao());
        dto.setNgayDanhGia(danhGia.getNgayDanhGia());
        dto.setNgayPhanHoi(danhGia.getNgayPhanHoi());
        dto.setUserId(danhGia.getUser() != null ? danhGia.getUser().getId() : null);
        dto.setNvId(danhGia.getNv() != null ? danhGia.getNv().getId() : null);
        dto.setDhctId(danhGia.getDhct() != null ? danhGia.getDhct().getId() : null);
        dto.setSpId(danhGia.getSp() != null ? danhGia.getSp().getId() : null);
        dto.setMaSP(danhGia.getSp()!= null ? danhGia.getSp().getMaSanPham() : null);
        dto.setTenSP(danhGia.getSp()!= null ? danhGia.getSp().getTenSanPham() : null);
        dto.setAnhUrls(anhUrls);
        dto.setVideo(videoResponse); // có thể là null nếu không có video
        return dto;
    }
}
