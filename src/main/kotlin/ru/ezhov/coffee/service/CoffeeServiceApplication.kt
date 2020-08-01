package ru.ezhov.coffee.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration
import java.time.Instant
import javax.annotation.PostConstruct

@SpringBootApplication
class CoffeeServiceApplication

fun main(args: Array<String>) {
//    val t1 = Test(1)
//    val t2 = Test(2)
//    val sum = t1 + t2
//
//    println(sum)


    runApplication<CoffeeServiceApplication>(*args)
}

@Configuration
class RouterConfig(private val service: CoffeeService) {
    @Bean
    fun route() = router {
        listOf(
                GET("/coffees", ::all),
                GET("/coffees/{id}", ::byId),
                GET("/coffees/{id}/orders", ::orders)
        )
    }

    fun all(req: ServerRequest) = ServerResponse.ok()
            .body(service.getAllCoffee(), Coffee::class.java)

    fun byId(req: ServerRequest) = ServerResponse.ok()
            .body(service.getCoffeeById(req.pathVariable("id")), Coffee::class.java)

    fun orders(req: ServerRequest) = ServerResponse.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(service.getOrdersForCoffeeById(req.pathVariable("id")), CoffeeOrder::class.java)
}

//@RestController
//@RequestMapping("/coffees")
//class CoffeeController(private val service: CoffeeService) {
//    @GetMapping
//    fun all() = service.getAllCoffee()
//
//    @GetMapping("/{id}")
//    fun byId(@PathVariable id: String) = service.getCoffeeById(id)
//
//    @GetMapping("/{id}/orders", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
//    fun ordersById(@PathVariable id: String) = service.getOrdersForCoffeeById(id)
//}

@Service
class CoffeeService(private val repo: CoffeeRepository) {
    fun getAllCoffee() = repo.findAll()

    fun getCoffeeById(id: String) = repo.findById(id)

    fun getOrdersForCoffeeById(coffeeId: String) = Flux.interval(Duration.ofSeconds(1))
            .onBackpressureDrop()
            .map { CoffeeOrder(coffeeId, Instant.now()) }
}


@Component
class DataLoader(private val repo: CoffeeRepository) {
    @PostConstruct
    fun load() =
            repo.deleteAll().thenMany(
                    listOf("Cappuccino", "Double espresso", "Americano", "Macciato", "Coretto", "Azul")
                            .toFlux()
                            .map { Coffee(name = it) }
                            .flatMap { repo.save(it) })
                    .thenMany(repo.findAll())
                    .subscribe { println(it) }

}

interface CoffeeRepository : ReactiveCrudRepository<Coffee, String>

data class CoffeeOrder(val coffeeId: String, val whenOrdered: Instant)

@Document
data class Coffee(@Id val id: String? = null, val name: String = "Any old joe")

data class Test(val value: Int) {
    operator fun plus(pl: Test) = Test(this.value + pl.value)
}