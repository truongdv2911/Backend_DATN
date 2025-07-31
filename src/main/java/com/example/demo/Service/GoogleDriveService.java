package com.example.demo.Service;

import com.example.demo.Component.GoogleAccessTokenProvider;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Service
public class GoogleDriveService {
    //Tạo một đối tượng JsonFactory để xử lý JSON khi tương tác với Google API
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    //Chỉ định ID của folder trên Drive mình sẽ sử dụng
    private static final String DRIVE_FOLDER_ID = "1HOeJiTw28bO9QKkYayBpqh9_hl6OzoSE";
    private final Drive drive; // <-- tạo 1 lần
    private GoogleAccessTokenProvider tokenProvider;

    @Autowired
    public GoogleDriveService(GoogleAccessTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        String accessToken = tokenProvider.getAccessToken();
        this.drive = getDriveService(accessToken);
    }

    private Drive getDriveService(String accessToken) {
        try {
            HttpRequestInitializer requestInitializer = request -> {
                request.getHeaders().setAuthorization("Bearer " + accessToken);
            };
            return new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    requestInitializer
            ).setApplicationName("ivent").build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //tải một tệp từ máy chủ lên Google Drive
    public String uploadFileToDrive(File file,String folderId) throws GeneralSecurityException, IOException {
        try {
            com.google.api.services.drive.model.File fileMetaData = createMetadata(file.getName(), folderId);
            // MIME type
            FileContent mediaContent = createFileContent(file);
            // Upload
            com.google.api.services.drive.model.File uploadedFile = this.drive.files()
                    .create(fileMetaData, mediaContent)
                    .setFields("id") //chỉ yêu cầu trả về ID file (để tạo URL) và thực hiện gọi API
                    .execute();

            // Cấp quyền công khai
            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            this.drive.permissions().create(uploadedFile.getId(), permission).execute();

            // Tạo URL xem video
            String fileUrl = "https://drive.google.com/file/d/" + uploadedFile.getId() + "/preview";
            System.out.println("VIDEO URL: " + fileUrl);

            file.delete(); // Xóa file tạm
            return fileUrl;
        } catch (Exception e) {
            System.out.println("Upload error: " + e.getMessage());
            return "loi"+e.getMessage();
        }
    }

    private FileContent createFileContent(File tempFile) {
        try {
            String mimeType = Files.probeContentType(tempFile.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            return new FileContent(mimeType, tempFile);
        } catch (IOException e) {
            throw new RuntimeException("loi khi tao file");
        }
    }

    private com.google.api.services.drive.model.File createMetadata(String fileName, String folderId) {
        com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
        fileMetaData.setName(fileName);
        fileMetaData.setParents(Collections.singletonList(folderId));
        return fileMetaData;
    }
}
