package husniddin.online_store.config;

import husniddin.online_store.entity.Category;
import husniddin.online_store.entity.Company;
import husniddin.online_store.entity.Product;
import husniddin.online_store.entity.User;
import husniddin.online_store.enums.Role;
import husniddin.online_store.repository.CategoryRepository;
import husniddin.online_store.repository.CompanyRepository;
import husniddin.online_store.repository.ProductRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true]}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-email}")
    private String adminEmail;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.admin-name}")
    private String adminName;

    @Value("${app.seed.admin-surname}")
    private String adminSurname;

    @Value("${app.seed.admin-phone}")
    private String adminPhone;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Data seeding is disabled");
            return;
        }

        log.info("Starting data seeding...");

        seedSuperAdmin();

        Category phones  = seedCategory("Smartphones",  "https://cdn.online-store.uz/categories/smartphones.png");
        Category laptops = seedCategory("Laptops",       "https://cdn.online-store.uz/categories/laptops.png");

        Company apple   = seedCompany("Apple",   "https://cdn.online-store.uz/companies/apple.png");
        Company samsung = seedCompany("Samsung", "https://cdn.online-store.uz/companies/samsung.png");

        seedProduct(
            "iPhone 15 Pro",
            "Apple iPhone 15 Pro — A17 Pro chip, titanium design, 48MP camera system, USB-C.",
            phones, apple,
            new BigDecimal("9800000"),
            new BigDecimal("11500000"),
            new BigDecimal("5"),
            50
        );

        seedProduct(
            "Samsung Galaxy S24 Ultra",
            "Samsung Galaxy S24 Ultra — Snapdragon 8 Gen 3, 200MP camera, built-in S Pen.",
            phones, samsung,
            new BigDecimal("8500000"),
            new BigDecimal("10200000"),
            new BigDecimal("0"),
            35
        );

        seedProduct(
            "MacBook Pro 14 M3",
            "Apple MacBook Pro 14\" — M3 chip, 18GB unified memory, 18-hour battery life.",
            laptops, apple,
            new BigDecimal("16000000"),
            new BigDecimal("19500000"),
            new BigDecimal("3"),
            20
        );

        seedProduct(
            "Samsung Galaxy Book4 Pro",
            "Samsung Galaxy Book4 Pro — Intel Core Ultra 7, AMOLED display, 63Wh battery.",
            laptops, samsung,
            new BigDecimal("10500000"),
            new BigDecimal("13000000"),
            new BigDecimal("0"),
            15
        );

        log.info("Data seeding completed successfully");
    }

    // ─── PRIVATE SEED METHODS ─────────────────────────────────────────────────

    private void seedSuperAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.debug("SUPER_ADMIN already exists: {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .name(adminName)
                .surname(adminSurname)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phoneNumber(adminPhone)
                .birthdayAt(LocalDate.of(1990, 1, 1))
                .role(Role.SUPER_ADMIN)
                .emailVerified(true)
                .build();

        userRepository.save(admin);
        log.info("SUPER_ADMIN created: {}", adminEmail);
    }

    private Category seedCategory(String name, String imageLink) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category category = Category.builder()
                    .name(name)
                    .imageLink(imageLink)
                    .build();
            Category saved = categoryRepository.save(category);
            log.info("Category created: {}", name);
            return saved;
        });
    }

    private Company seedCompany(String name, String imageLink) {
        return companyRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Company company = Company.builder()
                    .name(name)
                    .imageLink(imageLink)
                    .build();
            Company saved = companyRepository.save(company);
            log.info("Company created: {}", name);
            return saved;
        });
    }

    private void seedProduct(String name, String description,
                              Category category, Company company,
                              BigDecimal arrivalPrice, BigDecimal sellPrice,
                              BigDecimal discountPercent, int stockQuantity) {
        if (productRepository.existsByNameIgnoreCase(name)) {
            log.debug("Product already exists: {}", name);
            return;
        }

        Product product = Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .company(company)
                .arrivalPrice(arrivalPrice)
                .sellPrice(sellPrice)
                .discountPercent(discountPercent)
                .stockQuantity(stockQuantity)
                .build();

        productRepository.save(product);
        log.info("Product created: {}", name);
    }
}
