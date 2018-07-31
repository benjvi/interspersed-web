package com.benjvi.interspersedweb

import org.springframework.http.HttpStatus
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
import java.util.UUID



@Component
class Handler() {

    val inputFilesDir = "/tmp/input"
    val outputFilesDir = "/tmp/output"

    fun helloWorld(req: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(Mono.just("Hello World!"))
    }

    fun uploadAudio(req: ServerRequest): Mono<ServerResponse> {
        // TODO: spring request handling fails when size > ~2GB
        return req.body(BodyExtractors.toMultipartData()).flatMap { parts ->
            // TODO: uuid based on uploaded files so we can eliminate duplicate requests?
            val reqID = UUID.randomUUID().leastSignificantBits
            val outputAudioPath = String.format("%s/%d/", outputFilesDir, reqID)
            val outputAudioFolder = File(outputAudioPath)
            outputAudioFolder.mkdirs()
            val inputAudioFolder = File(String.format("%s/%d/", inputFilesDir, reqID))
            inputAudioFolder.mkdirs()

            val map: Map<String, Part> = parts.toSingleValueMap()


            val filePartBase : FilePart = map["file"]!! as FilePart
            val inputBaseAudioPath = String.format("%s/%d/audio-base.mp3", inputFilesDir, reqID)
            filePartBase.transferTo( File(inputBaseAudioPath))

            val inputTgtAudioPath = String.format("%s/%d/audio-tgt.mp3", inputFilesDir, reqID)
            val filePartTgt : FilePart = map["file-2"]!! as FilePart
            filePartTgt.transferTo( File(inputTgtAudioPath))

            println("started running python script to process audio for request: "+reqID.toString())
            // just for now (hopefully), invoke the python process on every request (!)
            val python = ProcessBuilder().command("python3", "interleaved/make_bilingual.py", inputBaseAudioPath, inputTgtAudioPath, outputAudioPath).inheritIO().start()

            val finished = python.waitFor(100, TimeUnit.SECONDS)
            if (!finished) {
                return@flatMap ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Mono.just("Failed to complete audio processing"))
            }
            println("finished running python: "+ finished.toString())

            //TODO: load a link to download the file in the response
            ServerResponse.ok().body(Mono.just("OK"))
        }
    }
}
