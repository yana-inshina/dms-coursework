package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.fa.dpi23.dmsisms.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class AppConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF включён по умолчанию — НЕ отключаем

                .authorizeHttpRequests(auth -> auth
                        // статика
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // публичные страницы
                        .requestMatchers("/login", "/register", "/about").permitAll()
                        .requestMatchers("/error", "/error/**").permitAll()

                        // админка
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // просмотр (любой авторизованный)
                        .requestMatchers("/", "/stats").authenticated()
                        .requestMatchers(HttpMethod.GET, "/clients", "/programs", "/policies").authenticated()

                        // CLIENTS
                        .requestMatchers("/clients/new", "/clients/*/edit", "/clients/save", "/clients/*/delete")
                        .hasAnyRole("MANAGER", "ADMIN")

                        // PROGRAMS
                        .requestMatchers("/programs/new", "/programs/*/edit", "/programs/save", "/programs/*/delete")
                        .hasAnyRole("MANAGER", "ADMIN")

                        // POLICIES
                        .requestMatchers("/policies/new", "/policies/*/edit", "/policies/*/delete")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/policies", "/policies/*")
                        .hasAnyRole("MANAGER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .userDetailsService(customUserDetailsService)
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        // logout по POST на /logout (стандарт Spring Security)
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

        return http.build();
    }
}
