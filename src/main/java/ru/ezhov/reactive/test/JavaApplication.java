package ru.ezhov.reactive.test;

import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JavaApplication {
    public static void main(String[] args) {
        Flux.just("1,2,3", "4,5,6")
                .flatMap(i -> Flux.fromIterable(Arrays.asList(i.split(","))))
                .collect(Collectors.toList())
                .subscribe(System.out::println);
    }
}
