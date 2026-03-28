#!/bin/bash
# ============================================================
# Online Store API — cURL Examples
# Usage: bash curl-examples.sh
# ============================================================

BASE="http://localhost:8080"
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiJ9..."   # Replace after login
REFRESH_TOKEN="550e8400-e29b-41d4-a716-446655440000"

# ─── AUTH ────────────────────────────────────────────────────

# Register
curl -s -X POST "$BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Husniddin",
    "surname": "Toshmatov",
    "email": "user@example.com",
    "password": "Password123",
    "phoneNumber": "+998901234567"
  }' | python3 -m json.tool

# Login
curl -s -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123"}' \
  | python3 -m json.tool

# Refresh token
curl -s -X POST "$BASE/api/auth/refresh-token" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" | python3 -m json.tool

# Logout
curl -s -X POST "$BASE/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"

# Verify email
curl -s "$BASE/api/auth/verify-email?token=uuid-token-here"

# Forgot password
curl -s -X POST "$BASE/api/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'

# Reset password
curl -s -X POST "$BASE/api/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{"token":"uuid-reset-token","newPassword":"NewPassword456"}'

# ─── USERS ───────────────────────────────────────────────────

# Get current user
curl -s "$BASE/api/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

# Update profile
curl -s -X PUT "$BASE/api/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Husniddin","surname":"Karimov","phoneNumber":"+998991234567"}'

# Change password
curl -s -X PUT "$BASE/api/users/me/change-password" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"Password123","newPassword":"NewPassword456"}'

# Get all users (admin)
curl -s "$BASE/api/users?search=ali&page=0&size=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

# Block user
curl -s -X PATCH "$BASE/api/users/5/block" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Unblock user
curl -s -X PATCH "$BASE/api/users/5/unblock" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Change role
curl -s -X PATCH "$BASE/api/users/5/role?role=ADMIN" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── PRODUCTS ────────────────────────────────────────────────

# Get products (public, with filters)
curl -s "$BASE/api/products?search=samsung&categoryId=1&minPrice=500000&maxPrice=5000000&page=0&size=20&sort=sellPrice,asc" \
  | python3 -m json.tool

# Get product by ID
curl -s "$BASE/api/products/1" | python3 -m json.tool

# Create product (admin)
curl -s -X POST "$BASE/api/products" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samsung Galaxy S24",
    "description": "Latest Samsung flagship",
    "discountPercent": 5.00,
    "stockQuantity": 30,
    "categoryId": 1,
    "companyId": 2,
    "arrivalPrice": 7000000.00,
    "sellPrice": 8500000.00
  }' | python3 -m json.tool

# Update product (admin)
curl -s -X PUT "$BASE/api/products/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samsung Galaxy S24 Ultra",
    "discountPercent": 10.00,
    "stockQuantity": 25,
    "categoryId": 1,
    "companyId": 2,
    "arrivalPrice": 9000000.00,
    "sellPrice": 11000000.00
  }'

# Delete product (admin)
curl -s -X DELETE "$BASE/api/products/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── CATEGORIES ──────────────────────────────────────────────

# Get all categories
curl -s "$BASE/api/categories?page=0&size=50" | python3 -m json.tool

# Create category (admin)
curl -s -X POST "$BASE/api/categories" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Smartphones","imageLink":"https://cdn.example.com/phones.png"}'

# Update category (admin)
curl -s -X PUT "$BASE/api/categories/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Mobile Phones","imageLink":"https://cdn.example.com/mobile.png"}'

# ─── COMPANIES ───────────────────────────────────────────────

curl -s "$BASE/api/companies" | python3 -m json.tool

curl -s -X POST "$BASE/api/companies" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Apple","imageLink":"https://cdn.example.com/apple.png"}'

# ─── CART ────────────────────────────────────────────────────

# View cart
curl -s "$BASE/api/carts" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

# Add to cart
curl -s -X POST "$BASE/api/carts/items" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}' | python3 -m json.tool

# Update cart item quantity
curl -s -X PATCH "$BASE/api/carts/items/5?quantity=3" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Remove from cart
curl -s -X DELETE "$BASE/api/carts/items/5" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Clear cart
curl -s -X DELETE "$BASE/api/carts" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── ADDRESSES ───────────────────────────────────────────────

# Get my addresses
curl -s "$BASE/api/addresses" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

# Create address
curl -s -X POST "$BASE/api/addresses" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "regionType": "TASHKENT_CITY",
    "cityType": "TASHKENT",
    "homeNumber": "45A",
    "roomNumber": "12"
  }' | python3 -m json.tool

# Update address
curl -s -X PUT "$BASE/api/addresses/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"regionType":"ANDIJAN","cityType":"ANDIJAN","homeNumber":"10","roomNumber":"5"}'

# Delete address
curl -s -X DELETE "$BASE/api/addresses/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── ORDERS ──────────────────────────────────────────────────

# Create order from cart
curl -s -X POST "$BASE/api/orders" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"addressId":1}' | python3 -m json.tool

# Get my orders
curl -s "$BASE/api/orders/my?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

# Get my orders filtered by status
curl -s "$BASE/api/orders/my?status=PENDING" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Get specific order
curl -s "$BASE/api/orders/my/10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

# Cancel order
curl -s -X PATCH "$BASE/api/orders/my/10/cancel" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Admin: get all orders
curl -s "$BASE/api/orders?status=PAID&page=0&size=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Admin: update order status
curl -s -X PATCH "$BASE/api/orders/10/status?status=SHIPPED" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── PAYMENTS ────────────────────────────────────────────────

# Pay for order
curl -s -X POST "$BASE/api/payments" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"orderId":10,"method":"CARD"}' | python3 -m json.tool

# Get payment by order
curl -s "$BASE/api/payments/order/10" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── COMMENTS ────────────────────────────────────────────────

# Get product reviews (public)
curl -s "$BASE/api/comments/product/1?page=0&size=10&sort=createdAt,desc" \
  | python3 -m json.tool

# Write review
curl -s -X POST "$BASE/api/comments" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"text":"Excellent product, highly recommend!","rating":5}'

# Update review
curl -s -X PUT "$BASE/api/comments/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"text":"Updated review text","rating":4}'

# Delete review
curl -s -X DELETE "$BASE/api/comments/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── FAVORITES ───────────────────────────────────────────────

# Get favorites
curl -s "$BASE/api/favorite-products?page=0&size=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Add to favorites
curl -s -X POST "$BASE/api/favorite-products/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Remove from favorites
curl -s -X DELETE "$BASE/api/favorite-products/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── NOTIFICATIONS ───────────────────────────────────────────

# Get notifications
curl -s "$BASE/api/notifications?page=0&size=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

# Unseen count
curl -s "$BASE/api/notifications/unseen-count" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Mark one as seen
curl -s -X PATCH "$BASE/api/notifications/1/seen" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Mark all as seen
curl -s -X PATCH "$BASE/api/notifications/seen-all" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# ─── POSTERS ─────────────────────────────────────────────────

# Get all posters (public)
curl -s "$BASE/api/posters" | python3 -m json.tool

# Track click
curl -s -X POST "$BASE/api/posters/1/click"

# Create poster (admin)
curl -s -X POST "$BASE/api/posters" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"imageLink":"https://cdn.example.com/banner.jpg","link":"https://example.com/sale"}'

# ─── PRODUCT IMAGES ──────────────────────────────────────────

# Get product images (public)
curl -s "$BASE/api/product-images/product/1"

# Add image (admin)
curl -s -X POST "$BASE/api/product-images/product/1?imageLink=https://cdn.example.com/img1.jpg&isMain=true" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Set as main
curl -s -X PATCH "$BASE/api/product-images/3/main" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Delete image
curl -s -X DELETE "$BASE/api/product-images/3" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

echo "Done!"
