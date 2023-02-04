package me.func.shema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Node(
    val url: String,
    @SerialName("previous_name")
    val rootPage: String,
    var title: String = "none",
    var content: MutableSet<String> = hashSetOf(),
)
