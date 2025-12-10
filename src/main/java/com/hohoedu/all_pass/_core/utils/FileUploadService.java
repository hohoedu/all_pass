package com.hohoedu.all_pass._core.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Transactional
public class FileUploadService {

    @Value("${ftp.server}")
    private String ftpServer;

    @Value("${ftp.port:21}")
    private int ftpPort;

    @Value("${ftp.username}")
    private String ftpUsername;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.notice-dir}")
    private String noticeDir;

    @Value("${ftp.course-book-dir}")
    private String courseBookDir;

    @Value("${ftp.signature-dir}")
    private String signatureDir;
    

    public String uploadNotice(MultipartFile file) {
        return uploadToFTP(file, noticeDir);
    }


    public String uploadCourseBook(MultipartFile file) {
        return uploadToFTP(file, courseBookDir);
    }

    public String uploadSignature(MultipartFile file) {
        return uploadToFTP(file, signatureDir);
    }

    public String uploadToFTP(MultipartFile file, String subDir) {
        FTPClient ftpClient = new FTPClient();
        try (InputStream inputStream = file.getInputStream()) {

            ftpClient.connect(ftpServer, ftpPort);
            boolean loggedIn = ftpClient.login(ftpUsername, ftpPassword);
            if (!loggedIn) throw new RuntimeException("FTP 로그인 실패");

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String remoteFilePath = subDir + "/" + file.getOriginalFilename();

            boolean uploaded = ftpClient.storeFile(remoteFilePath, inputStream);
            if (!uploaded) throw new RuntimeException("파일 업로드 실패");

            ftpClient.logout();
            return "https://" + ftpServer + "/" + remoteFilePath;

        } catch (IOException e) {
            throw new RuntimeException("FTP 업로드 중 오류", e);
        } finally {
            try {
                if (ftpClient.isConnected()) ftpClient.disconnect();
            } catch (IOException ignore) {
            }
        }
    }

    public void deleteFromFTP(String fileUrl) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(ftpServer, ftpPort);

            if (!ftpClient.login(ftpUsername, ftpPassword)) {
                throw new RuntimeException("FTP 로그인 실패");
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 1) URL을 FTP 경로로 변환
            // 예: https://hohoeduimg.speedgabia.com/course_book/2026/test.png
            // → /course_book/2026/test.png
            String path = fileUrl
                    .replace("https://" + ftpServer, "")
                    .replaceFirst("^/", "");

            System.out.println(">>> [FTP 삭제 시뮬레이션] 실제 삭제되지 않음");
            System.out.println(">>> 삭제 대상 파일 경로: " + path);
            // 2) 실제 삭제 수행
            boolean deleted = ftpClient.deleteFile(path);

            if (!deleted) {
                throw new RuntimeException("FTP 파일 삭제 실패: " + path);
            }

            ftpClient.logout();

        } catch (IOException e) {
            throw new RuntimeException("FTP 삭제 중 오류 발생", e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
