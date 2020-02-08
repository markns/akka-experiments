package com.mns.streams;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.stream.javadsl.Source;

import java.time.Duration;

public class ZipLatestTest {

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("QuickStart");


        Source<Integer, NotUsed> a = Source.range(1, 10);



        Source<Integer, Cancellable> b = Source
                .tick(Duration.ZERO, Duration.ofDays(1), NotUsed.getInstance())
                .map(x -> 1);
        Source<Integer, Cancellable> c = Source
                .tick(Duration.ZERO, Duration.ofSeconds(1), NotUsed.getInstance())
                .map(x -> 2);

        c.zipLatest(b).runForeach(System.out::println, system);
    }
}
