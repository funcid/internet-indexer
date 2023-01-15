package me.func

import java.io.BufferedReader
import java.net.URL
import java.nio.charset.Charset
import java.util.HashSet
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

const val THREADS = 6
const val ROOT_PAGE = "https://www.github.com"
val HREF_CONTENT_PATTERN: Pattern = Pattern.compile("href\\s*=\\s*\"(https?://[^\"]+?)\"")

val STACK = ConcurrentLinkedDeque<String>()
val CHECKED = HashSet<String>()

fun main() {

    val pages = startScrapping(ROOT_PAGE)

    pages.forEach {
        STACK.push(it)
    }

    val loop = AtomicInteger(0)
    val pool = Executors.newFixedThreadPool(THREADS)

    repeat(THREADS) {
        pool.execute {
            while (true) {

                val url = try {
                    STACK.pop()
                } catch (exception: Exception) {
                    continue
                }

                if (!CHECKED.add(url)) continue

                STACK.addAll(startScrapping(url))

                val stackSize = STACK.size

                println("Loop: ${loop.getAndIncrement()}, scanned: $url, stack size: $stackSize")
            }
        }
    }
}

fun startScrapping(url: String): Collection<String> {

    val urls = hashSetOf<String>()
    val time = System.currentTimeMillis()

    try {
        createReaderByURL(url).readText().split("\n").forEach {

            if (!it.contains("http")) return@forEach

            val matcher = HREF_CONTENT_PATTERN.matcher(it)

            while (matcher.find()) {

                val link = matcher.group(1).rootUrl()

                if (url == link || urls.contains(link)) continue

                urls.add(link)
            }
        }
    } catch (exception: Exception) {
        println(exception.message)
    }

    urls.forEach {
        println("Url: $url, new: $it")
    }
    println(System.currentTimeMillis() - time)

    return urls
}

fun createReaderByURL(url: String): BufferedReader {
    val connection = URL(url).openConnection()

    connection.connectTimeout = 5000
    connection.readTimeout = 5000

    return connection.getInputStream().bufferedReader(Charset.defaultCharset())
}

fun String.byteSize() = toByteArray(Charset.defaultCharset()).size

fun String.rootUrl(): String {

    val root = split("/")
        .take(3)
        .joinToString("/")
        .replace("www.", "")

    val dots = root.count { it == '.' }

    if (dots == 2) {

        val parts = root.split("/")
        val subParts = parts[2].split(".")

        return parts[0] + "//" + subParts[1] + "." + subParts[2]
    }

    return root
}