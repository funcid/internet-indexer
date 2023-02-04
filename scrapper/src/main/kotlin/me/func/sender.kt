package me.func

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.func.shema.Node
import java.io.OutputStreamWriter
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.HttpsURLConnection

val counter = AtomicInteger(1)
val threadPool: ExecutorService = Executors.newFixedThreadPool(4)
fun sendToOpenSearch(node: Node) {
    threadPool.execute {

        val url = URL("https://192.168.99.100:9200/internet/_doc/${counter.incrementAndGet()}")
        val json = Json.encodeToString(node)

        with(url.openConnection() as HttpsURLConnection) {

            requestMethod = "PUT"
            doOutput = true

            val auth = "Basic ${Base64.getEncoder().encodeToString("admin:admin".toByteArray())}"

            setRequestProperty("Authorization", auth)
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")

            val outputWriter = OutputStreamWriter(outputStream)

            outputWriter.write(json)
            outputWriter.flush()

            println("response code: $responseCode $responseMessage")
        }
    }
}
