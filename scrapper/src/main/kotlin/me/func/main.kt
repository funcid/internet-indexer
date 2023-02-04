package me.func

import me.func.shema.Node
import me.func.util.HREF_CONTENT_PATTERN
import me.func.util.HTML_P_TAG
import me.func.util.HTML_TITLE_TAG
import me.func.util.allowManInTheMiddleExploit
import java.io.BufferedReader
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

const val SCRAPPER_THREADS = 40
const val ROOT_PAGE = "https://github.com"

val STACK = ConcurrentLinkedDeque<Node>()
val CHECKED = ConcurrentSkipListSet<String>()

fun main() {

    allowManInTheMiddleExploit()

    val pages = startScrapping(Node(ROOT_PAGE, ROOT_PAGE, ROOT_PAGE))

    pages.forEach {
        STACK.push(it)
    }

    val loop = AtomicInteger(0)
    val pool = Executors.newFixedThreadPool(SCRAPPER_THREADS)

    repeat(SCRAPPER_THREADS) {
        pool.execute {
            while (true) {

                val node = try {
                    STACK.pop()
                } catch (exception: Exception) {
                    continue
                }

                if (!CHECKED.add(node.url)) continue

                STACK.addAll(startScrapping(node))

                val stackSize = STACK.size

                println("Loop: ${loop.getAndIncrement()}, scanned: ${node.url}, stack size: $stackSize")
            }
        }
    }
}

fun startScrapping(rootNode: Node, loop: Int = 0): Collection<Node> {

    if (loop > 6) {
        println("Bad server!")
        return setOf()
    }

    val urls = hashSetOf<Node>()

    val page = try {
        createReaderByURL(rootNode.url).readText()
    } catch (exception: Exception) {
        if (exception.message?.contains("connect timed out") == true) {
            return startScrapping(rootNode, loop + 1)
        } else {
            println(exception.message)
            return urls
        }
    }

    val title = HTML_TITLE_TAG.find(page)?.groupValues?.get(1) ?: "none"

    rootNode.content = HTML_P_TAG.findAll(page)
        .map { it.groupValues[1] }
        .toHashSet()

    rootNode.title = title

    page.split("\n").forEach {

        if (!it.contains("http")) return@forEach

        val matcher = HREF_CONTENT_PATTERN.matcher(it)

        while (matcher.find()) {

            val link = matcher.group(1)
                .split("/")
                .take(3)
                .joinToString("/")
            val node = Node(link, rootNode.url)

            if (rootNode.url == link || urls.contains(node)) continue

            urls.add(node)
        }
    }

    sendToOpenSearch(rootNode)

    return urls
}

fun createReaderByURL(url: String): BufferedReader {

    val connection = URL(url).openConnection()

    connection.connectTimeout = 2_000
    connection.readTimeout = 2_000

    return connection.getInputStream().bufferedReader(Charset.defaultCharset())
}
