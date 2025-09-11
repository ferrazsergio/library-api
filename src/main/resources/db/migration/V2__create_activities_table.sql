CREATE TABLE activities (
                            id BIGSERIAL PRIMARY KEY,
                            activity_type VARCHAR(50) NOT NULL,
                            description VARCHAR(500) NOT NULL,
                            timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
                            user_name VARCHAR(255),
                            book_title VARCHAR(255)
);

CREATE INDEX idx_activities_timestamp ON activities(timestamp);