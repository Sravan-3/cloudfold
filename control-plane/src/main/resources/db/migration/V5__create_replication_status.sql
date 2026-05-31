CREATE TABLE replication_status (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chunk_hash   VARCHAR(64)  NOT NULL,
    node_id      VARCHAR(255) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'HEALTHY',
    last_checked TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_chunk_node UNIQUE (chunk_hash, node_id),
    CONSTRAINT chk_replication_status
        CHECK (status IN ('HEALTHY', 'UNHEALTHY', 'PENDING'))
);

CREATE INDEX idx_replication_chunk ON replication_status(chunk_hash);

CREATE INDEX idx_replication_status ON replication_status(status);
