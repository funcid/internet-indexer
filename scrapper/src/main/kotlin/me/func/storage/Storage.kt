package me.func.storage

import me.func.shema.Node
import java.util.concurrent.CompletableFuture

interface Storage {

    fun store(node: Node)

    fun asyncStore(node: Node): CompletableFuture<Void>

}