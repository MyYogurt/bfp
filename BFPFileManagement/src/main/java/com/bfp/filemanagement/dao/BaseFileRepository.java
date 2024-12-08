package com.bfp.filemanagement.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BaseFileRepository extends JpaRepository<FileDO, UUID> {
    List<FileDO> findByOwnerId(String ownerId);
    void deleteByOwnerId(String ownerId);
}
