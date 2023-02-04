package me.func.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.*
import javax.net.ssl.HttpsURLConnection

fun String.createReader(apply: URLConnection.() -> Unit) = URL(this)
    .openConnection()
    .apply(apply)
    .getInputStream()
    .bufferedReader(Charset.defaultCharset())

fun String.createHttpsConnection(apply: HttpsURLConnection.() -> Unit) {

    val connection = URL(this).openConnection() as HttpsURLConnection

    with(connection) {
        apply(apply)
    }
}

fun HttpsURLConnection.setCredentials(login: String, password: String) {

    val auth = "Basic ${Base64.getEncoder().encodeToString("$login:$password".toByteArray())}"

    setRequestProperty("Authorization", auth)
    setRequestProperty("Content-Type", "application/json")
    setRequestProperty("Accept", "application/json")
}

inline fun <reified T> HttpsURLConnection.writeObjectAsJson(`object`: T) {

    val json = Json.encodeToString(`object`)
    val outputWriter = OutputStreamWriter(outputStream)

    outputWriter.write(json)
    outputWriter.flush()
}
