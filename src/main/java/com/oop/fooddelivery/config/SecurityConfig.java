
package com.oop.fooddelivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // ====================================
                // URL ACCESS RULES
                // ====================================
                .authorizeHttpRequests(auth -> auth

                        // Static resources
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // Public browsing and customer pages
                        .requestMatchers(
                                "/",
                                "/restaurants",
                                "/menu",
                                "/recommendations",
                                "/about",
                                "/track-order",
                                "/track-order/**",
                                "/cart",
                                "/cart/**",
                                "/checkout",
                                "/checkout/**",
                                "/review",
                                "/review/**",
                                "/register",
                                "/login"
                        ).permitAll()

                        // ADMIN ONLY
                        .requestMatchers(
                                "/dashboard",
                                "/dashboard/**",
                                "/customer",
                                "/customer/**",
                                "/restaurant",
                                "/restaurant/**",
                                "/food",
                                "/food/**",
                                "/order",
                                "/order/**",
                                "/delivery",
                                "/delivery/**",
                                "/payment",
                                "/payment/**",
                                "/feedback",
                                "/feedback/**"
                        ).hasRole("ADMIN")

                        // Require authentication for other routes
                        .anyRequest().authenticated()
                )

                // ====================================
                // FORM LOGIN
                // ====================================
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {

                            boolean isAdmin = authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(authority ->
                                            "ROLE_ADMIN".equals(authority.getAuthority())
                                    );

                            if (isAdmin) {
                                response.sendRedirect(
                                        request.getContextPath() + "/dashboard"
                                );
                            } else {
                                response.sendRedirect(
                                        request.getContextPath() + "/"
                                );
                            }
                        })
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // ====================================
                // LOGOUT
                // ====================================
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        // CSRF protection remains enabled.
        return http.build();
    }
}
