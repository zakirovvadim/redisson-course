package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLongReactive;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/*
bucket используются для строковых значений, поэтому они не содержат методов инкремента и декремента
Чтобы использовать инкремент и декремент нужно использовать абстракцию client.getAtomicLong() or client.getAtomicDouble
методы у этой абстракции старались сделать аналогичными методам стандартной atomicLong от java.
 */
public class Lec03NumberTest extends BaseTest {

    @Test
    public void keyValueIncreaseTest() {
        // set k v -- incr, decr
        RAtomicLongReactive atomicLong = this.client.getAtomicLong("user:1:visit");
        Mono<Void> mono = Flux.range(1, 30)
                .delayElements(Duration.ofSeconds(1))
                .flatMap(i -> atomicLong.incrementAndGet())
                .then();
        StepVerifier.create(mono)
                .verifyComplete();
    }
}
