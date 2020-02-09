package com.mns.streams;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.MergeLatest;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.Arrays;

public class CombineTest {

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("QuickStart");

        Source<Integer, NotUsed> i1 = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Source<Integer, NotUsed> i2 = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        Source.combine(i1, i2, new ArrayList<>(), i -> MergeLatest.create(2))
                .runForeach(System.out::println, system);

    }

}
