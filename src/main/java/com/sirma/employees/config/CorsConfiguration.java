package com.sirma.employees.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class CorsConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .configurationSource(request -> new org.springframework.web.cors.CorsConfiguration().applyPermitDefaultValues())
            .and()
            .csrf()
            .disable();
    }
}
