package com.boika.mylocker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    private final StoredFileRepository fileRepository;
    private final FolderRepository folderRepository;

    public FileController(StoredFileRepository fileRepository,
                          FolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    @GetMapping("/files")
    public String listFiles(@RequestParam(required = false) Long folderId,
                            Principal principal,
                            Model model) {

        String owner = principal.getName();

        Folder current = null;
        if (folderId != null) {
            current = folderRepository.findByIdAndOwnerUsername(folderId, owner).orElse(null);
        }

        List<Folder> visibleFolders;
        List<StoredFile> files;

        if (current == null) {
            visibleFolders = folderRepository
                    .findByOwnerUsernameAndParentIsNullOrderByNameAsc(owner);
            files = fileRepository
                    .findByOwnerUsernameAndFolderIsNullOrderByUploadedAtDesc(owner);
        } else {
            visibleFolders = folderRepository
                    .findByOwnerUsernameAndParentIdOrderByNameAsc(owner, current.getId());
            files = fileRepository
                    .findByOwnerUsernameAndFolderIdOrderByUploadedAtDesc(owner, current.getId());
        }

        model.addAttribute("currentFolder", current);
        model.addAttribute("visibleFolders", visibleFolders);
        model.addAttribute("allFolders", folderRepository.findByOwnerUsernameOrderByNameAsc(owner));
        model.addAttribute("files", files);
        model.addAttribute("canCreateSubfolder", current == null || current.getParent() == null);

        return "files";
    }

    @PostMapping("/files/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(required = false) Long folderId,
                         Principal principal,
                         RedirectAttributes redirect) {

        String owner = principal.getName();

        if (file.isEmpty()) {
            redirect.addFlashAttribute("error", "Please choose a file first.");
            return backTo(folderId);
        }

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findByIdAndOwnerUsername(folderId, owner).orElse(null);
        }

        try {
            Files.createDirectories(UPLOAD_DIR);

            String originalName = Paths.get(file.getOriginalFilename())
                    .getFileName()
                    .toString();

            String extension = "";
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) {
                extension = originalName.substring(dot);
            }

            String storedName = UUID.randomUUID() + extension;
            Path target = UPLOAD_DIR.resolve(storedName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            StoredFile record = new StoredFile();
            record.setOriginalName(originalName);
            record.setStoredName(storedName);
            record.setOwnerUsername(owner);
            record.setSizeInBytes(file.getSize());
            record.setFolder(folder);

            fileRepository.save(record);

            redirect.addFlashAttribute("message", "Uploaded: " + originalName);

        } catch (IOException e) {
            redirect.addFlashAttribute("error", "Upload failed. Try again.");
        }

        return backTo(folder == null ? null : folder.getId());
    }

    @PostMapping("/files/move")
    public String move(@RequestParam Long id,
                       @RequestParam(required = false) Long folderId,
                       Principal principal,
                       RedirectAttributes redirect) {

        String owner = principal.getName();

        Folder destination = null;
        if (folderId != null) {
            destination = folderRepository.findByIdAndOwnerUsername(folderId, owner).orElse(null);

            if (destination == null) {
                redirect.addFlashAttribute("error", "That folder was not found.");
                return "redirect:/files";
            }
        }

        final Folder target = destination;

        fileRepository.findByIdAndOwnerUsername(id, owner).ifPresent(record -> {
            record.setFolder(target);
            fileRepository.save(record);
            redirect.addFlashAttribute("message",
                    "Moved " + record.getOriginalName()
                            + " to " + (target == null ? "Unfiled" : target.getName()));
        });

        return backTo(folderId);
    }

    @GetMapping("/files/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id, Principal principal) {

        StoredFile record = fileRepository
                .findByIdAndOwnerUsername(id, principal.getName())
                .orElse(null);

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = UPLOAD_DIR.resolve(record.getStoredName());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + record.getOriginalName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/files/delete")
    public String delete(@RequestParam Long id,
                         @RequestParam(required = false) Long folderId,
                         Principal principal,
                         RedirectAttributes redirect) {

        fileRepository.findByIdAndOwnerUsername(id, principal.getName())
                .ifPresent(record -> {
                    try {
                        Files.deleteIfExists(UPLOAD_DIR.resolve(record.getStoredName()));
                    } catch (IOException ignored) {
                    }
                    fileRepository.delete(record);
                    redirect.addFlashAttribute("message", "Deleted: " + record.getOriginalName());
                });

        return backTo(folderId);
    }

    private String backTo(Long folderId) {
        return folderId == null ? "redirect:/files" : "redirect:/files?folderId=" + folderId;
    }
}