# Java 1.8 ga O'tkazish Uchun Qo'llanma

## ⚠️ Muhum O'zgarishlar

### 🔄 Versiyalar Pastga Tushirildi:
- **Spring Boot**: 3.3.4 → 2.7.18
- **Java**: 17 → 1.8
- **MapStruct**: 1.5.5 → 1.4.2
- **Lombok**: 1.18.30 → 1.18.24
- **JWT**: 0.12.3 → 0.9.1
- **SpringDoc**: 2.5.0 → 1.7.0

## 📋 Kod O'zgarishlari Kerak

### 1️⃣ Java 8+ Featurelarni Olib Tashlash
```java
// ❌ Bu kodlar ishlamaydi:
var user = new User(); // var keyword
List<String> list = List.of("a", "b", "c"); // List.of()
String text = """
    multiline text
    """; // text blocks

// ✅ Java 8 uchun:
User user = new User();
List<String> list = Arrays.asList("a", "b", "c");
String text = "multiline text";
```

### 2️⃣ JWT Token O'zgarishi
```java
// ❌ Eski (JWT 0.12.3):
String token = Jwts.builder()
    .subject(user.getEmail())
    .signWith(key)
    .compact();

// ✅ Yangi (JWT 0.9.1):
String token = Jwts.builder()
    .setSubject(user.getEmail())
    .signWith(SignatureAlgorithm.HS512, secret)
    .compact();
```

### 3️⃣ Spring Boot 2.7 O'zgarishlari
```java
// ❌ Spring Boot 3.x:
@NonNull
private String field;

// ✅ Spring Boot 2.7.x:
@NotNull // javax.validation.NotNull
private String field;
```

## 🔧 Qadam-Ba-Qadam O'tkazish

### 1️⃣ Pom.xml ni Almashtirish
```bash
# Eski pom.xml ni saqlab qo'yish
mv pom.xml pom-java17.xml

# Yangi pom-java8.xml ni pom.xml qilish
mv pom-java8.xml pom.xml
```

### 2️⃣ Importlarni Tuzatish
```java
// ❌ Eski importlar:
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

// ✅ Yangi importlar:
import javax.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable; // yoki olib tashlash
```

### 3️⃣ LocalDateTime → Date (agar kerak bo'lsa)
```java
// ❌ Java 8+:
private LocalDateTime createdAt;

// ✅ Java 8 ham qo'llaydi, lekin Date ham mumkin:
private Date createdAt;
```

## 🚀 Build Qilish

```bash
# Tozalash va qayta build
mvn clean compile

# Agar xatoliklar bo'lsa, ularni tuzating
mvn clean package -DskipTests
```

## ⚠️ Muhim Eslatmalar

1. **Spring Boot 2.7** - 2024 yil noyabrda support tugaydi
2. **Xavfsizlik** - Eski versiyalar xavfsizlik kamchiliklari bor
3. **Featurelar** - Ba'zi yangi featurelar ishlamaydi
4. **Performance** - Java 8 sekinroq

## 🎯 Tavsiya

**Agar imkon qo'lsa Java 17 da qoling**, chunki:
- Spring Boot 3.3.4 - eng so'ngi versiya
- Yaxshi performance
- Xavfsizlik yangilangan
- Yangi Java featurelari

**Faqat Java 1.8 majburiyat bo'lsa o'ting!**
