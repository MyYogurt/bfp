package com.bfp.filemanagement.dao;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileDAO {
    byte[] getFile(String fileId);
    String createFile(String fileId, MultipartFile file) throws IOException;
    void deleteFile(String fileId);
    void updateFile(String fileId, MultipartFile file) throws IOException;
}
