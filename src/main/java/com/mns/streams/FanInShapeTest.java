package com.mns.streams;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.*;
import akka.stream.javadsl.*;
import com.mns.streams.model.Product;
import com.mns.streams.model.Yield;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletionStage;

public class FanInShapeTest {

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("QuickStart");

        Random rand = new Random();
        Source<Product, Cancellable> products = Source.tick(Duration.ZERO, Duration.ofSeconds(1), NotUsed.getInstance())
                .map(__ -> new Product(rand.nextInt(10), LocalDate.of(2020, 12, 1)));

        Source<Yield, Cancellable> yields = Source.tick(Duration.ZERO, Duration.ofMillis(600), NotUsed.getInstance())
                .map(__ -> new Yield(rand.nextInt(10), rand.nextDouble()));


        final Sink<Pair<Product, Yield>, CompletionStage<Done>> resultSink = Sink.<Pair<Product, Yield>>foreach(System.out::println);

        final RunnableGraph<CompletionStage<Done>> g =
                RunnableGraph.<CompletionStage<Done>>fromGraph(
                        GraphDSL.create(
                                resultSink,
                                (builder, sink) -> {
                                    // import the partial graph explicitly
                                    final FanInShape2<Product, Yield, Pair<Product, Yield>> pm = builder.add(createFanInShape2());

                                    builder.from(builder.add(products)).toInlet(pm.in0());
                                    builder.from(builder.add(yields)).toInlet(pm.in1());
                                    builder.from(pm.out()).to(sink);
                                    return ClosedShape.getInstance();
                                }));

        final CompletionStage<Done> max = g.run(system);
    }

    static Graph<FanInShape2<Product, Yield, Pair<Product, Yield>>, NotUsed> createFanInShape2() {

        final Flow<Object, Pair<Product, Yield>, NotUsed> joinFlow =
                Flow.of(Object.class)
                        .statefulMapConcat(() -> new Function<Object, Iterable<Pair<Product, Yield>>>() {
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
                        });

        return GraphDSL.create(
                b -> {
                    final FlowShape<Product, Object> f1 = b.add(Flow.of(Product.class).map(p -> p));
                    final FlowShape<Yield, Object> f2 = b.add(Flow.of(Yield.class).map(y -> y));

                    final UniformFanInShape<Object, Object> merge = b.add(Merge.create(2));

                    final FlowShape<Object, Pair<Product, Yield>> joiner = b.add(joinFlow);

                    Outlet<Pair<Product, Yield>> out = b.from(f1).viaFanIn(merge).via(joiner).out();
                    b.from(f2).viaFanIn(merge);

                    return new FanInShape2<>(
                            f1.in(),
                            f2.in(),
                            out
                    );
                });
    }
}
