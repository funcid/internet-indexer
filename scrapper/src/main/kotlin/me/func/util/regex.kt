package me.func.util

import java.util.regex.Pattern

val HREF_CONTENT_PATTERN: Pattern = Pattern.compile("href\\s*=\\s*\"(https?://[^\"]+?)\"")
val HTML_TITLE_TAG = """<title>(.*?)</title>""".toRegex()
val HTML_P_TAG = """<p>(.*?)</p>""".toRegex()
