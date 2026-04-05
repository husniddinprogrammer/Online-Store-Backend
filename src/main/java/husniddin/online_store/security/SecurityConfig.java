package husniddin.online_store.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/products/**",
            "/api/categories/**",
            "/api/companies/**",
            "/api/posters/**",
            "/api/comments/**",
            "/uploads/**",
            "/upload/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource)
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(PUBLIC_URLS).permitAll()
                .antMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // ── Global write-method block: VIEWER may not call POST/PUT/DELETE/PATCH ──
                .antMatchers(HttpMethod.POST,   "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")
                .antMatchers(HttpMethod.PUT,    "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")
                .antMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")
                .antMatchers(HttpMethod.PATCH,  "/api/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY")

                // ── Route-level access (GET for all, writes already blocked above for VIEWER) ──
                .antMatchers("/api/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY", "VIEWER")
                .antMatchers("/api/orders/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CUSTOMER", "DELIVERY", "VIEWER")
                .antMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "VIEWER")
                .antMatchers("/api/carts/**").hasAnyRole("CUSTOMER", "VIEWER")
                .antMatchers("/api/favorite-products/**").hasAnyRole("CUSTOMER", "ADMIN", "VIEWER")
                .antMatchers("/api/notifications/**").authenticated()
                .antMatchers("/api/addresses/**").authenticated()

                // ── Admin-only write operations (belt-and-suspenders after global block) ──
                .antMatchers(HttpMethod.POST,   "/api/categories/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.PUT,    "/api/categories/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/categories/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/companies/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.PUT,    "/api/companies/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/companies/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.PUT,    "/api/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                .anyRequest().authenticated()
            .and()
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
