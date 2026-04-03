# Java 8 Migratsiya Skripti

## 🔄 Barcha Jakarta Importlarini Javax Ga Almashtirish

Quyidagi skriptni loyiha ildizida bajaring:

### PowerShell skripti:
```powershell
# Barcha .java fayllaridagi jakarta importlarini javax ga almashtirish
Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object {
    $content = Get-Content $_.FullName
    $content = $content -replace 'jakarta\.persistence\.', 'javax.persistence.'
    $content = $content -replace 'jakarta\.validation\.', 'javax.validation.'
    $content = $content -replace 'jakarta\.servlet\.', 'javax.servlet.'
    $content = $content -replace 'jakarta\.inject\.', 'javax.inject.'
    Set-Content $_.FullName $content
}
Write-Host "Jakarta importlari javax ga almashtirildi"
```

### CMD skripti:
```cmd
# Barcha fayllardagi jakarta ni javax ga almashtirish
for /r src %%f in (*.java) do (
    powershell -Command "(Get-Content '%%f') -replace 'jakarta\.persistence\.', 'javax.persistence.' -replace 'jakarta\.validation\.', 'javax.validation.' -replace 'jakarta\.servlet\.', 'javax.servlet.' | Set-Content '%%f'"
)
echo Jakarta importlari javax ga almashtirildi
```

## 🎯 Qo'lda Almashtirish (Agar skript ishlamasa):

Asosiy fayllarni qo'lda almashtiring:
1. `Product.java` - `jakarta.persistence.*` → `javax.persistence.*`
2. `User.java` - `jakarta.persistence.*` → `javax.persistence.*`
3. `JwtAuthenticationFilter.java` - `jakarta.servlet.*` → `javax.servlet.*`
4. `ProductController.java` - `jakarta.validation.*` → `javax.validation.*`
5. Barcha Request DTO lar - `jakarta.validation.*` → `javax.validation.*`

## 🚀 Build Test
```bash
mvn clean compile
```

Agar xatoliklar bo'lsa, ularni ko'rib chiqamiz!
