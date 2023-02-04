package me.func

import me.func.util.allowManInTheMiddleExploit
import me.func.util.prop

fun main() {

    if (prop("disable-certificate-trust").toBoolean()) {
        allowManInTheMiddleExploit()
    }

    Pipeline.run()

}
