package ru.ezhov.coffee.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

@WebFluxTest(CoffeeService::class)
class CoffeeServiceApplicationTests {
    @Autowired
    lateinit var service: CoffeeService

    @MockBean
    lateinit var repository: CoffeeRepository

    private val coffee1 = Coffee("000-TEST-111", "Tester's choice")
    private val coffee2 = Coffee("000-TEST-222", "Failgers")

    @BeforeEach
    fun setUp() {
        Mockito.`when`(repository.findAll()).thenReturn(Flux.just(coffee1, coffee2))
        Mockito.`when`(repository.findById(coffee1.id!!)).thenReturn(Mono.just(coffee1))
        Mockito.`when`(repository.findById(coffee2.id!!)).thenReturn(Mono.just(coffee2))
    }

    @Test
    fun getAllCoffees() {

    }

    @Test
    fun getCoffeeById() {

    }

    @Test
    fun `Get orders for coffee by Id`() {
        StepVerifier.withVirtualTime { service.getOrdersForCoffeeById(coffee1.id!!).take(10) }
                .thenAwait(Duration.ofHours(10))
                .expectNextCount(10)
                .verifyComplete()
    }
}
