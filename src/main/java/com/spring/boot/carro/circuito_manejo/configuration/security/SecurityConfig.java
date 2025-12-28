package com.spring.boot.carro.circuito_manejo.configuration.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // 1. Configurar CORS (Paso vital para Angular)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Deshabilitar CSRF si es una API REST pura
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(request -> {
                    request.requestMatchers("/login", "/oauth2/**").permitAll();
                    request.anyRequest().authenticated();
                })
                .exceptionHandling(exception -> exception
                        // Si el usuario no está logueado, lo manda a tu Angular
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // 3. Login normal y OAuth2
                //.formLogin(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        // Aquí podrías redirigir a una URL de Angular después del éxito
                        .defaultSuccessUrl("http://localhost:4200/dashboard", true)
                )

                .logout(logout -> logout
                        .logoutUrl("/api/v1/logout") // URL que llamaremos desde Angular
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Al salir, respondemos con un 200 OK para que Angular sepa que terminó
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .invalidateHttpSession(true)   // Destruye la sesión del servidor
                        .clearAuthentication(true)      // Limpia los datos de autenticación
                        .deleteCookies("JSESSIONID")   // Borra la cookie de la computadora del usuario
                )
                .build();
    }

    // Bean para configurar qué dominios pueden entrar a tu Backend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Agrega tanto localhost como tu URL de Render
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "https://tu-app-angular.onrender.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); // Muy importante para OAuth2 y sesiones

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);// Esto aplica a todos los controllers
        return source;
    }
}

