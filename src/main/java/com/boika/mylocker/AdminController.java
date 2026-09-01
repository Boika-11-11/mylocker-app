package com.boika.mylocker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    private final AppUserRepository userRepository;
    private final StoredFileRepository fileRepository;
    private final FolderRepository folderRepository;

    public AdminController(AppUserRepository userRepository,
                           StoredFileRepository fileRepository,
                           FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    @GetMapping("/admin")
    public String showPending(Model model) {
        model.addAttribute("pending", userRepository.findByApprovedFalse());
        return "admin";
    }

    @GetMapping("/admin/users")
    public String showAllUsers(Principal principal, Model model) {
        model.addAttribute("users", userRepository.findAllByOrderByUsernameAsc());
        model.addAttribute("me", principal.getName());
        return "users";
    }

    @PostMapping("/admin/approve")
    public String approve(@RequestParam Long id, RedirectAttributes redirect) {
        userRepository.findById(id).ifPresent(user -> {
            user.setApproved(true);
            userRepository.save(user);
            redirect.addFlashAttribute("message", "Approved " + user.getUsername() + ".");
        });
        return "redirect:/admin";
    }

    @PostMapping("/admin/reject")
    public String reject(@RequestParam Long id, RedirectAttributes redirect) {
        userRepository.findById(id).ifPresent(user -> {
            userRepository.delete(user);
            redirect.addFlashAttribute("message", "Rejected and removed " + user.getUsername() + ".");
        });
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/disable")
    public String disable(@RequestParam Long id,
                          Principal principal,
                          RedirectAttributes redirect) {

        AppUser user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirect.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        if (user.getUsername().equals(principal.getName())) {
            redirect.addFlashAttribute("error", "You cannot disable your own account.");
            return "redirect:/admin/users";
        }

        user.setApproved(false);
        userRepository.save(user);

        redirect.addFlashAttribute("message", "Disabled " + user.getUsername() + ".");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/enable")
    public String enable(@RequestParam Long id, RedirectAttributes redirect) {

        userRepository.findById(id).ifPresent(user -> {
            user.setApproved(true);
            userRepository.save(user);
            redirect.addFlashAttribute("message", "Enabled " + user.getUsername() + ".");
        });

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/delete")
    @Transactional
    public String delete(@RequestParam Long id,
                         Principal principal,
                         RedirectAttributes redirect) {

        AppUser user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirect.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        if (user.getUsername().equals(principal.getName())) {
            redirect.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/admin/users";
        }

        String owner = user.getUsername();

        List<StoredFile> theirFiles =
                fileRepository.findByOwnerUsernameOrderByUploadedAtDesc(owner);

        for (StoredFile file : theirFiles) {
            try {
                Files.deleteIfExists(UPLOAD_DIR.resolve(file.getStoredName()));
            } catch (IOException ignored) {
            }
            fileRepository.delete(file);
        }

        List<Folder> theirFolders = folderRepository.findByOwnerUsernameOrderByNameAsc(owner);
        for (Folder folder : theirFolders) {
            folderRepository.delete(folder);
        }

        userRepository.delete(user);

        redirect.addFlashAttribute("message",
                "Deleted " + owner + " along with "
                        + theirFiles.size() + " file(s) and "
                        + theirFolders.size() + " folder(s).");

        return "redirect:/admin/users";
    }
}