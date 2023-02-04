package me.func

import me.func.shema.Node
import me.func.storage.OpenSearchStorage
import me.func.storage.Storage
import me.func.util.*
import org.slf4j.LoggerFactory

object WebCrawler {

    private val storage: Storage = OpenSearchStorage(
        prop("open-search-remote"),
        prop("open-search-index")
    )

    private val defaultConnectionTimeout = prop("crawler-connection-timeout", "3000").toInt()
    private val defaultReadTimeout = prop("crawler-read-timeout", "3000").toInt()
    private val connectionRetryCount = prop("crawler-reties-count", "10").toInt()

    private val logger = LoggerFactory.getLogger(WebCrawler::class.java)

    fun crawl(node: Node): Collection<Node> {

        val page = tryReadWebPage(node) ?: return setOf()

        node.title = HTML_TITLE_TAG.find(page)?.groupValues?.get(1) ?: "none"

        node.content = HTML_P_TAG.findAll(page)
            .map { it.groupValues[1] }
            .toHashSet()

        val childrenPages = extractUrlsFromWebPage(page, node)

        storage.asyncStore(node)

        return childrenPages
    }

    private fun tryReadWebPage(node: Node, loop: Int = 0): String? {

        if (loop > connectionRetryCount) {
            logger.info("Bad server!")
            return null
        }

        return try {

            node.url.createReader {
                connectTimeout = defaultConnectionTimeout
                readTimeout = defaultReadTimeout
            }.readText()
        } catch (exception: Exception) {

            logger.info(exception.message)
            tryReadWebPage(node, loop + 1)
        }
    }

    private fun extractUrlsFromWebPage(page: String, previousNode: Node) = page
        .split("\n")
        .asSequence()
        .filter { it.contains("http") }
        .map(HREF_CONTENT_PATTERN::matcher)
        .map {

            val set = hashSetOf<Node>()

            while (it.find()) {

                val link = it.group(1)
                    .split("/")
                    .take(3)
                    .joinToString("/")

                if (previousNode.url == link) continue

                set.add(Node(link, previousNode.url))
            }

            set
        }.flatten()
        .toHashSet()

}