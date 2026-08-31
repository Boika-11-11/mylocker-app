package com.boika.mylocker;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FolderController {

    private final FolderRepository folderRepository;
    private final StoredFileRepository fileRepository;

    public FolderController(FolderRepository folderRepository,
                            StoredFileRepository fileRepository) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
    }

    @PostMapping("/folders/create")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) Long parentId,
                         Principal principal,
                         RedirectAttributes redirect) {

        String owner = principal.getName();
        String cleanName = name == null ? "" : name.trim();

        if (cleanName.isBlank() || cleanName.length() > 40
                || !cleanName.matches("^[a-zA-Z0-9 ._-]+$")) {
            redirect.addFlashAttribute("error",
                    "Folder name must be 1 to 40 letters, numbers, spaces, dot, dash or underscore.");
            return backTo(parentId);
        }

        Folder parent = null;

        if (parentId != null) {
            parent = folderRepository.findByIdAndOwnerUsername(parentId, owner).orElse(null);

            if (parent == null) {
                redirect.addFlashAttribute("error", "That folder was not found.");
                return "redirect:/files";
            }

            if (parent.getParent() != null) {
                redirect.addFlashAttribute("error",
                        "You can only go one level deep. Sub-folders cannot hold folders.");
                return backTo(parentId);
            }
        }

        boolean taken = (parent == null)
                ? folderRepository.existsByOwnerUsernameAndNameAndParentIsNull(owner, cleanName)
                : folderRepository.existsByOwnerUsernameAndNameAndParentId(owner, cleanName, parent.getId());

        if (taken) {
            redirect.addFlashAttribute("error", "You already have a folder called " + cleanName + " here.");
            return backTo(parentId);
        }

        Folder folder = new Folder();
        folder.setName(cleanName);
        folder.setOwnerUsername(owner);
        folder.setParent(parent);
        folderRepository.save(folder);

        redirect.addFlashAttribute("message", "Created folder: " + cleanName);
        return backTo(parentId);
    }

    @PostMapping("/folders/rename")
    public String rename(@RequestParam Long id,
                         @RequestParam String name,
                         Principal principal,
                         RedirectAttributes redirect) {

        String owner = principal.getName();
        String cleanName = name == null ? "" : name.trim();

        if (cleanName.isBlank() || cleanName.length() > 40
                || !cleanName.matches("^[a-zA-Z0-9 ._-]+$")) {
            redirect.addFlashAttribute("error", "Invalid folder name.");
            return "redirect:/files";
        }

        Folder folder = folderRepository.findByIdAndOwnerUsername(id, owner).orElse(null);

        if (folder == null) {
            redirect.addFlashAttribute("error", "That folder was not found.");
            return "redirect:/files";
        }

        Long parentId = folder.getParent() == null ? null : folder.getParent().getId();

        if (!folder.getName().equals(cleanName)) {

            boolean taken = (parentId == null)
                    ? folderRepository.existsByOwnerUsernameAndNameAndParentIsNull(owner, cleanName)
                    : folderRepository.existsByOwnerUsernameAndNameAndParentId(owner, cleanName, parentId);

            if (taken) {
                redirect.addFlashAttribute("error", "You already have a folder with that name here.");
                return "redirect:/files?folderId=" + folder.getId();
            }
        }

        folder.setName(cleanName);
        folderRepository.save(folder);

        redirect.addFlashAttribute("message", "Renamed to: " + cleanName);
        return "redirect:/files?folderId=" + folder.getId();
    }

    @PostMapping("/folders/delete")
    @Transactional
    public String delete(@RequestParam Long id,
                         Principal principal,
                         RedirectAttributes redirect) {

        String owner = principal.getName();

        Folder folder = folderRepository.findByIdAndOwnerUsername(id, owner).orElse(null);

        if (folder == null) {
            redirect.addFlashAttribute("error", "That folder was not found.");
            return "redirect:/files";
        }

        Long grandparentId = folder.getParent() == null ? null : folder.getParent().getId();

        List<StoredFile> contents = fileRepository.findByFolderId(folder.getId());
        for (StoredFile file : contents) {
            file.setFolder(null);
            fileRepository.save(file);
        }

        List<Folder> children = folderRepository.findByParentId(folder.getId());
        for (Folder child : children) {
            child.setParent(null);
            folderRepository.save(child);
        }

        String deletedName = folder.getName();
        folderRepository.delete(folder);

        redirect.addFlashAttribute("message",
                "Deleted " + deletedName + ". "
                        + contents.size() + " file(s) moved to Unfiled, "
                        + children.size() + " sub-folder(s) promoted to top level.");

        return backTo(grandparentId);
    }

    private String backTo(Long folderId) {
        return folderId == null ? "redirect:/files" : "redirect:/files?folderId=" + folderId;
    }
}