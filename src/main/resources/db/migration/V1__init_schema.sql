-- =============================================
-- Online Store Database Schema
-- Created: V1 - Initial Schema
-- =============================================

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id                              BIGSERIAL PRIMARY KEY,
    name                            VARCHAR(100) NOT NULL,
    surname                         VARCHAR(100) NOT NULL,
    email                           VARCHAR(255) NOT NULL UNIQUE,
    password                        VARCHAR(255) NOT NULL,
    birthday_at                     DATE,
    phone_number                    VARCHAR(20),
    balance                         NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    blocked                         BOOLEAN NOT NULL DEFAULT FALSE,
    role                            VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    last_login_at                   TIMESTAMP WITH TIME ZONE,
    created_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_deleted                      BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified                  BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token        VARCHAR(255),
    email_verification_token_expiry TIMESTAMP WITH TIME ZONE,
    password_reset_token            VARCHAR(255),
    password_reset_token_expiry     TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- REFRESH TOKENS
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(512) NOT NULL UNIQUE,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);

-- COMPANIES
CREATE TABLE IF NOT EXISTS companies (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    image_link TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- CATEGORIES
CREATE TABLE IF NOT EXISTS categories (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    image_link TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- PRODUCTS
CREATE TABLE IF NOT EXISTS products (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(300) NOT NULL,
    description      TEXT,
    discount_percent NUMERIC(5, 2) DEFAULT 0.00,
    stock_quantity   INTEGER NOT NULL DEFAULT 0,
    sold_quantity    INTEGER NOT NULL DEFAULT 0,
    category_id      BIGINT NOT NULL REFERENCES categories(id),
    company_id       BIGINT NOT NULL REFERENCES companies(id),
    arrival_price    NUMERIC(15, 2) NOT NULL,
    sell_price       NUMERIC(15, 2) NOT NULL,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE,
    created_by       BIGINT,
    updated_by       BIGINT
);

CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_company_id ON products(company_id);

-- PRODUCT IMAGES
CREATE TABLE IF NOT EXISTS product_images (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    image_link TEXT NOT NULL,
    is_main    BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON product_images(product_id);

-- ADDRESSES
CREATE TABLE IF NOT EXISTS addresses (
    id          BIGSERIAL PRIMARY KEY,
    region_type VARCHAR(30) NOT NULL,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    city_type   VARCHAR(40) NOT NULL,
    home_number VARCHAR(50),
    room_number VARCHAR(20),
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);

-- ORDERS
CREATE TABLE IF NOT EXISTS orders (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id),
    total_amount NUMERIC(15, 2) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,
    address_id   BIGINT REFERENCES addresses(id)
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- ORDER ITEMS
CREATE TABLE IF NOT EXISTS order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity   INTEGER NOT NULL,
    price      NUMERIC(15, 2) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- PAYMENTS
CREATE TABLE IF NOT EXISTS payments (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    amount     NUMERIC(15, 2) NOT NULL,
    method     VARCHAR(10) NOT NULL,
    status     VARCHAR(10) NOT NULL DEFAULT 'FAILED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- CARTS
CREATE TABLE IF NOT EXISTS carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL UNIQUE REFERENCES users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- CART ITEMS
CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT NOT NULL REFERENCES carts(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity   INTEGER NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);

-- FAVORITE PRODUCTS
CREATE TABLE IF NOT EXISTS favorite_products (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    user_id    BIGINT NOT NULL REFERENCES users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorite_products(user_id);

-- COMMENTS
CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    user_id    BIGINT NOT NULL REFERENCES users(id),
    text       TEXT NOT NULL,
    rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_comments_product_id ON comments(product_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);

-- NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGSERIAL PRIMARY KEY,
    type       VARCHAR(20) NOT NULL,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    text       TEXT NOT NULL,
    is_seen    BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);

-- POSTERS
CREATE TABLE IF NOT EXISTS posters (
    id             BIGSERIAL PRIMARY KEY,
    image_link     TEXT NOT NULL,
    click_quantity BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    link           VARCHAR(500),
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE
);

-- LOGS
CREATE TABLE IF NOT EXISTS logs (
    id           BIGSERIAL PRIMARY KEY,
    entity_name  VARCHAR(100) NOT NULL,
    entity_id    BIGINT NOT NULL,
    action       VARCHAR(10) NOT NULL,
    old_value    TEXT,
    new_value    TEXT,
    performed_by BIGINT REFERENCES users(id),
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_logs_entity ON logs(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_logs_performed_by ON logs(performed_by);
