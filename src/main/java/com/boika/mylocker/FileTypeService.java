package com.boika.mylocker;

import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class FileTypeService {

    private static final Set<String> ALLOWED = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "md", "rtf", "odt", "ods",
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic",
            "mp3", "wav", "m4a", "mp4", "mov", "avi", "mkv",
            "zip", "rar", "7z", "tar", "gz",
            "exe", "msi", "jar", "apk", "iso", "dmg"
    );

    private static final Set<String> BLOCKED = Set.of(
            "html", "htm", "svg", "js", "mjs", "xhtml", "xml", "swf", "jsp", "php"
    );

    public String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    public boolean isAllowed(String filename) {
        String ext = extensionOf(filename);
        if (ext.isEmpty()) {
            return false;
        }
        if (BLOCKED.contains(ext)) {
            return false;
        }
        return ALLOWED.contains(ext);
    }

    public String allowedListForDisplay() {
        return "documents, images, audio, video, archives and installers";
    }
}