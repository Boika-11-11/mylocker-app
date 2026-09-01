package com.boika.mylocker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private static final long INVITE_MINUTES = 30;

    private final AppUserRepository userRepository;
    private final StoredFileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public AdminController(AppUserRepository userRepository,
                           StoredFileRepository fileRepository,
                           FolderRepository folderRepository,
                           PasswordEncoder encoder,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @GetMapping("/admin/users")
    public String showAllUsers(Principal principal, Model model) {
        model.addAttribute("users", userRepository.findAllByOrderByUsernameAsc());
        model.addAttribute("me", principal.getName());
        return "users";
    }

    @PostMapping("/admin/users/invite")
    public String invite(@RequestParam String email,
                         @RequestParam String username,
                         RedirectAttributes redirect) {

        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        String cleanName = username == null ? "" : username.trim();

        if (!cleanEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || cleanEmail.length() > 100) {
            redirect.addFlashAttribute("error", "Please enter a valid email address.");
            return "redirect:/admin/users";
        }

        if (cleanName.isBlank() || cleanName.length() > 30
                || !cleanName.matches("^[a-zA-Z0-9 ._-]+$")) {
            redirect.addFlashAttribute("error",
                    "Name must be 1 to 30 letters, numbers, spaces, dot, dash or underscore.");
            return "redirect:/admin/users";
        }

        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            redirect.addFlashAttribute("error", "An account with that email already exists.");
            return "redirect:/admin/users";
        }

        String token = UUID.randomUUID().toString();

        AppUser user = new AppUser();
        user.setEmail(cleanEmail);
        user.setUsername(capitalise(cleanName));
        user.setPassword(encoder.encode(UUID.randomUUID().toString()));
        user.setRole("USER");
        user.setApproved(true);
        user.setResetToken(token);
        user.setResetTokenExpires(LocalDateTime.now().plusMinutes(INVITE_MINUTES));

        userRepository.save(user);

        emailService.sendInvite(cleanEmail, user.getUsername(), token);

        redirect.addFlashAttribute("message",
                "Invited " + user.getUsername() + ". They have 30 minutes to set a password.");

        return "redirect:/admin/users";
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

        if (user.getEmail().equals(principal.getName())) {
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

        if (user.getEmail().equals(principal.getName())) {
            redirect.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/admin/users";
        }

        String owner = user.getEmail();

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

        String name = user.getUsername();
        userRepository.delete(user);

        redirect.addFlashAttribute("message",
                "Deleted " + name + " along with "
                        + theirFiles.size() + " file(s) and "
                        + theirFolders.size() + " folder(s).");

        return "redirect:/admin/users";
    }

    private String capitalise(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}