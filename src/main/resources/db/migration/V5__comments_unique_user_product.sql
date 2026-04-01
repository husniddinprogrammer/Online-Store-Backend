ALTER TABLE comments
    DROP CONSTRAINT IF EXISTS uq_comments_user_product;

ALTER TABLE comments
    ADD CONSTRAINT uq_comments_user_product UNIQUE (user_id, product_id);
