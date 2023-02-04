package me.func

import me.func.shema.Node
import me.func.storage.OpenSearchStorage
import me.func.storage.Storage
import me.func.util.HREF_CONTENT_PATTERN
import me.func.util.HTML_P_TAG
import me.func.util.HTML_TITLE_TAG
import me.func.util.createReader
import org.slf4j.LoggerFactory

object WebCrawler {

    private val storage: Storage = OpenSearchStorage(
        Configuration.read("open-search-remote"),
        Configuration.read("open-search-index")
    )

    private val defaultConnectionTimeout = Configuration.read("crawler-connection-timeout", "3000").toInt()
    private val defaultReadTimeout = Configuration.read("crawler-connection-timeout", "3000").toInt()

    private val logger = LoggerFactory.getLogger(WebCrawler::class.java)

    fun crawl(rootNode: Node, loop: Int = 0): Collection<Node> {

        if (loop > 6) {
            logger.info("Bad server!")
            return setOf()
        }

        val urls = hashSetOf<Node>()
        val page: String

        try {
            page = rootNode.url.createReader {
                connectTimeout = defaultConnectionTimeout
                readTimeout = defaultReadTimeout
            }.readText()
        } catch (exception: Exception) {
            return if (exception.message?.contains("connect timed out") == true) {
                crawl(rootNode, loop + 1)
            } else {
                logger.info(exception.message)
                urls
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

        storage.asyncStore(rootNode)

        return urls
    }

}