CREATE TABLE referral_request (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    resume_id UUID NOT NULL,
    job_description TEXT NOT NULL,
    share_token VARCHAR(64) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE referral_template (
    id UUID PRIMARY KEY,
    referral_request_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    version INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_referral_request
        FOREIGN KEY (referral_request_id)
        REFERENCES referral_request(id)
        ON DELETE CASCADE
);
