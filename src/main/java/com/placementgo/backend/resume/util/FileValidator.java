package com.placementgo.backend.resume.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator {

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public static void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF or DOCX files are allowed");
        }
    }
}
