package com.benjvi.interspersedweb

import org.springframework.stereotype.Component
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
    import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.io.File
import java.util.concurrent.TimeUnit



@Component
class Handler() {

    fun helloWorld(req: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(Mono.just("Hello World!"))
    }

    fun uploadAudio(req: ServerRequest): Mono<ServerResponse> {
        // TODO: spring request handling fails when size > ~2GB
        return req.body(BodyExtractors.toMultipartData()).flatMap { parts ->
            val map: Map<String, Part> = parts.toSingleValueMap()

            val filePartBase : FilePart = map["file"]!! as FilePart
            filePartBase.transferTo( File("/tmp/audio-base"))

            val filePartTgt : FilePart = map["file-2"]!! as FilePart
            filePartTgt.transferTo( File("/tmp/audio-tgt"))

            // just for now (hopefully), invoke the python process on every request (!)
            val ipconfig = ProcessBuilder().command("ipconfig").inheritIO().start()
            val finished = ipconfig.waitFor(100, TimeUnit.SECONDS)
            println("finished running ipconfig: "+ finished.toString())


            ServerResponse.ok().body(Mono.just("OK"))
        }
    }
}
