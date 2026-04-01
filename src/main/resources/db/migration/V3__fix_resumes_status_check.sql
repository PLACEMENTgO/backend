-- Expand the resumes status check constraint to include all ResumeStatus enum values.
ALTER TABLE resumes DROP CONSTRAINT IF EXISTS resumes_status_check;

ALTER TABLE resumes
    ADD CONSTRAINT resumes_status_check
        CHECK (status IN (
            'UPLOADED',
            'PARSING',
            'PARSED',
            'GENERATING_LATEX',
            'GENERATED',
            'COMPILING_PDF',
            'PDF_GENERATED',
            'FAILED'
        ));
