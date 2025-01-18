package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RDequeReactive;
import org.redisson.api.RListReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Lec09ListQueueStackTest extends BaseTest {

    @Test
    public void listTest() {
        // lrange number-input 0 -1
        RListReactive<Long> list = this.client.getList("number-input", LongCodec.INSTANCE);
        /*
        Нужно помнить, что если добавим методы реактивным способом, то в результате у нас не будет сохранен порядок добавления
        Mono<Void> listAdd = Flux.range(0, 10)
                .map(Long::valueOf)
                .flatMap(list::add)
                .then();
         */

        // Для того чтобы сохранить порядок добавления готовим данные заранее и добавляем целиком.
        List<Long> longList = LongStream.rangeClosed(1, 10)
                .boxed()
                .collect(Collectors.toList());

        StepVerifier.create(list.addAll(longList).then())
                .verifyComplete();
        StepVerifier.create(list.size())
                .expectNext(10)
                .verifyComplete();
    }

    @Test
    public void queueTest() {
        RQueueReactive<Long> queue = this.client.getQueue("number-input", LongCodec.INSTANCE); // так как у нас уже есть лист в предыдущем тесте, мы можем переиспользовать его как очередь
        Mono<Void> queuePoll = queue.poll()
                .repeat(3)
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(queuePoll)
                .verifyComplete();
        StepVerifier.create(queue.size())
                .expectNext(6)
                .verifyComplete();
    }

    @Test
    public void stackTest() {
        RDequeReactive<Long> dequeue = this.client.getDeque("number-input", LongCodec.INSTANCE); // так как у нас уже есть лист в предыдущем тесте, мы можем переиспользовать его как очередь
        Mono<Void> dequePollLast = dequeue.pollLast()
                .repeat(3)
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(dequePollLast)
                .verifyComplete();
        StepVerifier.create(dequeue.size())
                .expectNext(2)
                .verifyComplete();
    }
}
