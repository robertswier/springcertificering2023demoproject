package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /*
    Spring Security expects a Bean to configure its Filter Chain, which you learned about in the Simple Spring Security lesson. Annotating a method returning a SecurityFilterChain with the @Bean satisfies this expectation.

    All HTTP requests to cashcards/ endpoints are required to be authenticated using HTTP Basic Authentication security (username and password).
Also, do not require CSRF security.

CRSF
When should you use CSRF protection? Our recommendation is to use CSRF protection for any request that could be processed by a browser by normal users. If you are only creating a service that is used by non-browser clients, you will likely want to disable CSRF protection.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers("/cashcards/**")
                .hasRole("CARD-OWNER") // enable RBAC: Replace the .authenticated() code with this line.
                .and()
                .csrf().disable()
                .httpBasic();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    This UserDetailsService configuration should be understandable: configure a user named sarah1 with password abc123
    en rol CARD-OWNER.

Spring's IoC container will find the UserDetailsService Bean and Spring Data will use it when needed.
     */
    @Bean
    public UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails sarah = users
                .username("sarah1")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER") // new role
                .build();
        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("qrs456"))
                .roles("NON-OWNER") // new role
                .build();
        return new InMemoryUserDetailsManager(sarah, hankOwnsNoCards);
    }
}
