package ru.vadim.redissoncourse;

/*
Гиперлог это вероятностная структура данных, в нем много цифр и задействуется сложный алгоритм работы.
Он не сохраняет элементы/
Используется, когда нужно получить примерное количество уникальных элементов, т.е. примерная оценка.
 */

import org.junit.jupiter.api.Test;
import org.redisson.api.RHyperLogLogReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Lec11HyperLogLogTest extends BaseTest {

    @Test // max memory using 12.5 kb
    public void count() {
        RHyperLogLogReactive<Long> counter = this.client.getHyperLogLog("user:visits", LongCodec.INSTANCE);
        List<Long> list1 = LongStream.rangeClosed(1, 25000)
                .boxed()
                .collect(Collectors.toList());

        List<Long> list2 = LongStream.rangeClosed(25001, 50000)
                .boxed()
                .collect(Collectors.toList());

        List<Long> list3 = LongStream.rangeClosed(1, 75000)
                .boxed()
                .collect(Collectors.toList());

        List<Long> list4 = LongStream.rangeClosed(50000, 100000)
                .boxed()
                .collect(Collectors.toList());

        Mono<Void> mono = Flux.just(list1, list2, list3, list4)
                .flatMap(l -> counter.addAll(l))
                .then();

        StepVerifier.create(mono.then())
                .verifyComplete();

        counter.count()
                .doOnNext(System.out::println)
                .subscribe();
    }
}
