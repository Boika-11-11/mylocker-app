package com.boika.mylocker;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    List<StoredFile> findByOwnerUsernameOrderByUploadedAtDesc(String ownerUsername);

    Optional<StoredFile> findByIdAndOwnerUsername(Long id, String ownerUsername);

    List<StoredFile> findByOwnerUsernameAndFolderIdOrderByUploadedAtDesc(
            String ownerUsername, Long folderId);

    List<StoredFile> findByOwnerUsernameAndFolderIsNullOrderByUploadedAtDesc(
            String ownerUsername);

    List<StoredFile> findByFolderId(Long folderId);
}