package ru.ezhov.reactive.test

import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import java.io.IOException
import java.time.Duration
import java.util.stream.Collectors
import java.util.stream.Stream


fun main() {
    Mono
            .just("one")
            .flatMap {
                Mono.just(it?.length ?: 0)
            }
            .doFinally {
                println("from Mono current Thread name '${Thread.currentThread().name}'")
            }
            .block(Duration.ofSeconds(2)).let {
                println("from Mono $it")
            }

    Flux
            .just(1, 2, 3, 4, 5, 6)
            .buffer(2)
            .doFirst {
                println("from Flux 'doFirst' current Thread name '${Thread.currentThread().name}'")
            }
            .subscribe {
                println("from Flux $it")
            }
            .toMono()
            .doFinally {
                println("from Flux/Mono 'doFinally' current Thread name '${Thread.currentThread().name}'")
            }
            .block()

    Flux
            .just(1, 2, 3, 4, 5, 6)
            .subscribe(
                    {
                        if (it > 4) throw IllegalArgumentException("Oops :)")
                        println("from third Flux $it")
                    },
                    {
                        println(it.message)
                    },
                    {
                        println("~Complete~")
                    }
            )

    Flux
            .just(1, 2, 3, 4)
            .map { it + 1 }
            .subscribe { println("$it") }
            .also {
                println("map — преобразует элементы, испускаемые этим потоком, применяя синхронную функцию к каждому элементу")
            }

    Flux
            .just("1,2,3", "4,5,6")
            .flatMap { Flux.fromIterable(it.split(",")) }
            .collect(Collectors.toList())
            .subscribe { println("Result: $it") }

    Flux
            .range(1, 10)
            .flatMap {
                if (it < 5)
                    Flux.just(it * it)
                else
                    Flux.error(IOException("Error: "))
            }
            .subscribe(System.out::println, Throwable::printStackTrace)

    Flux.range(1, 10)
            .parallel(2)
            .subscribe { println(Thread.currentThread().name + " -> " + it) }

    Flux.range(1, 10)
            .parallel(2)
            .runOn(Schedulers.parallel())
            .subscribe { println(Thread.currentThread().name + " -> " + it) }

    val oddFlux = Flux.just(1, 3)
    val evenFlux = Flux.just(2, 4)

    Flux.concat(evenFlux, oddFlux)
            .subscribe { println("Outer: $it") }

    val oddFlux1 = Flux.just(1, 3)
    val evenFlux1 = Flux.just(2, 4)

    oddFlux1.mergeWith(evenFlux1)
            .subscribe { value: Int -> println("Outer: $value") }

    showMovie()

}

fun getMovie(): Stream<String> {
    println("Start streaming...")
    return Stream.of(
            "thread 1",
            "thread 2",
            "thread 3",
            "thread 4",
            "thread 5"
    )
}


fun showMovie() {
    val streamingVideo = Flux.fromStream(::getMovie)
            .delayElements(Duration.ofSeconds(2))

    // Первый человек начал смотреть фильм "Titanic"
    streamingVideo.subscribe { println("First person is watching $it") }

    // Второй человек начал смотреть фильм "Titanic" спустя 5 секунд
    Thread.sleep(5000)
    streamingVideo.subscribe { println("Second person is watching $it") }
    Thread.sleep(10000)
}