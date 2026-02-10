package com.placementgo.backend.resume.service;

import com.placementgo.backend.resume.model.Resume;
import com.placementgo.backend.resume.model.ResumeStatus;
import com.placementgo.backend.resume.util.FileValidator;
import com.placementgo.backend.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeParsingService parsingService;

    public Resume uploadResume(UUID userId, MultipartFile file) throws Exception {

        FileValidator.validate(file);
        Path uploadDir = Path.of("uploads");
        Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(UUID.randomUUID() + "_" + file.getOriginalFilename());
        Files.write(filePath, file.getBytes());

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setOriginalFileName(file.getOriginalFilename());
        resume.setStoredFilePath(filePath.toString());
        resume.setStatus(ResumeStatus.UPLOADED);
        resume.setCreatedAt(LocalDateTime.now());

        Resume savedResume = resumeRepository.save(resume);

        parsingService.parseAsync(savedResume.getId());

        return savedResume;
    }

    public Resume getResumeById(UUID resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
    }
}