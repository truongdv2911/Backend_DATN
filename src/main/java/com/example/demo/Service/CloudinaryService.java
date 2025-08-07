package com.example.demo.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class CloudinaryService {

        Cloudinary cloudinary;

    public CloudinaryService() {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("cloud_name", "durppqsk4");
        valueMap.put("api_key", "354341218383646");
        valueMap.put("api_secret", "Rp6c5E_3Acoj4lsn3I4xbB9_y-s");
        cloudinary = new Cloudinary(valueMap);
    }

    public Map upload(MultipartFile file) throws IOException {
        File file1 = convert(file);
        // Kiểm tra định dạng file
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        // Cấu hình options upload
        Map<String, Object> options = new HashMap<>();
        if (fileExtension.matches("mp4|avi|mov|mkv|webm")) {
            options.put("resource_type", "video"); // Đảm bảo Cloudinary xử lý đúng loại video
        } else {
            options.put("resource_type", "auto"); // Tự đoán (ảnh, raw...)
        }

        Map result = cloudinary.uploader().upload(file1, options);

        // Xóa file tạm
        if (!Files.deleteIfExists(file1.toPath())) {
            throw new IOException("fail to delete " + file1.getAbsolutePath());
        }
        return result;
    }

    public Map delete(String publicId, String type) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", type));
    }

    private File convert(MultipartFile file) throws IOException {
        File file1 = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fo = new FileOutputStream(file1);
        fo.write(file.getBytes());
        fo.close();
        return file1;
    }

}
