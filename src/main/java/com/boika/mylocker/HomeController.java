package com.boika.mylocker;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final long STORAGE_LIMIT_BYTES = 400L * 1024 * 1024; // 400 MB
    private final AppUserRepository userRepository;
    private final StoredFileRepository fileRepository;
    private final FolderRepository folderRepository;

    public HomeController(AppUserRepository userRepository,
                          StoredFileRepository fileRepository,
                          FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Principal principal, Model model) {

        AppUser user = userRepository.findByEmail(principal.getName()).orElse(null);

        if (user == null) {
            model.addAttribute("displayName", "there");
            model.addAttribute("firstVisit", false);
            model.addAttribute("isDemo", false);
            model.addAttribute("totalFiles", 0L);
            model.addAttribute("totalFolders", 0L);
            model.addAttribute("storageUsedFormatted", "0 B");
            model.addAttribute("storagePercent", 0);
            model.addAttribute("storageLimitFormatted", "400 MB");            model.addAttribute("recentFiles", List.of());
            model.addAttribute("isAdmin", false);
            model.addAttribute("peopleCount", 0L);
            return "home";
        }

        String username = user.getUsername();
        boolean firstVisit = user.getLastLoginAt() == null;

        List<StoredFile> files = fileRepository.findByOwnerUsernameOrderByUploadedAtDesc(username);
        long totalFiles = files.size();
        long totalFolders = folderRepository.findByOwnerUsernameOrderByNameAsc(username).size();

        long totalBytes = files.stream()
                .mapToLong(StoredFile::getSizeInBytes)
                .sum();

        double percent = (double) totalBytes / STORAGE_LIMIT_BYTES * 100.0;
        int storagePercent = (int) Math.min(100, Math.round(percent));

        List<StoredFile> recentFiles = files.size() > 5 ? files.subList(0, 5) : files;

        boolean isAdmin = "ADMIN".equals(user.getRole());
        long peopleCount = isAdmin ? userRepository.count() : 0L;

        model.addAttribute("displayName", user.getUsername());
        model.addAttribute("firstVisit", firstVisit);
        model.addAttribute("isDemo", "DEMO".equals(user.getRole()));
        model.addAttribute("totalFiles", totalFiles);
        model.addAttribute("totalFolders", totalFolders);
        model.addAttribute("storageUsedFormatted", formatBytes(totalBytes));
        model.addAttribute("storagePercent", storagePercent);
        model.addAttribute("storageLimitFormatted", "400 MB");        model.addAttribute("recentFiles", recentFiles);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("peopleCount", peopleCount);

        return "home";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }
}
