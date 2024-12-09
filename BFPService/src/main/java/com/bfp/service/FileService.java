package com.bfp.service;

import com.bfp.filemanagement.FileHandler;
import com.bfp.filemanagement.dao.FileDO;
import com.bfp.model.CreateFileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class FileService {
    private final FileHandler fileHandler;

    @Autowired
    public FileService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @PostMapping("/file")
    public CreateFileResponse createFile(MultipartFile file) throws IOException {
        return fileHandler.createFile(file);
    }

    @GetMapping("/file")
    public FileDO getFile(String fileId) {
        return fileHandler.getFile(fileId);
    }

    @PutMapping("/file")
    public FileDO updateFile(String fileId, MultipartFile file) throws IOException {
        return fileHandler.updateFile(fileId, file);
    }

    @DeleteMapping("/file")
    public void deleteFile(String fileId) {
        fileHandler.deleteFile(fileId);
    }

    @GetMapping("/listfiles")
    public List<FileDO> listFiles() {
        return fileHandler.listFiles();
    }
}
