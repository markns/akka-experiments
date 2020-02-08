package com.mns.streams;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.function.Function;
import akka.stream.javadsl.Source;

import java.time.*;
import java.util.*;

public class StatefulTest {

    private static class Snapshot {
        private final int data;

        private Snapshot(int data) {
            this.data = data;
        }

        public int getData() {
            return data;
        }

        @Override
        public String toString() {
            return "Snapshot{" +
                    "data=" + data +
                    '}';
        }
    }

    private static class Bond {
        private final String id;
        private final LocalDate date;

        private Bond(String id, LocalDate date) {
            this.id = id;
            this.date = date;
        }

        public String getId() {
            return id;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "Bond{" +
                    "id='" + id + '\'' +
                    ", date=" + date +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bond bond = (Bond) o;
            return Objects.equals(id, bond.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("QuickStart");

        Source<Snapshot, NotUsed> snapshots = Source.from(List.of(new Snapshot(1)));

        Source<Bond, NotUsed> s = Source.from(List.of(
                new Bond("a", LocalDate.of(2020, 12, 1)),
                new Bond("b", LocalDate.of(2020, 6, 1)),
                new Bond("c", LocalDate.of(2020, 12, 1)),
                new Bond("d", LocalDate.of(2020, 6, 1)),
                new Bond("e", LocalDate.of(2020, 12, 1)),
                new Bond("d", LocalDate.of(2020, 12, 1))));

        s.groupBy(1000, Bond::getDate)
                .statefulMapConcat(() -> {
                    return new Function<Bond, Iterable<Set<Bond>>>() {
                        Set<Bond> bonds = new HashSet<>();

                        @Override
                        public Iterable<Set<Bond>> apply(Bond bond) {
                            bonds.add(bond);
                            if (bond.id.equals("e")){
                                return Collections.emptyList();
                            } else {
                                return Collections.singleton(bonds);
                            }
                        }
                    };
                })
//                .zipLatest()
//                .flatMapMerge(2, i -> Source.from(List.of(1, 2, 3)))
                .mergeSubstreams()
                .runForeach(System.out::println, system)
        ;

//        Source.range(1, 100)
//                .throttle(1, Duration.ofMillis(100))
//                .map(elem -> new Pair<>(elem, Instant.now()))
//                .statefulMapConcat(
//                        () -> {
//                            return new Function<Pair<Integer, Instant>, Iterable<Pair<Integer, Boolean>>>() {
//                                // stateful decision in statefulMapConcat
//                                // keep track of time bucket (one per second)
//                                LocalDateTime currentTimeBucket =
//                                        LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC);
//
//                                @Override
//                                public Iterable<Pair<Integer, Boolean>> apply(
//                                        Pair<Integer, Instant> elemTimestamp) {
//                                    LocalDateTime time =
//                                            LocalDateTime.ofInstant(elemTimestamp.second(), ZoneOffset.UTC);
//                                    LocalDateTime bucket = time.withNano(0);
//                                    boolean newBucket = !bucket.equals(currentTimeBucket);
//                                    if (newBucket) currentTimeBucket = bucket;
//                                    return Collections.singleton(new Pair<>(elemTimestamp.first(), newBucket));
//                                }
//                            };
//                        })
//                .splitWhen(elemDecision -> elemDecision.second()) // split when time bucket changes
//                .map(elemDecision -> elemDecision.first())
//                .fold(0, (acc, notUsed) -> acc + 1) // sum
//                .to(Sink.foreach(System.out::println))
//                .run(system);
    }
}
