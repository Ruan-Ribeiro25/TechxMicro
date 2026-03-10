package com.copamir.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // <--- IMPORTANTE: O que faltava
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.copamir.service.impl.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    // =========================================================================
    // NOVIDADE: MODO "IGNORAR" (Pula toda a verificação de segurança)
    // Isso garante que o /api/teste/** nunca pedirá login, aconteça o que acontecer.
    // =========================================================================
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/teste/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Mantemos o CSRF desativado para garantir compatibilidade geral
            .csrf(csrf -> csrf.disable()) 

            .authorizeHttpRequests(auth -> auth
                // --- RECURSOS ESTÁTICOS & PWA ---
                .requestMatchers(
                    "/css/**", 
                    "/js/**", 
                    "/uploads/**", 
                    "/img/**", 
                    "/webjars/**", 
                    "/fragments/**",
                    "/manifest.json", 
                    "/sw.js",
                    "/favicon.ico"
                ).permitAll()
                
                // NOTA: A regra do "/api/teste/**" foi removida daqui porque
                // agora ela é tratada no webSecurityCustomizer acima (é mais forte).
                
                // --- MONITORAMENTO ---
                .requestMatchers("/actuator/**").permitAll()

                // --- PÁGINAS PÚBLICAS ---
                .requestMatchers(
                    "/", "/login", "/login-professional", "/admin/login",
                    "/register", "/register-professional", "/register-medico", "/register-admin",
                    "/verificar-conta", "/forgot-password", "/enter-code", 
                    "/verify-reset-code", "/update-password", "/perfil/selecionar-polo",
                    "/acesso-profissional", "/bem-vindo"
                ).permitAll()
                
                // --- ROTAS PROTEGIDAS (Preservadas) ---
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/financeiro/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                
                // --- PERMISSÕES DE PROFISSIONAIS (Preservadas) ---
                .requestMatchers("/profissional/**").hasAnyAuthority(
                    "MEDICO", "ROLE_MEDICO", 
                    "ENFERMEIRO", "ROLE_ENFERMEIRO", 
                    "MOTORISTA", "ROLE_MOTORISTA", 
                    "ADMIN", "ROLE_ADMIN",
                    "TECNICO", "ROLE_TECNICO",
                    "AUXILIAR", "ROLE_AUXILIAR",
                    "RECEPCAO", "ROLE_RECEPCAO",
                    "SERVICOS_GERAIS", "ROLE_SERVICOS_GERAIS"
                )
                
                // Áreas específicas que exigem login
                .requestMatchers("/pacientes/**", "/agendamentos/**", "/documentos/**", "/telemedicina/**").authenticated()
                
                // Qualquer outra rota exige login
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