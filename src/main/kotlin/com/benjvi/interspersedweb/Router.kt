package com.benjvi.interspersedweb

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router
import org.springframework.http.MediaType.*
import com.benjvi.interspersedweb.Handler

@Configuration
open class Router(private val mainHandler: Handler) {
    @Bean
    open fun apiRouter() = router {
        GET("/hello", mainHandler::helloWorld)
        (accept(MULTIPART_FORM_DATA) and "/upload").nest {
            POST("/audio", mainHandler::uploadAudio)
        }
    }
}