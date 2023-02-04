package me.func

import me.func.shema.Node
import me.func.util.prop
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Pipeline {

    private val STACK = ConcurrentLinkedDeque<Node>()
    private val CHECKED = ConcurrentSkipListSet<String>()

    private val LOGGER = LoggerFactory.getLogger(Pipeline::class.java)

    private val ROOT_PAGE = prop("crawler-root-page", "https://github.com")

    private val THREAD_AMOUNT = prop("crawler-thread-size", "10").toInt()
    private val THREAD_POOL: ExecutorService = Executors.newFixedThreadPool(THREAD_AMOUNT)

    init {

        // fill first page in a stack to start crawl web from this
        STACK.push(
            Node(
                ROOT_PAGE,
                ROOT_PAGE,
                ROOT_PAGE
            )
        )
        LOGGER.info("Stack inited.")
    }

    fun run() {

        LOGGER.info("Run $THREAD_AMOUNT threads.")

        fillThreadPool(times = THREAD_AMOUNT) {

            // getting top url to crawl from reentrant stack
            val node = try {
                STACK.pop()
            } catch (exception: Exception) {

                LOGGER.info("Pop operation exception: ${exception.message}, try again...")
                return@fillThreadPool
            }

            if (!CHECKED.add(node.url)) return@fillThreadPool

            val crawl = WebCrawler.crawl(node)

            LOGGER.info("Crawled ${crawl.size} urls")

            // fill new pages to stack
            STACK.addAll(crawl)
        }
    }


    private fun fillThreadPool(times: Int, task: Runnable) {

        // set infinite tasks to thread pool
        repeat(times) {
            THREAD_POOL.execute {
                while (true) {
                    task.run()
                }
            }
        }
    }
}
