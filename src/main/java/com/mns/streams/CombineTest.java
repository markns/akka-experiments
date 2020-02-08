package com.mns.streams;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.javadsl.Source;
import com.mns.streams.model.Product;
import com.mns.streams.model.Yield;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@SuppressWarnings("Convert2Diamond")
public class CombineTest {

//    https://stackoverflow.com/questions/45801339/how-to-inner-join-2-sources-by-a-key-in-akka-scala

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("QuickStart");

        Random rand = new Random();
        Source<Product, Cancellable> products = Source.tick(Duration.ZERO, Duration.ofSeconds(1), NotUsed.getInstance())
                .map(__ -> new Product(rand.nextInt(10), LocalDate.of(2020, 12, 1)));

        Source<Yield, Cancellable> yields = Source.tick(Duration.ZERO, Duration.ofMillis(600), NotUsed.getInstance())
                .map(__ -> new Yield(rand.nextInt(10), rand.nextDouble()));



//        https://www.codota.com/code/java/classes/akka.stream.FanInShape2
//https://github.com/eclipse/ditto/blob/918bd866facccdd20a9d320448ec78ad35a13ad3/services/utils/akka/src/main/java/org/eclipse/ditto/services/utils/akka/controlflow/Transistor.java

        Source<Pair<Product, Yield>, Cancellable> s = products.map(x -> (Object) x)
                .merge(yields.map(x -> (Object) x))
                .statefulMapConcat(() -> {
                    return new Function<Object, Iterable<Pair<Product, Yield>>>() {
                        Map<Integer, Product> products = new HashMap<>();
                        Map<Integer, Yield> yields = new HashMap<>();

                        @Override
                        public Iterable<Pair<Product, Yield>> apply(Object o) {
                            if (o instanceof Product) {
                                Product p = (Product) o;
                                products.put(p.getId(), p);
                                if (yields.containsKey(p.getId())) {
                                    return Collections.singletonList(Pair.create(p, yields.get(p.getId())));
                                } else {
                                    return Collections.emptyList();
                                }
                            } else if (o instanceof Yield) {
                                Yield y = (Yield) o;
                                yields.put(y.getId(), y);
                                if (products.containsKey(y.getId())) {
                                    return Collections.singletonList(Pair.create(products.get(y.getId()), y));
                                } else {
                                    return Collections.emptyList();
                                }

                            } else {
                                return Collections.emptyList();
                            }


                        }
                    };
                });

        s.runForeach(System.out::println, system);
    }

}