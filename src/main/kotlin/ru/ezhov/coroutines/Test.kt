package ru.ezhov.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() = runBlocking {
    val startTime = System.currentTimeMillis()

    execute(
            async { calc(1000) }.await(),
            async { calc(2000) }.await(),
            async { calc(5000) }.await(),
            async { calc(500) }.await(),
            async { calc(6000) }.await(),
            async { calc(3000) }.await()
    )
    println("time: ${(System.currentTimeMillis() - startTime) / 1000}")
}

suspend fun calc(delay: Long): String {
    val random = "${Random.nextInt()} random"

    val startTime = System.currentTimeMillis()

    delay(delay)

    val endTime = System.currentTimeMillis()

    println("delay: ${endTime - startTime}")

    return random
}

fun execute(vararg values: String) {
    println(values.joinToString(separator = " | "))
    println("~ complete ~")
}