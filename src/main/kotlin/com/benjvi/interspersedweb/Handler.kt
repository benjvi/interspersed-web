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
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.PathVariable
import java.util.*
import java.util.Collections.singletonMap


@Component
class Handler() {

    val inputFilesDir = "/tmp/input"
    val outputFilesDir = "/tmp/output"

    fun helloWorld(req: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(Mono.just("Hello World!"))
    }

    fun landingPage(req: ServerRequest): Mono<ServerResponse> {
        val model = HashMap<String, Any>()
        return ServerResponse.ok().contentType(MediaType.TEXT_HTML).render("index", model)
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


            val filePartBase : FilePart = map["audio-base"]!! as FilePart
            val inputBaseAudioPath = String.format("%s/%d/audio-base.mp3", inputFilesDir, reqID)
            filePartBase.transferTo( File(inputBaseAudioPath))

            val inputTgtAudioPath = String.format("%s/%d/audio-tgt.mp3", inputFilesDir, reqID)
            val filePartTgt : FilePart = map["audio-tgt"]!! as FilePart
            filePartTgt.transferTo( File(inputTgtAudioPath))

            println("started running python script to process audio for request: "+reqID.toString())
            // just for now (hopefully), invoke the python process on every request (!)
            val python = ProcessBuilder().command("python3", "interleaved/make_bilingual.py", inputBaseAudioPath, inputTgtAudioPath, outputAudioPath).inheritIO().start()

            val finished = python.waitFor(100, TimeUnit.SECONDS)
            if (!finished) {
                return@flatMap ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Mono.just("Failed to complete audio processing"))
            }
            println("finished running python: "+ finished.toString())


            val model = HashMap<String, Any>()
            model["reqID"] = reqID
            model["audioFilenameBase"]  = filePartBase.filename()
            model["audioFilenameTgt"] = filePartTgt.filename()
            ServerResponse.ok().contentType(MediaType.TEXT_HTML).render("upload-confirmed", model)
        }
    }

    // TODO: add a list endpoint which gets all uploaded audio
    // uses flux object so its properly reactive

    fun downloadAudio(req: ServerRequest): Mono<ServerResponse> {
        // TODO add a filename to the end of the URL here
        // so users get a sensible filename instead of a random id
        // https://www.nurkiewicz.com/2015/07/writing-download-server-part-vi.html
        val reqId = req.pathVariable("reqId")
        return ServerResponse.ok().body(Mono.just(FileSystemResource("%s/%s/audio-out.mp3".format(outputFilesDir, reqId))))
    }
}
