CREATE TABLE chunk_completions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    upload_id    UUID NOT NULL REFERENCES uploads(upload_id),
    chunk_number INT NOT NULL,
    chunk_hash   VARCHAR(64) NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_upload_chunk UNIQUE (upload_id, chunk_number)
);

CREATE INDEX idx_chunk_completions_upload ON chunk_completions(upload_id);