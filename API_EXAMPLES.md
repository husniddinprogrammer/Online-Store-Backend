# Product API Examples for Frontend

## Base URL
```
http://localhost:8080/api/products
```

## 1. Get All Products (Default)
```bash
GET /api/products
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "iPhone 15 Pro",
        "description": "Latest iPhone with titanium design",
        "discountPercent": 10,
        "stockQuantity": 50,
        "soldQuantity": 120,
        "category": {
          "id": 1,
          "name": "Smartphones"
        },
        "company": {
          "id": 1,
          "name": "Apple"
        },
        "arrivalPrice": 999.99,
        "sellPrice": 1099.99,
        "discountedPrice": 989.99,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-20T14:22:00",
        "images": [
          {
            "id": 1,
            "imageUrl": "http://localhost:8080/uploads/products/iphone15pro-1.jpg"
          }
        ],
        "averageRating": 4.5
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

## 2. Search Products
```bash
GET /api/products?search=iPhone
```

## 3. Filter by Category
```bash
GET /api/products?categoryId=1
```

## 4. Filter by Company
```bash
GET /api/products?companyId=1
```

## 5. Price Range Filter
```bash
GET /api/products?minPrice=500&maxPrice=1500
```

## 6. Sort Products

### Sort by Newest (Default)
```bash
GET /api/products?sort=NEWEST
```

### Sort by Most Popular
```bash
GET /api/products?sort=POPULAR
```

### Sort by Price (Low to High)
```bash
GET /api/products?sort=PRICE_ASC
```

### Sort by Price (High to Low)
```bash
GET /api/products?sort=PRICE_DESC
```

### Sort by Highest Discount
```bash
GET /api/products?sort=DISCOUNT_DESC
```

### Sort by Lowest Discount
```bash
GET /api/products?sort=DISCOUNT_ASC
```

### Sort by ID (Highest first)
```bash
GET /api/products?sort=ID_DESC
```

### Sort by ID (Lowest first)
```bash
GET /api/products?sort=ID_ASC
```

### Sort by Stock (Highest first)
```bash
GET /api/products?sort=STOCK_DESC
```

### Sort by Stock (Lowest first)
```bash
GET /api/products?sort=STOCK_ASC
```

### Sort by Sold Quantity (Most first)
```bash
GET /api/products?sort=SOLD_DESC
```

### Sort by Sold Quantity (Least first)
```bash
GET /api/products?sort=SOLD_ASC
```

## 7. Combined Filters with Sorting and Pagination
```bash
GET /api/products?search=phone&categoryId=1&minPrice=500&maxPrice=2000&sort=PRICE_ASC&page=0&size=10
```

## 8. Get Single Product
```bash
GET /api/products/1
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone with titanium design",
    "discountPercent": 10,
    "stockQuantity": 50,
    "soldQuantity": 120,
    "category": {
      "id": 1,
      "name": "Smartphones"
    },
    "company": {
      "id": 1,
      "name": "Apple"
    },
    "arrivalPrice": 999.99,
    "sellPrice": 1099.99,
    "discountedPrice": 989.99,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-20T14:22:00",
    "images": [
      {
        "id": 1,
        "imageUrl": "http://localhost:8080/uploads/products/iphone15pro-1.jpg"
      },
      {
        "id": 2,
        "imageUrl": "http://localhost:8080/uploads/products/iphone15pro-2.jpg"
      }
    ],
    "averageRating": 4.5
  }
}
```

## Available Sort Options
- `NEWEST` - Latest additions first (sorted by createdAt DESC)
- `POPULAR` - Most sold first (sorted by soldQuantity DESC)
- `PRICE_ASC` - Cheapest first (sorted by sellPrice ASC)
- `PRICE_DESC` - Most expensive first (sorted by sellPrice DESC)
- `DISCOUNT_DESC` - Highest discount first (sorted by discountPercent DESC)
- `DISCOUNT_ASC` - Lowest discount first (sorted by discountPercent ASC)
- `ID_DESC` - Highest ID first (sorted by id DESC)
- `ID_ASC` - Lowest ID first (sorted by id ASC)
- `STOCK_DESC` - Highest stock first (sorted by stockQuantity DESC)
- `STOCK_ASC` - Lowest stock first (sorted by stockQuantity ASC)
- `SOLD_DESC` - Most sold first (sorted by soldQuantity DESC)
- `SOLD_ASC` - Least sold first (sorted by soldQuantity ASC)

## Pagination Parameters
- `page` - Page number (default: 0, starts from 0)
- `size` - Number of items per page (default: 20, max: 100)

## JavaScript/Fetch Examples

### Basic fetch
```javascript
const response = await fetch('http://localhost:8080/api/products');
const data = await response.json();
console.log(data.data.content);
```

### With filters and sorting
```javascript
const params = new URLSearchParams({
  search: 'iPhone',
  categoryId: '1',
  minPrice: '500',
  maxPrice: '1500',
  sort: 'PRICE_ASC',
  page: '0',
  size: '10'
});

const response = await fetch(`http://localhost:8080/api/products?${params}`);
const data = await response.json();
```

### React example
```jsx
import { useState, useEffect } from 'react';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/products?sort=NEWEST');
        const data = await response.json();
        setProducts(data.data.content);
      } catch (error) {
        console.error('Error fetching products:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      {products.map(product => (
        <div key={product.id}>
          <h3>{product.name}</h3>
          <p>{product.description}</p>
          <p>Price: ${product.discountedPrice}</p>
          <p>Rating: {product.averageRating}</p>
        </div>
      ))}
    </div>
  );
}
```
