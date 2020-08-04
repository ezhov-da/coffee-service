package ru.ezhov.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() = runBlocking {
    test1()
//    `Flows are cold`()
}

suspend fun test1() {
    List(10) { Random.nextLong(400, 1000) }
            .asFlow()
            .map { v -> await(v, v.toString()) }
            .collect {
                println(it)
            }
}

suspend fun await(delay: Long, name: String) = coroutineScope {
    println("Before delay: $delay name: $name")
    delay(delay)
    println("After delay: $delay name: $name")
    name
}

suspend fun `Flows are cold`() {
    println("Calling simple function...")
    val flow = simple()
    println("Calling collect...")
    flow.collect { value -> println(value) }
    println("Calling collect again...")
    flow.collect { value -> println(value) }
}

fun simple(): Flow<Int> = flow {
    println("Flow started")
    for (i in 1..3) {
        delay(100)
        emit(i)
    }
}