package me.func.storage

import me.func.Configuration
import me.func.shema.Node
import me.func.util.createHttpsConnection
import me.func.util.setCredentials
import me.func.util.writeObjectAsJson
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

class OpenSearchStorage(private var remote: String, private val index: String) : Storage {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OpenSearchStorage::class.java)

        private val LOGIN = Configuration.read("open-search-login", "admin")
        private val PASSWORD = Configuration.read("open-search-password", "admin")
    }

    private val counter = AtomicInteger(1)

    init {
        if (!remote.startsWith("https")) remote = "https://$remote"
    }

    override fun store(node: Node) {

        val offset = counter.incrementAndGet()

        "$remote/$index/_doc/$offset".createHttpsConnection {

            requestMethod = "PUT"
            doOutput = true

            setCredentials(LOGIN, PASSWORD)
            writeObjectAsJson(node)

            LOGGER.info("response code: $responseCode $responseMessage")
        }
    }

    override fun asyncStore(node: Node): CompletableFuture<Void> = CompletableFuture.runAsync { store(node) }

}
