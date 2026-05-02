CREATE TABLE uploads (
    upload_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id          UUID NOT NULL,
    status           VARCHAR(20) NOT NULL,
    total_chunks     INT NOT NULL,
    completed_chunks INT NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_file
    FOREIGN KEY (file_id) REFERENCES file_metadata(id),
    CONSTRAINT chk_upload_status
    CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','FAILED'))
);

CREATE INDEX idx_upload_id_status  ON uploads(upload_id, status);
CREATE INDEX idx_uploads_file_id   ON uploads(file_id);