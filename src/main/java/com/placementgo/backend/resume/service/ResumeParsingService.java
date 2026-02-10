package com.placementgo.backend.resume.service;


import com.placementgo.backend.resume.model.Resume;
import com.placementgo.backend.resume.model.ResumeStatus;
import com.placementgo.backend.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeParsingService {

    private final ResumeRepository resumeRepository;

    @Async
    public void parseAsync(UUID resumeId) {

        Resume resume = resumeRepository.findById(resumeId).orElseThrow();

        try {
            resume.setStatus(ResumeStatus.PARSING);
            resumeRepository.save(resume);

            // 🚧 TEMP: mock parsing result
            String parsedJson = """
            {
              "name": "John Doe",
              "skills": ["Java", "Spring Boot"]
            }
            """;

            resume.setParsedJson(parsedJson);
            resume.setStatus(ResumeStatus.PARSED);
            resumeRepository.save(resume);

        } catch (Exception e) {
            resume.setStatus(ResumeStatus.FAILED);
            resumeRepository.save(resume);
        }
    }
}
