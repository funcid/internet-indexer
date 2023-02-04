package me.func.util

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

fun allowManInTheMiddleExploit() {

    val emptyTrustManager = object : X509TrustManager {

        override fun checkClientTrusted(p0: Array<out X509Certificate>, p1: String) {}
        override fun checkServerTrusted(p0: Array<out X509Certificate>, p1: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    val sslContext = SSLContext.getInstance("SSL")

    sslContext.init(
        null,
        arrayOf(emptyTrustManager),
        SecureRandom()
    )

    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
}
