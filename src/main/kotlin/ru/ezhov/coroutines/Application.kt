package ru.ezhov.coroutines

import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import kotlin.system.measureTimeMillis

fun main() = runBlocking { // this: CoroutineScope
//    `Scope builder`()
//    `With timeout`()
//    `With Timeout Or Null`()
//    `Sequential by default`()
//    `Concurrent using async`()
//    `Lazily started async`()
//    `Async-style functions`()
//    `Structured concurrency with async`()
    `Cancellation is always propagated through coroutines hierarchy`()
}

suspend fun exception() {
    coroutineScope {
        println("First")
        throw IllegalArgumentException("First")
    }

    coroutineScope {
        println("Second")
        throw IllegalArgumentException("Second")
    }
}

suspend fun `Scope builder`() {
    withContext(Dispatchers.Default) {
        launch {
            delay(200L)
            println("Task from runBlocking")
        }

        coroutineScope { // Creates a coroutine scope
            launch {
                delay(500L)
                println("Task from nested launch")
            }

            delay(100L)
            println("Task from coroutine scope") // This line will be printed before the nested launch
        }

        println("Coroutine scope is over") // This line is not printed until the nested launch completes
    }
}

suspend fun `With timeout`() {
    try {

        withTimeout(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        }
    } catch (e: Exception) {
        println(e.message)
    }
}

suspend fun `With Timeout Or Null`() {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(100L)
        }
        "Done" // will get cancelled before it produces this result
    }
    println("Result is $result")
}

suspend fun `Sequential by default`() {
    val time = measureTimeMillis {
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}

suspend fun `Concurrent using async`() = coroutineScope {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

suspend fun `Lazily started async`() = coroutineScope {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        // some computation
        println("Not started")
        one.start() // start the first one
        println("Started one")
        two.start() // start the second one
        println("Started two")
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

fun `Async-style functions`() {
    val time = measureTimeMillis {
        // we can initiate async actions outside of a coroutine
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()
        // but waiting for a result must involve either suspending or blocking.
        // here we use `runBlocking { ... }` to block the main thread while waiting for the result
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }
    println("Completed in $time ms")
}

// The result type of somethingUsefulOneAsync is Deferred<Int>
fun somethingUsefulOneAsync() = GlobalScope.async {
    doSomethingUsefulOne()
}

// The result type of somethingUsefulTwoAsync is Deferred<Int>
fun somethingUsefulTwoAsync() = GlobalScope.async {
    doSomethingUsefulTwo()
}

suspend fun `Structured concurrency with async`() = coroutineScope {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}

suspend fun `Cancellation is always propagated through coroutines hierarchy`() {
    try {
        failedConcurrentSum()
    } catch (e: ArithmeticException) {
        println("Computation failed with ArithmeticException")
    }
}

suspend fun failedConcurrentSum(): Int = coroutineScope {
    val one = async<Int> {
        try {
            delay(Long.MAX_VALUE) // Emulates very long computation
            42
        } finally {
            println("First child was cancelled")
        }
    }
    val two = async<Int> {
        println("Second child throws an exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}