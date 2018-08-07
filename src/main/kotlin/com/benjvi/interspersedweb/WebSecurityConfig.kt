package com.benjvi.interspersedweb;

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.matchers
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder
import org.springframework.security.core.userdetails.UserDetails




@EnableWebFluxSecurity
@Configuration
class WebSecurityConfig(
        @Value("\${admin_username}")
        val usernameOne: String,
        @Value("\${admin_password}")
        val passwordOne: String
    ){

    @Bean
    fun userDetailsRepository(): MapReactiveUserDetailsService {
        val user = User.withDefaultPasswordEncoder()
                .username(usernameOne)
                .password(passwordOne)
                .roles("USER", "ADMIN")
                .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .httpBasic().and()
                .formLogin().and()
                // fix
                .csrf().disable()
        return http.build()
    }
}