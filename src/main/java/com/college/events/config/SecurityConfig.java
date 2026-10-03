package com.college.events.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless HTTP Basic auth with three role-based demo accounts.
 * Passwords come from environment variables / Kubernetes Secrets (never hard-coded in the image).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/app.js", "/style.css").permitAll()
                .requestMatchers("/actuator/health/**", "/actuator/prometheus", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/events").hasAnyRole("ADMIN", "ORGANIZER")
                .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("ADMIN", "ORGANIZER")
                .requestMatchers(HttpMethod.DELETE, "/api/events/*").hasAnyRole("ADMIN", "ORGANIZER")
                .requestMatchers("/api/events/*/attendance/**").hasAnyRole("ADMIN", "ORGANIZER")
                .requestMatchers("/api/events/*/registrations").hasAnyRole("ADMIN", "ORGANIZER")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder,
            @Value("${app.users.admin-password}") String adminPw,
            @Value("${app.users.organizer-password}") String orgPw,
            @Value("${app.users.student-password}") String studentPw) {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password(encoder.encode(adminPw)).roles("ADMIN").build(),
            User.withUsername("organizer").password(encoder.encode(orgPw)).roles("ORGANIZER").build(),
            User.withUsername("student").password(encoder.encode(studentPw)).roles("STUDENT").build());
    }
}
