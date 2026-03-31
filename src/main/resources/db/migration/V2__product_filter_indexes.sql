-- ============================================================
-- V2 — Product filter & sort indexes
-- Covers all query patterns for GET /api/products
-- Partial indexes (WHERE is_deleted = false) keep them small
-- and let the planner use them without extra filtering cost.
-- ============================================================

-- Sorting: newest (default)
CREATE INDEX IF NOT EXISTS idx_products_created_at
    ON products (created_at DESC)
    WHERE is_deleted = false;

-- Sorting: price ASC / DESC
CREATE INDEX IF NOT EXISTS idx_products_sell_price
    ON products (sell_price)
    WHERE is_deleted = false;

-- Sorting: discount DESC
CREATE INDEX IF NOT EXISTS idx_products_discount_percent
    ON products (discount_percent DESC)
    WHERE is_deleted = false;

-- Sorting: popular (most sold)
CREATE INDEX IF NOT EXISTS idx_products_sold_quantity
    ON products (sold_quantity DESC)
    WHERE is_deleted = false;

-- Composite: category + price — most common category-page query
CREATE INDEX IF NOT EXISTS idx_products_category_price
    ON products (category_id, sell_price)
    WHERE is_deleted = false;

-- Composite: company + price — brand/company filter page
CREATE INDEX IF NOT EXISTS idx_products_company_price
    ON products (company_id, sell_price)
    WHERE is_deleted = false;

-- Text search: case-insensitive LIKE on name
-- Requires pg_trgm extension (ships with PostgreSQL, not enabled by default)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_products_name_trgm
    ON products USING GIN (lower(name) gin_trgm_ops)
    WHERE is_deleted = false;
