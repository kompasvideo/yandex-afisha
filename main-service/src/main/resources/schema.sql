CREATE TABLE IF NOT EXISTS users
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT UQ_USER_EMAIL UNIQUE(email)
);

CREATE TABLE IF NOT EXISTS categories
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR (255) NOT NULL,
    CONSTRAINT UQ_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS compilations
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN NOT NULL,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS locations
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    lon FLOAT NOT NULL,
    lat FLOAT NOT NULL,
    radius FLOAT NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS events
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    category BIGINT NOT NULL,
    confirmed_requests BIGINT NOT NULL,
    created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    annotation VARCHAR(5000) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    initiator BIGINT NOT NULL,
    paid BOOLEAN NOT NULL,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL,
    participant_limit BIGINT NOT NULL,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN NOT NULL,
    state VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    views BIGINT NOT NULL,
    CONSTRAINT FK_CATEGORY FOREIGN KEY(category) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT FK_INITIATOR FOREIGN KEY(initiator) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    event BIGINT NOT NULL,
    requester BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT FK_REQUESTER_ID FOREIGN KEY(requester) REFERENCES users( id) ON DELETE CASCADE,
    CONSTRAINT FK_EVENT_ID FOREIGN KEY(event) REFERENCES events(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS compilations_events
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    event_id BIGINT NOT NULL,
    compilation_id BIGINT NOT NULL,
    CONSTRAINT FK_COMPILATION FOREIGN KEY(compilation_id) REFERENCES compilations( id) ON DELETE CASCADE,
    CONSTRAINT FK_EVENT FOREIGN KEY(event_id) REFERENCES events(id) ON DELETE CASCADE
);
