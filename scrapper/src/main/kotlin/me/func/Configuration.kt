package me.func

import org.slf4j.LoggerFactory
import java.util.*

object Configuration {

    private const val CONFIG_FILE = "crawler.properties"
    private val LOGGER = LoggerFactory.getLogger(Configuration::class.java)

    private val PROPERTIES = Properties()

    init {
        PROPERTIES.load(this@Configuration.javaClass.classLoader.getResourceAsStream(CONFIG_FILE))

        LOGGER.info("Application configured successfully!")
    }

    fun read(key: String, default: String = ""): String = PROPERTIES.getProperty(key, default)

}
