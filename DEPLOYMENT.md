# Online Store - Hostingga Yuklash Uchun Qo'llanma

## 📦 Loyiha Tavsifi
- **Nom**: Online Store (E-Commerce platform)
- **Texnologiyalar**: Spring Boot 3.3.4, PostgreSQL, Redis, JWT
- **Java versiyasi**: 17
- **Build tizimi**: Maven

## 🚀 Hostingga Yuklash Uchun Kerakli Fayllar

### 1. JAR Faylni Yig'dirish
```bash
# Terminalda quyidagi buyruqni bajaring:
mvn clean package -DskipTests

# Natijada: target/online-store-0.0.1-SNAPSHOT.jar fayli hosil bo'ladi
```

### 2. Hosting Tavsiyalari

#### ✅ Tavsiya etilgan Hostinglar:
1. **Heroku** (Bepul)
2. **Railway** (Bepul)
3. **Render** (Bepul)
4. **DigitalOcean** (Pullik)
5. **AWS EC2** (Pullik)

#### 🎯 Eng Yaxshi Variant: Railway
- PostgreSQL va Redis avtomatik ravishda o'rnatiladi
- Simple deploy
- Bepul plan mavjud

## 📋 Railway ga Deploy Qilish

### 1. Railway Hisob Ochish
1. [railway.app](https://railway.app) saytiga kirish
2. GitHub bilan login qilish
3. Yangi project yaratish

### 2. Fayllarni Tayyorlash
`railway.yml` faylini loyiha ildiziga yaratish:

```yaml
build:
  builder: NIXPACKS
  buildCommand: mvn clean package -DskipTests
  deployCommand: java -jar target/online-store-0.0.1-SNAPSHOT.jar

deploy:
  startCommand: java -jar target/online-store-0.0.1-SNAPSHOT.jar
  restartPolicyType: ON_FAILURE
  restartPolicyMaxRetries: 10

services:
  - type: web
    name: online-store-api
    env: java
    plan: free
```

### 3. Environment Variables (Railway da)
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://localhost:5432/onlinest_database
DATABASE_USERNAME=onlinest_database
DATABASE_PASSWORD=husniddin2003
REDIS_URL=redis://localhost:6379
JWT_SECRET=your-super-secret-jwt-key-here-min-256-bits
UPLOAD_BASE_DIR=uploads
```

## 🐳 Docker Variant (Agar kerak bo'lsa)

### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/online-store-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://postgres:5432/onlinest_database
      - DATABASE_USERNAME=onlinest_database
      - DATABASE_PASSWORD=husniddin2003
      - REDIS_URL=redis://redis:6379
      - JWT_SECRET=your-super-secret-jwt-key-here-min-256-bits
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: onlinest_database
      POSTGRES_USER: onlinest_database
      POSTGRES_PASSWORD: husniddin2003
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine

volumes:
  postgres_data:
```

## 📱 Mobil Ilova Uchun API

### Base URL
```
https://your-app-name.railway.app/api
```

### Asosiy Endpointlar
- `POST /auth/login` - Login
- `POST /auth/register` - Ro'yxatdan o'tish
- `GET /products` - Mahsulotlar ro'yxati
- `GET /products/{id}` - Mahsulot tafsilotlari
- `GET /categories` - Kategoriyalar
- `POST /orders` - Buyurtma berish

## 🔧 Konfiguratsiya

### application-prod.yml (Hosting uchun)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
  data:
    redis:
      url: ${REDIS_URL}

server:
  port: ${PORT:8080}

logging:
  level:
    root: WARN
    husniddin.online_store: INFO

app:
  upload:
    base-dir: ${UPLOAD_BASE_DIR:uploads}
  jwt:
    secret: ${JWT_SECRET}
```

## 🚀 Tezkor Deploy Qadamlari

### 1. JAR Faylni Yig'dirish
```bash
mvn clean package -DskipTests
```

### 2. Git ga Push Qilish
```bash
git add .
git commit -m "Ready for deployment"
git push origin main
```

### 3. Railway da Deploy
1. GitHub repository ni ulash
2. Avtomatik deploy qilinishini kuzatish
3. URL ni olish va test qilish

## 📊 Monitoring

### Health Check
```
GET /actuator/health
```

### Application Info
```
GET /actuator/info
```

## 🆘 Yordam

### Muhim Masalalar:
1. **Database connection** - Environment variables ni tekshiring
2. **Port** - 8080 port ochiq bo'lishi kerak
3. **Memory** - Minimum 512MB RAM
4. **Java version** - 17 talab qilinadi

### Qo'shimcha Resurslar:
- [Spring Boot Deploy Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Railway Spring Boot Guide](https://docs.railway.app/guides/deploying-a-spring-boot-app)

---

**Eslatma**: Loyihani yuklashdan oldin mahalliy muhitda to'liq test qiling!
