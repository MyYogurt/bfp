package com.bfp.service;

import com.bfp.filemanagement.FileHandler;
import com.bfp.filemanagement.dao.FileDO;
import com.bfp.model.CreateFileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileService {
    private final FileHandler fileHandler;

    @Autowired
    public FileService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @PostMapping("/file")
    public CreateFileResponse createFile(MultipartFile uploadFile) throws IOException {
        return fileHandler.createFile(uploadFile);
    }

    @GetMapping("/file")
    public FileDO getFile(String fileId) {
        return fileHandler.getFile(fileId);
    }

    @PutMapping("/file")
    public FileDO updateFile(String fileId, String fileLocation) throws IOException {
        return fileHandler.updateFile(fileId, fileLocation);
    }
}
