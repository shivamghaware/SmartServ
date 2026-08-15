package com.smartserv.admin;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration(proxyBeanMethods = false)
public class SecuritySecureConfig {

    private final AdminServerProperties adminServer;

    public SecuritySecureConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = 
            new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.getContextPath() + "/");

        http
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(new AntPathRequestMatcher(this.adminServer.getContextPath() + "/assets/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(this.adminServer.getContextPath() + "/login")).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage(this.adminServer.getContextPath() + "/login")
                .successHandler(successHandler)
            )
            .logout(logout -> logout
                .logoutUrl(this.adminServer.getContextPath() + "/logout")
            )
            .httpBasic(httpBasic -> {})
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher(this.adminServer.getContextPath() + "/instances", "POST"),
                    new AntPathRequestMatcher(this.adminServer.getContextPath() + "/instances/*", "DELETE"),
                    new AntPathRequestMatcher(this.adminServer.getContextPath() + "/actuator/**")
                )
            );
        return http.build();
    }
}
