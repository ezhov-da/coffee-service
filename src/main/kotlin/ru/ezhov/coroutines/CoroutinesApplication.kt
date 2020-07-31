package ru.ezhov.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

suspend fun main() {
//    test()
//    val cs = CoroutineScope()
//    cs.downloader()
}

suspend fun test() {
    val jobs = List(100_000) {
        GlobalScope.launch {
            delay(2000)
            print(".")
        }
    }
    jobs.forEach { it.join() }
}

fun CoroutineScope.downloader(
        references: ReceiveChannel<Reference>
) = launch {
    val requested = mutableSetOf<String>()
    for (ref in references) {
        if (requested.add(ref.name)) {
            launch {
                val execute = ref.executor.execute()
                println("${Thread.currentThread().name} + $execute")
            }
        }
    }
}

data class Reference(val name: String, val executor: Executor)

interface Executor {
    fun execute(): String
}

