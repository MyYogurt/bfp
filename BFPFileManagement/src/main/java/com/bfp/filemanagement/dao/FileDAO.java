package com.bfp.filemanagement.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileDAO {
    Optional<FileDO> getFile(UUID fileId);
    FileDO saveFile(FileDO fileDO);
    List<FileDO> getAllFilesByOwnerId(String ownerId);
    void deleteFile(UUID fileId);
    void deleteFile(FileDO fileDO);
    void updateFile(FileDO fileDO);
    void deleteAllFilesByOwnerId(String ownerId);
}
