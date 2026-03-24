package com.hospital.hospital.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class ResourceServer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/paciente/registro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/medico/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/medico/menos-citas").permitAll()
                        // jenni
                        .requestMatchers(HttpMethod.GET, "/api/paciente/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/paciente/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/receta/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cita/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cita/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/cita/**").permitAll()

                        // Rutas protegidas (necesitan token) jenni 
                        .requestMatchers(HttpMethod.GET, "/api/paciente/mi-perfil").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/paciente/buscar").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/receta/mis-recetas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/cita/mis-citas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/cita/citas-medico").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cita/agendar").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/cita/cancelar").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/cita/completar").authenticated()

                        .requestMatchers("/api/medico/**").hasAuthority("MEDICO")
                        .requestMatchers("/api/paciente/**").hasAnyAuthority("PACIENTE", "MEDICO")
                        .requestMatchers("/expedientes/**").hasAnyAuthority("PACIENTE", "MEDICO")
                        .requestMatchers("/api/receta/**").hasAnyAuthority("PACIENTE", "MEDICO")
                        .requestMatchers("/api/cita/**").hasAnyAuthority("PACIENTE", "MEDICO")
                        .requestMatchers("/diagnosticos/**").hasAnyAuthority("PACIENTE", "MEDICO")
                        .requestMatchers("/signos-vitales/**").hasAnyAuthority("PACIENTE", "MEDICO")
                        .requestMatchers("/medicamentos/**").hasAnyAuthority("PACIENTE", "MEDICO")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(cookieBearerTokenResolver())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    public BearerTokenResolver cookieBearerTokenResolver() {
        return (HttpServletRequest request) -> {
            String path = request.getRequestURI();

            // Rutas públicas — no leer token para evitar que Spring intente validarlo
            if (path.startsWith("/swagger-ui") ||
                    path.startsWith("/v3/api-docs") ||
                    path.equals("/swagger-ui.html")) {
                return null;
            }

            // Primero intenta leer de la cookie
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("access_token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }

            // Si no hay cookie, lee el header Authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }

            return null;
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("authorities");
        converter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:5173",
                "http://localhost:4173",
                "https://www.produccionxd.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}