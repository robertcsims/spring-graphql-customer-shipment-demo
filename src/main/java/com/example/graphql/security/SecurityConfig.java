package com.example.graphql.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security configuration.
 *
 * For executive demos, start the app with:
 *   --spring.profiles.active=demo
 *
 * This disables authentication on / and /graphiql + /graphql so leadership can focus purely
 * on the GraphQL experience without login friction.
 *
 * In normal / production mode, Basic Auth is enforced (users: admin/admin123, viewer/viewer123).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @org.springframework.beans.factory.annotation.Value("${app.security.demo-mode:false}") boolean demoMode) throws Exception {

        http.csrf(csrf -> csrf.disable());

        if (demoMode) {
            // Demo mode: everything open so the focus stays on GraphQL capabilities
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/actuator/**").permitAll()
                    .requestMatchers("/graphiql/**", "/graphql/**").authenticated()
                    .anyRequest().permitAll()
            ).httpBasic(withDefaults());
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN", "USER")
                .build();

        UserDetails viewer = User.builder()
                .username("viewer")
                .password(passwordEncoder.encode("viewer123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, viewer);
    }
}
