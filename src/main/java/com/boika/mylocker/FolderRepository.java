package com.boika.mylocker;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByOwnerUsernameOrderByNameAsc(String ownerUsername);

    List<Folder> findByOwnerUsernameAndParentIsNullOrderByNameAsc(String ownerUsername);

    List<Folder> findByOwnerUsernameAndParentIdOrderByNameAsc(String ownerUsername, Long parentId);

    Optional<Folder> findByIdAndOwnerUsername(Long id, String ownerUsername);

    boolean existsByOwnerUsernameAndNameAndParentIsNull(String ownerUsername, String name);

    boolean existsByOwnerUsernameAndNameAndParentId(String ownerUsername, String name, Long parentId);

    List<Folder> findByParentId(Long parentId);
}