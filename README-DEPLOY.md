# Online Store - Hostingga Yuklash Uchun Kerakli Fayllar

## 🎯 Eng Oson Yo'l: Railway

### 1️⃣ Tayyorlash
```bash
# JAR faylni yig'dirish
mvn clean package -DskipTests

# Git ga push qilish
git add .
git commit -m "Ready for railway deploy"
git push origin main
```

### 2️⃣ Railway.app da
1. [railway.app](https://railway.app) ga kirish
2. Yangi project → "Deploy from GitHub repo"
3. O'z repositoryingizni tanlang
4. Avtomatik deploy qilinishini kuting

### 3️⃣ Environment Variables (Railway da)
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://localhost:5432/onlinest_database
DATABASE_USERNAME=onlinest_database  
DATABASE_PASSWORD=husniddin2003
REDIS_URL=redis://localhost:6379
JWT_SECRET=your-super-secret-jwt-key-here-min-256-bits
UPLOAD_BASE_DIR=uploads
```

## 📦 Kerakli Fayllar Ro'yxati

✅ **Yaratilgan fayllar:**
- `DEPLOYMENT.md` - To'liq qo'llanma
- `railway.yml` - Railway konfiguratsiyasi  
- `Dockerfile` - Docker uchun
- `docker-compose.yml` - Mahalliy test uchun

## 🚀 Tezkor Test

### Mahalliy test (Docker)
```bash
docker-compose up -d
```

### API Test
```bash
curl http://localhost:8080/api/products
```

## 📱 API URL (Deploydan keyin)
```
https://your-app-name.railway.app/api
```

---

**Natija:** 5 daqiqa ichida hostingga yuklash tayyor! 🎉
