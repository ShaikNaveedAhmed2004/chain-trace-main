-- ======================================================
-- USERS TABLE
-- ======================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ======================================================
-- PRODUCTS TABLE
-- ======================================================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ======================================================
-- BATCHES TABLE
-- ======================================================
CREATE TABLE batches (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    batch_number VARCHAR(100) NOT NULL UNIQUE,
    quantity INTEGER NOT NULL,
    current_owner BIGINT NOT NULL,
    current_location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (current_owner) REFERENCES users(id)
);

-- ======================================================
-- SUPPLY CHAIN EVENTS TABLE
-- ======================================================
CREATE TABLE supply_chain_events (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    from_party BIGINT NOT NULL,
    to_party BIGINT NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    product_name VARCHAR(255) NOT NULL,  -- FIXED from 50 → 255
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tx_hash VARCHAR(255) NOT NULL,
    block_number BIGINT NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (batch_id) REFERENCES batches(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (from_party) REFERENCES users(id),
    FOREIGN KEY (to_party) REFERENCES users(id)
);

-- ======================================================
-- PAYMENTS TABLE
-- ======================================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    tx_hash VARCHAR(255) NOT NULL,
    released_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES batches(id)
);

-- ======================================================
-- INDEXES FOR PERFORMANCE
-- ======================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

CREATE INDEX idx_products_created_by ON products(created_by);

CREATE INDEX idx_batches_product_id ON batches(product_id);
CREATE INDEX idx_batches_current_owner ON batches(current_owner);
CREATE INDEX idx_batches_status ON batches(status);

CREATE INDEX idx_supply_chain_events_batch_id ON supply_chain_events(batch_id);
CREATE INDEX idx_supply_chain_events_product_id ON supply_chain_events(product_id);

CREATE INDEX idx_payments_batch_id ON payments(batch_id);
