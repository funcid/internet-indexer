package me.func.storage

import me.func.Configuration
import me.func.shema.Node
import me.func.util.*
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

class OpenSearchStorage(private var remote: String, private val index: String) : Storage {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OpenSearchStorage::class.java)

        private val LOGIN = prop("open-search-login", "admin")
        private val PASSWORD = prop("open-search-password", "admin")
    }

    private val counter = AtomicInteger(1)

    init {

        // always remote must start with `https`
        if (!remote.startsWith("https")) {
            remote = "https://$remote"
        }
    }

    override fun store(node: Node) {

        val offset = counter.incrementAndGet()

        "$remote/$index/_doc/$offset".createHttpsConnection {

            requestMethod = "PUT"
            doOutput = true

            setCredentials(LOGIN, PASSWORD)
            setJsonType()
            writeObjectAsJson(node)

            LOGGER.info("Store request! Code: $responseCode, message: $responseMessage, url: ${node.url}")
        }
    }

    override fun asyncStore(node: Node): CompletableFuture<Void> = CompletableFuture.runAsync { store(node) }

}
