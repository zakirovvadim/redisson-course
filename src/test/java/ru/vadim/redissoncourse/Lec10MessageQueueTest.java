package ru.vadim.redissoncourse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBlockingDequeReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
/*
Прикольная штука месаджинг, когда есть продьюссер и получаемые сообщения консюмерами, по поведению похож на
мультикаст синк из флакса.
 */
public class Lec10MessageQueueTest extends BaseTest {

    private RBlockingDequeReactive<Long> msQueue;

    @BeforeAll
    public void setupQueue() {
        msQueue = this.client.getBlockingDeque("message-queue", LongCodec.INSTANCE);
    }
    @Test
    public void consumer1() {
        this.msQueue.takeElements()
                .doOnNext(i -> System.out.println("Consumer 1 : " + i))
                .doOnError(System.out::println)
                .subscribe();

        sleep(600_000);
    }

    @Test
    public void consumer2() {
        this.msQueue.takeElements()
                .doOnNext(i -> System.out.println("Consumer 2 : " + i))
                .doOnError(System.out::println)
                .subscribe();

        sleep(600_000);
    }

    @Test
    public void producer() {
        Mono<Void> mono = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(500))
                .doOnNext(i -> System.out.println("going to add " + 1))
                .flatMap(i -> this.msQueue.add(Long.valueOf(i)))
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }
}
