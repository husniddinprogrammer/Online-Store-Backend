# Online Store — Full API Documentation

> **Base URL:** `http://localhost:8080`
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`
> **OpenAPI JSON:** `http://localhost:8080/api-docs`
> **All dates/times:** ISO-8601, UTC
> **All image URLs:** absolute `http://…` URLs served from `/uploads/**`

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Users](#2-users)
3. [Addresses](#3-addresses)
4. [Categories](#4-categories)
5. [Companies](#5-companies)
6. [Products](#6-products)
7. [Product Images](#7-product-images)
8. [Cart](#8-cart)
9. [Orders](#9-orders)
10. [Payments](#10-payments)
11. [Comments & Reviews](#11-comments--reviews)
12. [Favorites](#12-favorites)
13. [Notifications](#13-notifications)
14. [Posters](#14-posters)
15. [Common Patterns](#15-common-patterns)
16. [Swagger / OpenAPI Spec](#16-swagger--openapi-spec)

---

## General Response Envelope

Every response is wrapped:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... }
}
```

For paginated endpoints `data` is:

```json
{
  "content": [ ... ],
  "totalElements": 120,
  "totalPages": 6,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

### Common Error Responses

| Status | When |
|--------|------|
| `400` | Validation failure / bad request body |
| `401` | Missing or expired JWT token |
| `403` | Authenticated but insufficient role |
| `404` | Resource not found |
| `409` | Conflict (duplicate email, already in favorites, etc.) |
| `500` | Unexpected server error |

Error body:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": null
}
```

---

## Authentication Header

Protected endpoints require:
```
Authorization: Bearer <accessToken>
```

---

## 1. Authentication

### POST /api/auth/register
Register a new user account. Sends a verification email.

**Auth:** Public

**Request:**
```json
{
  "name": "John",
  "surname": "Doe",
  "email": "john@example.com",
  "password": "Secret123",
  "birthdayAt": "1995-06-15",
  "phoneNumber": "+998901234567"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `name` | string | ✅ | 2–100 chars |
| `surname` | string | ✅ | 2–100 chars |
| `email` | string | ✅ | valid email |
| `password` | string | ✅ | min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit |
| `birthdayAt` | date (YYYY-MM-DD) | ❌ | must be in the past |
| `phoneNumber` | string | ❌ | `+?[0-9]{9,15}` |

**Response `201`:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "d8f3a...",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "userId": 1,
    "email": "john@example.com",
    "name": "John",
    "role": "CUSTOMER"
  }
}
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John","surname":"Doe","email":"john@example.com","password":"Secret123"}'
```

**axios:**
```js
const { data } = await axios.post('/api/auth/register', {
  name: 'John', surname: 'Doe',
  email: 'john@example.com', password: 'Secret123'
});
const token = data.data.accessToken;
```

---

### POST /api/auth/login

**Auth:** Public

**Request:**
```json
{
  "email": "john@example.com",
  "password": "Secret123"
}
```

**Response `200`:** Same as register response.

**curl:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Secret123"}'
```

**Errors:** `401` — wrong credentials or account blocked.

---

### POST /api/auth/refresh-token

**Auth:** Public

**Request:**
```json
{
  "refreshToken": "d8f3a..."
}
```

**Response `200`:** New `AuthResponse` with fresh `accessToken` and `refreshToken`.

**Errors:** `401` — token expired or invalid.

---

### POST /api/auth/logout

**Auth:** Public (sends refresh token)

**Request:**
```json
{
  "refreshToken": "d8f3a..."
}
```

**Response `200`:**
```json
{ "success": true, "message": "Logged out successfully", "data": null }
```

---

### GET /api/auth/verify-email?token={token}

Verify email after registration.

**Auth:** Public

**Query Params:**

| Param | Required |
|-------|----------|
| `token` | ✅ |

**Response `200`:**
```json
{ "success": true, "message": "Email verified successfully", "data": null }
```

---

### POST /api/auth/forgot-password

**Auth:** Public

**Request:**
```json
{ "email": "john@example.com" }
```

**Response `200`:**
```json
{ "success": true, "message": "Password reset link sent to your email", "data": null }
```

---

### POST /api/auth/reset-password

**Auth:** Public

**Request:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "NewPass456"
}
```

**Response `200`:**
```json
{ "success": true, "message": "Password reset successfully", "data": null }
```

---

## 2. Users

All endpoints require `Authorization: Bearer <token>`.

### GET /api/users/me

Get the currently authenticated user's profile.

**Auth:** Any authenticated user

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com",
    "birthdayAt": "1995-06-15",
    "phoneNumber": "+998901234567",
    "balance": 150000.00,
    "blocked": false,
    "role": "CUSTOMER",
    "lastLoginAt": "2026-03-29T10:00:00",
    "createdAt": "2026-01-01T09:00:00",
    "emailVerified": true
  }
}
```

---

### PUT /api/users/me

Update current user's profile.

**Auth:** Any authenticated user

**Request:**
```json
{
  "name": "John",
  "surname": "Smith",
  "birthdayAt": "1995-06-15",
  "phoneNumber": "+998901234567"
}
```

**Response `200`:** Updated `UserResponse`.

---

### PUT /api/users/me/change-password

**Auth:** Any authenticated user

**Request:**
```json
{
  "oldPassword": "Secret123",
  "newPassword": "NewPass456"
}
```

**Response `200`:**
```json
{ "success": true, "message": "Password changed successfully", "data": null }
```

**Errors:** `400` — old password incorrect.

---

### GET /api/users

Get all users with optional search.

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Query Params:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `search` | string | ❌ | Filter by name, surname, or email |
| `page` | int | ❌ | Default 0 |
| `size` | int | ❌ | Default 20 |
| `sort` | string | ❌ | e.g. `createdAt,desc` |

**Response `200`:** Paginated `UserResponse`.

**axios:**
```js
const { data } = await axios.get('/api/users', {
  params: { search: 'john', page: 0, size: 20 },
  headers: { Authorization: `Bearer ${token}` }
});
```

---

### GET /api/users/{id}

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:** Single `UserResponse`.

---

### PATCH /api/users/{id}/block

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:** Updated `UserResponse` with `blocked: true`.

---

### PATCH /api/users/{id}/unblock

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:** Updated `UserResponse` with `blocked: false`.

---

### PATCH /api/users/{id}/role?role={ROLE}

Change user role.

**Auth:** `SUPER_ADMIN` only

**Query Params:**

| Param | Values |
|-------|--------|
| `role` | `CUSTOMER`, `ADMIN`, `DELIVERY`, `SUPER_ADMIN` |

**Response `200`:** Updated `UserResponse`.

---

### DELETE /api/users/{id}

Soft-delete a user.

**Auth:** `SUPER_ADMIN` only

**Response `200`:**
```json
{ "success": true, "message": "User deleted", "data": null }
```

---

## 3. Addresses

All endpoints require JWT.

### GET /api/addresses

Get the current user's saved addresses.

**Auth:** Any authenticated user

**Response `200`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "regionType": "TASHKENT_CITY",
      "cityType": "TASHKENT",
      "homeNumber": "12A",
      "roomNumber": "5",
      "createdAt": "2026-03-01T10:00:00"
    }
  ]
}
```

---

### POST /api/addresses

**Auth:** Any authenticated user

**Request:**
```json
{
  "regionType": "TASHKENT_CITY",
  "cityType": "TASHKENT",
  "homeNumber": "12A",
  "roomNumber": "5"
}
```

> **`regionType` values:** `TASHKENT_CITY`, `TASHKENT_REGION`, `ANDIJAN`, `FERGANA`, `NAMANGAN`, `SAMARKAND`, `BUKHARA`, `NAVOI`, `KASHKADARYA`, `SURKHANDARYA`, `JIZZAKH`, `SIRDARYA`, `KHOREZM`, `KARAKALPAKSTAN`

**Response `201`:** Created `AddressResponse`.

---

### PUT /api/addresses/{id}

**Auth:** Any authenticated user (owns the address)

**Request:** Same as POST.

**Response `200`:** Updated `AddressResponse`.

---

### DELETE /api/addresses/{id}

**Auth:** Any authenticated user (owns the address)

**Response `200`:**
```json
{ "success": true, "message": "Address deleted", "data": null }
```

---

## 4. Categories

### GET /api/categories

**Auth:** Public

**Query Params:** Pagination (`page`, `size`, `sort`)

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "name": "Electronics", "imageLink": "http://localhost:8080/uploads/categories/abc.jpg" }
    ],
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### GET /api/categories/{id}

**Auth:** Public

**Response `200`:** Single `CategoryResponse`.

---

### POST /api/categories
**Content-Type:** `multipart/form-data`

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Form Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `data` | JSON part | ✅ | `Content-Type: application/json` on this part |
| `image` | file | ❌ | jpg/jpeg/png, max 3MB; resized to 800×800 at 70% quality |

> ⚠️ The `data` part must be sent with explicit `Content-Type: application/json`.

**`data` part JSON:**
```json
{ "name": "Electronics" }
```

**Response `201`:**
```json
{
  "success": true,
  "message": "Category created",
  "data": { "id": 3, "name": "Electronics", "imageLink": "http://localhost:8080/uploads/categories/uuid.jpg" }
}
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"name":"Electronics"};type=application/json' \
  -F 'image=@/path/to/image.jpg'
```

**axios:**
```js
const form = new FormData();
form.append('data', new Blob([JSON.stringify({ name: 'Electronics' })], { type: 'application/json' }));
form.append('image', file); // File object from input

await axios.post('/api/categories', form, {
  headers: { Authorization: `Bearer ${token}` }
});
```

---

### PUT /api/categories/{id}
**Content-Type:** `multipart/form-data`

**Auth:** `SUPER_ADMIN`, `ADMIN`

Same form fields as POST. Omit `image` to keep the existing image.

**Response `200`:** Updated `CategoryResponse`.

---

### DELETE /api/categories/{id}

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:**
```json
{ "success": true, "message": "Category deleted", "data": null }
```

---

## 5. Companies

Same pattern as Categories.

### GET /api/companies

**Auth:** Public  **Query:** Pagination

**Response `200`:** Paginated `CompanyResponse`:
```json
{ "id": 1, "name": "Samsung", "imageLink": "http://localhost:8080/uploads/companies/abc.jpg" }
```

---

### GET /api/companies/{id}

**Auth:** Public

---

### POST /api/companies
**Content-Type:** `multipart/form-data`  **Auth:** `SUPER_ADMIN`, `ADMIN`

| Field | Type | Required |
|-------|------|----------|
| `data` | JSON part (`Content-Type: application/json`) | ✅ |
| `image` | file (jpg/jpeg/png, max 3MB) | ❌ |

**`data` JSON:**
```json
{ "name": "Samsung" }
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/companies \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"name":"Samsung"};type=application/json' \
  -F 'image=@/path/samsung.jpg'
```

**Response `201`:** Created `CompanyResponse`.

---

### PUT /api/companies/{id}
**Content-Type:** `multipart/form-data`  **Auth:** `SUPER_ADMIN`, `ADMIN`

Same as POST. Omit `image` to keep existing.

---

### DELETE /api/companies/{id}

**Auth:** `SUPER_ADMIN`, `ADMIN`

---

## 6. Products

### GET /api/products

**Auth:** Public

**Query Params:**

| Param | Type | Description |
|-------|------|-------------|
| `search` | string | Filter by product name |
| `categoryId` | long | Filter by category |
| `companyId` | long | Filter by company |
| `minPrice` | decimal | Min sell price |
| `maxPrice` | decimal | Max sell price |
| `page` | int | Default 0 |
| `size` | int | Default 20 |
| `sort` | string | Default `createdAt,desc` |

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "iPhone 15",
        "description": "Apple smartphone",
        "discountPercent": 10.00,
        "stockQuantity": 50,
        "soldQuantity": 120,
        "category": { "id": 1, "name": "Electronics", "imageLink": "..." },
        "company": { "id": 2, "name": "Apple", "imageLink": "..." },
        "arrivalPrice": 900.00,
        "sellPrice": 1100.00,
        "discountedPrice": 990.00,
        "createdAt": "2026-01-10T09:00:00",
        "updatedAt": "2026-03-01T10:00:00",
        "images": [
          { "id": 1, "imageLink": "http://localhost:8080/uploads/products/img1.jpg", "isMain": true },
          { "id": 2, "imageLink": "http://localhost:8080/uploads/products/img2.jpg", "isMain": false }
        ],
        "averageRating": 4.5
      }
    ],
    "totalElements": 85,
    "totalPages": 5
  }
}
```

**axios:**
```js
const { data } = await axios.get('/api/products', {
  params: {
    search: 'iphone',
    categoryId: 1,
    minPrice: 500,
    maxPrice: 2000,
    page: 0,
    size: 20,
    sort: 'createdAt,desc'
  }
});
```

---

### GET /api/products/{id}

**Auth:** Public

**Response `200`:** Single `ProductResponse` (same structure as above).

---

### POST /api/products

**Auth:** `SUPER_ADMIN`, `ADMIN`
**Content-Type:** `application/json`

**Request:**
```json
{
  "name": "iPhone 15",
  "description": "Apple flagship smartphone",
  "discountPercent": 10.0,
  "stockQuantity": 50,
  "categoryId": 1,
  "companyId": 2,
  "arrivalPrice": 900.00,
  "sellPrice": 1100.00
}
```

| Field | Required | Rules |
|-------|----------|-------|
| `name` | ✅ | max 300 chars |
| `description` | ❌ | |
| `discountPercent` | ❌ | 0.0–100.0 |
| `stockQuantity` | ✅ | ≥ 0 |
| `categoryId` | ✅ | |
| `companyId` | ✅ | |
| `arrivalPrice` | ✅ | > 0 |
| `sellPrice` | ✅ | > 0 |

**Response `201`:** Created `ProductResponse`.

**curl:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","stockQuantity":50,"categoryId":1,"companyId":2,"arrivalPrice":900,"sellPrice":1100}'
```

---

### PUT /api/products/{id}

**Auth:** `SUPER_ADMIN`, `ADMIN`
Same body as POST.

**Response `200`:** Updated `ProductResponse`.

---

### DELETE /api/products/{id}

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:**
```json
{ "success": true, "message": "Product deleted", "data": null }
```

---

## 7. Product Images

### GET /api/product-images/product/{productId}

Get all images for a product.

**Auth:** Public

**Response `200`:**
```json
{
  "success": true,
  "data": [
    { "id": 1, "imageLink": "http://localhost:8080/uploads/products/abc.jpg", "isMain": true },
    { "id": 2, "imageLink": "http://localhost:8080/uploads/products/def.jpg", "isMain": false }
  ]
}
```

---

### POST /api/product-images/upload/{productId}
**Content-Type:** `multipart/form-data`

Upload one or more product images. Each file is validated, resized to 800×800, and saved at 70% quality.

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Form Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `files` | file[] | ✅ | jpg/jpeg/png, max 3MB each; multiple allowed |
| `firstIsMain` | boolean | ❌ | Default `true` — first file becomes the main image |

**Response `201`:**
```json
{
  "success": true,
  "message": "Images uploaded",
  "data": [
    { "id": 5, "imageLink": "http://localhost:8080/uploads/products/abc.jpg", "isMain": true },
    { "id": 6, "imageLink": "http://localhost:8080/uploads/products/def.jpg", "isMain": false }
  ]
}
```

**curl:**
```bash
curl -X POST "http://localhost:8080/api/product-images/upload/1" \
  -H "Authorization: Bearer $TOKEN" \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg" \
  -F "firstIsMain=true"
```

**axios:**
```js
const form = new FormData();
files.forEach(f => form.append('files', f));
form.append('firstIsMain', 'true');

const { data } = await axios.post(`/api/product-images/upload/${productId}`, form, {
  headers: { Authorization: `Bearer ${token}` }
});
```

**Errors:**
- `400` — no files provided, file too large (>3MB), or unsupported type

---

### POST /api/product-images/product/{productId}

Add image by URL (no upload, no resize).

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Query Params:**

| Param | Required |
|-------|----------|
| `imageLink` | ✅ |
| `isMain` | ❌ (default `false`) |

**Response `201`:** Single `ProductImageResponse`.

---

### PATCH /api/product-images/{imageId}/main

Set an image as the main product image.

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:**
```json
{ "success": true, "message": "Main image updated", "data": null }
```

---

### DELETE /api/product-images/{imageId}

Soft-delete a product image.

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:**
```json
{ "success": true, "message": "Image deleted", "data": null }
```

---

## 8. Cart

All endpoints require JWT.

### GET /api/carts

Get the current user's cart.

**Auth:** Any authenticated user

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "items": [
      {
        "id": 10,
        "productId": 3,
        "productName": "iPhone 15",
        "productImageLink": "http://localhost:8080/uploads/products/abc.jpg",
        "productPrice": 990.00,
        "quantity": 2,
        "totalPrice": 1980.00
      }
    ],
    "totalAmount": 1980.00,
    "totalItems": 2
  }
}
```

---

### POST /api/carts/items

Add item to cart (or increase quantity if already in cart).

**Auth:** Any authenticated user

**Request:**
```json
{
  "productId": 3,
  "quantity": 2
}
```

**Response `200`:** Updated full `CartResponse`.

**axios:**
```js
const { data } = await axios.post('/api/carts/items',
  { productId: 3, quantity: 2 },
  { headers: { Authorization: `Bearer ${token}` } }
);
```

---

### PATCH /api/carts/items/{cartItemId}?quantity={n}

Update the quantity of a specific cart item.

**Auth:** Any authenticated user

**Query Params:** `quantity` (int, ≥ 1)

**Response `200`:** Updated `CartResponse`.

---

### DELETE /api/carts/items/{cartItemId}

Remove one item from cart.

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "message": "Item removed from cart", "data": null }
```

---

### DELETE /api/carts

Clear all items from the cart.

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "message": "Cart cleared", "data": null }
```

---

## 9. Orders

All endpoints require JWT.

### POST /api/orders

Create an order from the current cart contents.

**Auth:** Any authenticated user

**Request:**
```json
{
  "addressId": 1,
  "note": "Please leave at the door"
}
```

**Response `201`:**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 42,
    "userId": 1,
    "userName": "John Doe",
    "totalAmount": 1980.00,
    "status": "PENDING",
    "createdAt": "2026-03-29T12:00:00",
    "deliveryAddress": {
      "id": 1,
      "regionType": "TASHKENT_CITY",
      "cityType": "TASHKENT",
      "homeNumber": "12A",
      "roomNumber": "5"
    },
    "items": [
      {
        "id": 1,
        "productId": 3,
        "productName": "iPhone 15",
        "productImageLink": "http://localhost:8080/uploads/products/abc.jpg",
        "quantity": 2,
        "price": 990.00,
        "totalPrice": 1980.00
      }
    ]
  }
}
```

**Errors:** `400` — cart is empty; `404` — address not found.

---

### GET /api/orders/my

Get the current user's order history.

**Auth:** Any authenticated user

**Query Params:**

| Param | Type | Description |
|-------|------|-------------|
| `status` | enum | `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| `page` | int | Default 0 |
| `size` | int | Default 20 |

**Response `200`:** Paginated `OrderResponse`.

---

### GET /api/orders/my/{id}

Get a specific order by ID (must belong to current user).

**Auth:** Any authenticated user

**Response `200`:** Single `OrderResponse`.

---

### PATCH /api/orders/my/{id}/cancel

Cancel an order (only if status is `PENDING`).

**Auth:** Any authenticated user (owns the order)

**Response `200`:** Updated `OrderResponse` with `status: "CANCELLED"`.

---

### GET /api/orders

Get all orders (admin view).

**Auth:** `SUPER_ADMIN`, `ADMIN`, `DELIVERY`

**Query Params:** `status`, `page`, `size`

**Response `200`:** Paginated `OrderResponse`.

---

### PATCH /api/orders/{id}/status?status={STATUS}

Update order status.

**Auth:** `SUPER_ADMIN`, `ADMIN`, `DELIVERY`

**Query Params:**

| Param | Values |
|-------|--------|
| `status` | `PENDING` → `PAID` → `SHIPPED` → `DELIVERED` \| `CANCELLED` |

**Response `200`:** Updated `OrderResponse`.

**curl:**
```bash
curl -X PATCH "http://localhost:8080/api/orders/42/status?status=SHIPPED" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 10. Payments

All endpoints require JWT.

### POST /api/payments

Process payment for an order.

**Auth:** Any authenticated user

**Request:**
```json
{
  "orderId": 42,
  "method": "CARD"
}
```

> **`method` values:** `CARD`, `CASH`

**Response `200`:**
```json
{
  "success": true,
  "message": "Payment processed",
  "data": {
    "id": 7,
    "orderId": 42,
    "amount": 1980.00,
    "method": "CARD",
    "status": "PAID",
    "createdAt": "2026-03-29T12:05:00"
  }
}
```

> **`status` values:** `PAID`, `FAILED`

---

### GET /api/payments/order/{orderId}

Get payment details for an order.

**Auth:** Any authenticated user

**Response `200`:** Single `PaymentResponse`.

---

## 11. Comments & Reviews

### GET /api/comments/product/{productId}

Get paginated reviews for a product.

**Auth:** Public

**Query Params:** `page`, `size` (default 10, sorted newest first)

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5,
        "productId": 3,
        "userId": 1,
        "userName": "John",
        "userSurname": "Doe",
        "text": "Great product, fast delivery!",
        "rating": 5,
        "createdAt": "2026-03-20T15:00:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 3
  }
}
```

---

### POST /api/comments

Write a review.

**Auth:** Any authenticated user

**Request:**
```json
{
  "productId": 3,
  "text": "Excellent quality!",
  "rating": 5
}
```

| Field | Required | Rules |
|-------|----------|-------|
| `productId` | ✅ | |
| `text` | ✅ | 1–2000 chars |
| `rating` | ✅ | 1–5 |

**Response `201`:** Created `CommentResponse`.

---

### PUT /api/comments/{id}

Update your own review.

**Auth:** Any authenticated user (owns the comment)

**Request:** Same as POST (without `productId`).

**Response `200`:** Updated `CommentResponse`.

---

### DELETE /api/comments/{id}

Delete your own review.

**Auth:** Any authenticated user (owns the comment)

**Response `200`:**
```json
{ "success": true, "message": "Review deleted", "data": null }
```

---

## 12. Favorites

All endpoints require JWT.

### GET /api/favorite-products

Get current user's favorite products.

**Auth:** Any authenticated user

**Query Params:** Pagination

**Response `200`:** Paginated `ProductResponse` (same structure as product listing).

---

### POST /api/favorite-products/{productId}

Add a product to favorites.

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "message": "Added to favorites", "data": null }
```

**Errors:** `409` — already in favorites.

---

### DELETE /api/favorite-products/{productId}

Remove a product from favorites.

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "message": "Removed from favorites", "data": null }
```

---

## 13. Notifications

All endpoints require JWT.

### GET /api/notifications

Get current user's notifications (newest first).

**Auth:** Any authenticated user

**Query Params:** `page`, `size` (default 20)

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 10,
        "type": "INFO",
        "text": "Your order #42 has been shipped.",
        "isSeen": false
      }
    ],
    "totalElements": 5,
    "totalPages": 1
  }
}
```

> **`type` values:** `INFO`, `WARNING`, `ERROR`

---

### GET /api/notifications/unseen-count

Get the count of unread notifications (useful for badge indicator).

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "data": 3 }
```

---

### PATCH /api/notifications/{id}/seen

Mark a single notification as seen.

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "message": "Marked as seen", "data": null }
```

---

### PATCH /api/notifications/seen-all

Mark all notifications as seen.

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "message": "All marked as seen", "data": null }
```

---

### DELETE /api/notifications/{id}

Delete a notification.

**Auth:** Any authenticated user

**Response `200`:**
```json
{ "success": true, "message": "Notification deleted", "data": null }
```

---

## 14. Posters

### GET /api/posters

Get all posters (banner/slider).

**Auth:** Public

**Query Params:** Pagination

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "imageLink": "http://localhost:8080/uploads/posters/abc.jpg",
        "clickQuantity": 245,
        "link": "https://example.com/sale",
        "createdAt": "2026-03-01T09:00:00"
      }
    ],
    "totalElements": 4,
    "totalPages": 1
  }
}
```

---

### POST /api/posters/{id}/click

Track a poster click (increment `clickQuantity`).

**Auth:** Public

**Response `200`:** Updated `PosterResponse`.

---

### POST /api/posters
**Content-Type:** `multipart/form-data`

Create a new poster. Image is center-cropped to 16:9 and resized to **1280×720** at 90% quality.

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Form Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `image` | file | ✅ | jpg/jpeg/png, max 3MB; output: 1280×720 jpg at 90% quality |
| `link` | string | ❌ | URL to open when poster is clicked |

**Response `201`:**
```json
{
  "success": true,
  "message": "Poster created",
  "data": {
    "id": 3,
    "imageLink": "http://localhost:8080/uploads/posters/uuid.jpg",
    "clickQuantity": 0,
    "link": "https://example.com/summer-sale",
    "createdAt": "2026-03-29T12:00:00"
  }
}
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/posters \
  -H "Authorization: Bearer $TOKEN" \
  -F "image=@/path/to/banner.jpg" \
  -F "link=https://example.com/summer-sale"
```

**axios:**
```js
const form = new FormData();
form.append('image', file);          // File from <input type="file">
form.append('link', 'https://...');  // optional

const { data } = await axios.post('/api/posters', form, {
  headers: { Authorization: `Bearer ${token}` }
});
```

---

### PUT /api/posters/{id}
**Content-Type:** `multipart/form-data`

Update poster. Omit `image` to keep existing image.

**Auth:** `SUPER_ADMIN`, `ADMIN`

| Field | Type | Required |
|-------|------|----------|
| `image` | file | ❌ (omit = keep existing) |
| `link` | string | ❌ |

**Response `200`:** Updated `PosterResponse`.

---

### DELETE /api/posters/{id}

**Auth:** `SUPER_ADMIN`, `ADMIN`

**Response `200`:**
```json
{ "success": true, "message": "Poster deleted", "data": null }
```

---

## 15. Common Patterns

### Pagination

All paginated endpoints accept:

| Param | Default | Example |
|-------|---------|---------|
| `page` | `0` | `?page=2` |
| `size` | `20` | `?size=10` |
| `sort` | varies | `?sort=createdAt,desc` |

### Image Upload Rules

| Entity | Endpoint | Max Size | Output |
|--------|----------|----------|--------|
| Product images | `/api/product-images/upload/{id}` | 3MB per file | 800×800, 70% quality |
| Category image | `/api/categories` (POST/PUT) | 3MB | 800×800, 70% quality |
| Company image | `/api/companies` (POST/PUT) | 3MB | 800×800, 70% quality |
| Poster image | `/api/posters` (POST/PUT) | 3MB | **1280×720** (16:9 center-crop), 90% quality |

### Role Hierarchy

| Role | Can do |
|------|--------|
| `CUSTOMER` | Browse, cart, orders, comments, favorites, notifications |
| `DELIVERY` | View all orders, update order status |
| `ADMIN` | Everything CUSTOMER + manage products, categories, companies, posters |
| `SUPER_ADMIN` | Everything ADMIN + manage users (block/delete/role changes) |

### Setting up axios globally

```js
// api.js
import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080' });

api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  res => res,
  async err => {
    if (err.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      try {
        const { data } = await axios.post('/api/auth/refresh-token', { refreshToken });
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
        err.config.headers.Authorization = `Bearer ${data.data.accessToken}`;
        return api(err.config);
      } catch {
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

export default api;
```

---

## 16. Swagger / OpenAPI Spec

Below is the full OpenAPI 3.0 spec for integration with Swagger UI, Postman, or code generators.

```yaml
openapi: "3.0.3"
info:
  title: Online Store API
  version: "1.0.0"
  description: Production-grade e-commerce REST API

servers:
  - url: http://localhost:8080
    description: Local development

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    ApiResponse:
      type: object
      properties:
        success: { type: boolean }
        message: { type: string }
        data: {}

    AuthResponse:
      type: object
      properties:
        accessToken: { type: string }
        refreshToken: { type: string }
        tokenType: { type: string, example: Bearer }
        expiresIn: { type: integer, example: 900000 }
        userId: { type: integer }
        email: { type: string }
        name: { type: string }
        role:
          type: string
          enum: [CUSTOMER, ADMIN, DELIVERY, SUPER_ADMIN]

    UserResponse:
      type: object
      properties:
        id: { type: integer }
        name: { type: string }
        surname: { type: string }
        email: { type: string }
        birthdayAt: { type: string, format: date }
        phoneNumber: { type: string }
        balance: { type: number }
        blocked: { type: boolean }
        role: { type: string, enum: [CUSTOMER, ADMIN, DELIVERY, SUPER_ADMIN] }
        lastLoginAt: { type: string, format: date-time }
        createdAt: { type: string, format: date-time }
        emailVerified: { type: boolean }

    CategoryResponse:
      type: object
      properties:
        id: { type: integer }
        name: { type: string }
        imageLink: { type: string, format: uri }

    CompanyResponse:
      type: object
      properties:
        id: { type: integer }
        name: { type: string }
        imageLink: { type: string, format: uri }

    ProductImageResponse:
      type: object
      properties:
        id: { type: integer }
        imageLink: { type: string, format: uri }
        isMain: { type: boolean }

    ProductResponse:
      type: object
      properties:
        id: { type: integer }
        name: { type: string }
        description: { type: string }
        discountPercent: { type: number }
        stockQuantity: { type: integer }
        soldQuantity: { type: integer }
        category: { $ref: '#/components/schemas/CategoryResponse' }
        company: { $ref: '#/components/schemas/CompanyResponse' }
        arrivalPrice: { type: number }
        sellPrice: { type: number }
        discountedPrice: { type: number }
        createdAt: { type: string, format: date-time }
        updatedAt: { type: string, format: date-time }
        images:
          type: array
          items: { $ref: '#/components/schemas/ProductImageResponse' }
        averageRating: { type: number }

    CartItemResponse:
      type: object
      properties:
        id: { type: integer }
        productId: { type: integer }
        productName: { type: string }
        productImageLink: { type: string, format: uri }
        productPrice: { type: number }
        quantity: { type: integer }
        totalPrice: { type: number }

    CartResponse:
      type: object
      properties:
        id: { type: integer }
        items:
          type: array
          items: { $ref: '#/components/schemas/CartItemResponse' }
        totalAmount: { type: number }
        totalItems: { type: integer }

    AddressResponse:
      type: object
      properties:
        id: { type: integer }
        regionType: { type: string }
        cityType: { type: string }
        homeNumber: { type: string }
        roomNumber: { type: string }
        createdAt: { type: string, format: date-time }

    OrderItemResponse:
      type: object
      properties:
        id: { type: integer }
        productId: { type: integer }
        productName: { type: string }
        productImageLink: { type: string, format: uri }
        quantity: { type: integer }
        price: { type: number }
        totalPrice: { type: number }

    OrderResponse:
      type: object
      properties:
        id: { type: integer }
        userId: { type: integer }
        userName: { type: string }
        totalAmount: { type: number }
        status:
          type: string
          enum: [PENDING, PAID, SHIPPED, DELIVERED, CANCELLED]
        createdAt: { type: string, format: date-time }
        deliveryAddress: { $ref: '#/components/schemas/AddressResponse' }
        items:
          type: array
          items: { $ref: '#/components/schemas/OrderItemResponse' }

    PaymentResponse:
      type: object
      properties:
        id: { type: integer }
        orderId: { type: integer }
        amount: { type: number }
        method: { type: string, enum: [CARD, CASH] }
        status: { type: string, enum: [PAID, FAILED] }
        createdAt: { type: string, format: date-time }

    CommentResponse:
      type: object
      properties:
        id: { type: integer }
        productId: { type: integer }
        userId: { type: integer }
        userName: { type: string }
        userSurname: { type: string }
        text: { type: string }
        rating: { type: integer, minimum: 1, maximum: 5 }
        createdAt: { type: string, format: date-time }

    NotificationResponse:
      type: object
      properties:
        id: { type: integer }
        type: { type: string, enum: [INFO, WARNING, ERROR] }
        text: { type: string }
        isSeen: { type: boolean }

    PosterResponse:
      type: object
      properties:
        id: { type: integer }
        imageLink: { type: string, format: uri }
        clickQuantity: { type: integer }
        link: { type: string, format: uri }
        createdAt: { type: string, format: date-time }

paths:
  # ── AUTH ──────────────────────────────────────────────────────────────────
  /api/auth/register:
    post:
      tags: [Authentication]
      summary: Register new user
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [name, surname, email, password]
              properties:
                name: { type: string, minLength: 2, maxLength: 100 }
                surname: { type: string, minLength: 2, maxLength: 100 }
                email: { type: string, format: email }
                password: { type: string, minLength: 8 }
                birthdayAt: { type: string, format: date }
                phoneNumber: { type: string }
      responses:
        '201':
          description: Registered
          content:
            application/json:
              schema: { $ref: '#/components/schemas/AuthResponse' }
        '400': { description: Validation error }
        '409': { description: Email already exists }

  /api/auth/login:
    post:
      tags: [Authentication]
      summary: Login
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [email, password]
              properties:
                email: { type: string, format: email }
                password: { type: string }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { $ref: '#/components/schemas/AuthResponse' }
        '401': { description: Invalid credentials }

  /api/auth/refresh-token:
    post:
      tags: [Authentication]
      summary: Refresh access token
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [refreshToken]
              properties:
                refreshToken: { type: string }
      responses:
        '200': { description: New tokens }
        '401': { description: Expired refresh token }

  /api/auth/logout:
    post:
      tags: [Authentication]
      summary: Logout
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [refreshToken]
              properties:
                refreshToken: { type: string }
      responses:
        '200': { description: Logged out }

  /api/auth/verify-email:
    get:
      tags: [Authentication]
      summary: Verify email
      parameters:
        - in: query
          name: token
          required: true
          schema: { type: string }
      responses:
        '200': { description: Email verified }

  /api/auth/forgot-password:
    post:
      tags: [Authentication]
      summary: Request password reset
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [email]
              properties:
                email: { type: string, format: email }
      responses:
        '200': { description: Reset email sent }

  /api/auth/reset-password:
    post:
      tags: [Authentication]
      summary: Reset password
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [token, newPassword]
              properties:
                token: { type: string }
                newPassword: { type: string, minLength: 8 }
      responses:
        '200': { description: Password reset }

  # ── USERS ─────────────────────────────────────────────────────────────────
  /api/users/me:
    get:
      tags: [Users]
      summary: Get my profile
      security: [{ bearerAuth: [] }]
      responses:
        '200': { description: User profile }
        '401': { description: Unauthorized }
    put:
      tags: [Users]
      summary: Update my profile
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                name: { type: string }
                surname: { type: string }
                birthdayAt: { type: string, format: date }
                phoneNumber: { type: string }
      responses:
        '200': { description: Updated profile }

  /api/users/me/change-password:
    put:
      tags: [Users]
      summary: Change password
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [oldPassword, newPassword]
              properties:
                oldPassword: { type: string }
                newPassword: { type: string, minLength: 8 }
      responses:
        '200': { description: Password changed }
        '400': { description: Wrong old password }

  /api/users:
    get:
      tags: [Users]
      summary: Get all users (Admin)
      security: [{ bearerAuth: [] }]
      parameters:
        - in: query
          name: search
          schema: { type: string }
        - in: query
          name: page
          schema: { type: integer, default: 0 }
        - in: query
          name: size
          schema: { type: integer, default: 20 }
      responses:
        '200': { description: Paginated users }
        '403': { description: Forbidden }

  /api/users/{id}/block:
    patch:
      tags: [Users]
      summary: Block user
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: User blocked }

  /api/users/{id}/role:
    patch:
      tags: [Users]
      summary: Change user role (Super Admin)
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
        - in: query
          name: role
          required: true
          schema:
            type: string
            enum: [CUSTOMER, ADMIN, DELIVERY, SUPER_ADMIN]
      responses:
        '200': { description: Role updated }

  # ── PRODUCTS ──────────────────────────────────────────────────────────────
  /api/products:
    get:
      tags: [Products]
      summary: Get products with filters
      parameters:
        - in: query
          name: search
          schema: { type: string }
        - in: query
          name: categoryId
          schema: { type: integer }
        - in: query
          name: companyId
          schema: { type: integer }
        - in: query
          name: minPrice
          schema: { type: number }
        - in: query
          name: maxPrice
          schema: { type: number }
        - in: query
          name: page
          schema: { type: integer, default: 0 }
        - in: query
          name: size
          schema: { type: integer, default: 20 }
      responses:
        '200': { description: Paginated products }
    post:
      tags: [Products]
      summary: Create product
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [name, stockQuantity, categoryId, companyId, arrivalPrice, sellPrice]
              properties:
                name: { type: string }
                description: { type: string }
                discountPercent: { type: number, minimum: 0, maximum: 100 }
                stockQuantity: { type: integer, minimum: 0 }
                categoryId: { type: integer }
                companyId: { type: integer }
                arrivalPrice: { type: number }
                sellPrice: { type: number }
      responses:
        '201': { description: Product created }

  /api/products/{id}:
    get:
      tags: [Products]
      summary: Get product by ID
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Product }
        '404': { description: Not found }
    put:
      tags: [Products]
      summary: Update product
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ProductResponse'
      responses:
        '200': { description: Updated }
    delete:
      tags: [Products]
      summary: Delete product
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Deleted }

  # ── PRODUCT IMAGES ────────────────────────────────────────────────────────
  /api/product-images/upload/{productId}:
    post:
      tags: [Product Images]
      summary: Upload product images
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: productId
          required: true
          schema: { type: integer }
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                files:
                  type: array
                  items: { type: string, format: binary }
                firstIsMain:
                  type: boolean
                  default: true
      responses:
        '201': { description: Images uploaded }
        '400': { description: File too large or bad format }

  /api/product-images/{imageId}/main:
    patch:
      tags: [Product Images]
      summary: Set image as main
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: imageId
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Main image updated }

  # ── CATEGORIES ────────────────────────────────────────────────────────────
  /api/categories:
    get:
      tags: [Categories]
      summary: Get all categories
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 0 }
        - in: query
          name: size
          schema: { type: integer, default: 20 }
      responses:
        '200': { description: Paginated categories }
    post:
      tags: [Categories]
      summary: Create category with image
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              required: [data]
              properties:
                data:
                  type: string
                  description: JSON string — {"name":"Electronics"}
                image:
                  type: string
                  format: binary
      responses:
        '201': { description: Created }

  /api/categories/{id}:
    get:
      tags: [Categories]
      summary: Get category by ID
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Category }
    put:
      tags: [Categories]
      summary: Update category
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      requestBody:
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                data:
                  type: string
                  description: JSON string — {"name":"Electronics"}
                image:
                  type: string
                  format: binary
      responses:
        '200': { description: Updated }
    delete:
      tags: [Categories]
      summary: Delete category
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Deleted }

  # ── POSTERS ───────────────────────────────────────────────────────────────
  /api/posters:
    get:
      tags: [Posters]
      summary: Get all posters
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 0 }
        - in: query
          name: size
          schema: { type: integer, default: 20 }
      responses:
        '200': { description: Paginated posters }
    post:
      tags: [Posters]
      summary: Create poster (16:9 crop, 1280×720)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              required: [image]
              properties:
                image:
                  type: string
                  format: binary
                  description: jpg/jpeg/png, max 3MB; output 1280×720 at 90% quality
                link:
                  type: string
                  format: uri
      responses:
        '201': { description: Poster created }

  /api/posters/{id}:
    put:
      tags: [Posters]
      summary: Update poster
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      requestBody:
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                image:
                  type: string
                  format: binary
                link:
                  type: string
                  format: uri
      responses:
        '200': { description: Updated }
    delete:
      tags: [Posters]
      summary: Delete poster
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Deleted }

  /api/posters/{id}/click:
    post:
      tags: [Posters]
      summary: Track poster click
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Click tracked }

  # ── CART ──────────────────────────────────────────────────────────────────
  /api/carts:
    get:
      tags: [Cart]
      summary: Get my cart
      security: [{ bearerAuth: [] }]
      responses:
        '200': { description: Cart }
    delete:
      tags: [Cart]
      summary: Clear cart
      security: [{ bearerAuth: [] }]
      responses:
        '200': { description: Cleared }

  /api/carts/items:
    post:
      tags: [Cart]
      summary: Add to cart
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [productId, quantity]
              properties:
                productId: { type: integer }
                quantity: { type: integer, minimum: 1 }
      responses:
        '200': { description: Cart updated }

  /api/carts/items/{cartItemId}:
    patch:
      tags: [Cart]
      summary: Update cart item quantity
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: cartItemId
          required: true
          schema: { type: integer }
        - in: query
          name: quantity
          required: true
          schema: { type: integer, minimum: 1 }
      responses:
        '200': { description: Cart updated }
    delete:
      tags: [Cart]
      summary: Remove item from cart
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: cartItemId
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Item removed }

  # ── ORDERS ────────────────────────────────────────────────────────────────
  /api/orders:
    post:
      tags: [Orders]
      summary: Create order from cart
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [addressId]
              properties:
                addressId: { type: integer }
                note: { type: string }
      responses:
        '201': { description: Order created }
        '400': { description: Cart is empty }
    get:
      tags: [Orders]
      summary: Get all orders (Admin/Delivery)
      security: [{ bearerAuth: [] }]
      parameters:
        - in: query
          name: status
          schema:
            type: string
            enum: [PENDING, PAID, SHIPPED, DELIVERED, CANCELLED]
        - in: query
          name: page
          schema: { type: integer, default: 0 }
      responses:
        '200': { description: Paginated orders }

  /api/orders/my:
    get:
      tags: [Orders]
      summary: Get my orders
      security: [{ bearerAuth: [] }]
      parameters:
        - in: query
          name: status
          schema:
            type: string
            enum: [PENDING, PAID, SHIPPED, DELIVERED, CANCELLED]
        - in: query
          name: page
          schema: { type: integer, default: 0 }
      responses:
        '200': { description: My orders }

  /api/orders/my/{id}/cancel:
    patch:
      tags: [Orders]
      summary: Cancel my order
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Order cancelled }

  /api/orders/{id}/status:
    patch:
      tags: [Orders]
      summary: Update order status (Admin/Delivery)
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
        - in: query
          name: status
          required: true
          schema:
            type: string
            enum: [PENDING, PAID, SHIPPED, DELIVERED, CANCELLED]
      responses:
        '200': { description: Status updated }

  # ── PAYMENTS ──────────────────────────────────────────────────────────────
  /api/payments:
    post:
      tags: [Payments]
      summary: Process payment
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [orderId, method]
              properties:
                orderId: { type: integer }
                method: { type: string, enum: [CARD, CASH] }
      responses:
        '200': { description: Payment processed }

  /api/payments/order/{orderId}:
    get:
      tags: [Payments]
      summary: Get payment by order ID
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: orderId
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Payment details }

  # ── COMMENTS ──────────────────────────────────────────────────────────────
  /api/comments/product/{productId}:
    get:
      tags: [Comments]
      summary: Get product reviews
      parameters:
        - in: path
          name: productId
          required: true
          schema: { type: integer }
        - in: query
          name: page
          schema: { type: integer, default: 0 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
      responses:
        '200': { description: Paginated reviews }

  /api/comments:
    post:
      tags: [Comments]
      summary: Write a review
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [productId, text, rating]
              properties:
                productId: { type: integer }
                text: { type: string, maxLength: 2000 }
                rating: { type: integer, minimum: 1, maximum: 5 }
      responses:
        '201': { description: Review submitted }

  /api/comments/{id}:
    put:
      tags: [Comments]
      summary: Update review
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                text: { type: string }
                rating: { type: integer, minimum: 1, maximum: 5 }
      responses:
        '200': { description: Review updated }
    delete:
      tags: [Comments]
      summary: Delete review
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Deleted }

  # ── FAVORITES ─────────────────────────────────────────────────────────────
  /api/favorite-products:
    get:
      tags: [Favorites]
      summary: Get my favorites
      security: [{ bearerAuth: [] }]
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 0 }
      responses:
        '200': { description: Paginated favorite products }

  /api/favorite-products/{productId}:
    post:
      tags: [Favorites]
      summary: Add to favorites
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: productId
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Added }
    delete:
      tags: [Favorites]
      summary: Remove from favorites
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: productId
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Removed }

  # ── NOTIFICATIONS ─────────────────────────────────────────────────────────
  /api/notifications:
    get:
      tags: [Notifications]
      summary: Get my notifications
      security: [{ bearerAuth: [] }]
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 0 }
        - in: query
          name: size
          schema: { type: integer, default: 20 }
      responses:
        '200': { description: Paginated notifications }

  /api/notifications/unseen-count:
    get:
      tags: [Notifications]
      summary: Get unseen count
      security: [{ bearerAuth: [] }]
      responses:
        '200': { description: Count of unseen notifications }

  /api/notifications/seen-all:
    patch:
      tags: [Notifications]
      summary: Mark all as seen
      security: [{ bearerAuth: [] }]
      responses:
        '200': { description: All marked seen }

  /api/notifications/{id}/seen:
    patch:
      tags: [Notifications]
      summary: Mark notification as seen
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Marked seen }

  /api/notifications/{id}:
    delete:
      tags: [Notifications]
      summary: Delete notification
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Deleted }

  # ── ADDRESSES ─────────────────────────────────────────────────────────────
  /api/addresses:
    get:
      tags: [Addresses]
      summary: Get my addresses
      security: [{ bearerAuth: [] }]
      responses:
        '200': { description: List of addresses }
    post:
      tags: [Addresses]
      summary: Create address
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [regionType, cityType]
              properties:
                regionType: { type: string }
                cityType: { type: string }
                homeNumber: { type: string }
                roomNumber: { type: string }
      responses:
        '201': { description: Created }

  /api/addresses/{id}:
    put:
      tags: [Addresses]
      summary: Update address
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                regionType: { type: string }
                cityType: { type: string }
                homeNumber: { type: string }
                roomNumber: { type: string }
      responses:
        '200': { description: Updated }
    delete:
      tags: [Addresses]
      summary: Delete address
      security: [{ bearerAuth: [] }]
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer }
      responses:
        '200': { description: Deleted }
```
