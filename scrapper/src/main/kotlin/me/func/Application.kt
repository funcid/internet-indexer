package me.func

import me.func.util.allowManInTheMiddleExploit
import me.func.util.prop

fun main() {

    if (prop("disable-certificate-trust").toBoolean()) {

        // disable verifying trusted certificate to speed up
        // starting https connect
        allowManInTheMiddleExploit()
    }

    // run ETL (extract, transfer, load) web crawler:
    // 1. Extract WEB-urls in HTML page
    // 2. Transfer urls to thread-pool
    // 3. Load WEB-page by url
    Pipeline.run()

}
