package com.nagaralert.config;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final com.nagaralert.repository.AppUserRepository appUserRepository;

    public SecurityConfig(com.nagaralert.repository.AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            .authorizeHttpRequests(auth -> auth
                // Public paths accessible to everyone (Citizens)
                .requestMatchers("/", "/report", "/alert/**", "/my-alerts", "/login", "/require-phone", "/submit-phone", "/css/**", "/js/**", "/images/**", "/uploads/**", "/error", "/api/**").permitAll()
                // H2 Console
                .requestMatchers(org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                // Admin portal is restricted
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    String dept = request.getParameter("department");
                    if (dept != null && !dept.isEmpty() && !dept.equals("ALL")) {
                        response.sendRedirect("/admin?dept=" + dept);
                    } else {
                        response.sendRedirect("/admin");
                    }
                })
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                                (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
                        
                        org.springframework.security.oauth2.core.user.OAuth2User oauth2User = oauthToken.getPrincipal();
                        String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();
                        
                        String oauthId = oauth2User.getName();
                        if (provider.equals("GOOGLE") && oauth2User.getAttribute("sub") != null) {
                            oauthId = oauth2User.getAttribute("sub");
                        } else if (oauth2User.getAttribute("id") != null) {
                            oauthId = oauth2User.getAttribute("id").toString();
                        }
                        
                        String email = oauth2User.getAttribute("email");
                        String name = oauth2User.getAttribute("name");
                        String picture = oauth2User.getAttribute("picture"); // Extract profile picture
                        
                        final String finalOauthId = oauthId;
                        com.nagaralert.model.AppUser appUser = appUserRepository.findByOauthIdAndOauthProvider(oauthId, provider)
                                .orElseGet(() -> {
                                    com.nagaralert.model.AppUser newUser = new com.nagaralert.model.AppUser();
                                    newUser.setOauthProvider(provider);
                                    newUser.setOauthId(finalOauthId);
                                    return newUser;
                                });
                                
                        appUser.setName(name);
                        appUser.setEmail(email);
                        if (picture != null && !picture.isEmpty()) {
                            appUser.setProfileImage(picture);
                        }
                        appUserRepository.save(appUser);
                        
                        if (appUser.getMobileNumber() == null || appUser.getMobileNumber().isEmpty()) {
                            response.sendRedirect("/require-phone");
                        } else {
                            response.sendRedirect("/");
                        }
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withUsername("admin")
            .password("{noop}admin123") // {noop} means plain text password (for demo purposes)
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
