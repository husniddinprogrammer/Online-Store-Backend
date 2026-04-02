# Maven O'rnatish Uchun Tezkor Qo'llanma

## 🎯 Eng Omon Yo'l: Maven Wrapper

Loyihangizda `mvnw.cmd` mavjud. Shundan foydalaning:

```cmd
# Loyiha ildizida (c:\Users\User\Desktop\online-store\online-store)
.\mvnw.cmd clean package -DskipTests
```

## 📦 Maven O'rnatish Usullari

### 1️⃣ Chocolatey Orqali (Eng Oson)
```cmd
# Chocolatey o'rnatish (agar yo'q bo'lsa)
powershell -Command "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"

# Maven o'rnatish
choco install maven
```

### 2️⃣ Qo'lbola O'rnatish
1. [Maven v3.9.6](https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip) yuklab oling
2. ZIP faylini `C:\Program Files\Apache\maven` ga oching
3. Environment Variables:
   - `MAVEN_HOME` = `C:\Program Files\Apache\maven`
   - PATH ga `%MAVEN_HOME%\bin` qo'shing

### 3️⃣ Docker Orqali (Mavensiz)
```cmd
# Docker bilan build qilish
docker build -f Dockerfile-with-maven -t online-store .
docker run -p 8080:8080 online-store
```

## 🔍 Tekshirish

```cmd
# Maven versiyasini tekshirish
mvn --version

# Yoki wrapper versiyasi
.\mvnw.cmd --version
```

## 🚀 Build Qilish

```cmd
# Agar mavenni o'rnatgan bo'lsangiz
mvn clean package -DskipTests

# Agar wrapperdan foydalansangiz (tavsiya etiladi)
.\mvnw.cmd clean package -DskipTests
```

---

**Eslatma:** Maven Wrapper loyiha bilan birga keladi, alohida o'rnatish shart emas! 🎯
