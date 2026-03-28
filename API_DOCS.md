# Online Store — REST API Documentation

**Base URL:** `http://localhost:8080`
**Content-Type:** `application/json`
**Auth:** `Authorization: Bearer <accessToken>` (JWT)

---

## Response Envelope

Every response is wrapped in:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error Format (RFC 7807 ProblemDetail)
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "timestamp": "2024-01-15T10:30:00Z",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

### Common HTTP Status Codes
| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request / Validation Error |
| 401 | Unauthorized (missing/invalid JWT) |
| 403 | Forbidden (insufficient role) |
| 404 | Not Found |
| 409 | Conflict (e.g. email already exists) |
| 500 | Internal Server Error |

---

## Authentication 🔐
> **Base:** `/api/auth` — No JWT required

---

### POST /api/auth/register
Register a new user account.

**Request Body:**
```json
{
  "name": "Husniddin",
  "surname": "Toshmatov",
  "email": "user@example.com",
  "password": "Password123",
  "birthdayAt": "1998-05-20",
  "phoneNumber": "+998901234567"
}
```

**Response `201`:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "userId": 1,
    "email": "user@example.com",
    "name": "Husniddin",
    "role": "CUSTOMER"
  }
}
```

**Errors:**
```json
// 409 - Email already taken
{ "title": "Email Already Exists", "status": 409, "detail": "Email already registered: user@example.com" }

// 400 - Validation
{ "title": "Validation Error", "status": 400, "errors": { "password": "Password must contain at least one uppercase, one lowercase and one digit" } }
```

---

### POST /api/auth/login
Login with email and password.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "userId": 1,
    "email": "user@example.com",
    "name": "Husniddin",
    "role": "CUSTOMER"
  }
}
```

**Errors:**
```json
// 401
{ "title": "Authentication Failed", "status": 401, "detail": "Invalid email or password" }
```

---

### POST /api/auth/refresh-token
Get a new access token using refresh token.

**Request Body:**
```json
{ "refreshToken": "550e8400-e29b-41d4-a716-446655440000" }
```

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "userId": 1,
    "email": "user@example.com",
    "name": "Husniddin",
    "role": "CUSTOMER"
  }
}
```

**Errors:**
```json
// 401
{ "title": "Unauthorized", "status": 401, "detail": "Refresh token has expired" }
```

---

### POST /api/auth/logout
Revoke the refresh token.

**Request Body:**
```json
{ "refreshToken": "550e8400-e29b-41d4-a716-446655440000" }
```

**Response `200`:**
```json
{ "success": true, "message": "Logged out successfully" }
```

---

### GET /api/auth/verify-email?token={token}
Verify email address.

**Query Params:** `token` (string, required)

**Response `200`:**
```json
{ "success": true, "message": "Email verified successfully" }
```

**Errors:**
```json
// 400
{ "title": "Bad Request", "status": 400, "detail": "Verification token has expired" }
```

---

### POST /api/auth/forgot-password
Request password reset email.

**Request Body:**
```json
{ "email": "user@example.com" }
```

**Response `200`:**
```json
{ "success": true, "message": "Password reset link sent to your email" }
```

---

### POST /api/auth/reset-password
Reset password with token from email.

**Request Body:**
```json
{
  "token": "uuid-token-from-email",
  "newPassword": "NewPassword123"
}
```

**Response `200`:**
```json
{ "success": true, "message": "Password reset successfully" }
```

---

## Users 👤
> **Base:** `/api/users` — JWT Required

---

### GET /api/users/me
Get current logged-in user profile.

**Auth:** Any authenticated user

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Husniddin",
    "surname": "Toshmatov",
    "email": "user@example.com",
    "birthdayAt": "1998-05-20",
    "phoneNumber": "+998901234567",
    "balance": 250000.00,
    "blocked": false,
    "role": "CUSTOMER",
    "lastLoginAt": "2024-01-15T10:30:00",
    "createdAt": "2024-01-01T08:00:00",
    "emailVerified": true
  }
}
```

---

### PUT /api/users/me
Update current user profile.

**Auth:** Any authenticated user

**Request Body:**
```json
{
  "name": "Husniddin",
  "surname": "Karimov",
  "birthdayAt": "1998-05-20",
  "phoneNumber": "+998901234567"
}
```

**Response `200`:** Same as GET /api/users/me

---

### PUT /api/users/me/change-password
Change current user's password.

**Auth:** Any authenticated user

**Request Body:**
```json
{
  "currentPassword": "Password123",
  "newPassword": "NewPassword456"
}
```

**Response `200`:**
```json
{ "success": true, "message": "Password changed successfully" }
```

**Errors:**
```json
// 400
{ "title": "Bad Request", "status": 400, "detail": "Current password is incorrect" }
```

---

### GET /api/users
Get all users with search and pagination.

**Auth:** SUPER_ADMIN, ADMIN only

**Query Params:**
- `search` (string, optional) — name/surname/email search
- `page` (int, default: 0)
- `size` (int, default: 20)
- `sort` (string, e.g. `createdAt,desc`)

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [ { ...userObject } ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

---

### GET /api/users/{id}
Get user by ID. **Auth:** SUPER_ADMIN, ADMIN

---

### PATCH /api/users/{id}/block
Block a user. **Auth:** SUPER_ADMIN, ADMIN

**Response `200`:** Updated user object

---

### PATCH /api/users/{id}/unblock
Unblock a user. **Auth:** SUPER_ADMIN, ADMIN

---

### PATCH /api/users/{id}/role?role={ROLE}
Change user's role. **Auth:** SUPER_ADMIN only

**Query Param:** `role` = `SUPER_ADMIN | ADMIN | DELIVERY | CUSTOMER`

---

### DELETE /api/users/{id}
Soft-delete a user. **Auth:** SUPER_ADMIN only

---

## Products 📦
> **Base:** `/api/products` — Public GET, JWT for write

---

### GET /api/products
Get products with filters, sorting, and pagination.

**Query Params:**
| Param | Type | Description |
|-------|------|-------------|
| search | string | Search by name |
| categoryId | long | Filter by category |
| companyId | long | Filter by company |
| minPrice | decimal | Min sell price |
| maxPrice | decimal | Max sell price |
| page | int | Page number (0-based) |
| size | int | Page size (default 20) |
| sort | string | e.g. `sellPrice,asc` or `createdAt,desc` |

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "iPhone 15 Pro",
        "description": "Latest Apple smartphone",
        "discountPercent": 10.00,
        "stockQuantity": 50,
        "soldQuantity": 120,
        "category": { "id": 1, "name": "Phones", "imageLink": "https://..." },
        "company": { "id": 1, "name": "Apple", "imageLink": "https://..." },
        "arrivalPrice": 9000000.00,
        "sellPrice": 10000000.00,
        "discountedPrice": 9000000.00,
        "createdAt": "2024-01-01T08:00:00",
        "updatedAt": "2024-01-10T12:00:00",
        "images": [
          { "id": 1, "imageLink": "https://cdn.../iphone15.jpg", "isMain": true }
        ],
        "averageRating": 4.7
      }
    ],
    "totalElements": 250,
    "totalPages": 13,
    "size": 20,
    "number": 0
  }
}
```

---

### GET /api/products/{id}
Get single product by ID.

**Response `200`:** Single product object (same structure as above)

**Errors:**
```json
// 404
{ "title": "Resource Not Found", "status": 404, "detail": "Product not found with id: 99" }
```

---

### POST /api/products
Create a new product. **Auth:** SUPER_ADMIN, ADMIN

**Request Body:**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest Apple flagship smartphone",
  "discountPercent": 10.00,
  "stockQuantity": 50,
  "categoryId": 1,
  "companyId": 1,
  "arrivalPrice": 9000000.00,
  "sellPrice": 10000000.00
}
```

**Response `201`:** Product object

---

### PUT /api/products/{id}
Update product. **Auth:** SUPER_ADMIN, ADMIN

**Request Body:** Same as POST

---

### DELETE /api/products/{id}
Soft-delete product. **Auth:** SUPER_ADMIN, ADMIN

---

## Categories 🏷️
> **Base:** `/api/categories` — Public GET

### GET /api/categories
**Query Params:** `page`, `size`, `sort`

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "name": "Smartphones", "imageLink": "https://..." }
    ],
    "totalElements": 15
  }
}
```

### GET /api/categories/{id}
### POST /api/categories *(ADMIN)*
```json
{ "name": "Laptops", "imageLink": "https://cdn.example.com/laptops.png" }
```
### PUT /api/categories/{id} *(ADMIN)*
### DELETE /api/categories/{id} *(ADMIN)*

---

## Companies 🏢
> **Base:** `/api/companies` — Public GET

### GET /api/companies
### GET /api/companies/{id}
### POST /api/companies *(ADMIN)*
```json
{ "name": "Samsung", "imageLink": "https://cdn.example.com/samsung.png" }
```
### PUT /api/companies/{id} *(ADMIN)*
### DELETE /api/companies/{id} *(ADMIN)*

---

## Orders 🛒
> **Base:** `/api/orders` — JWT Required

---

### POST /api/orders
Create an order from current cart.

**Auth:** Any authenticated user

**Request Body:**
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
    "id": 10,
    "userId": 1,
    "userName": "Husniddin Toshmatov",
    "totalAmount": 20000000.00,
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00",
    "deliveryAddress": {
      "id": 1,
      "regionType": "TASHKENT_CITY",
      "cityType": "TASHKENT",
      "homeNumber": "45",
      "roomNumber": "12",
      "createdAt": "2024-01-10T09:00:00"
    },
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "iPhone 15 Pro",
        "productImageLink": "https://...",
        "quantity": 2,
        "price": 10000000.00,
        "totalPrice": 20000000.00
      }
    ]
  }
}
```

**Errors:**
```json
// 400 - Empty cart
{ "title": "Bad Request", "status": 400, "detail": "Cart is empty" }

// 400 - Insufficient stock
{ "title": "Bad Request", "status": 400, "detail": "Insufficient stock for product: iPhone 15 Pro" }
```

---

### GET /api/orders/my
Get current user's orders.

**Query Params:**
- `status` (optional): `PENDING | PAID | SHIPPED | DELIVERED | CANCELLED`
- `page`, `size`, `sort`

**Response `200`:** Paginated list of orders

---

### GET /api/orders/my/{id}
Get specific order by ID (must belong to current user).

---

### PATCH /api/orders/my/{id}/cancel
Cancel a PENDING order.

**Response `200`:** Updated order with status `CANCELLED`

**Errors:**
```json
// 400
{ "title": "Bad Request", "status": 400, "detail": "Only PENDING orders can be cancelled" }
```

---

### GET /api/orders
Get all orders (admin view). **Auth:** SUPER_ADMIN, ADMIN, DELIVERY

**Query Params:** `status`, `page`, `size`, `sort`

---

### PATCH /api/orders/{id}/status?status={STATUS}
Update order status. **Auth:** SUPER_ADMIN, ADMIN, DELIVERY

**Query Param:** `status` = `PENDING | PAID | SHIPPED | DELIVERED | CANCELLED`

---

## Cart 🛍️
> **Base:** `/api/carts` — JWT Required (CUSTOMER role)

---

### GET /api/carts
Get current user's cart.

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "items": [
      {
        "id": 5,
        "productId": 1,
        "productName": "iPhone 15 Pro",
        "productImageLink": "https://cdn.../iphone15.jpg",
        "productPrice": 10000000.00,
        "quantity": 2,
        "totalPrice": 20000000.00
      }
    ],
    "totalAmount": 20000000.00,
    "totalItems": 1
  }
}
```

---

### POST /api/carts/items
Add item to cart (or increase quantity if already in cart).

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Response `200`:** Updated cart

**Errors:**
```json
// 400
{ "title": "Bad Request", "status": 400, "detail": "Insufficient stock. Available: 5" }
```

---

### PATCH /api/carts/items/{cartItemId}?quantity={qty}
Update cart item quantity. Set `quantity=0` to remove.

**Query Param:** `quantity` (int, required)

**Response `200`:** Updated cart

---

### DELETE /api/carts/items/{cartItemId}
Remove item from cart.

---

### DELETE /api/carts
Clear entire cart.

---

## Favorite Products ❤️
> **Base:** `/api/favorite-products` — JWT Required

### GET /api/favorite-products
Get current user's favorites (paginated).

**Response `200`:** Paginated list of product objects

### POST /api/favorite-products/{productId}
Add product to favorites.

**Response `200`:**
```json
{ "success": true, "message": "Added to favorites" }
```

**Errors:**
```json
// 400
{ "title": "Bad Request", "status": 400, "detail": "Product is already in favorites" }
```

### DELETE /api/favorite-products/{productId}
Remove product from favorites.

---

## Comments & Reviews ⭐
> **Base:** `/api/comments`

### GET /api/comments/product/{productId}
Get reviews for a product. **Public**

**Query Params:** `page`, `size`, `sort`

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "productId": 1,
        "userId": 5,
        "userName": "Ali",
        "userSurname": "Valiyev",
        "text": "Excellent product! Very fast delivery.",
        "rating": 5,
        "createdAt": "2024-01-12T14:20:00"
      }
    ],
    "totalElements": 42
  }
}
```

### POST /api/comments
Write a review. **Auth:** Any authenticated user

**Request Body:**
```json
{
  "productId": 1,
  "text": "Amazing phone, highly recommend!",
  "rating": 5
}
```

**Errors:**
```json
// 400 - Already reviewed
{ "title": "Bad Request", "status": 400, "detail": "You have already reviewed this product" }
```

### PUT /api/comments/{id}
Update your own review. **Auth:** Comment owner only

**Request Body:** Same as POST

### DELETE /api/comments/{id}
Delete your own review. **Auth:** Comment owner only

---

## Addresses 📍
> **Base:** `/api/addresses` — JWT Required

### GET /api/addresses
Get current user's addresses.

**Response `200`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "regionType": "TASHKENT_CITY",
      "cityType": "TASHKENT",
      "homeNumber": "45",
      "roomNumber": "12",
      "createdAt": "2024-01-10T09:00:00"
    }
  ]
}
```

### POST /api/addresses
Create a new address.

**Request Body:**
```json
{
  "regionType": "TASHKENT_CITY",
  "cityType": "TASHKENT",
  "homeNumber": "45",
  "roomNumber": "12"
}
```

**Available RegionTypes:**
`TASHKENT_CITY, TASHKENT_REGION, ANDIJAN, FERGANA, NAMANGAN, SAMARKAND, BUKHARA, NAVOI, KASHKADARYA, SURKHANDARYA, JIZZAKH, SIRDARYA, KHOREZM, KARAKALPAKSTAN`

### PUT /api/addresses/{id}
Update address. **Auth:** Address owner only

### DELETE /api/addresses/{id}
Delete address. **Auth:** Address owner only

---

## Notifications 🔔
> **Base:** `/api/notifications` — JWT Required

### GET /api/notifications
Get current user's notifications.

**Query Params:** `page`, `size`

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "type": "INFO",
        "text": "Your order #10 has been placed successfully!",
        "isSeen": false
      }
    ],
    "totalElements": 5
  }
}
```

### GET /api/notifications/unseen-count
Get count of unseen notifications.

**Response `200`:**
```json
{ "success": true, "data": 3 }
```

### PATCH /api/notifications/{id}/seen
Mark one notification as seen.

### PATCH /api/notifications/seen-all
Mark all notifications as seen.

### DELETE /api/notifications/{id}
Delete a notification.

---

## Posters/Banners 🖼️
> **Base:** `/api/posters`

### GET /api/posters
Get all active posters. **Public**

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "imageLink": "https://cdn.example.com/banner1.jpg",
        "clickQuantity": 1245,
        "link": "https://example.com/sale",
        "createdAt": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

### POST /api/posters/{id}/click
Track a poster click (increment counter). **Public**

### POST /api/posters *(ADMIN)*
```json
{
  "imageLink": "https://cdn.example.com/new-banner.jpg",
  "link": "https://example.com/promo"
}
```

### PUT /api/posters/{id} *(ADMIN)*
### DELETE /api/posters/{id} *(ADMIN)*

---

## Product Images 🖼️
> **Base:** `/api/product-images`

### GET /api/product-images/product/{productId}
Get all images for a product. **Public**

**Response `200`:**
```json
{
  "success": true,
  "data": [
    { "id": 1, "imageLink": "https://cdn.../img1.jpg", "isMain": true },
    { "id": 2, "imageLink": "https://cdn.../img2.jpg", "isMain": false }
  ]
}
```

### POST /api/product-images/product/{productId} *(ADMIN)*
**Query Params:** `imageLink` (string), `isMain` (boolean, default false)

### PATCH /api/product-images/{imageId}/main *(ADMIN)*
Set image as the main product image.

### DELETE /api/product-images/{imageId} *(ADMIN)*

---

## Payments 💳
> **Base:** `/api/payments` — JWT Required

### POST /api/payments
Process payment for an order.

**Request Body:**
```json
{
  "orderId": 10,
  "method": "CARD"
}
```
**PayMethod values:** `CARD`, `CASH`

**Response `200`:**
```json
{
  "success": true,
  "message": "Payment processed",
  "data": {
    "id": 1,
    "orderId": 10,
    "amount": 20000000.00,
    "method": "CARD",
    "status": "PAID",
    "createdAt": "2024-01-15T10:35:00"
  }
}
```

**Errors:**
```json
// 400
{ "title": "Bad Request", "status": 400, "detail": "Order is not in PENDING status" }
// 400
{ "title": "Bad Request", "status": 400, "detail": "Payment already exists for this order" }
```

### GET /api/payments/order/{orderId}
Get payment details for an order.

---

## Pagination Standard

All paginated responses follow:
```
GET /api/products?page=0&size=20&sort=createdAt,desc
GET /api/products?page=1&size=10&sort=sellPrice,asc
```

---

## Swagger UI

Interactive API docs available at: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/api-docs`
