package com.mns.streams;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ThrottleTest {

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("QuickStart");

//        throttleTest(system);

        memberVariableTest(system);
    }

    private static void memberVariableTest(ActorSystem system) {
        Source<String, NotUsed> s = Source.from(List.of("foo", "bar", "baz"));

        Map<String, Integer> p = Map.of("foo", 1, "bar", 2, "baz", 3);
        s.map(x -> new Pair<>(x, p.get(x)))
                .runForeach(System.out::println, system);

    }

    private static void throttleTest(ActorSystem system) {
        Source<Integer, Cancellable> source = Source.tick(Duration.ZERO,
                Duration.ofMillis(1), 1)
                .scan(0, Integer::sum);

//        source.throttle(1, Duration.ofSeconds(2))
//                .runForeach(System.out::println, system);

        source.groupedWithin(100, Duration.ofSeconds(2))
                .map(x -> x.get(x.size() - 1))
                .runForeach(System.out::println, system);
    }
}
