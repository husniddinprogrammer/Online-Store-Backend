/**
 * Online Store — Frontend API Client
 * Compatible with: React, Vue, Angular, Vanilla JS
 * Using: axios
 *
 * Install: npm install axios
 */

import axios from "axios";

// ─── CONFIG ────────────────────────────────────────────────────────────────────
const BASE_URL = "http://localhost:8080";

const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 10000,
});

// ─── TOKEN MANAGEMENT ──────────────────────────────────────────────────────────
const TOKEN_KEY = "accessToken";
const REFRESH_KEY = "refreshToken";

export const tokenStorage = {
  getAccess: () => localStorage.getItem(TOKEN_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  set: (access, refresh) => {
    localStorage.setItem(TOKEN_KEY, access);
    localStorage.setItem(REFRESH_KEY, refresh);
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
};

// ─── REQUEST INTERCEPTOR (attach token) ────────────────────────────────────────
api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ─── RESPONSE INTERCEPTOR (auto refresh on 401) ────────────────────────────────
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token)));
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = tokenStorage.getRefresh();
      if (!refreshToken) {
        tokenStorage.clear();
        window.location.href = "/login";
        return Promise.reject(error);
      }

      try {
        const res = await axios.post(`${BASE_URL}/api/auth/refresh-token`, {
          refreshToken,
        });
        const { accessToken, refreshToken: newRefresh } = res.data.data;
        tokenStorage.set(accessToken, newRefresh);
        api.defaults.headers.Authorization = `Bearer ${accessToken}`;
        processQueue(null, accessToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        tokenStorage.clear();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// ─── HELPER ────────────────────────────────────────────────────────────────────
const handleError = (error) => {
  const problem = error.response?.data;
  throw {
    status: problem?.status || 500,
    title: problem?.title || "Error",
    detail: problem?.detail || "Something went wrong",
    errors: problem?.errors || {},
  };
};

// ═══════════════════════════════════════════════════════════════════════════════
// AUTH
// ═══════════════════════════════════════════════════════════════════════════════
export const authApi = {
  /**
   * Register new user
   * @example
   * const auth = await authApi.register({
   *   name: "Husniddin", surname: "Toshmatov",
   *   email: "user@example.com", password: "Password123"
   * });
   * tokenStorage.set(auth.accessToken, auth.refreshToken);
   */
  register: async (data) => {
    try {
      const res = await api.post("/api/auth/register", data);
      return res.data.data;
    } catch (e) {
      handleError(e);
    }
  },

  /**
   * Login
   * @example
   * const auth = await authApi.login({ email: "user@example.com", password: "Password123" });
   * tokenStorage.set(auth.accessToken, auth.refreshToken);
   */
  login: async (email, password) => {
    try {
      const res = await api.post("/api/auth/login", { email, password });
      const data = res.data.data;
      tokenStorage.set(data.accessToken, data.refreshToken);
      return data;
    } catch (e) {
      handleError(e);
    }
  },

  /** Logout — revokes refresh token */
  logout: async () => {
    try {
      await api.post("/api/auth/logout", {
        refreshToken: tokenStorage.getRefresh(),
      });
    } finally {
      tokenStorage.clear();
    }
  },

  verifyEmail: async (token) => {
    const res = await api.get(`/api/auth/verify-email?token=${token}`);
    return res.data;
  },

  forgotPassword: async (email) => {
    const res = await api.post("/api/auth/forgot-password", { email });
    return res.data;
  },

  resetPassword: async (token, newPassword) => {
    const res = await api.post("/api/auth/reset-password", {
      token,
      newPassword,
    });
    return res.data;
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// USERS
// ═══════════════════════════════════════════════════════════════════════════════
export const userApi = {
  /** Get current logged-in user */
  getMe: async () => {
    const res = await api.get("/api/users/me");
    return res.data.data;
  },

  /** Update current user profile */
  updateMe: async (data) => {
    const res = await api.put("/api/users/me", data);
    return res.data.data;
  },

  /** Change password */
  changePassword: async (currentPassword, newPassword) => {
    const res = await api.put("/api/users/me/change-password", {
      currentPassword,
      newPassword,
    });
    return res.data;
  },

  /** Get all users — Admin only */
  getAll: async (params = {}) => {
    const res = await api.get("/api/users", { params });
    return res.data.data;
  },

  /** Block user — Admin only */
  block: async (id) => {
    const res = await api.patch(`/api/users/${id}/block`);
    return res.data.data;
  },

  /** Unblock user — Admin only */
  unblock: async (id) => {
    const res = await api.patch(`/api/users/${id}/unblock`);
    return res.data.data;
  },

  /** Change role — SuperAdmin only */
  changeRole: async (id, role) => {
    const res = await api.patch(`/api/users/${id}/role`, null, {
      params: { role },
    });
    return res.data.data;
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// PRODUCTS
// ═══════════════════════════════════════════════════════════════════════════════
export const productApi = {
  /**
   * Get products with filters
   * @example
   * const { content, totalElements } = await productApi.getAll({
   *   search: "iphone", categoryId: 1, minPrice: 500, maxPrice: 2000,
   *   page: 0, size: 20, sort: "sellPrice,asc"
   * });
   */
  getAll: async (params = {}) => {
    const res = await api.get("/api/products", { params });
    return res.data.data;
  },

  getById: async (id) => {
    const res = await api.get(`/api/products/${id}`);
    return res.data.data;
  },

  /** Admin only */
  create: async (data) => {
    const res = await api.post("/api/products", data);
    return res.data.data;
  },

  /** Admin only */
  update: async (id, data) => {
    const res = await api.put(`/api/products/${id}`, data);
    return res.data.data;
  },

  /** Admin only */
  delete: async (id) => {
    await api.delete(`/api/products/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// CATEGORIES
// ═══════════════════════════════════════════════════════════════════════════════
export const categoryApi = {
  getAll: async (params = {}) => {
    const res = await api.get("/api/categories", { params });
    return res.data.data;
  },
  getById: async (id) => {
    const res = await api.get(`/api/categories/${id}`);
    return res.data.data;
  },
  create: async (data) => {
    const res = await api.post("/api/categories", data);
    return res.data.data;
  },
  update: async (id, data) => {
    const res = await api.put(`/api/categories/${id}`, data);
    return res.data.data;
  },
  delete: async (id) => {
    await api.delete(`/api/categories/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// COMPANIES
// ═══════════════════════════════════════════════════════════════════════════════
export const companyApi = {
  getAll: async (params = {}) => {
    const res = await api.get("/api/companies", { params });
    return res.data.data;
  },
  getById: async (id) => {
    const res = await api.get(`/api/companies/${id}`);
    return res.data.data;
  },
  create: async (data) => {
    const res = await api.post("/api/companies", data);
    return res.data.data;
  },
  update: async (id, data) => {
    const res = await api.put(`/api/companies/${id}`, data);
    return res.data.data;
  },
  delete: async (id) => {
    await api.delete(`/api/companies/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// CART
// ═══════════════════════════════════════════════════════════════════════════════
export const cartApi = {
  /** Get current cart */
  get: async () => {
    const res = await api.get("/api/carts");
    return res.data.data;
  },

  /**
   * Add item to cart
   * @example await cartApi.addItem(1, 2); // productId=1, quantity=2
   */
  addItem: async (productId, quantity) => {
    const res = await api.post("/api/carts/items", { productId, quantity });
    return res.data.data;
  },

  /** Update quantity (set 0 to remove) */
  updateItem: async (cartItemId, quantity) => {
    const res = await api.patch(`/api/carts/items/${cartItemId}`, null, {
      params: { quantity },
    });
    return res.data.data;
  },

  removeItem: async (cartItemId) => {
    await api.delete(`/api/carts/items/${cartItemId}`);
  },

  clear: async () => {
    await api.delete("/api/carts");
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// ORDERS
// ═══════════════════════════════════════════════════════════════════════════════
export const orderApi = {
  /**
   * Create order from cart
   * @example
   * const order = await orderApi.create({ addressId: 1 });
   */
  create: async (data) => {
    const res = await api.post("/api/orders", data);
    return res.data.data;
  },

  /** Get my orders */
  getMy: async (params = {}) => {
    const res = await api.get("/api/orders/my", { params });
    return res.data.data;
  },

  getMyById: async (id) => {
    const res = await api.get(`/api/orders/my/${id}`);
    return res.data.data;
  },

  cancel: async (id) => {
    const res = await api.patch(`/api/orders/my/${id}/cancel`);
    return res.data.data;
  },

  /** Admin/Delivery: get all orders */
  getAll: async (params = {}) => {
    const res = await api.get("/api/orders", { params });
    return res.data.data;
  },

  /** Admin/Delivery: update status */
  updateStatus: async (id, status) => {
    const res = await api.patch(`/api/orders/${id}/status`, null, {
      params: { status },
    });
    return res.data.data;
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// PAYMENTS
// ═══════════════════════════════════════════════════════════════════════════════
export const paymentApi = {
  /**
   * Pay for an order
   * @example await paymentApi.pay(10, "CARD");
   */
  pay: async (orderId, method) => {
    const res = await api.post("/api/payments", { orderId, method });
    return res.data.data;
  },

  getByOrderId: async (orderId) => {
    const res = await api.get(`/api/payments/order/${orderId}`);
    return res.data.data;
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// FAVORITES
// ═══════════════════════════════════════════════════════════════════════════════
export const favoriteApi = {
  getAll: async (params = {}) => {
    const res = await api.get("/api/favorite-products", { params });
    return res.data.data;
  },
  add: async (productId) => {
    await api.post(`/api/favorite-products/${productId}`);
  },
  remove: async (productId) => {
    await api.delete(`/api/favorite-products/${productId}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// COMMENTS
// ═══════════════════════════════════════════════════════════════════════════════
export const commentApi = {
  getByProduct: async (productId, params = {}) => {
    const res = await api.get(`/api/comments/product/${productId}`, { params });
    return res.data.data;
  },

  create: async (productId, text, rating) => {
    const res = await api.post("/api/comments", { productId, text, rating });
    return res.data.data;
  },

  update: async (id, text, rating, productId) => {
    const res = await api.put(`/api/comments/${id}`, {
      productId,
      text,
      rating,
    });
    return res.data.data;
  },

  delete: async (id) => {
    await api.delete(`/api/comments/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// ADDRESSES
// ═══════════════════════════════════════════════════════════════════════════════
export const addressApi = {
  getAll: async () => {
    const res = await api.get("/api/addresses");
    return res.data.data;
  },

  create: async (data) => {
    const res = await api.post("/api/addresses", data);
    return res.data.data;
  },

  update: async (id, data) => {
    const res = await api.put(`/api/addresses/${id}`, data);
    return res.data.data;
  },

  delete: async (id) => {
    await api.delete(`/api/addresses/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// NOTIFICATIONS
// ═══════════════════════════════════════════════════════════════════════════════
export const notificationApi = {
  getAll: async (params = {}) => {
    const res = await api.get("/api/notifications", { params });
    return res.data.data;
  },

  getUnseenCount: async () => {
    const res = await api.get("/api/notifications/unseen-count");
    return res.data.data;
  },

  markSeen: async (id) => {
    await api.patch(`/api/notifications/${id}/seen`);
  },

  markAllSeen: async () => {
    await api.patch("/api/notifications/seen-all");
  },

  delete: async (id) => {
    await api.delete(`/api/notifications/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// POSTERS
// ═══════════════════════════════════════════════════════════════════════════════
export const posterApi = {
  getAll: async (params = {}) => {
    const res = await api.get("/api/posters", { params });
    return res.data.data;
  },

  click: async (id) => {
    const res = await api.post(`/api/posters/${id}/click`);
    return res.data.data;
  },

  create: async (data) => {
    const res = await api.post("/api/posters", data);
    return res.data.data;
  },

  update: async (id, data) => {
    const res = await api.put(`/api/posters/${id}`, data);
    return res.data.data;
  },

  delete: async (id) => {
    await api.delete(`/api/posters/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// PRODUCT IMAGES
// ═══════════════════════════════════════════════════════════════════════════════
export const productImageApi = {
  getByProduct: async (productId) => {
    const res = await api.get(`/api/product-images/product/${productId}`);
    return res.data.data;
  },

  add: async (productId, imageLink, isMain = false) => {
    const res = await api.post(
      `/api/product-images/product/${productId}`,
      null,
      { params: { imageLink, isMain } }
    );
    return res.data.data;
  },

  setMain: async (imageId) => {
    await api.patch(`/api/product-images/${imageId}/main`);
  },

  delete: async (imageId) => {
    await api.delete(`/api/product-images/${imageId}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════════
// USAGE EXAMPLES
// ═══════════════════════════════════════════════════════════════════════════════

/*
// ── REGISTER + LOGIN ──────────────────────────────────────────────────────────
async function registerExample() {
  const auth = await authApi.register({
    name: "Husniddin",
    surname: "Toshmatov",
    email: "user@example.com",
    password: "Password123",
    phoneNumber: "+998901234567"
  });
  console.log("Registered:", auth.role); // "CUSTOMER"
}

async function loginExample() {
  const auth = await authApi.login("user@example.com", "Password123");
  // tokenStorage.set() is called automatically
  console.log("Logged in as:", auth.name);
}

// ── PRODUCT LISTING ───────────────────────────────────────────────────────────
async function productListExample() {
  const page = await productApi.getAll({
    search: "samsung",
    categoryId: 1,
    minPrice: 1000000,
    maxPrice: 5000000,
    sort: "sellPrice,asc",
    page: 0,
    size: 12
  });
  // page.content = products array
  // page.totalElements = total count
  // page.totalPages = total pages
}

// ── ADD TO CART + ORDER ───────────────────────────────────────────────────────
async function checkoutExample() {
  // 1. Add products to cart
  await cartApi.addItem(1, 2);   // iPhone x2
  await cartApi.addItem(5, 1);   // Case x1

  // 2. Check cart
  const cart = await cartApi.get();
  console.log("Total:", cart.totalAmount);

  // 3. Create address
  const address = await addressApi.create({
    regionType: "TASHKENT_CITY",
    cityType: "TASHKENT",
    homeNumber: "45",
    roomNumber: "12"
  });

  // 4. Place order
  const order = await orderApi.create({ addressId: address.id });
  console.log("Order #" + order.id + " placed");

  // 5. Pay
  await paymentApi.pay(order.id, "CARD");
  console.log("Paid successfully!");
}

// ── NOTIFICATIONS ─────────────────────────────────────────────────────────────
async function notificationsExample() {
  const count = await notificationApi.getUnseenCount();
  if (count > 0) {
    const { content } = await notificationApi.getAll({ size: count });
    content.forEach(n => console.log(`[${n.type}] ${n.text}`));
    await notificationApi.markAllSeen();
  }
}

// ── PRODUCT REVIEWS ───────────────────────────────────────────────────────────
async function reviewExample() {
  const { content } = await commentApi.getByProduct(1, { page: 0, size: 5 });
  content.forEach(c => console.log(`${c.userName}: ${"★".repeat(c.rating)} ${c.text}`));

  // Write review
  await commentApi.create(1, "Excellent product!", 5);
}
*/

export default api;
