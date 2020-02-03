import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class CombineTest {

//    https://stackoverflow.com/questions/45801339/how-to-inner-join-2-sources-by-a-key-in-akka-scala

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("QuickStart");

        Source<List<Product>, Cancellable> a = Source.tick(Duration.ZERO, Duration.ofDays(1),
                List.of(new Product("a", LocalDate.of(2020, 12, 1)),
                        new Product("b", LocalDate.of(2020, 6, 1)),
                        new Product("c", LocalDate.of(2020, 12, 1)),
                        new Product("d", LocalDate.of(2020, 6, 1)),
                        new Product("e", LocalDate.of(2020, 12, 1)),
                        new Product("d", LocalDate.of(2020, 12, 1))));

        Source<String, Cancellable> irs = Source
                .tick(Duration.ZERO, Duration.ofSeconds(1), NotUsed.getInstance())
                .scan(0, (acc, x) -> acc + 1)
                .map(seq -> "irs " + seq);

        Random rnd = new Random();
        a.mapConcat(x -> x.stream().collect(Collectors.groupingBy(Product::getDate)).entrySet())
                .groupBy(Integer.MAX_VALUE, Map.Entry::getKey)
                .zipLatestWith(irs, Pair::create)
                .map(x -> Pair.create(x.first(), "solved " + x.second() + " " + rnd.nextInt()))
                .mergeSubstreams()
                .mapConcat(x -> x.first().getValue().)
                .runForeach(System.out::println, system);



    }

    private static class Product {
        private final String id;
        private final LocalDate date;

        private Product(String id, LocalDate date) {
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
    }
}