
CREATE TABLE file_metadata (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename    TEXT NOT NULL,
    size        BIGINT NOT NULL,
    checksum    VARCHAR(64),
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_file_status
       CHECK (status IN ('PENDING','UPLOADING','COMPLETED','FAILED'))
);

CREATE INDEX idx_file_filename   ON file_metadata(filename);
CREATE INDEX idx_file_checksum   ON file_metadata(checksum);
CREATE INDEX idx_file_created_at ON file_metadata(created_at DESC);