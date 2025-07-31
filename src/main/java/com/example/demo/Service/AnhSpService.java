package com.example.demo.Service;

import com.example.demo.DTOs.Anh_sp_DTO;
import com.example.demo.Entity.AnhSp;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.Anh_sp_Repo;
import com.example.demo.Repository.San_pham_Repo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnhSpService {

    // Giả định các biến tĩnh
    private final Path uploadDir = Paths.get("uploads");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/gif");
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");
    private static final String folderId = "1HOeJiTw28bO9QKkYayBpqh9_hl6OzoSE";

    // Giả định repository
    private final Anh_sp_Repo anhSpRepository;
    private final San_pham_Repo sanPhamRepository;
    private final GoogleDriveService googleDriveService;


    public List<Anh_sp_DTO> getAllAnhSp() {
        List<AnhSp> anhSpList = anhSpRepository.findAll();
        return anhSpList.stream().map(anhSp -> new Anh_sp_DTO(
                anhSp.getUrl(),
                anhSp.getMoTa(),
                anhSp.getAnhChinh(),
                anhSp.getSanPham() != null ? anhSp.getSanPham().getId() : null
        )).collect(Collectors.toList());
    }

    public Anh_sp_DTO getAnhSpById(Integer id) {
        AnhSp anhSp = anhSpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
        return new Anh_sp_DTO(
                anhSp.getUrl(),
                anhSp.getMoTa(),
                anhSp.getAnhChinh(),
                anhSp.getSanPham() != null ? anhSp.getSanPham().getId() : null
        );
    }

//    public void deleteAnhSp(Integer id) {
//        anhSpRepository.deleteById(id);
//    }
public void deleteAnhSp(Integer id) {
    AnhSp anh = anhSpRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
    SanPham sanPham = anh.getSanPham();
    boolean isAnhChinh = Boolean.TRUE.equals(anh.getAnhChinh());

    Path path = uploadDir.resolve(anh.getUrl());
    try {
        Files.deleteIfExists(path);
    } catch (IOException e) {
        System.out.println("Không thể xóa file vật lý: " + e.getMessage());
    }
    anhSpRepository.deleteById(id);
    // Nếu ảnh bị xoá là ảnh chính, cần cập nhật lại ảnh chính mới
    if (isAnhChinh) {
        List<AnhSp> remainingImages = anhSpRepository.findBySanPhamId(sanPham.getId());
        if (!remainingImages.isEmpty()) {
            // Gán ảnh đầu tiên làm ảnh chính mới
            AnhSp newAnhChinh = remainingImages.get(0);
            newAnhChinh.setAnhChinh(true);
            anhSpRepository.save(newAnhChinh);
        }
    }
}


    public Anh_sp_DTO updateAnhSp(Integer id, MultipartFile file, String moTa, Integer thuTu, Boolean anhChinh, Integer sanPhamId) {
        try {
            AnhSp anhSp = anhSpRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

            if (file != null && !file.isEmpty()) {
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new RuntimeException("Kích thước file vượt quá 10 MB: " + file.getOriginalFilename());
                }

                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                if (originalFilename.contains("..")) {
                    throw new RuntimeException("Tên file không hợp lệ");
                }

                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Chỉ cho phép upload file ảnh (.jpg, .jpeg, .png, .gif)");
                }

                String extension = StringUtils.getFilenameExtension(originalFilename);
                if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                    throw new IllegalArgumentException("Định dạng file ảnh không hợp lệ: ." + extension);
                }

                String randomString = generateRandomString(10);
                String uniqueFilename = "anh_" + randomString + "_" + originalFilename;
                Path filePath = uploadDir.resolve(uniqueFilename);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, file.getBytes());
                anhSp.setUrl(uniqueFilename);
            }

            anhSp.setMoTa(moTa);
            anhSp.setThuTu(thuTu);
            anhSp.setAnhChinh(anhChinh);

            if (sanPhamId != null) {
                SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                anhSp.setSanPham(sanPham);
            }

            AnhSp updatedAnhSp = anhSpRepository.save(anhSp);
            return new Anh_sp_DTO(
                    updatedAnhSp.getUrl(),
                    updatedAnhSp.getMoTa(),
                    updatedAnhSp.getAnhChinh(),
                    updatedAnhSp.getSanPham() != null ? updatedAnhSp.getSanPham().getId() : null
            );
        } catch (IOException e) {
            throw new RuntimeException("Lỗi cập nhật ảnh: " + e.getMessage());
        }
    }

    public List<Anh_sp_DTO> uploadAndCreateAnhSp(MultipartFile[] files, String moTa, Boolean anhChinh, Integer sanPhamId) {
        if (files == null || files.length == 0) {
            throw new RuntimeException("Chưa chọn ảnh để upload.");
        }
        try {
            // Lấy sản phẩm
            SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Lấy danh sách ảnh đã có của sản phẩm
            List<AnhSp> existingImages = anhSpRepository.findBySanPhamId(sanPhamId);
            int totalImagesAfterUpload = existingImages.size() + files.length;

            if (totalImagesAfterUpload > 7) {
                throw new RuntimeException("Sản phẩm đã có " + existingImages.size() + " ảnh. "
                        + "Chỉ được upload tối đa 7 ảnh. Chỉ có thể thêm " + (7 - existingImages.size()) + " ảnh nữa.");
            }


            List<Anh_sp_DTO> anhSpList = new ArrayList<>();
            boolean daCoAnhChinh = existingImages.stream().anyMatch(AnhSp::getAnhChinh);
            boolean daGanAnhChinh = false;
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];

                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new RuntimeException("Kích thước file vượt quá 10 MB: " + file.getOriginalFilename());
                }
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                if (originalFilename.contains("..")) {
                    throw new RuntimeException("Tên file không hợp lệ: " + originalFilename);
                }

                // Kiểm tra MIME type
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Chỉ cho phép upload file ảnh (.jpg, .jpeg, .png, .gif)");
                }

                // Kiểm tra đuôi file
                String extension = StringUtils.getFilenameExtension(originalFilename);
                if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                    throw new IllegalArgumentException("Định dạng file ảnh không hợp lệ: ." + extension);
                }

                File fileTemp = File.createTempFile("temp", null);
                file.transferTo(fileTemp);
                String url = googleDriveService.uploadFileToDrive(fileTemp, folderId);

                AnhSp anhSp = new AnhSp();
                anhSp.setUrl(url);
                anhSp.setMoTa(moTa);
                // --- XỬ LÝ ẢNH CHÍNH ---
                if (!daCoAnhChinh && !daGanAnhChinh) {
                    // Nếu chưa có ảnh chính trước đó, gán ảnh đầu tiên là ảnh chính
                    anhSp.setAnhChinh(true);
                    sanPham.setAnhDaiDien(url);
                    daGanAnhChinh = true;
                } else if (Boolean.TRUE.equals(anhChinh) && !daGanAnhChinh) {
                    // Nếu người dùng yêu cầu ảnh chính (qua param) thì:
                    // Gỡ bỏ ảnh chính cũ (nếu có)
                    for (AnhSp img : existingImages) {
                        if (Boolean.TRUE.equals(img.getAnhChinh())) {
                            img.setAnhChinh(false);
                            anhSpRepository.save(img);
                        }
                    }
                    anhSp.setAnhChinh(true);
                    sanPham.setAnhDaiDien(url);
                    daGanAnhChinh = true;
                } else {
                    anhSp.setAnhChinh(false);
                }
                anhSp.setSanPham(sanPham);
                // Lưu ảnh và ánh xạ sang DTO
                AnhSp savedAnhSp = anhSpRepository.save(anhSp);
                Anh_sp_DTO anhSpDTO = new Anh_sp_DTO(
                        savedAnhSp.getUrl(),
                        savedAnhSp.getMoTa(),
                        savedAnhSp.getAnhChinh(),
                        sanPhamId
                );
                anhSpList.add(anhSpDTO);
            }

            return anhSpList;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload ảnh", e);
        }
    }

    public UrlResource loadImage(String fileName) {
        try {
            Path imagePath = uploadDir.resolve(fileName);
            UrlResource resource = new UrlResource(imagePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Không tìm thấy ảnh");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi load ảnh", e);
        }
    }
//    public List<AnhSp> getAnhBySanPhamId(Integer sanPhamId) {
//        return anhSpRepository.findBySanPham_Id(sanPhamId);
//    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    //

    public List<Anh_sp_DTO> getAnhBySanPhamId(Integer sanPhamId) {
        List<AnhSp> list = anhSpRepository.findBySanPhamId(sanPhamId);
        return list.stream().map(anhSp -> new Anh_sp_DTO(
                anhSp.getUrl(),
                anhSp.getMoTa(),
                anhSp.getAnhChinh(),
                anhSp.getSanPham().getId()
        )).collect(Collectors.toList());
    }

}