package com.benjvi.interspersedweb

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.Principal

@ControllerAdvice
class SecurityControllerAdvice(currentUser: Mono<Principal>) {

    private lateinit var currentUser : Mono<Principal>

    @ModelAttribute
    fun csrfToken(exchange: ServerWebExchange): Mono<CsrfToken> {
        val csrfToken = exchange.getAttribute<Mono<CsrfToken>?>(CsrfToken::class.java.name as String)
        return csrfToken?.doOnSuccess {
            token ->
            exchange.attributes[CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME] = token
        }!!
    }

    @ModelAttribute("currentUser")
    fun currentUser(currentUser: Mono<Principal>): Mono<Principal> {
        return currentUser;
    }
}
