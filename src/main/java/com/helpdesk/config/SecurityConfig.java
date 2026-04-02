package com.helpdesk.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.helpdesk.service.impl.CustomUserDetailsService;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/teste/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/uploads/**", "/img/**", "/webjars/**", "/fragments/**", "/manifest.json", "/sw.js", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/", "/login", "/login-professional", "/admin/login", "/register", "/register-professional", "/register-medico", "/register-admin", "/verificar-conta", "/forgot-password", "/enter-code", "/verify-reset-code", "/update-password", "/perfil/selecionar-polo", "/acesso-profissional", "/bem-vindo").permitAll()
                
                // REGRAS DE ACESSO ATUALIZADAS COM AS NOVAS COMPETÊNCIAS
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/financeiro/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/helpdesk/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "TECNICO", "ROLE_TECNICO", "TECNICO_TI", "ROLE_TECNICO_TI", "TECNICO_SOFTWARE", "ROLE_TECNICO_SOFTWARE", "TECNICO_AMBOS", "ROLE_TECNICO_AMBOS")
                .requestMatchers("/profissional/**").hasAnyAuthority("MEDICO", "ROLE_MEDICO", "ENFERMEIRO", "ROLE_ENFERMEIRO", "MOTORISTA", "ROLE_MOTORISTA", "ADMIN", "ROLE_ADMIN", "TECNICO", "ROLE_TECNICO", "TECNICO_TI", "ROLE_TECNICO_TI", "TECNICO_SOFTWARE", "ROLE_TECNICO_SOFTWARE", "TECNICO_AMBOS", "ROLE_TECNICO_AMBOS", "AUXILIAR", "ROLE_AUXILIAR", "RECEPCAO", "ROLE_RECEPCAO", "SERVICOS_GERAIS", "ROLE_SERVICOS_GERAIS")
                .requestMatchers("/pacientes/**", "/agendamentos/**", "/documentos/**", "/telemedicina/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/admin")) {
                        response.sendRedirect("/admin/login?error=denied");
                    } else {
                        response.sendRedirect("/home");
                    }
                })
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "https://pixelti.app.br", 
            "https://www.pixelti.app.br", 
            "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}