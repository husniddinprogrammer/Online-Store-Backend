package husniddin.online_store.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/products/**",
            "/api/categories/**",
            "/api/companies/**",
            "/api/posters/**",
            "/api/comments/**",
            "/uploads/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            // WebSocket handshake (SockJS uses multiple sub-paths under /ws)
            "/ws/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // ── Global write-method block: VIEWER may not call POST/PUT/DELETE/PATCH ──
                .requestMatchers(HttpMethod.POST,   "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")
                .requestMatchers(HttpMethod.PUT,    "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")
                .requestMatchers(HttpMethod.PATCH,  "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")

                // ── Route-level access (GET for all, writes already blocked above for VIEWER) ──
                .requestMatchers("/api/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY", "VIEWER")
                .requestMatchers("/api/orders/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY", "VIEWER")
                .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "VIEWER")
                .requestMatchers("/api/carts/**").hasAnyRole("CUSTOMER", "VIEWER")
                .requestMatchers("/api/favorite-products/**").hasAnyRole("CUSTOMER", "ADMIN", "VIEWER")
                .requestMatchers("/api/notifications/**").authenticated()
                .requestMatchers("/api/addresses/**").authenticated()

                // ── Admin-only write operations (belt-and-suspenders after global block) ──
                .requestMatchers(HttpMethod.POST,   "/api/categories/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/categories/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST,   "/api/companies/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/companies/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/companies/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST,   "/api/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers("/api/posters/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
