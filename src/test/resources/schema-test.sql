-- H2 인메모리 DB용 테스트 스키마

DROP TABLE IF EXISTS command_history;

CREATE TABLE command_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id UUID NOT NULL,
    command TEXT NOT NULL,
    response TEXT NOT NULL,
    intent VARCHAR(50),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_command_history_session_id ON command_history(session_id);
CREATE INDEX idx_command_history_timestamp ON command_history(session_id, timestamp DESC);
