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

    @Value("${ftp.base-dir:/uploads}")
    private String baseDir;

    public String uploadToFTP(MultipartFile file) {
        FTPClient ftpClient = new FTPClient();
        try (InputStream inputStream = file.getInputStream()) {

            ftpClient.connect(ftpServer, ftpPort);
            boolean loggedIn = ftpClient.login(ftpUsername, ftpPassword);
            if (!loggedIn) throw new RuntimeException("FTP 로그인 실패");

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String remoteFilePath = baseDir + "/" + file.getOriginalFilename();

            boolean uploaded = ftpClient.storeFile(remoteFilePath, inputStream);
            if (!uploaded) throw new RuntimeException("파일 업로드 실패");

            ftpClient.logout();
            return "https://" + ftpServer + remoteFilePath;

        } catch (IOException e) {
            throw new RuntimeException("FTP 업로드 중 오류", e);
        } finally {
            try {
                if (ftpClient.isConnected()) ftpClient.disconnect();
            } catch (IOException ignore) {
            }
        }
    }
}
