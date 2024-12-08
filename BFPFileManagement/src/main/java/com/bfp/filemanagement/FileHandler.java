package com.bfp.filemanagement;

import com.bfp.common.CommonRequestHelper;
import com.bfp.filemanagement.dao.FileDAO;
import com.bfp.filemanagement.dao.FileDO;
import com.bfp.model.CreateFileResponse;
import com.bfp.model.exceptions.InvalidParameterException;
import com.bfp.model.exceptions.ResourceNotFoundException;
import com.bfp.model.exceptions.UnauthorizedException;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileHandler {
    private static final String BUCKET_NAME = "bfpfilebucket";

    private final FileDAO fileDAO;
    private final S3Client s3Client;

    public FileHandler(FileDAO fileDAO, S3Client s3Client) {
        this.fileDAO = fileDAO;
        this.s3Client = s3Client;
    }

    public CreateFileResponse createFile(MultipartFile uploadFile) throws IOException {
        // Implementation for handling file upload

        if (uploadFile == null) {
            throw new InvalidParameterException("File cannot be null");
        }

        String ownerId = CommonRequestHelper.getUserId();
        UUID fileId = UUID.randomUUID();
        String filename = uploadFile.getOriginalFilename();
        Instant createdAt = Instant.now();
        String fileLocation = uploadFileToS3(uploadFile.getInputStream(), uploadFile.getSize(), BUCKET_NAME, fileId.toString());

        FileDO fileDO = FileDO.builder()
                .id(fileId)
                .ownerId(ownerId)
                .fileLocation(fileLocation)
                .fileName(filename)
                .fileSize(uploadFile.getSize())
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        fileDAO.saveFile(fileDO);

        return CreateFileResponse.builder()
                .id(fileId.toString())
                .build();
    }

    public MultipartFile getFile(FileDO fileDO) {
        // Implementation for retrieving a file
        return null;
    }

    public FileDO getFile(String fileId) {
        FileDO file =  fileDAO.getFile(UUID.fromString(fileId)).orElseThrow();

        String ownerId = CommonRequestHelper.getUserId();

        if (!file.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You are not authorized to access this file.");
        }

        return file;
    }

    public FileDO updateFile(String fileId, MultipartFile newFile) throws IOException {
        Optional<FileDO> file =  fileDAO.getFile(UUID.fromString(fileId));

        if (file.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        FileDO fileDO = file.get();

        verifyFileOwner(fileDO);

        uploadFileToS3(newFile.getInputStream(), newFile.getSize(), BUCKET_NAME, fileId);

        updateFileDO(fileDO, newFile);

        fileDAO.updateFile(fileDO);

        return fileDO;
    }

    public void deleteFile(String fileId) {
        Optional<FileDO> file =  fileDAO.getFile(UUID.fromString(fileId));

        if (file.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        FileDO fileDO = file.get();

        verifyFileOwner(fileDO);

        deleteFileFromS3(BUCKET_NAME, fileDO.getId().toString());

        fileDAO.deleteFile(fileDO.getId());
    }

    private String uploadFileToS3(InputStream fileInputStream, long contentLength, String bucketName, String filename) {
        List<CompletedPart> completedParts = new ArrayList<>();
        int partNumber = 1;

        CreateMultipartUploadResponse  createMultipartUploadResponse = s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build());
        UploadPartResponse uploadPartResponse = s3Client.uploadPart(UploadPartRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .uploadId(createMultipartUploadResponse.uploadId())
                .partNumber(partNumber)
                .build(), RequestBody.fromInputStream(fileInputStream, contentLength));
        completedParts.add(CompletedPart.builder()
                .partNumber(partNumber)
                .eTag(uploadPartResponse.eTag())
                .build());
        CompletedMultipartUpload completed = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();
        CompleteMultipartUploadResponse completeMultipartUploadResponse = s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .uploadId(createMultipartUploadResponse.uploadId())
                .multipartUpload(completed)
                .build());
        return completeMultipartUploadResponse.location();
    }

    private void deleteFileFromS3(String bucketName, String filename) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build());
    }

    public List<FileDO> listFiles() {
        String ownerId = CommonRequestHelper.getUserId();
        return fileDAO.getAllFilesByOwnerId(ownerId);
    }

    private void updateFileDO(FileDO fileDO, MultipartFile newFile) {
        fileDO.setFileName(newFile.getOriginalFilename());
        fileDO.setFileSize(newFile.getSize());
        fileDO.setUpdatedAt(Instant.now());
    }

    private void verifyFileOwner(FileDO fileDO) {
        String ownerId = CommonRequestHelper.getUserId();

        if (!fileDO.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You are not authorized to access this file.");
        }
    }
}
