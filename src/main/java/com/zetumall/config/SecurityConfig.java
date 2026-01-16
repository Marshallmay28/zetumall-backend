package com.zetumall.config;

import com.zetumall.security.SupabaseAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final SupabaseAuthenticationFilter supabaseAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configure(http))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers("/", "/api", "/api/health", "/api/public/**",
                                                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()
                                                // Admin endpoints
                                                .requestMatchers("/api/admin/**", "/actuator/**")
                                                .hasAnyRole("ADMIN", "SUPER_ADMIN", "FINANCE_ADMIN", "SECURITY_ADMIN",
                                                                "OPERATIONS_ADMIN", "SUPPORT_ADMIN")
                                                // All other endpoints require authentication
                                                .anyRequest().authenticated())
                                .addFilterBefore(
                                                supabaseAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
